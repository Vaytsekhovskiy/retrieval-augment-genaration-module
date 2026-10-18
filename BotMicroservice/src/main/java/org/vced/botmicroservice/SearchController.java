package org.vced.botmicroservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.vced.ragutillib.authentication.CryptoUtils;
import org.vced.ragutillib.orm.AuthenticationDataORM;
import org.vced.ragutillib.orm.ReviewDTO;
import org.vced.ragutillib.review.Review;
import org.vced.ragutillib.review.ReviewConfig;
import org.vced.ragutillib.review.ReviewService;
import org.vced.ragutillib.orm.SuggestedDocumentORM;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**@author Егор
 * Файл который отправляет сообщение о самых лучших файлах и отправляет в mainmicroservice*/
@RestController
@RequiredArgsConstructor
@Import(value = {ReviewConfig.class})
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;
    private final ReviewService reviewService;
    @GetMapping("files/get")
    public List<SuggestedDocumentORM>
    getAppropriateFiles(@RequestParam(name = "request", required = true) String request, @RequestParam(name = "x-user") String XUser) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Десериализуем AuthenticationData из заголовка
        String decryptedAuth = CryptoUtils.decrypt(XUser);
        AuthenticationDataORM authenticationDataORM = mapper.readValue(decryptedAuth, AuthenticationDataORM.class);
        List<String> authorities = new ArrayList<>(authenticationDataORM.getAuthorities());

        if (request.isBlank() || request.length() > 800) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be too long or empty");
        }
        String decodedRequest = URLDecoder.decode(request, StandardCharsets.UTF_8);
        if (decodedRequest.length() >= 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be too long");
        }
        return searchService.getAppropriateFiles(decodedRequest, authorities);
    }
    @PostMapping("/review")
    public ResponseEntity<String> saveReview(@RequestBody ReviewDTO reviewDTO) throws Exception {
        var response = Review.builder()
                .AiAnswer(reviewDTO.getAiAnswer())
                .eval(reviewDTO.getEval())
                .login(reviewDTO.getLogin())
                .moderated(false)
                .request(reviewDTO.getRequest())
                .timestamp(System.currentTimeMillis())
                .userReview(reviewDTO.getUserReview())
                .build();
        reviewService.saveReview(response);
        return ResponseEntity.ok().build();
    }
}
