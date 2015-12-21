package org.elinker.core.api.java.core.api.java;

/**
 * Created by nilesh on 12/10/15.
 */
public class Config {
    private String[] modelFiles = null;
    private String solrURI = null;
    private String mysqlURI = null;
    private org.elinker.core.api.scala.Config scalaConfig = null;

    /**
     * Config class for initializing an FremeNER instance.
     *
     * Created by nilesh on 12/10/15.
     *
     * @param languages Array of ISO language codes (eg. en, de, fr)
     * @param modelsLocation Directory where ner models are loaded
     * @param solrURI SOLR URI for the entity linker
     * @param databaseUri MySQL URI for managing dataset metadata
     * @param dbpediaInstanceTypesFile Location of dbpedia instance types transitive dataset
     * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
     */
    public Config(String[] languages,
                  String modelsLocation,
                  String solrURI,
                  String databaseUri,
                  String dbpediaInstanceTypesFile,
                  String domainsFile) {
        this.solrURI = solrURI;
        this.mysqlURI = databaseUri;
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                solrURI,
                databaseUri,
                dbpediaInstanceTypesFile,
                domainsFile);
    }

    protected org.elinker.core.api.scala.Config getScalaConfig() {
        return scalaConfig;
    }
}
