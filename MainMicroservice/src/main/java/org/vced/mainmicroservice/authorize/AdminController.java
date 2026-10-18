package org.vced.mainmicroservice.authorize;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.vced.mainmicroservice.ApiHandler;
import org.vced.mainmicroservice.MainMicroserviceWebUtil;

import java.security.Principal;
import java.util.List;

/**@author Егор
 * Класс, отвечающий за роли админа на сервисе.
 * Возможность зайти в панель
 * и Забанить юзера*/
@Controller
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority(T(org.vced.ragutillib.authority.ImportantAuthorities).ADMIN_ROLE.toString())"
                ) // на фронте используются группы из SpringSecurity напрямую, а не AuthenticationDataORM, соот-но представлены они в названиях а не id. весело
public class AdminController {
    @Value("${microservice.gateway}")
    private String apiGatewayUrl;
    private final DiscoveryClient discoveryClient;
    private final MainMicroserviceWebUtil mainMicroserviceWebUtil;
    private final ApiHandler apiHandler;
    @GetMapping("/admin")
    public String getAdminPanel(Model model, Principal principal, HttpServletRequest servletRequest) throws Exception {
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        model.addAttribute("user", userDetails);
        String XUser = mainMicroserviceWebUtil.extractXUserFromSession(principal, servletRequest);
        model.addAttribute("foldersInfo", apiHandler.getFolderInfo(XUser));
        model.addAttribute("principal", XUser);
        List<String> services = discoveryClient.getServices();
        model.addAttribute("services", services);
        return "admin";
    }

    @SneakyThrows
    @PostMapping("/admin/update-authorities")
    @ResponseBody
    public ResponseEntity<Void> updateAuthorities(@RequestParam("x-user") String xUser, @RequestHeader("X-CSRF-TOKEN") String xCsrfToken) throws JsonProcessingException, ResourceAccessException {
        try {
            // отправляем запрос в ConvertMicroservice для обновления прав доступа
            log.info("Admin started doc authorities sync.");
            RestClient restClient = RestClient.create();
            ResponseEntity<Integer> response = restClient.post()
                .uri(apiGatewayUrl + "/api/convert/admin/update-authorities?x-user=" + xUser)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("X-CSRF-TOKEN", xCsrfToken)
                .retrieve()
                .toEntity(Integer.class);
            Integer updatedCount = response.getBody();
            if (updatedCount != null) {
                log.info("Admin finished doc authorities sync. Updated {} documents", updatedCount);
            } else {
                log.warn("Admin finished doc authorities sync. Response body was null");
                updatedCount = 0;
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update authorities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

