package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static eu.freme.common.conversion.rdf.RDFConstants.nifPrefix;
import static org.junit.Assert.assertEquals;

public class FremeNERIssue21Test extends ResourceTestFiles {

    private FremeNer fremeNer;

    public FremeNERIssue21Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotResponseForOrcidGiannisMustHaveNIFProperties() throws Exception {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spot(text, language, outputFormat, rdfPrefix, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue21/spotResponseForOrcidGiannisMustHaveNIFProperties.nif");
        assertEquals(nif, response);
    }


    @Test
    public void spotClassifyResponseForOrcidGiannisMustHaveNIFProperties() throws Exception {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String nifVersion = RDFConstants.nifVersion2_0;

        //Execution
        String response = fremeNer.spotClassify(text, language, outputFormat, rdfPrefix, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue21/spotClassifyResponseForOrcidGiannisMustHaveNIFProperties.nif");
        assertEquals(nif, response);
    }

    @Test
    public void spotLinkClassifyResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() throws Exception {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
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
        String nif = getContent("issue21/spotLinkClassifyResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities.nif");
        assertEquals(nif, response);
    }

    @Test
    public void spotLinkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() throws Exception {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
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
        String nif = getContent("issue21/spotLinkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities.nif");
        assertEquals(nif, response);
    }

    @Test
    public void linkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() throws Exception {
        //Init
        String text = "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
                "@prefix nif:   <"+nifPrefix+"> .\n" +
                "\n" +
                "<http://freme-project.eu#char=0,16>\n" +
                "        a                     nif:Word , nif:String , nif:Context , nif:Phrase , nif:RFC5147String ;\n" +
                "        nif:anchorOf          \"Giannis Stoitsis\"^^xsd:string ;\n" +
                "        nif:beginIndex        \"0\"^^xsd:int ;\n" +
                "        nif:endIndex          \"16\"^^xsd:int ;\n" +
                "        nif:isString          \"Giannis Stoitsis\"^^xsd:string ;\n" +
                "        nif:referenceContext  <http://freme-project.eu#char=0,16> ;";

        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_0;


        //Execution
        String response = fremeNer.link(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("issue21/linkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities.nif");
        assertEquals(nif, response);
    }


}
