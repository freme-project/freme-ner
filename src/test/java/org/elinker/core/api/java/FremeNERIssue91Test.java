package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNERIssue91Test {


    private FremeNer fremeNer;

    public FremeNERIssue91Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotLinkResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef() {
        //Init
        String text = "Jose F. Marcos";
        String language = "en";
        String datasets = "orcid,dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3339-2584>"));
    }

    @Ignore
    @Test
    public void spotLinkResponseForOrcidWithAccentedTextMustHaveTaIdentRef() {
        //Init
        String text = "José F. Marcos";
        String language = "en";
        String datasets = "orcid,dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3339-2584>"));
    }


    @Test
    public void spotLinkClassifyResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef() {
        //Init
        String text = "Jose F. Marcos";
        String language = "en";
        String datasets = "orcid,dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3339-2584>"));
    }

    @Ignore
    @Test
    public void spotLinkClassifyResponseForOrcidWithAccentedTextMustHaveTaIdentRef() {
        //Init
        String text = "José F. Marcos";
        String language = "en";
        String datasets = "orcid,dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3339-2584>"));
    }

}