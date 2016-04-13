package org.elinker.core.api.java;

import static org.junit.Assert.assertTrue;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.OwnedResourceManagingHelper;
import eu.freme.bservices.testhelper.SimpleEntityRequest;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.persistence.model.DatasetMetadata;
import eu.freme.common.rest.OwnedResourceManagingController;

import org.apache.log4j.Logger;
import org.elinker.core.rest.FremeNerManageDatasets;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.common.rest.RestHelper;
import eu.freme.common.starter.FREMEStarter;

import java.io.IOException;

public class FremeNerTest {

	private Logger logger = Logger.getLogger(FremeNerTest.class);
	private AuthenticatedTestHelper ath;
	private OwnedResourceManagingHelper<DatasetMetadata> ormh;
	private FremeNer fner;
	final static String serviceUrl = "/e-entity/freme-ner";

	final static String dataset1 = "  <http://www.lib.ncsu.edu/ld/onld/00000326> <http://www.w3.org/2004/02/skos/core#prefLabel> \"101 Communications\" .\n" +
			"  <http://www.lib.ncsu.edu/ld/onld/00000326> <http://www.w3.org/2004/02/skos/core#altLabel> \"101 Communications, Inc\" .\n" +
			"  <http://www.lib.ncsu.edu/ld/onld/00000326> <http://www.w3.org/2004/02/skos/core#altLabel> \"101 Communications, LLC\" .\n" +
			"  <http://www.lib.ncsu.edu/ld/onld/00000326> <http://www.w3.org/2004/02/skos/core#altLabel> \"101communications\" .";

	final static String dataset2 = "  <http://www.lib.ncsu.edu/ld/onld/00001273> <http://www.w3.org/2004/02/skos/core#prefLabel> \"2Cs Communications\" .\n" +
			"  <http://www.lib.ncsu.edu/ld/onld/00000842> <http://www.w3.org/2004/02/skos/core#prefLabel> \"2x4 (Design studio)\" .";

	ConfigurableApplicationContext context;
	TestHelper testHelper;

	public FremeNerTest() throws IOException, UnirestException {
		ApplicationContext context = IntegrationTestSetup.getContext("freme-ner-test-package.xml");
//		ath = context.getBean(AuthenticatedTestHelper.class);
//		ormh = new OwnedResourceManagingHelper<>(serviceUrl+"/datasets",DatasetMetadata.class, ath);
//		ath.authenticateUsers();
		fner = context.getBean(FremeNer.class);
	}

//	@Test
//	@Ignore
//	public void testDatasetMetadataManagement() throws IOException, UnirestException {
//		String datasetName = "testDataset";
//		SimpleEntityRequest createRequest = new SimpleEntityRequest(dataset1)
//				.putParameter(DatasetMetadata.getIdentifierName(), datasetName)
//				.putHeader("content-type", "text/turtle")
//				.putParameter("language", "en")
//				.putParameter(OwnedResourceManagingController.descriptionParameterName, "Test dataset");
//		SimpleEntityRequest updateRequest = new SimpleEntityRequest(dataset2)
//				.putHeader("content-type", "text/turtle")
//				.putParameter("language", "en");
//		DatasetMetadata expectedCreatedEntity = new DatasetMetadata();
//		expectedCreatedEntity.setName(datasetName);
//		expectedCreatedEntity.setTotalEntities(4);
//
//		DatasetMetadata expectedUpdatedEntity = new DatasetMetadata();
//		expectedUpdatedEntity.setName(datasetName);
//		expectedUpdatedEntity.setTotalEntities(6);
//
//		ormh.checkCRUDOperations(createRequest,updateRequest,expectedCreatedEntity, expectedUpdatedEntity, "xxx");
//	}

	@Test
	public void testFremeNer() {


		String response = fner.spot("Welcome to Berlin, the capital of Germany.", "en", "TTL",
				"http://freme-project.eu");
		assertTrue(response.contains("itsrdf:taConfidence"));

	}

}
