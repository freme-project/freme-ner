package org.elinker.core.rest;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.elinker.core.api.java.ResourceTestFiles;
import org.elinker.core.common.FremeActionMode;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FremeNerEnrichmentTest extends ResourceTestFiles {

    private FremeNerEnrichment fremeNerEnrichment;

    public FremeNerEnrichmentTest() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNerEnrichment = context.getBean(FremeNerEnrichment.class);
    }


    private FremeRequest request()  throws Exception {

        String path = "labelmatch/content.nif";

        FremeRequest request = new FremeRequest();

        Map<String, String> allParams = new HashMap<>();

        allParams.put("input", getContent(path));

        request.setAllParams(allParams);
        request.setDataset("dbpedia");
        request.setLanguage("en");
        request.setNumLinksParam(1);


        return request;
    }


    @Test
    public void spotResponse() throws Exception {
        //Arrange
        FremeRequest request = request();

        //Execution
        String response = fremeNerEnrichment.annotate(request());

        //Check
        System.out.println(response);
    }
}
