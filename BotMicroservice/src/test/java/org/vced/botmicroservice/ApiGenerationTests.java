//package org.vced.botmicroservice;
//
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.ai.chat.model.Generation;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Flux;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.List;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
////@WebFluxTest(controllers = BotController.class) // https://howtodoinjava.com/spring-boot2/testing/webfluxtest-with-webtestclient/
//// аннотация выше по сути и есть @SpringBootTest, однако она мокирует только контроллеры, @SpringBootTest мокирует всё
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml")
//@ContextConfiguration(initializers = {TestContainerInitializer.Initializer.class})
//public class ApiGenerationTests extends ApiTests {
//    @Autowired
//    private CustomQuestionAnswerAdvisor customQuestionAnswerAdvisor;
//    @Autowired
//    private WebTestClient webClient; // like mockMvc but for reactive
//    @MockitoBean
//    ChatModel chatModel;
//    @Test
//    @SneakyThrows
//    public void should_return_expected_response(){
//        // Mockito.any(Prompt.class) указывает, что мок должен срабатывать для любого объекта Prompt.
//        Mockito.when(chatModel.stream(Mockito.any(Prompt.class))).thenReturn(Flux.just(ChatResponse
//                .builder()
//                .generations(List.of(new Generation(new AssistantMessage("expectedResponse"))))
//                .build()));
//        webClient.get()
//                .uri("/api/bot/response/get?request=testRequest")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(String.class)
//                .consumeWith(response -> {
//                    String responseBody = response.getResponseBody(); // Получаем тело ответа
//                    assertNotNull(responseBody); // Проверяем, что тело ответа не null
//                    assertEquals("'expectedResponse'END", responseBody);
//                    // Проверяем, что тело ответа соответствует ожидаемому
//                });
//    }
//    @Test
//    @SneakyThrows
//    public void should_return_bad_request_for_no_params(){
//        webClient.get()
//                .uri("/api/bot/response/get")
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//    @Test
//    @SneakyThrows
//    public void should_return_bad_request_for_empty_param() {
//        webClient.get()
//                .uri("/api/bot/response/get?request=")
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//}
