package org.vced.ragutillib.document;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "document_image")
public class CustomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "path", columnDefinition = "TEXT")
    private String path;
    private int page;
    @Column(name = "y_pos")
    private float yPos;
    @Column(name = "is_strange", nullable = true)
    private Boolean isStrange;
}
