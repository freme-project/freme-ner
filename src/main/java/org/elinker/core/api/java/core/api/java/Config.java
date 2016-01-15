package org.elinker.core.api.java.core.api.java;

import eu.freme.common.persistence.dao.DatasetSimpleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * Created by nilesh on 12/10/15.
 */
public class Config {
    @Autowired
    private DatasetSimpleDAO datasetDAO;

    @Value("${fremener.solrurl:http://localhost:8983}")
    String solrURI = "";

    @Value("${fremener.languages:en,de}")
    String languages = "";

    @Value("${fremener.models-location:/opt/freme/freme-ner/models}")
    String modelsLocation = "";

    @Value("${fremener.domains:/opt/freme/freme-ner/domains.csv}")
    String domainsFile = "";

    private org.elinker.core.api.scala.Config scalaConfig = null;

    /**
     * Config class for initializing an FremeNER instance.
     *
     * Created by nilesh on 12/10/15.
     *
     * @param languages Array of ISO language codes (eg. en, de, fr)
     * @param modelsLocation Directory where ner models are loaded
     * @param solrURI SOLR URI for the entity linker
     * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
     * @param datasetDAO Spring DAO for managing datasets
     */
    public Config(String[] languages,
                  String modelsLocation,
                  String solrURI,
                  String domainsFile,
                  DatasetSimpleDAO datasetDAO) {
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                solrURI,
                domainsFile,
                datasetDAO);
    }

    protected org.elinker.core.api.scala.Config getScalaConfig() {
        return scalaConfig;
    }

    @PostConstruct
    public void init(){
        String[] languages = this.languages.split(",");
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                solrURI,
                domainsFile,
                datasetDAO);
    }
}
