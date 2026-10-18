package org.vced.mainmicroservice.authorize;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.vced.ragutillib.authentication.AuthenticationConfig;
import org.vced.ragutillib.authentication.Principal2XUser;
import org.vced.ragutillib.authority.AuthorityMappingConfig;
import org.vced.ragutillib.ldap.CustomLdapAuthoritiesPopulator;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Import(value = {AuthorityMappingConfig.class, AuthenticationConfig.class})
@EnableMethodSecurity()
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.ldap.external.enabled", havingValue = "false")
    public AuthenticationProvider embeddedLdapAuthenticationProvider(
            @Value("${spring.ldap.embedded.user-dn-pattern}") String userDnPattern,
            @Value("${spring.ldap.embedded.group-search-base}") String groupSearchBase,
            @Value("${spring.ldap.embedded.url}") String embeddedLdapUrl,
            @Value("${spring.ldap.embedded.base-dn}") String embeddedBaseDn
    ) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(embeddedLdapUrl);
        contextSource.setBase(embeddedBaseDn);
        contextSource.afterPropertiesSet();

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserDnPatterns(new String[]{userDnPattern});

        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);

        authoritiesPopulator.setGroupRoleAttribute("cn");
        authoritiesPopulator.setRolePrefix("");
        authoritiesPopulator.setConvertToUpperCase(false);

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ldap.external.enabled", havingValue = "true")
    public ActiveDirectoryLdapAuthenticationProvider externalLdapAuthenticationProvider(
            @Value("${spring.ldap.external.url}") String externalLdapUrl,
            @Value("${spring.ldap.external.domain}") String externalDomain,
            @Value("${spring.ldap.external.search-base}") String searchBase,
            @Value("${spring.ldap.external.domain-sid}") String domainSid
    ) {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(externalLdapUrl);
        contextSource.setBase(searchBase);
        contextSource.setReferral("ignore");
        contextSource.setPooled(true);
        contextSource.afterPropertiesSet();

        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider(externalDomain, externalLdapUrl);
        provider.setSearchFilter("(&(objectClass=user)(userPrincipalName={0}))");
//        provider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");

        CustomLdapAuthoritiesPopulator authoritiesPopulator =
                new CustomLdapAuthoritiesPopulator(contextSource, "", domainSid);
        // специальный фильтр, по которому AD-сервер сам проводит рекурсивный поиск и возвращает все вложенные группы
        authoritiesPopulator.setGroupSearchFilter("(&(objectClass=group)(member:1.2.840.113556.1.4.1941:={0}))");
        authoritiesPopulator.setGroupRoleAttribute("sAMAccountName"); // smbcacls возвращает группы в каком-то древнем формате
        authoritiesPopulator.setIgnorePartialResultException(true);
        authoritiesPopulator.setSearchSubtree(true);
        authoritiesPopulator.setRolePrefix("");
        authoritiesPopulator.setConvertToUpperCase(false);

        provider.setAuthoritiesPopulator(authoritiesPopulator);
        return provider;
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(Principal2XUser principal2Xuser) {
        return new LoginSuccessHandler(principal2Xuser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginSuccessHandler loginSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/api/main/**").permitAll()
                        .requestMatchers("/api/bot/**").authenticated() // Защита эндпоинтов
                        .requestMatchers("/api/convert/**").authenticated() // Защита эндпоинтов
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }
}