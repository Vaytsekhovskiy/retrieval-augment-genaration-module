package org.vced.ragutillib.authority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для преобразования строковых имён групп в числовые ID и обратно.
 * Использует in-memory кэш для минимизации обращений к БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorityMappingService {

    private final AuthorityMappingRepository repository;

    private final ConcurrentHashMap<String, Integer> nameToIdCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> idToNameCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        long start = System.currentTimeMillis();
        try {
            List<AuthorityMapping> all = repository.findAll();
            for (AuthorityMapping mapping : all) {
                nameToIdCache.put(mapping.getExternalName(), mapping.getId());
                idToNameCache.put(mapping.getId(), mapping.getExternalName());
            }
            log.info("Authority mapping cache initialized: {} entries in {} ms",
                    all.size(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Failed to initialize authority mapping cache", e);
        }
    }

    /**
     * Преобразует коллекцию строковых имён групп в список строковых ID.
     * Если группа не найдена в БД, создаёт новую запись.
     *
     * @param externalNames строковые имена групп
     * @return список строковых ID (например, ["1", "2", "3"])
     */
    @Transactional
    public List<String> mapToIds(Collection<String> externalNames) {
        if (externalNames == null || externalNames.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>(externalNames.size());

        for (String name : externalNames) {
            if (name == null || name.isBlank()) continue;

            Integer id = nameToIdCache.get(name);
            if (id == null) {
                id = getOrCreateMapping(name);
            }
            if (id != null) {
                result.add(String.valueOf(id));
            }
        }

        return result;
    }

    /**
     * Преобразует список строковых ID обратно в строковые имена групп.
     *
     * @param stringIds список строковых ID (например, ["1", "2", "3"])
     * @return список строковых имён групп
     */
    public List<String> mapToNames(Collection<String> stringIds) {
        if (stringIds == null || stringIds.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>(stringIds.size());

        for (String strId : stringIds) {
            if (strId == null || strId.isBlank()) continue;

            try {
                Integer id = Integer.parseInt(strId);
                String name = idToNameCache.get(id);

                if (name == null) {
                    // Fallback: ищем в БД (на случай если другой инстанс добавил)
                    Optional<AuthorityMapping> mapping = repository.findById(id);
                    if (mapping.isPresent()) {
                        name = mapping.get().getExternalName();
                        idToNameCache.put(id, name);
                        nameToIdCache.put(name, id);
                    } else {
                        log.warn("Authority mapping not found for ID: {}", id);
                        continue;
                    }
                }

                result.add(name);
            } catch (NumberFormatException e) {
                log.warn("Invalid authority ID format: {}", strId);
            }
        }

        return result;
    }

    /**
     * Получает ID для имени группы. Если записи нет — создаёт новую.
     * Thread-safe благодаря synchronized.
     */
    private synchronized Integer getOrCreateMapping(String externalName) {
        // Double-check: может другой поток уже создал
        Integer cached = nameToIdCache.get(externalName);
        if (cached != null) {
            return cached;
        }

        // Проверяем БД
        Optional<AuthorityMapping> existing = repository.findByExternalName(externalName);
        if (existing.isPresent()) {
            AuthorityMapping mapping = existing.get();
            nameToIdCache.put(mapping.getExternalName(), mapping.getId());
            idToNameCache.put(mapping.getId(), mapping.getExternalName());
            log.debug("Loaded authority mapping from DB: {} -> {}", externalName, mapping.getId());
            return mapping.getId();
        }

        // Создаём новую запись
        try {
            AuthorityMapping newMapping = AuthorityMapping.builder()
                    .externalName(externalName)
                    .build();
            newMapping = repository.save(newMapping);

            nameToIdCache.put(newMapping.getExternalName(), newMapping.getId());
            idToNameCache.put(newMapping.getId(), newMapping.getExternalName());

            log.info("Created new authority mapping: {} -> {}", externalName, newMapping.getId());
            return newMapping.getId();
        } catch (Exception e) {
            log.error("Failed to create authority mapping for: {}", externalName, e);
            return null;
        }
    }
}
