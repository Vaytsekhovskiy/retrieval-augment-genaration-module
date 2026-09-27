package org.vced.ragutillib.vector;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.vced.ragutillib.authority.ImportantAuthorities;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитный класс для построения фильтров доступа к чанкам
 */
public final class AuthorityChunkFilters {
    private AuthorityChunkFilters() {
        // Prevent instantiation
    }

    /**
     * Построение фильтра на основе ролей пользователя
     */
    public static Filter.Expression buildAuthBasedFilter(List<String> userAuthorities) {
        String domainUsers = ImportantAuthorities.DOMAIN_USERS.getId();

        if (userAuthorities.isEmpty()) {
            // Если ролей нет (такого не должно происходить), разрешаем только публичные документы (доступны Domain Users)
            return new FilterExpressionTextParser().parse("authorities IN ['"+domainUsers+"']");
        }

        // переводим в формат json массива ['a', 'b'] т.к. спринговский шедевропарсер фильтров почему-то так не умеет
        return new FilterExpressionTextParser().parse("authorities IN " +
                userAuthorities.stream().collect(Collectors.joining("', '", "['", "']")));
    }

    /**
     * Единый фильтр доступа для всех типов документов:
     * - Для статей YouTrack: ( (authorities IN A) OR (authorities == ['UnlimitedVisibility']) ) AND ( (organization_id IN O) OR (project_key IN P) )
     * - Для остальных документов: (authorities IN A)
     */
    public static Filter.Expression buildAccessFilter(List<String> userAuthorities) {
        FilterExpressionTextParser parser = new FilterExpressionTextParser();
        String domainUsers = ImportantAuthorities.DOMAIN_USERS.getId();
        String unlimitedVis = ImportantAuthorities.UNLIMITED_VISIBILITY.getId();

        String authExpr;
        if (userAuthorities == null || userAuthorities.isEmpty()) {
            authExpr = "authorities IN ['"+domainUsers+"']";
        } else {
            authExpr = "authorities IN " + userAuthorities.stream().collect(Collectors.joining("', '", "['", "']"));
        }

        // Собираем доступные по организациям/проектам для статей (YT)
        String ytOrExpr = null;

        String customDocsExpr = "(doc_type == 'custom') AND (" + authExpr + ")";

        String articleAuthExpr = "((" + authExpr + ") OR (authorities IN ['"+unlimitedVis+"']))";
        String articleDocsExpr = null;
        if (ytOrExpr != null) {
            articleDocsExpr = "(doc_type == 'article') AND (" + articleAuthExpr + ") AND (" + ytOrExpr + ")";
        }

        // Комбинируем: файлы (custom) OR статьи (article). Если доступа ни к каким орг/проектам нет, возвращаем только файлы
        String fullExpr = (articleDocsExpr == null)
                ? customDocsExpr
                : "(" + customDocsExpr + ") OR (" + articleDocsExpr + ")";

        return parser.parse(fullExpr);
    }
}
