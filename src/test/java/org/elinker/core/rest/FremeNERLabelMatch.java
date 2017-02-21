package org.elinker.core.rest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.junit.Assert;
import org.junit.Test;
import org.nlp2rdf.nif21.NIF21Format;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FremeNERLabelMatch {

    private FremeNerEnrichment fremeNer;

    public FremeNERLabelMatch() throws IOException, UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
        fremeNer = context.getBean(FremeNerEnrichment.class);
    }

    private final String acceptHeader = "text/turtle";
    private final String contentTypeHeader = "text";
    private final String language = "nl";
    private final String dataset = "sbrnet";
    private final Integer numLinksParam = 100;
    private final String enrichementType = "";
    private final String mode = "";
    private final String domain = "";
    private final String types = "";
    private final String datasetKey = "";


    private Integer getNumberOfLinks(String nif) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(nif.getBytes()), null, "TTL");
        StmtIterator iter = model.listStatements();

        Integer result = 0;

        while (iter.hasNext()) {
            Statement stm = iter.nextStatement();
            if (NIF21Format.RDF_PROPERTY_IDENTREF.equals(stm.getPredicate().toString())) {
                result += 1;
            }
        }

        return result;
    }


    @Test
    public void houten_kozijn_must_return_exact_match_links() {

        //Init
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mode", "all");
        String postBody = "houten kozijn";

        //Execute
        String result = fremeNer.annotate(acceptHeader,
                contentTypeHeader,
                language,
                dataset,
                numLinksParam,
                enrichementType,
                mode,
                domain,
                types,
                datasetKey,
                allParams,
                postBody);


        //Assert
        Assert.assertEquals(Integer.valueOf(8), getNumberOfLinks(result));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/203.4.2.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/201.4.2.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/203.4.1.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/201.4.1.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/202.4.2.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/201.0.3.01.T1"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/201.0.1.01.T1"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/202.4.1.01"));
    }

    @Test
    public void ankerloze_spouwmuur_must_return_exact_match_links() {

        //Init
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mode", "all");
        String postBody = "ankerloze spouwmuur";

        //Execute
        String result = fremeNer.annotate(acceptHeader,
                contentTypeHeader,
                language,
                dataset,
                numLinksParam,
                enrichementType,
                mode,
                domain,
                types,
                datasetKey,
                allParams,
                postBody);


        //Assert
        Assert.assertEquals(Integer.valueOf(5), getNumberOfLinks(result));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/204.4.2.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Bouwmuurtype/2"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/204.4.1.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/402.1.0.01.G1"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/204.1.1.01.G1"));

    }

    @Test
    public void ankerloze_spouwmuur_dakelementen_op_de_ankerloze_spouwmuur_must_return_exact_match_links() {

        //Init
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mode", "all");
        String postBody = "ankerloze spouwmuur, dakelementen op de ankerloze spouwmuur";

        //Execute
        String result = fremeNer.annotate(acceptHeader,
                contentTypeHeader,
                language,
                dataset,
                numLinksParam,
                enrichementType,
                mode,
                domain,
                types,
                datasetKey,
                allParams,
                postBody);


        //Assert
        Assert.assertEquals(Integer.valueOf(2), getNumberOfLinks(result));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/402.4.0.02"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/418.4.0.01"));
    }

    @Test
    public void fundering_must_return_exact_match_links() {

        //Init
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mode", "all");
        String postBody = "fundering - bouwmuur ankerloze spouwmuur, dakelementen op de ankerloze spouwmuur";

        //Execute
        String result = fremeNer.annotate(acceptHeader,
                contentTypeHeader,
                language,
                dataset,
                numLinksParam,
                enrichementType,
                mode,
                domain,
                types,
                datasetKey,
                allParams,
                postBody);


        //Assert
        Assert.assertEquals(Integer.valueOf(3), getNumberOfLinks(result));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/402.4.0.02"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/418.4.0.01"));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Posite/104"));
    }

    @Test
    public void zwevende_dekvloer_must_return_exact_match_links() {

        //Init
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mode", "all");
        String postBody = "zwevende dekvloer";

        //Execute
        String result = fremeNer.annotate(acceptHeader,
                contentTypeHeader,
                language,
                dataset,
                numLinksParam,
                enrichementType,
                mode,
                domain,
                types,
                datasetKey,
                allParams,
                postBody);


        //Assert
        Assert.assertEquals(Integer.valueOf(1), getNumberOfLinks(result));
        Assert.assertTrue(result.contains("http://data.sbrcurnet/Referentiedetail/367.1.0.01.G1"));
    }
}
