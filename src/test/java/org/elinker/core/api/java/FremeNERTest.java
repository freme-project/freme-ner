package org.elinker.core.api.java;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNERTest {


    private FremeNer fremeNer;


    public FremeNERTest() throws IOException, UnirestException {
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
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spot(text, language, outputFormat, rdfPrefix, nifVersion);
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
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks,domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        assertTrue(response.contains("itsrdf:taConfidence"));
        assertTrue(response.contains("nif:anchorOf"));
        assertTrue(response.contains("nif:beginIndex"));
        assertTrue(response.contains("nif:endIndex"));
    }

}
