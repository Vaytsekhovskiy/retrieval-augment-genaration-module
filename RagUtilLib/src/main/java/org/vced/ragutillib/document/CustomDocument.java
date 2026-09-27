package org.vced.ragutillib.document;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Представляет документ, загруженный из файла.
 * В некоторой степени совместим с спринговским Document. Или нет.
 * @author Егорч*/
@Entity
@DiscriminatorValue("custom") // Значение для колонки doc_type, чтобы отличать типы документов
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CustomDocument extends BaseDocument {
    /** pdf путь файла*/
    @Column(name = "path", columnDefinition = "TEXT")
    private String path;
    /** Оригинальный путь к документу*/
    @Column(name = "originalPath", columnDefinition = "TEXT", unique = true)
    private String originalPath;
    /** False - "хороший документ", True - "вероятно это документ форма либо обычный шаблон"
     * подобные документы НЕ заносятся в векторную БД*/
    @Column(name = "is_strange")
    @Builder.Default
    private boolean isStrange = false;
}
