package org.vced.ragutillib.ldap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.*;

import static org.springframework.ldap.query.LdapQueryBuilder.query;


/**
 * Кастомный LdapAuthoritiesPopulator для нашего проекта.
 * Наследник DefaultLdapAuthoritiesPopulator с добавлением в authorities ещё и основной группы из атрибута PrimaryGroup
 * (обычно Domain Users, Domain Admins и т.п.), т.к. в AD это обрабатывается отдельно,
 * а также с добавлением групп прав из YouTrack.<br>
 * Ещё добавляет логин пользователя в authorities для случая когда к файлу доступ у отдельного пользователя.<br>
 * Внешние названия групп позже переводятся в числовые ID через {@link org.vced.ragutillib.authentication.Principal2XUser#process Principal2XUser.process()}
 * с помощью {@link org.vced.ragutillib.authority.AuthorityMappingService AuthorityMappingService}
 * для экономии места в headers.
 */
@Slf4j
public class CustomLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

    private final String domainSid;
    private static final java.time.Duration YT_TIMEOUT = java.time.Duration.ofSeconds(5); // таймаут обращения к сервам YouTrack

    public CustomLdapAuthoritiesPopulator(ContextSource contextSource,
                                                String groupSearchBase,
                                                String domainSid) {
        super(contextSource, groupSearchBase);
        this.domainSid = domainSid;
    }

    /**
     * Предоставляет дополнительные роли (YT + primaryGroup + username),
     * которые потом объединяются с ролями из AD.
     * @param user the context representing the user who's roles are required
     * @param username sAMAccountName
     * @return Set со всеми доп ролями в виде SimpleGrantedAuthority
     */
    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        Set<GrantedAuthority> authorities = new HashSet<>(); // new HashSet, иначе сломается при пустом ответе
        GrantedAuthority primary = getPrimaryGroup(user, username);
        if (primary != null) authorities.add(primary);
        if (username != null && !username.isBlank()) authorities.add(new SimpleGrantedAuthority(username));

        return authorities;
    }

    /**
     * Метод для поиска PrimaryGroup пользователя
     * @param user the context representing the user who's roles are required
     * @param username sAMAccountName
     * @return PrimaryGroup в виде SimpleGrantedAuthority
     */
    private GrantedAuthority getPrimaryGroup(DirContextOperations user, String username) {
        long start = System.currentTimeMillis();
        try {
            // primaryGroup просто нету в атрибутах пользователя и приходится искать по id
            // а даже если бы был, то в виде DN, и всё равно пришлось бы делать поиск

            String primaryGroupId = user.getStringAttribute("primaryGroupID");
            if (primaryGroupId == null) {
                log.warn("LDAP: no primaryGroupID for user {}", username);
                return null;
            }

            String primaryGroupSid = domainSid + "-" + primaryGroupId;
            LdapQuery groupQuery = query().where("objectSid").is(primaryGroupSid);
            String primaryGroup = this.getLdapTemplate()
                    .searchForContext(groupQuery)
                    .getStringAttribute("sAMAccountName");

            long total = System.currentTimeMillis() - start;
            log.debug("LDAP: primary group lookup for {} took {} ms", username, total);
            return new SimpleGrantedAuthority(primaryGroup);
        } catch (IncorrectResultSizeDataAccessException e) {
            long total = System.currentTimeMillis() - start;
            log.warn("LDAP: error resolving primary group for {} after {} ms: {}", username, total, e.toString());
            return null;
        }
    }
    /**
     * Метод для сбора групп AD пользователя.
     * Полностью наследует всю логику у родителя, добавлены дебаг-логирование и fallback при ошибке.
     */
    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
        long start = System.currentTimeMillis();
        try {
            var roles = super.getGroupMembershipRoles(userDn, username);

            if (log.isDebugEnabled()) {
                long took = System.currentTimeMillis() - start;
                log.debug("LDAP: group membership lookup for {} took {} ms ({} roles)",
                        username, took, (roles == null ? 0 : roles.size()));
            }
            return roles;
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                long took = System.currentTimeMillis() - start;
                log.warn("LDAP: error resolving groups for {} after {} ms: {}",
                        username, took, e.toString());
            }
            return Set.of(new SimpleGrantedAuthority("Domain Users")); // fallback
        }
    }
}
