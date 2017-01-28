package org.elinker.core.common;

import static org.elinker.core.common.SolrFields.DATASET;
import static org.elinker.core.common.SolrFields.LABEL;
import static org.elinker.core.common.SolrFields.LANGUAGE;

public interface SolrExpressions {

    String SOLR_CORE = "elinker";

    String AND = " AND ";

    String OR = " OR ";

    String CONTAINS = ":(\"%s\")";

    String EQUALS = ":'%s'";

    String AND_LANGUAGE_CONTAINS = AND + "(" +  LANGUAGE + EQUALS + OR +  LANGUAGE + ":xx" + ")";

    String AND_DATASET_CONTAINS = AND + DATASET + CONTAINS;

    String LABEL_EQUALS = LABEL + EQUALS;
}