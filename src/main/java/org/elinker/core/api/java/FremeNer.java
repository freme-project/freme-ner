package org.elinker.core.api.java;

/**
 * Created by nilesh on 12/10/15.
 */
public class FremeNer {
    private org.elinker.core.api.scala.FremeNer fremeNer = null;

    String solrUrl;

    public FremeNer(Config config) {
        org.elinker.core.api.scala.Config scalaConfig = config.getScalaConfig();
        fremeNer = new org.elinker.core.api.scala.FremeNer(scalaConfig);
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

    public String spotLinkClassify(String text, String language, String dataset, String outputFormat, String rdfPrefix, Integer numLinks) {
        return fremeNer.spotLinkClassify(text, language, dataset, outputFormat, rdfPrefix, numLinks);
    }
}
