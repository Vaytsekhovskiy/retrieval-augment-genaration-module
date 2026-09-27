package org.vced.ragutillib.orm;

import lombok.*;

import java.util.Set;

/**
 * Предложенный файл, тоже для защиты прослойка
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SuggestedDocumentORM {
    private long id;
    private String fileName;
    private String relevantContent;
    private Set<SuggestedDocumentORM> relatedDocs;
    private Integer page; // nullable

    // for related doc
    public SuggestedDocumentORM(long id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }
}
