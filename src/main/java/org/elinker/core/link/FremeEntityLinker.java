package org.elinker.core.link;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.elinker.core.api.process.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.Option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elinker.core.common.SolrExpressions.SOLR_CORE;
import static org.elinker.core.common.SolrFields.URL;
import static org.elinker.core.common.Symbols.EMPTY;

@Service
public class FremeEntityLinker {

    @Value("${freme.ner.solrURI:}")
    private String solrURI;


    public List<Result> link(List<String> datasets, Integer topN, String language, List<Result> entities) {

        QueryBuilder queryBuilder = new QueryBuilder(datasets, language);
        SolrClient solr = new HttpSolrClient(solrURI);
        List<Result> results = new ArrayList<>();


        for (Result result : entities) {

            try {

                SolrQuery query = queryBuilder.buildQuery(result);
                query.setRows(topN);

                QueryResponse response = solr.query(SOLR_CORE, query);

                SolrDocumentList list = response.getResults();

                Set<String> urls = new HashSet<>();

                list.stream().forEach(document -> {
                    results.add(new Result(EMPTY, result.mention(), result.beginIndex(), result.endIndex(),
                            Option.apply(extractValue(document, URL)), result.score()));
                });

            } catch (Exception e) {
                results.add(new Result(EMPTY, result.mention(), result.beginIndex(), result.endIndex(),
                        Option.apply(EMPTY), result.score()));
            }
        }

        return results;
    }

    private String extractValue(SolrDocument document, String field) {

        if (document.get(field) == null) return EMPTY;

        if ( document.get(field) instanceof  ArrayList) {
            List<String> result = (List<String>) document.get(field);
            if (result.size() == 1) {
                return result.get(0);
            }
            return EMPTY;
        }

        return document.get(field).toString();
    }

}