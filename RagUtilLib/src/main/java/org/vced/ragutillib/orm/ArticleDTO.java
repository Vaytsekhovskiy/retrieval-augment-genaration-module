package org.vced.ragutillib.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO содержащий минимальную инфу о статье для сравнения с другими статьями и обновления БД.
 * Не содержит тяжёлых полей типа content, чтобы не перегружать память.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    private String youTrackId;
    private Long updated;
}
