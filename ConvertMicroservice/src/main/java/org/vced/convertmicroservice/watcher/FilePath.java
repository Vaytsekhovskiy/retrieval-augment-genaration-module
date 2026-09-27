package org.vced.convertmicroservice.watcher;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_path")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class FilePath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path", columnDefinition = "text", nullable = false, unique = true)
    private String path;

    @Column(name = "path_except")
    private boolean pathExcept;
}
