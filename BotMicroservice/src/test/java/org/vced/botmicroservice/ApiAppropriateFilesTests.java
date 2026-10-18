//package org.vced.botmicroservice;
//
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//
//import static org.hamcrest.Matchers.is;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.yml")
//@ContextConfiguration(initializers = {TestContainerInitializer.Initializer.class})
//public class ApiAppropriateFilesTests extends ApiTests{
//    @Autowired
//    private MockMvc mockMvc;
//    @Test
//    @SneakyThrows
//    public void should_return_bad_request_for_no_params(){
//        this.mockMvc.perform(get("/api/search/files/get"))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }
//    @Test
//    @SneakyThrows
//    public void should_return_test_document(){
//        this.mockMvc.perform(get("/api/search/files/get")
//                        .param("request", "testDocument"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].relevantContent", is("testDocument")))
//                .andExpect(jsonPath("$[0].id").exists())
//                .andExpect(jsonPath("$[0].fileName").exists())
//                .andExpect(jsonPath("$[0].relatedDocs").exists())
//                .andExpect(jsonPath("$[0].relatedDocs[0].fileName", is("relatedDocument")));
//
//    }
//    @Test
//    @SneakyThrows
//    public void should_return_bad_request_for_empty_param() {
//        this.mockMvc.perform(get("/api/search/files/get")
//                        .param("request", "")) // Пустой параметр
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }
//    @Test
//    @SneakyThrows
//    public void should_allow_cors() {
//        this.mockMvc.perform(get("/api/search/files/get")
//                        .param("request", "testDocument")
//                        .header("Origin", "https://example.com")) // Заголовок Origin
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(header().exists("Access-Control-Allow-Origin")); // Проверка заголовка CORS
//    }
//    @Test
//    @SneakyThrows
//    public void should_handle_long_request_param() {
//        String longRequest = "a".repeat(10000); // Очень длинный параметр
//        this.mockMvc.perform(get("/api/search/files/get")
//                        .param("request", longRequest))
//                .andDo(print())
//                .andExpect(status().isBadRequest()); // или isBadRequest(), в зависимости от логики
//    }
//    @Test
//    @SneakyThrows
//    public void should_return_response() {
//        this.mockMvc.perform(get("/api/bot/response/get")
//                        .param("request", "hello! what's your name?"))
//                .andDo(print())
//                .andExpect(status().isOk());
//    }
//}