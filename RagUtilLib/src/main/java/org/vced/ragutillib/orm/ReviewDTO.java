package org.vced.ragutillib.orm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String AiAnswer;
    private int eval;
    private String login;
    private String request;
    private String userReview;
}
