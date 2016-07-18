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
    
//	@Autowired
//    private DatasetMetadataDAO datasetMetadataDAO;
//>>>>>>> refs/remotes/origin/configuration-optional-#108
    
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

    @Value("${freme.ner.linkingMethod}")
    String linkingMethod = "";
    
    private boolean sparqlEndointEnabled;
    private boolean solrURIEnabled;
    private boolean domainsFileEnabled;

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
                linkingMethod
        );
        
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

    public String getLinkingMethod() {
        return linkingMethod;
    }

    public void setLinkingMethod(String linkingMethod) {
        this.linkingMethod = linkingMethod;
    }
}
