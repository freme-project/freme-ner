package org.elinker.core.api.java;


import org.elinker.core.api.process.Datasets;
import org.elinker.core.api.process.Rest;
import org.springframework.beans.factory.annotation.Autowired;

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

    public String spotLink(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks) {
        return fremeNer.spotLink(text, language, dataset, outputFormat, rdfPrefix, numLinks);
    }

    public String spotLink(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, Set<String> types) {
        return fremeNer.spotLink(text, language, dataset, outputFormat, rdfPrefix, numLinks, JavaConverters.asScalaSetConverter(types).asScala().toSet());
    }

    public String spotLink(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, String domain) {
        return fremeNer.spotLink(text, language, dataset, outputFormat, rdfPrefix, numLinks, domain);
    }

    public String spotLinkClassify(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks) {
        return fremeNer.spotLinkClassify(text, language, dataset, outputFormat, rdfPrefix, numLinks);
    }

    public String spotLinkClassify(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, Set<String> types) {
        return fremeNer.spotLinkClassify(text, language, dataset, outputFormat, rdfPrefix, numLinks, JavaConverters.asScalaSetConverter(types).asScala().toSet());
    }

    public String spotLinkClassify(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks, String domain) {
        return fremeNer.spotLinkClassify(text, language, dataset, outputFormat, rdfPrefix, numLinks, domain);
    }

    @Transactional
    public Long addToDataset(String name, org.elinker.core.api.scala.FremeNer.InputType dataset, String format, String language, String[] properties) {
        return fremeNer.addDataset(name, dataset, format, language, properties);
    }

    /*@Transactional
    public Long updateDataset(String name, org.elinker.core.api.scala.FremeNer.InputType dataset, String description, String format, String language, String[] properties) {
        return fremeNer.updateDataset(name, dataset, description, format, language, properties);
    }*/

    @Transactional
    public void deleteDataset(String name) {
        fremeNer.deleteDataset(name);
    }

    /*public Datasets.Dataset getDataset(String name) {
        return fremeNer.getDataset(name);
    }

    public Datasets.Dataset[] getAllDatasets() {
        return fremeNer.getAllDatasets();
    }*/
}
