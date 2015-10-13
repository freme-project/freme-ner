package eu.freme.fremener;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by nilesh on 12/10/15.
 */
@Component
public class FremeNer {
    private org.elinker.core.api.scala.FremeNer fremeNer = null;

    @Value("${fremener.solrurl:http://localhost:8983}")
    String solrUrl = "";
    
    @Value("${fremener.languages:en,de}")
    String languages = "en,de";

    @Value("${fremener.models-location:c:/freme/}")
    String modelsLocation = "";

    //@Value("${fremener.solrurl:http://localhost:8983}")
    String mysqlURI = "http://localhost";
    
    @PostConstruct
    public void init(){
    	String[] languagesArray = languages.split(",");
    	Config config = new Config(languagesArray, modelsLocation, solrUrl, mysqlURI);
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
