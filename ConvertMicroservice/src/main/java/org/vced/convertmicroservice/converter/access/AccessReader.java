package org.vced.convertmicroservice.converter.access;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.vced.ragutillib.authority.AuthorityMappingConfig;
import org.vced.ragutillib.authority.AuthorityMappingService;
import org.vced.ragutillib.authority.ImportantAuthorities;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
@Import(value = {AuthorityMappingConfig.class})
public class AccessReader {
    @Value("${main.ad.user.auth-file}")
    private String authFile;
    @Value("${main.ad.user.netbios-name}")
    private String netbiosName;
    @Value("${main.mount-point}")
    private String mountPoint;
    @Value("${main.ad.server.domain}")
    private String serverDomain;
    @Value("${main.ad.server.share}")
    private String share;
    @Value("${main.ad.server.netbios-domain}")
    private String netbiosDomain;

    private final AuthorityMappingService authorityMappingService;

    private String sharePath;

    @PostConstruct
    void init() {
        this.sharePath = "//" + serverDomain + "/" + share; // создаём после инъекции Value
        if (log.isDebugEnabled()) {
            log.debug("AccessReader initialized. sharePath={}, mountPoint={}, netbiosName={}, netbiosDomain={}",
                    sharePath, mountPoint, netbiosName, netbiosDomain);
        }
    }

    /**
     * Получает ACL для файла и возвращает ID групп
     */
    public List<String> getAcl(String filePath) {
        List<String> aclNames = getAclNames(filePath);
        if (aclNames.isEmpty()) {
            return List.of();
        }
        return authorityMappingService.mapToIds(aclNames);
    }

    /**
     * Внутренний метод для извлечения названий групп из smbcacls
     */
    private List<String> getAclNames(String filePath) {
        if (SystemUtils.IS_OS_LINUX) {
            String relativePath = String.valueOf(Paths.get(mountPoint).relativize(Paths.get(filePath)));
            List<String> cmd = new ArrayList<>();
            cmd.add("smbcacls");
            cmd.add(sharePath);
            cmd.add(relativePath);
            cmd.add("-A");
            cmd.add(authFile);
            cmd.add("--netbiosname");
            cmd.add(netbiosName);
            if (log.isDebugEnabled()) {
                cmd.add("--debuglevel=1");
            } // изменить при необходимости
            try {
                Process process = new ProcessBuilder().command(cmd).start();
                process.waitFor();
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                /*
                 * Пример того, что находится в output для папки N:\ISO9001 и 13485\ИП РИ ОИ СТО на момент 15.08.2025
                 * REVISION:1
                 * CONTROL:SR|DI|DP
                 * OWNER:ELEKTRON\elena
                 * GROUP:ELEKTRON\Domain Users
                 * ACL:NT AUTHORITY\SYSTEM:ALLOWED/OI|CI|I/FULL
                 * ACL:ELEKTRON\Отдел Сертификации_:ALLOWED/OI|CI|I/RWXD
                 * ACL:ELEKTRON\(ISO9001 и 13485):ALLOWED/OI|CI|I/RWXD
                 * ACL:ELEKTRON\(ISO9001 и 13485 - ro):ALLOWED/OI|CI|I/READ
                 * ACL:ELEKTRON\Domain Admins:ALLOWED/OI|CI|I/FULL
                 * ACL:ELEKTRON\Domain Users:ALLOWED/OI|CI|I/READ
                 * ACL:ELEKTRON\Служба Качества - Руководитель:ALLOWED/OI|CI|I/RWXD
                 */

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("smbcacls failed, exit code: {}", exitCode);
                    if (log.isDebugEnabled()) {
                        log.debug(output);
                    }
                    return List.of();
                }
                Pattern aclPattern = Pattern.compile("^ACL:"+netbiosDomain+"\\\\([^:]+):ALLOWED.*?/(FULL|CHANGE|READ|R)", Pattern.CASE_INSENSITIVE);
                // ^ \\\\ конвертится джавой в \\, которое используется в regex вместо \

                List<String> authorities = Arrays.stream(output.split("\n"))
                        .map(String::trim)
                        .map(line -> {
                            Matcher m = aclPattern.matcher(line);
                            if (m.find()) {
                                return m.group(1); // имя группы/пользователя
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .toList();
                return authorities;
            } catch (Exception e) {
                log.error("Failed to read ACL for file: {}", filePath, e);
                return List.of();
            }
        } else {
            log.warn("Non-Linux environment detected; smbcacls ACL data will be skipped.");
            return List.of(ImportantAuthorities.DOMAIN_USERS.toString());
        }
    }
}
