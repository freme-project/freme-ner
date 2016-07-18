package org.elinker.core.api.java;


import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import eu.freme.common.exception.BadRequestException;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import scala.collection.JavaConverters;

/**
 * FremeNER Java API for performing spotting, linking and dataset management.
 * This class is essentially a wrapper around the Scala API.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
public class FremeNer {
    private org.elinker.core.api.scala.FremeNer fremeNer = null;
    
    @Autowired
    Config config;
    
    public FremeNer(){
    }
    
    @PostConstruct
    public void init(){
        fremeNer = new org.elinker.core.api.scala.FremeNer(config.getScalaConfig());
    }

    public String spot(String text, String language, String outputFormat, String rdfPrefix) {
        return fremeNer.spot(text, language, outputFormat, rdfPrefix);
    }

    public String spotClassify(String text, String language, String outputFormat, String rdfPrefix) {
        return fremeNer.spotClassify(text, language, outputFormat, rdfPrefix);
    }

    public String spotLink(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, String domain, String types) {
        return fremeNer.spotLink(text, language, dataset, outputFormat, rdfPrefix, numLinks, domain, types);
    }

    public String link(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, String domain, String types) {
        return fremeNer.link(text, language, dataset, outputFormat, rdfPrefix, numLinks, domain, types);
    }

    public String spotLinkClassify(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, String domain, String types) {
        return fremeNer.spotLinkClassify(text, language, dataset, outputFormat, rdfPrefix, numLinks, domain, types);
    }

    @Transactional
    public Long addToDataset(String name, org.elinker.core.api.scala.FremeNer.InputType dataset, String format, String language, String[] properties) {
        return fremeNer.addToDataset(name, dataset, format, language, properties);
    }

    @Transactional
    public void deleteDataset(String name) {
    	
		// check if the solr server is configured
		if(!this.config.isSolrURIEnabled()){
			throw new BadRequestException("The configuration of Freme NER is insufficient for this API Call. Please add the configuration option"
    					+ " \"freme.ner.solrURI.\"");
		}
    	
        fremeNer.deleteDataset(name);
    }

}
