package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FremeNERNIF21Test extends ResourceTestFiles{

    private FremeNer fremeNer;

    public FremeNERNIF21Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    @Ignore
    public void spotResponseMustReturnValidNIF20() throws Exception {
        //Init
        String text = "Berlin was an European Capital of Culture in 1988";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String nifVersion = RDFConstants.nifVersion2_1;

        //Execution
        String response = fremeNer.spot(text, language, outputFormat, rdfPrefix, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("nif21/fremener-spot.nif");
        assertEquals(nif, response);
    }

    @Test
    @Ignore
    public void spotClassifyMustReturnValidNIF20() throws Exception {
        //Init
        String text = "Berlin was an European Capital of Culture in 1988";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "N3";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String nifVersion = RDFConstants.nifVersion2_1;

        //Execution
        String response = fremeNer.spotClassify(text, language, outputFormat, rdfPrefix, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("nif21/fremener-spotclassify.nif");
        assertEquals(nif, response);

    }


    @Test
    public void spotLinkMustReturnValidNIF20() throws Exception {
        //Init
        String text = "Berlin was an European Capital of Culture in 1988";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_1;

        //Execution
        String response = fremeNer.spotLink(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);


        //Check
        String nif = getContent("nif21/fremener-spotlink.nif");
        assertEquals(nif, response);

    }

    @Test
    @Ignore
    public void spotLinkClassifyMustReturnValidNIF20() throws Exception {
        //Init
        String text = "Berlin was an European Capital of Culture in 1988";
        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_1;

        //Execution
        String response = fremeNer.spotLinkClassify(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("nif21/fremener-spotlinkclassify.nif");
        assertEquals(nif, response);

    }


    @Test
    @Ignore
    public void linkResponseMustReturnValidNIF20() throws Exception {
        //Init
        String text = "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
                "@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
                "<http://freme-project.eu#char=0,49>\n" +
                "        a               nif:RFC5147String , nif:Context , nif:String ;\n" +
                "        nif:beginIndex  \"0\"^^xsd:int ;\n" +
                "        nif:endIndex    \"49\"^^xsd:int ;\n" +
                "        nif:isString    \"Berlin was an European Capital of Culture in 1988\"^^xsd:string .\n";

        String language = "en";
        String datasets = "dbpedia";
        String outputFormat = "TTL";
        String rdfPrefix = "http://freme-project.eu";
        Integer numLinks = 1;
        String domain = "";
        String types = "";
        String linkingMethod = "";
        String nifVersion = RDFConstants.nifVersion2_1;

        //Execution
        String response = fremeNer.link(text, language, datasets, outputFormat, rdfPrefix, numLinks, domain, types, linkingMethod, nifVersion);
        System.out.println(response);

        //Check
        String nif = getContent("nif21/fremener-link.nif");
        assertEquals(nif, response);
    }



}
