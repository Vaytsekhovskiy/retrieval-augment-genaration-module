package org.vced.mainmicroservice.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.vced.ragutillib.authority.ImportantAuthorities;
import org.vced.ragutillib.orm.SuggestedDocumentORM;
import org.vced.mainmicroservice.ApiHandler;
import org.vced.mainmicroservice.MainMicroserviceWebUtil;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.Authentication;

@Controller
@Slf4j
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class ResponseController {
    // нужен для отправки json
    private final MainMicroserviceWebUtil mainMicroserviceWebUtil;
    private final ApiHandler apiHandler;

    // начало сервиса
    @GetMapping("/")
    public String responses(
            Model model,
            Principal principal,
            @RequestParam(name = "request", required = false) String request,
            HttpServletRequest servletRequest
    ) throws Exception {
        model.addAttribute("request", request);
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        model.addAttribute("user", userDetails);
        String XUser = mainMicroserviceWebUtil.extractXUserFromSession(principal, servletRequest);

        model.addAttribute("principal", XUser);
        model.addAttribute("dialogue", apiHandler.getMessages(XUser));
        model.addAttribute("adminRole", ImportantAuthorities.ADMIN_ROLE.toString());
        // на фронте используются группы из SpringSecurity напрямую, а не AuthenticationDataORM, соот-но представлены они в названиях а не id. весело

        if (request != null) {
            try {
                List<SuggestedDocumentORM> appropriateFiles = apiHandler.getSuggestedDocuments(request, XUser);
                if (!appropriateFiles.isEmpty() && "false".equals(appropriateFiles.getFirst().getFileName())) {
                    model.addAttribute("appropriateFiles", false);
                } else {
                    model.addAttribute("appropriateFiles", appropriateFiles);
                }
            } catch (Exception e) {
                // ошибка выводится в консоль + на фронт
                log.error("Error processing JSON response", e);
            }
        }
        
        return "responses";
    }

    @PostMapping("/clear-dialogue")
    @ResponseBody
    public ResponseEntity<?> clearDialogue(@RequestParam("x-user") String XUser) {
        return apiHandler.clearDialogue(XUser);
    }
}
