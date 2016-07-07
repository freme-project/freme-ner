package org.elinker.core.api.java;

import eu.freme.common.persistence.dao.DatasetMetadataDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Config class for initializing a FremeNER instance.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
@Component
public class Config {
    
	@Autowired
    private DatasetMetadataDAO datasetMetadataDAO;
    
    @Value("${freme.ner.sparqlEndpoint:}")
    String sparqlEndpoint = "";
    
    @Value("${freme.ner.solrURI:}")
    String solrURI = "";
    
    @Value("${freme.ner.languages}")
    String languages = "";
    
    @Value("${freme.ner.modelsLocation}")
    String modelsLocation = "";
    
    @Value("${freme.ner.domainsFile:}")
    String domainsFile = "";
    
    boolean sparqlEndointEnabled;
    boolean solrURIEnabled;
    boolean domainsFileEnabled;

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
     * @param languages Array of ISO language codes (eg. en, de, fr)
     * @param modelsLocation Directory where ner models are loaded
     * @param sparqlEndpoint URI of SPARQL endpoint (mainly used for fetching resource types)
     * @param solrURI SOLR URI for the entity linker
     * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
     * @param datasetDAO Spring DAO for managing datasets
     */
//    public Config(String[] languages,
//                  String modelsLocation,
//                  String sparqlEndpoint,
//                  String solrURI,
//                  String domainsFile,
//                  DatasetSimpleDAO datasetDAO) {
//        this.scalaConfig = new org.elinker.core.api.scala.Config(
//                languages,
//                modelsLocation,
//                sparqlEndpoint,
//                solrURI,
//                domainsFile,
//                datasetDAO);
//    }

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
                datasetMetadataDAO);
        
        if(!this.sparqlEndpoint.isEmpty()){
        	this.sparqlEndointEnabled = true;
        }
        if(!this.solrURI.isEmpty()){
        	this.solrURIEnabled = true;
        }
        if(!this.domainsFile.isEmpty()){
        	this.domainsFileEnabled = true;
        }
    }

	public boolean isSparqlEndointEnabled() {
		return sparqlEndointEnabled;
	}

	public boolean isSolrURIEnabled() {
		return solrURIEnabled;
	}

	public boolean isDomainsFileEnabled() {
		return domainsFileEnabled;
	}
    
    
}
