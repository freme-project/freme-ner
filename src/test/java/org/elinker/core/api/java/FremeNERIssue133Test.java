package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Ignore
public class FremeNERIssue133Test extends ResourceTestFiles {

    private FremeNer fremeNer;


    public FremeNERIssue133Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    //Enable linking with multiple datasets at once
    @Test
    public void spotLinkResponseWithMultipleDatasetsMustHaveEntitesFromEachDataset() throws Exception {
        //Init
        String text = "Berlin is in Germany.";
        String language = "en";
        String datasets = "dbpedia,geopolitical";
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
        String nif = getContent("issue133/spotLinkResponseWithMultipleDatasetsMustHaveEntitesFromEachDataset.nif");
        assertEquals(nif, response);
    }
}
