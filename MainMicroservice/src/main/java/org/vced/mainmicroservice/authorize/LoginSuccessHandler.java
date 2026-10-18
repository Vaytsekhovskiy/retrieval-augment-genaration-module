package org.vced.mainmicroservice.authorize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.vced.ragutillib.authentication.Principal2XUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String AUTH_DATA_SESSION_KEY = "AUTH_DATA";

    private final Principal2XUser principal2Xuser;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String username = authentication.getName();
        long startAll = System.currentTimeMillis();
        try {
            String XUser = principal2Xuser.process(authentication, true);


            request.getSession(true).setAttribute(AUTH_DATA_SESSION_KEY, XUser);
            long tookAll = System.currentTimeMillis() - startAll;
            log.debug("Stored AuthenticationDataORM for {} in session, took {} ms",
                    authentication.getName(),
                    tookAll);
        } catch (Exception e) {
            log.warn("Failed to prefetch data for {}: {}", username, e.toString());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
