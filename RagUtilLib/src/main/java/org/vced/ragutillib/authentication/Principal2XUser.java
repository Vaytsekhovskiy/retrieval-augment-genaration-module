package org.vced.ragutillib.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.vced.ragutillib.authority.AuthorityMappingService;
import org.vced.ragutillib.authority.ImportantAuthorities;
import org.vced.ragutillib.orm.AuthenticationDataORM;

import java.security.Principal;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class Principal2XUser {
    private final AuthorityMappingService authorityMappingService;
    private final ObjectMapper objectMapper;
    private static final Duration YT_TIMEOUT = Duration.ofSeconds(5);
    private static int AUTHORITIES_LIMIT = 200; // limitByDefault

    public void setAuthoritiesLimit(int authoritiesLimit) {
        AUTHORITIES_LIMIT = authoritiesLimit > 0 ? authoritiesLimit : AUTHORITIES_LIMIT;
    }

    public String process(Principal principal, boolean limitAuthorities) {
        if (principal == null) return null;
        List<String> authorities;
        try {
            authorities = ((Authentication) principal)
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        } catch (ClassCastException e) {
            log.warn("Strange behavior, principal: {} has no authorities. Principal authorities will be empty list",
                    principal.getName());
            authorities = List.of();
        }

        authorities = authorityMappingService.mapToIds(authorities);
        try {
        } catch (Exception e) {
            log.warn("Exception while retrieving readable organization ids and project keys for youtrack" +
                    "for user: {}. Ids and key will be empty value", principal.getName());
        }
        if (limitAuthorities) {
            limitAuthorities(authorities, principal.getName());;
        }
        AuthenticationDataORM data = AuthenticationDataORM
                .builder()
                .name(principal.getName())
                .authorities(authorities)
                .build();
        try {
            return CryptoUtils.encrypt(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error("FATAL SHIT! AuthenticationDataORM cannot be serialized into JSON: {} for the user: {} " +
                            "Fix AuthenticationDataORM and then restart project!",
                    principal.getName(), e.getMessage());
            return "";
        }
    }

    private void limitAuthorities(List<String> authorities, String username) {
        if (authorities.isEmpty() || authorities.size() < AUTHORITIES_LIMIT) return;
        var importantAuthoritiesIds = new HashSet<>(Arrays.asList(ImportantAuthorities.idValues()));
        log.warn("Authorities limit for user {} reached. Authorities: {} . Trying to reduce authorities",
                username, authorities);
        int prevAuthoritiesSize = authorities.size();
        for (Iterator<String> it = authorities.iterator(); it.hasNext(); ) {
            String authority = it.next();
            if (!importantAuthoritiesIds.contains(authority)) {
                it.remove();     // удалить этот элемент
                if (authorities.size() <= AUTHORITIES_LIMIT) break;
            }
        }
        if (prevAuthoritiesSize == authorities.size()) {
            log.error("Reducing the amount of authorities was unsuccessful! " +
                            "User: {} contains only ImportantAuthorities: {}, " +
                            "and its authorities list still longer than AUTHORITIES_LIMIT = {}",
                    username, authorities, AUTHORITIES_LIMIT);
            return;
        }
        log.warn("Authorities for user {} reduced successfully. New authorities: {}.",
                username, authorities);
    }
}
