package org.elinker.core.api.java;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNerTest {


    private FremeNer fremeNer;


    public FremeNerTest() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotResponseMustHaveNIFproperties() {
        //Init
        String text = "Welcome to Berlin, the capital of Germany.";
        String language = "en";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";

        //Execution
        String response = fremeNer.spot(text, language, outputFormat, rdfPrefix);
        System.out.println(response);

        //Check
        assertTrue(response.contains("itsrdf:taConfidence"));
        assertTrue(response.contains("nif:anchorOf"));
        assertTrue(response.contains("nif:beginIndex"));
        assertTrue(response.contains("nif:endIndex"));
    }

    @Test
    public void spotClassifyResponseMustHaveNIFproperties() {
        //Init
        String text = "Welcome to Berlin, the capital of Germany.";
        String language = "en";
        String datasets = "orcid,dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks,domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("itsrdf:taConfidence"));
        assertTrue(response.contains("nif:anchorOf"));
        assertTrue(response.contains("nif:beginIndex"));
        assertTrue(response.contains("nif:endIndex"));
    }

}
