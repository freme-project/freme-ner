package org.elinker.core.api.java;


import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static eu.freme.common.conversion.rdf.RDFConstants.nifPrefix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FremeNERIssue155Test extends ResourceTestFiles {

    private FremeNer fremeNer;

    public FremeNERIssue155Test() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    @Ignore
    public void mergeNIFValues()  throws Exception {

        //Init
        String text =  getContent("issue155/issue155.nif");
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
        assertEquals(text, response);

    }
}
