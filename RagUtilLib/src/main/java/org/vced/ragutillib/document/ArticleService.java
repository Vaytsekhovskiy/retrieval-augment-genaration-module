package org.vced.ragutillib.document;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.vced.ragutillib.vector.ChunkService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService extends BaseDocumentService<Article, ArticleRepository> {
    public ArticleService(ArticleRepository repository, ChunkService chunkService) { super(repository, chunkService); }

    public Article getByYouTrackId(String youTrackId) {
        return repository.findArticleByYouTrackId(youTrackId).orElse(null);
    }

    public List<Article> getByProjectKey(String projectKey) {
        return repository.findArticlesByProjectKey(projectKey);
    }

    /** timestamp - время последнего (прошлого) обновления БД. True = пора обновить этот Article */
    public boolean needsUpdate(Article article, long timestamp) {
        if (article == null || article.getUpdated() == null) return true;
        return article.getUpdated() > timestamp;
    }

    /**
     * findAll() в виде DTO а не полных Article, чтобы не загружать в память тяжёлые тексты и картинки.
     * Используется при поллинге и применении изменений.
     */
    public List<org.vced.ragutillib.orm.ArticleDTO> findAllAsDTO() {
        return repository.findAllAsDTO();
    }

    /** Аналогично PDFReader, разбивает по главам (heading) и пакует в спринговские Document,
     * подготавливая к обработке в ChunkSplitter и хранению в PgVector.<br>
     * При необходимости, можно отличать вектор-чанки артиклов от кастомДокументов
     * по наличию поля youtrack_id в метаданных. */
    public List<Document> splitByHeadings(Article article) {

        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();

        var id = article.getId();
        var youtrackId = article.getYouTrackId();
        var orgId = article.getOrganizationId();
        var projKey = article.getProjectKey();
        var name = article.getName();
        var authorities = article.getAuthorities();
        var set = article.getContent();
        String content = set.isEmpty() ? "" : set.iterator().next();
        Node document = parser.parse(content);

        List<Document> chunks = new ArrayList<>();
        List<Node> headings = new ArrayList<>();

        // Проходимся парсером, собираем все заголовки
        NodeVisitor headingCollector = new NodeVisitor(
                new VisitHandler<>(Heading.class, heading -> {
                    if (heading.getLevel() <= 3) { // макс уровень заголовка, по которым разбиваем
                        // (21.10)  4 и больше это слишком, это всегда подпункты без контекста. 3 - спорно, зависит от статьи
                        headings.add(heading);
                    }
                })
        );
        headingCollector.visit(document);

        int startPos;
        String currentTitle;
        int currentLevel;

        // Обрабатываем текст расположенный до первого заголовка, если он есть
        if (headings.isEmpty() || headings.get(0).getStartOffset() > 0) {
            int endPos = headings.isEmpty() ? content.length() : headings.get(0).getStartOffset();
            String chunkText = content.substring(0, endPos).trim();
            if (!chunkText.isEmpty()) {
                chunks.add(Document.builder()
                        .text(chunkText)
                        .metadata("id", id)
                        .metadata("doc_type", "article")
                        .metadata("youtrack_id", youtrackId)
                        .metadata("organization_id", orgId)
                        .metadata("project_key", projKey)
                        .metadata("level", 0)
                        // нет заголовка - ес чё обрабатывайте выше через metadata.containsKey("title")
                        .metadata("file_name", name)
                        .metadata("authorities", authorities)
                        .build());
            }
        }

        // Обрабатываем каждый раздел, определённый заголовками
        for (int i = 0; i < headings.size(); i++) {
            Heading heading = (Heading) headings.get(i);
            startPos = heading.getStartOffset();
            int endPos = (i < headings.size() - 1) ? headings.get(i + 1).getStartOffset() : content.length();

            currentTitle = heading.getText().toString();
            currentLevel = heading.getLevel();

            String chunkText = content.substring(startPos, endPos).trim();
            if (!chunkText.isEmpty()) {
                chunks.add(Document.builder()
                        .text(chunkText)
                        .metadata("id", id)
                        .metadata("doc_type", "article")
                        .metadata("youtrack_id", youtrackId)
                        .metadata("organization_id", orgId)
                        .metadata("project_key", projKey)
                        .metadata("level", currentLevel)
                        .metadata("title", currentTitle)
                        .metadata("file_name", name)
                        .metadata("authorities", authorities)
                        .build());
            }
        }

        return chunks;
    }

}
