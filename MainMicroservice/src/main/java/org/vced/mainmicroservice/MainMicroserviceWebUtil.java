package org.vced.mainmicroservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.vced.ragutillib.authentication.Principal2XUser;
import org.vced.mainmicroservice.authorize.LoginSuccessHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainMicroserviceWebUtil {
    private final ObjectMapper objectMapper;
    private final Principal2XUser principal2Xuser;

    public static String getImage(String pathToImage) {
        Path path = Paths.get(pathToImage);
        byte[] data = null;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] encoded = Base64.getEncoder().encode(data);
        String imgDataAsBase64 = new String(encoded);
        String imgAsBase64 = "data:image/png;base64," + imgDataAsBase64;
        return imgAsBase64;

    }

    private static String extractFromSession() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                var session = sra.getRequest().getSession(false);
                if (session != null) {
                    Object stored = session.getAttribute(LoginSuccessHandler.AUTH_DATA_SESSION_KEY);
                    if (stored instanceof String data) {
                        return data;
                    }
                }
            }
        } catch (Exception ignored) {
            // Вне веб-запроса или недоступен контекст — просто не используем сессию
        }
        return null;
    }

    public String extractXUserFromSession(Principal principal, HttpServletRequest servletRequest) throws Exception {
        // 1) Если в сессии уже лежит подготовленный AuthenticationDataORM — используем его
        String sessionXUserAuthData = extractFromSession();
        if (sessionXUserAuthData != null) {
            return sessionXUserAuthData;
        } else {
            String XUser = principal2Xuser.process(principal, true);
            /**
             * Пробуем добавить в сессию AuthenticationDataORM по такой же логике как и в {@link LoginSuccessHandler}
             */
            servletRequest.getSession(true).setAttribute(LoginSuccessHandler.AUTH_DATA_SESSION_KEY, XUser);
            return XUser;
        }
    }
}