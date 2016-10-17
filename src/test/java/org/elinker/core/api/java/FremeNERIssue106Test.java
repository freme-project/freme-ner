package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class FremeNERIssue106Test extends ResourceTestFiles {

    private FremeNer fremeNer;

    public FremeNERIssue106Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }


    @Test
    public void spotLinkResponseWithTourismDomainMustHaveEntites() throws Exception {
        //Init
        String text = "I like Berlin, the capital of Germany";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-1510";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod,nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue106/spotLinkResponseWithTourismDomainMustHaveEntites.nif");
        assertEquals(nif, response);
    }


    @Test
    public void spotLinkResponseWithEconomicsDomainMustHaveEntites() throws Exception {
        //Init
        String text = "The World Economic Forum is a Swiss nonprofit foundation, based in Cologny, Geneva";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-0300";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;
        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod,nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue106/spotLinkResponseWithEconomicsDomainMustHaveEntites.nif");
        assertEquals(nif, response);

    }

    @Test
    public void spotLinkResponseWithAnInvalidDomainMustNotThrowNoSuchElementException() throws Exception {
        //Init
        String text = "I like Berlin, the capital of Germany";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "I am an invalid Domain";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod,nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue106/spotLinkResponseWithAnInvalidDomainMustNotThrowNoSuchElementException.nif");
        assertEquals(nif, response);
    }

    @Test
    public void spotLinkResponseWithEconomicsRelatedTextAndSocialSciencesDomainMustDoNotHaveEntites()throws Exception {
        //Init
        String text = "The World Economic Forum is a Swiss nonprofit foundation, based in Cologny, Geneva";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "TaaS-2000";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod,nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue106/spotLinkResponseWithEconomicsRelatedTextAndSocialSciencesDomainMustDoNotHaveEntites.nif");
        assertEquals(nif, response);

    }

}
