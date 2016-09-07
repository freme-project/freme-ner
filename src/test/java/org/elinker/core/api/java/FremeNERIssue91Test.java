package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FremeNERIssue91Test extends ResourceTestFiles {


    private FremeNer fremeNer;

    public FremeNERIssue91Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotLinkResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef() throws Exception {
        //Init
        String text = "Jose F. Marcos";
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
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue91/spotLinkResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef.nif");
        assertEquals(nif, response);
    }

    @Test
    public void spotLinkResponseForOrcidWithAccentedTextMustHaveTaIdentRef() throws Exception {
        //Init
        String text = "José F. Marcos";
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
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue91/spotLinkResponseForOrcidWithAccentedTextMustHaveTaIdentRef.nif");
        assertEquals(nif, response);
    }


    @Test
    public void spotLinkClassifyResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef() throws Exception {
        //Init
        String text = "Jose F. Marcos";
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
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue91/spotLinkClassifyResponseForOrcidWithUnaccentedTextMustHaveTaIdentRef.nif");
        assertEquals(nif, response);
    }

    @Test
    public void spotLinkClassifyResponseForOrcidWithAccentedTextMustHaveTaIdentRef() throws Exception {
        //Init
        String text = "José F. Marcos";
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
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue91/spotLinkClassifyResponseForOrcidWithAccentedTextMustHaveTaIdentRef.nif");
        assertEquals(nif, response);
    }

}