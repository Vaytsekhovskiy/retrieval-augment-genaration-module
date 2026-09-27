package org.vced.ragutillib.document;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс, с которым работает таблица documents.
 * Общий наследник для двух видов документов: CustomDocument (файлы) и Article (статьи из YouTrack)
 * Наследники различаются через стобик doc_type (если стратегия InheritanceType.JOINED)
 */
@Entity
@Table(name = "documents")
@Inheritance(strategy = InheritanceType.JOINED) // or SINGLE_TABLE, TABLE_PER_CLASS
// TODO: поменять стратегию при необходимости
@DiscriminatorColumn(name = "doc_type")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDocument {
    /**
     * Id документа
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * для доков - Name.pdf; для статей - заголовок статьи (summary)
     */
    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

//    private boolean isStrange;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "document_authority",
            joinColumns = @JoinColumn(name = "document_id")
    )
    @Column(name = "authority")
    private List<String> authorities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_related", joinColumns = @JoinColumn(name = "document_id"))
//    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "bigint")
    private Set<Long> relatedDocs;

    /**
     * Создаёт вспомогательную таблицу для ролей
     * Задаём параметры вспомогательной таблицы (имя + по какому полю объединять две таблицы)
     * преобразует элементы коллекций в тип String
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_content", joinColumns = @JoinColumn(name = "document_id"))
//    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private Set<String> content = new HashSet<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id") // Создаёт внешний ключ в таблице CustomImage
    private List<CustomImage> images = new ArrayList<>();
}