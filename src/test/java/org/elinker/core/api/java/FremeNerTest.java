package org.elinker.core.api.java;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FremeNerTest {


    private FremeNer fremeNer;


    public FremeNerTest() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNer.class);
    }

    @Test
    public void spotResponseMustHaveNIFproperties() {

        String response = fremeNer.spot("Welcome to Berlin, the capital of Germany.", "en", "TTL", "http://freme-project.eu");
        assertTrue(response.contains("itsrdf:taConfidence"));
        assertTrue(response.contains("nif:anchorOf"));
        assertTrue(response.contains("nif:beginIndex"));
        assertTrue(response.contains("nif:endIndex"));
    }

    @Test
    public void spotClassifyResponseMustHaveNIFproperties() {

        String response = fremeNer.spot("Welcome to Berlin, the capital of Germany.", "en", "TTL", "http://freme-project.eu");
        assertTrue(response.contains("itsrdf:taConfidence"));
        assertTrue(response.contains("nif:anchorOf"));
        assertTrue(response.contains("nif:beginIndex"));
        assertTrue(response.contains("nif:endIndex"));
    }


}
