package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNERTestIssue21 {

    private FremeNer fremeNer;

    public FremeNERTestIssue21() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotResponseForOrcidGiannisMustHaveNIFProperties() {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spot(text, language, outputFormat, rdfPrefix);
        System.out.println(response);

        //Check
        assertTrue(response.contains("nif:isString          \"Giannis Stoitsis\"^^xsd:string"));
    }


    @Test
    public void spotClassifyResponseForOrcidGiannisMustHaveNIFProperties() {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotClassify(text, language, outputFormat, rdfPrefix);
        System.out.println(response);

        //Check
        assertTrue(response.contains("nif:isString          \"Giannis Stoitsis\"^^xsd:string"));
    }

    @Test
    public void spotLinkClassifyResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3347-8265>"));
    }

    @Test
    public void spotLinkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() {
        //Init
        String text = "Giannis Stoitsis";
        String language = "en";
        String datasets = "orcid";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3347-8265>"));
    }

    @Test
    public void linkResponseForOrcidGiannisThatAreIntheIndexMustHaveEntities() {
        //Init
        String text = "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
                "@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
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

        //Execution
        String response = fremeNer.link(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types);
        System.out.println(response);

        //Check
        assertTrue(response.contains("<http://orcid.org/0000-0003-3347-8265>"));
    }

}
