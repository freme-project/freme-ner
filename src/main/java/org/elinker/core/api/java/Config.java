package org.elinker.core.api.java;

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
     * @param mysqlURI MySQL URI for managing dataset metadata
     */
    public Config(String[] languages, String modelsLocation, String solrURI, String mysqlURI) {
        this.solrURI = solrURI;
        this.mysqlURI = mysqlURI;
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                solrURI,
                mysqlURI);
    }

    protected org.elinker.core.api.scala.Config getScalaConfig() {
        return scalaConfig
    }
}
