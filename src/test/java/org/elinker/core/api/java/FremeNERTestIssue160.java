package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNERTestIssue160 {

    private FremeNer fremeNer;

    public FremeNERTestIssue160() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }


    @Test
    public void spotLinkResponseWithTourismDomainMustHaveEntites() {
        //Init
        String text = "I like Berlin, the capital of Germany";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-1510";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://dbpedia.org/resource/Germany>"));
    }


    @Test
    public void spotLinkResponseWithEconomicsDomainMustHaveEntites() {
        //Init
        String text = "The World Economic Forum is a Swiss nonprofit foundation, based in Cologny, Geneva";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-0300";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://dbpedia.org/resource/World_Economic_Forum>"));

    }

    @Test (expected = java.util.NoSuchElementException.class)
    public void spotLinkResponseWithAnInvalidDomainMustThrowNoSuchElementException() {
        //Init
        String text = "I like Berlin, the capital of Germany";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "I am an invalid Domain";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);
    }

    @Test
    public void spotLinkResponseWithEconomicsRelatedTextAndSocialSciencesDomainMustDoNotHaveEntites() {
        //Init
        String text = "The World Economic Forum is a Swiss nonprofit foundation, based in Cologny, Geneva";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-2000";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(!response.contains("<http://dbpedia.org/resource/World_Economic_Forum>"));

    }

}
