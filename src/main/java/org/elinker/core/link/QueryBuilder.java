package org.elinker.core.link;

import org.apache.solr.client.solrj.SolrQuery;
import org.elinker.core.api.process.Result;

import java.util.List;
import java.util.stream.Collectors;

import static org.elinker.core.common.SolrExpressions.AND_DATASET_CONTAINS;
import static org.elinker.core.common.SolrExpressions.AND_LANGUAGE_CONTAINS;
import static org.elinker.core.common.SolrExpressions.LABEL_EQUALS;
import static org.elinker.core.common.SolrFields.URL;
import static org.elinker.core.common.Symbols.COMMA;
import static org.elinker.core.common.Symbols.EMPTY;
import static org.elinker.core.common.Symbols.QUOTES_WITH_SPACE;

public class QueryBuilder {

    private final SolrQuery query;
    private final List<String> datasets;
    private final String language;

    public QueryBuilder(List<String> datasets,
                        String language) {
        this.query = new SolrQuery();
        this.datasets = datasets;
        this.language = language;
    }

    public SolrQuery buildQuery(final Result result) {

        final String QUERY_FORMAT = "%s%s%s";

        final String QUERY_STRING = String.format(QUERY_FORMAT,
                label(result.mention()),
                datasets(),
                language());

        query.setFields(URL).setQuery(QUERY_STRING);
        query.set("sort", "score desc, count desc");

        return query;

    }

    private String language() {

        if (this.language == null || this.language.isEmpty()) return EMPTY;

        return String.format(AND_LANGUAGE_CONTAINS, language);
    }

    private String datasets() {

        if (this.datasets == null || this.datasets.isEmpty()) return EMPTY;

        return String.format(AND_DATASET_CONTAINS,
                this.datasets.stream().collect(Collectors.joining(COMMA)).replace(COMMA, QUOTES_WITH_SPACE));
    }

    private String label(final String label) {

        return String.format(LABEL_EQUALS, label);
    }

}