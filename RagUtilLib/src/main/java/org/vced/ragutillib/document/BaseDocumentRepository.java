package org.vced.ragutillib.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseDocumentRepository<T extends BaseDocument> extends JpaRepository<T, Long> {
    // findAll и findById наслудются от JpaRepository
    Optional<T> findByName(String name);
    @Query(value = """
            SELECT
                            count(*), 
                            count(*) filter (where d.doc_type = 'custom' ),
                            count(*) filter (where d.doc_type = 'article')
            FROM documents d 
            """, nativeQuery = true)
    List<Object[]> findDocumentsStats();
    @Query(value = """
            SELECT id, name, doc_type
            FROM documents
            ORDER BY id
            LIMIT :size OFFSET :offset;
            """, nativeQuery = true)
    List<Object[]> findAllLimit(@Param("size") int size,
                                @Param("offset") int offset
    );
}