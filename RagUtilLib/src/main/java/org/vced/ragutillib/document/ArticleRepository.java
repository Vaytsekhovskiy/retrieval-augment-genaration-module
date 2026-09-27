package org.vced.ragutillib.document;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends BaseDocumentRepository<Article> {
    Optional<Article> findArticleByYouTrackId(String youTrackId);
    List<Article> findArticlesByProjectKey(String projectReadableId);

    /**
     * Fetch only ID and youTrackId for efficient polling without loading heavy content
     */
    @Query("SELECT new org.vced.ragutillib.orm.ArticleDTO(a.youTrackId, a.updated) FROM Article a")
    List<org.vced.ragutillib.orm.ArticleDTO> findAllAsDTO();
}
