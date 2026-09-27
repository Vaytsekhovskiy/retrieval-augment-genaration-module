package org.vced.ragutillib.vector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    /**
     * Удаляет строки из таблицы vector_store, где поле metadata содержит заданный id
     *
     * @param fileName идентификатор, который ищется в metadata
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM vector_store WHERE metadata->>'file_name' = :fileName;", nativeQuery = true)
    void deleteChunkByFileName(@Param("fileName") String fileName);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM vector_store WHERE (metadata->>'id')::bigint = :metaId;", nativeQuery = true)
    void deleteChunksByMetadataId(@Param("metaId") Long id);

    @Query(value = "SELECT * FROM vector_store WHERE vector_store.metadata ->> ?1 = ?2;", nativeQuery = true)
    List<Chunk> findByAttribute(String key, String value);

    @Modifying
    @Query(value = "DELETE FROM vector_store", nativeQuery = true)
    void deleteAll();

    @Modifying
    @Query(value = "SELECT * FROM vector_store", nativeQuery = true)
    List<Chunk> findAll();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE vector_store 
            SET metadata = jsonb_set(
                COALESCE(metadata, '{}'::jsonb),
                '{authorities}',
                to_jsonb(CAST(:authorities AS text[])),
                true
            )
            WHERE (metadata->>'id')::bigint = :id
            """, nativeQuery = true)
    int updateAuthoritiesById(@Param("id") Long id,
                                   @Param("authorities") String[] authorities);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE vector_store 
            SET metadata = jsonb_set(
                COALESCE(metadata, '{}'::jsonb),
                '{usage_count}',
                to_jsonb(COALESCE((metadata->>'usage_count')::int, 0) + 1),
                true
            )
            WHERE id = CAST(:uuid AS uuid)
            """, nativeQuery = true)
    int incrementUsageCountByUuid(@Param("uuid") String uuid);

    @Query(value = """
            SELECT * FROM vector_store 
            WHERE metadata->>'usage_count' IS NOT NULL
            ORDER BY (metadata->>'usage_count')::int DESC 
            LIMIT :limit
            """, nativeQuery = true)
    List<Chunk> findTopChunksByUsageCount(@Param("limit") int limit);
}
