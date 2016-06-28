package org.elinker.core.api.java;


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
    //@Autowired
    //private DatasetMetadataDAO datasetMetadataDAO;
    
    @Value("${freme.ner.sparqlEndpoint}")
    String sparqlEndpoint = "";
    
    @Value("${freme.ner.solrURI}")
    String solrURI = "";
    
    @Value("${freme.ner.languages}")
    String languages = "";
    
    @Value("${freme.ner.modelsLocation}")
    String modelsLocation = "";
    
    @Value("${freme.ner.domainsFile}")
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
                domainsFile
        );
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
