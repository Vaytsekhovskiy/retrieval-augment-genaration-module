package org.vced.ragutillib.authority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.vced.ragutillib.vector.AuthorityChunkFilters;

/**
 * Инициализация в БД маппингов важных групп (см. {@link ImportantAuthorities})
 * для {@link AuthorityChunkFilters} и прочего недо-хардкода.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorityMappingDataInitializer {

    private final AuthorityMappingRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAuthorityMapping() {
        for (ImportantAuthorities authority : ImportantAuthorities.values()) {
            createIfMissing(authority);  // цикл по всем объявленным членам enum
        }
    }

    private void createIfMissing(ImportantAuthorities authority) {
        String externalName = authority.toString();
        String expectedId = authority.getId();

        var existing = repository.findByExternalName(externalName);
        if (existing.isPresent()) {
            try {
                String id = existing.get().getId().toString();
                if (!id.equals(expectedId)) {
                    log.error("ACHTUNG! Could not initialize \"{}\"->{} mapping: " +
                                    "{} already has id {}" +
                                    "\nChunk lookup won't work for this group! This may be very very bad! " +
                                    "Check table authority_mapping. DB may need to be dropped.",
                            externalName, expectedId, externalName, id);
                }
            } catch (NullPointerException e) {
                log.error("Could not find id for {}. This shouldn't happen.", externalName);
            }
            return; // маппинг уже присутствует
        }

        AuthorityMapping mapping = AuthorityMapping.builder()
                .externalName(externalName)
                .build();

        mapping = repository.save(mapping);
        String newId = mapping.getId().toString();
        log.info("Initialized authority_mapping row: {} -> {}", externalName, newId);
        if (!newId.equals(expectedId)) {
            log.error("ACHTUNG! The authority_mapping table was not initialized correctly before this, " +
                    "which has led to {} being assigned id {} instead of {}. " +
                    "Drop DB and reprocess all documents and mappings!",
                    externalName, newId, expectedId);
        }
    }
}