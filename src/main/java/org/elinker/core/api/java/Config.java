    package org.elinker.core.api.java;

import eu.freme.common.persistence.dao.DatasetSimpleDAO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

    /**
 * Created by nilesh on 12/10/15.
 */
public class Config {
    @Autowired
    private DatasetSimpleDAO datasetDAO;
    String sparqlEndpoint = "";
    String solrURI = "";
    String languages = "";
    String modelsLocation = "";
    String domainsFile = "";

    public void setSparqlEndpoint(String sparqlEndpoint) { this.sparqlEndpoint = sparqlEndpoint; }

    public void setSolrURI(String solrURI) {
        this.solrURI = solrURI;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public void setModelsLocation(String modelsLocation) {
        this.modelsLocation = modelsLocation;
    }

    public void setDomainsFile(String domainsFile) {
        this.domainsFile = domainsFile;
    }

    private org.elinker.core.api.scala.Config scalaConfig = null;

    /**
     * Default constructor for Spring
     */
    public Config() {
    }

    /**
     * Config class for initializing an FremeNER instance.
     *
     * Created by nilesh on 12/10/15.
     *
     * @param languages Array of ISO language codes (eg. en, de, fr)
     * @param modelsLocation Directory where ner models are loaded
     * @param sparqlEndpoint URI of SPARQL endpoint (mainly used for fetching resource types)
     * @param solrURI SOLR URI for the entity linker
     * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
     * @param datasetDAO Spring DAO for managing datasets
     */
    public Config(String[] languages,
                  String modelsLocation,
                  String sparqlEndpoint,
                  String solrURI,
                  String domainsFile,
                  DatasetSimpleDAO datasetDAO) {
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                sparqlEndpoint,
                solrURI,
                domainsFile,
                datasetDAO);
    }

    public org.elinker.core.api.scala.Config getScalaConfig() {
        return scalaConfig;
    }

    @PostConstruct
    public void init(){
        String[] languages = this.languages.split(",");
        this.scalaConfig = new org.elinker.core.api.scala.Config(
                languages,
                modelsLocation,
                sparqlEndpoint,
                solrURI,
                domainsFile,
                datasetDAO);
    }
}
