package org.vced.ragutillib.vector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "vector_store")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Chunk {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private String id;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    @Column(name = "embedding", columnDefinition = "vector")
    private String embedding;
}
