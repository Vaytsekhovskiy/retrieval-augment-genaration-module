package org.vced.ragutillib.document;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 *
 */
@Entity
@DiscriminatorValue("article") // Значение для колонки doc_type, чтобы отличать типы документов
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Article extends BaseDocument {
    /** YouTrack ID статьи, например, 103-227. Не путать с readableId типа TEST_PROJ-A-10 */
    @Column(name = "youtrack_id", columnDefinition = "TEXT", unique = true)
    private String youTrackId;

    /** Hub id организации, например, TEST_PROJ.*/
    @Column(name = "organization_id", columnDefinition = "TEXT")
    private String organizationId;

    /** key (shortName) проекта, например, TEST_PROJ.*/
    @Column(name = "project_key", columnDefinition = "TEXT")
    private String projectKey;

    @Builder.Default
    /** UNIX UTC millisecond timestamp времени последнего обновления статьи */
    @Column(name = "updated")
    private Long updated = 0L;
}
