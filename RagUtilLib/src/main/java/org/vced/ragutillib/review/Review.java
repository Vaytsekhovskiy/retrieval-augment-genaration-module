package org.vced.ragutillib.review;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ai_answer", columnDefinition = "text", nullable = false)
    private String AiAnswer;

    @Column(name = "eval", nullable = false)
    private int eval;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "moderated", nullable = false)
    private boolean moderated;

    @Column(name = "request", columnDefinition = "text", nullable = false)
    private String request;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "user_review", columnDefinition = "text")
    private String userReview;
}
