package org.elinker.core.api.java;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.common.rest.RestHelper;
import eu.freme.common.starter.FREMEStarter;

public class FremeNerTest {

	ConfigurableApplicationContext context;
	TestHelper testHelper;

	public FremeNerTest() {
		context = FREMEStarter
				.startPackageFromClasspath("freme-ner-test-package.xml");
		testHelper = context.getBean(TestHelper.class);
	}

	@Test
	public void testFremeNer() {

		FremeNer fner = context.getBean(FremeNer.class);
		String response = fner.spot("Welcome to Berlin, the capital of Germany.", "en", "TTL",
				"http://freme-project.eu");
		assertTrue(response.contains("itsrdf:taConfidence"));

		// System.out.println(fner.spot("Willkommen in Berlin", "de", "TTL",
		// "http://freme-project.eu"));

		// System.out.println(fner.spotLinkClassify("Berlin is a city in Germany.",
		// "en", "dbpedia", "TTL", "http://freme-project.eu", 1));
		//
		// Set<String> types = new HashSet<>();
		//
		// types.add("http://dbpedia.org/ontology/Scientist");
		// types.add("http://dbpedia.org/ontology/Philosopher");
		// System.out.println(fner.spotLinkClassify("Albert Einstein was a famous physicist.",
		// "en", "dbpedia", "TTL", "", 1, types));
		// System.out.println(fner.spotLinkClassify("Arthur Schopenhauer was a renowned philosopher.",
		// "en", "dbpedia", "TTL", "", 1, types));
		//
		// String domain = "TaaS-2007 Sports";
		// System.out.println(fner.spotLinkClassify("Alaska is a place. Albert Einstein is from Austria. Andre Agassi plays tennis.",
		// "en", "dbpedia", "TTL", "", 1, domain));

	}

}
