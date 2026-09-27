package org.vced.ragutillib.authority;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity для хранения маппинга между строковыми именами AD/YouTrack групп и внутренними числовыми ID.
 * Таблица append-only: записи добавляются, но никогда не удаляются и не изменяются.
 */
@Entity
@Table(name = "authority_mapping", indexes = {
        @Index(name = "idx_authority_name", columnList = "external_name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Внешнее имя группы (из AD или YouTrack) */
    @Column(name = "external_name", nullable = false, unique = true, length = 256)
    private String externalName;

    /** Время создания записи (для дебага и т.п.) */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();
}
