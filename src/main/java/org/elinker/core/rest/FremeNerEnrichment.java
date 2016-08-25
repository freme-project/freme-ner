/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elinker.core.rest;

import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.AccessDeniedException;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.exception.NIFVersionNotSupportedException;
import eu.freme.common.persistence.dao.OwnedResourceDAO;
import eu.freme.common.persistence.model.DatasetMetadata;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;

import org.apache.log4j.Logger;
import org.elinker.core.api.java.Config;
import org.elinker.core.api.java.FremeNer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

import java.util.*;

@RestController
public class FremeNerEnrichment extends BaseRestController {

	public static final String MODE_SPOT = "spot";
	public static final String MODE_CLASSIFY = "classify";
	public static final String MODE_LINK = "link";
	public static final Set<String> SUPPORTED_MODES = new HashSet<>(
			Arrays.asList(new String[] { MODE_SPOT, MODE_CLASSIFY, MODE_LINK }));
	@Value("${datasets.wandkey:default}")
	String wandKey;
	@Autowired
	RestHelper restHelper;
	@Autowired
	RDFConversionService rdfConversionService;
	@Autowired
	FremeNer fremeNer;
	@Autowired
	OwnedResourceDAO<DatasetMetadata> entityDAO;
	@Autowired
	Config fremeNerConfig;
	Logger logger = Logger.getLogger(FremeNerEnrichment.class);
	@Value("${freme.ner.languages}")
	String languages = "";
	Set<String> SUPPORTED_LANGUAGES;

	@PostConstruct
	public void init() {
		SUPPORTED_LANGUAGES = new HashSet<>();
		for (String lang : languages.split(",")) {
			SUPPORTED_LANGUAGES.add(lang);
		}
	}

	// Submitting document for processing.
	@RequestMapping(value = "/e-entity/freme-ner/documents", method = {
			RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> execute(

			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestParam(value = "language") String language,
			@RequestParam(value = "dataset", required = false) String dataset,
			@RequestParam(value = "numLinks", required = false) String numLinksParam,
			@RequestParam(value = "enrichement", required = false) String enrichementType,
			@RequestParam(value = "mode", required = false) String mode,
			@RequestParam(value = "domain", defaultValue = "") String domain,
			@RequestParam(value = "types", defaultValue = "") String types,
			@RequestParam(value = "datasetKey", required = false) String datasetKey,
			@RequestParam Map<String, String> allParams,
			@RequestParam(value = "nif-version", required = false) String nifVersion,
			@RequestBody(required = false) String postBody) {

		String linkingMethod = allParams.getOrDefault("linkingMethod", "");

		if (nifVersion != null
				&& !(nifVersion.equals(RDFConstants.nifVersion2_0)
				|| nifVersion.equals(RDFConstants.nifVersion2_1))) {
			throw new NIFVersionNotSupportedException("NIF version \""
					+ nifVersion + "\" is not supported");
		}

		// Check the language parameter.
		if (!SUPPORTED_LANGUAGES.contains(language)) {
			// The language specified with the langauge parameter is not
			// supported.
			throw new BadRequestException("Unsupported language.");
		}

		// check if the sparqlEndpoint is configured
		if (!this.fremeNerConfig.isSparqlEndointEnabled()) {
			throw new BadRequestException(
					"The configuration of Freme NER is insufficient for this API Call. A SparqlEndpoint needs to be set in order"
							+ " to make this call.");
		}

		ArrayList<String> rMode = new ArrayList<>();

		// Check the MODE parameter.
		if (mode != null) {
			String[] modes = mode.split(",");
			for (String m : modes) {
				m = m.trim();
				if (SUPPORTED_MODES.contains(m) || m.equals("all")) {
					// OK, the mode is supported.
					rMode.add(m);
				} else {
					// The mode specified is not supported.
					throw new BadRequestException("Unsupported mode: " + m);
				}
			}
		}
		if (rMode.isEmpty() || rMode.contains("all")) {
			rMode.clear();
			rMode.add(MODE_SPOT);
			rMode.add(MODE_CLASSIFY);
			rMode.add(MODE_LINK);
		}
		if (rMode.contains(MODE_CLASSIFY) && !rMode.contains(MODE_SPOT)) {
			throw new BadRequestException(
					"Unsupported mode combination: classification must be performed in combination with spotting.");
		}

		if (rMode.contains(MODE_LINK)) {

			if (!fremeNerConfig.isSolrURIEnabled()) {
				throw new BadRequestException(
						"FREME NER is not configured for mode=link. Please add the configuration option \"freme.ner.solrURI.\"");
			}

			if (!fremeNerConfig.isDomainsFileEnabled() && !domain.isEmpty()) {
				throw new BadRequestException(
						"FREME NER is not configured for using the domain parameter. "
								+ "Please add the configuration option \"freme.ner.domainsFile.\"");
			}

			if (Strings.isNullOrEmpty(dataset)) {
				throw new BadRequestException(
						"No dataset name provided. Please set the parameter 'dataset' to enable any linking functionality, i.e. for mode=link or mode=all (default).");
			}
			// TODO: remove this, after dataset security is fully implemented?
			// check access to wand dataset
			if (dataset.equals("wand")) {
				if (datasetKey != null) {
					if (datasetKey.equals(wandKey)) {
						// The user has access right to the dataset.
					} else {
						throw new AccessDeniedException(
								"You dont have access right for this dataset"
										+ wandKey);
					}
				} else {
					throw new AccessDeniedException(
							"You dont have access right for this dataset");
				}
			} else {
				// check dataset access rights
				String datasets[] = dataset.split(",");
				for (String d : datasets) {
					DatasetMetadata metadata = entityDAO.findOneByIdentifier(d);
				}
			}
		}

		int numLinks = 1;
		// Check the dataset parameter.
		if (numLinksParam != null) {
			numLinks = Integer.parseInt(numLinksParam);
			if (numLinks > 5) {
				numLinks = 1;
			}
		}

		NIFParameterSet nifParameters = this.normalizeNif(postBody,
				acceptHeader, contentTypeHeader, allParams, false);

		Model inputModel = null;
		String plaintext;
		Statement firstPlaintextStm;
		try {
			inputModel = convertInputToRDFModel(nifParameters, nifVersion);
			firstPlaintextStm = rdfConversionService
					.extractFirstPlaintext(inputModel);
			plaintext = firstPlaintextStm.getObject().asLiteral().getString();
		} catch (Exception e) {
			logger.error(e);
			throw new BadRequestException(e.getLocalizedMessage());
		}

		String outputModel = null;
		try {
			if (rMode.contains(MODE_SPOT) && rMode.contains(MODE_CLASSIFY)
					&& rMode.contains(MODE_LINK)) {
				outputModel = fremeNer.spotLinkClassify(plaintext, language,
						dataset, "TTL", nifParameters.getPrefix(), numLinks,
						domain, types, linkingMethod);
			} else if (rMode.contains(MODE_SPOT)
					&& rMode.contains(MODE_CLASSIFY)) {
				outputModel = fremeNer.spotClassify(plaintext, language, "TTL",
						nifParameters.getPrefix());
			} else if (rMode.contains(MODE_SPOT) && rMode.contains(MODE_LINK)) {
				outputModel = fremeNer.spotLink(plaintext, language, dataset,
						"TTL", nifParameters.getPrefix(), numLinks, domain,
						types, linkingMethod);
			} else if (rMode.contains(MODE_SPOT)) {
				outputModel = fremeNer.spot(plaintext, language, "TTL",
						nifParameters.getPrefix());
			} else if (rMode.contains(MODE_LINK)) {
				// // add property (anchorOf) and type (Phrase) for linking of
				// plaintext
				if (nifParameters.getInformat().equals(
						RDFSerialization.PLAINTEXT)) {
					Resource plaintextSubject = firstPlaintextStm.getSubject();
					plaintextSubject
							.addLiteral(
									inputModel
											.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf"),
									plaintext);
					plaintextSubject
							.addProperty(
									RDF.type,
									inputModel
											.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase"));
				}
				String inputStr;
				try {
					inputStr = rdfConversionService.serializeRDF(inputModel,
							RDFSerialization.TURTLE);
				} catch (Exception e) {
					throw new InternalServerErrorException(
							"Can not serialize inputModel to turtle.");
				}
				outputModel = fremeNer.link(inputStr, language, dataset, "TTL",
						nifParameters.getPrefix(), numLinks, domain, types,
						linkingMethod);
			} else {
				throw new InternalServerErrorException(
						"Unknown mode combination: " + String.join(", ", rMode));
			}
		} catch (java.util.NoSuchElementException e) {
			throw new BadRequestException(e.getMessage());
		}

		try {
			Model enrichment = rdfConversionService.unserializeRDF(outputModel,
					RDFSerialization.TURTLE);
			return restHelper.createSuccessResponse(enrichment,
					nifParameters.getOutformat());
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}

	}

	public Model convertInputToRDFModel(NIFParameterSet parameters,
			String nifVersion) {

		// create rdf model
		Model model = ModelFactory.createDefaultModel();

		if (!parameters.getInformat().equals(
				RDFConstants.RDFSerialization.PLAINTEXT)) {
			// input is nif
			try {
				model = this.unserializeNif(parameters.getInput(),
						parameters.getInformat());
				return model;
			} catch (Exception e) {
				logger.error("failed", e);
				throw new BadRequestException("Error parsing NIF input");
			}
		} else {
			// input is plaintext
			rdfConversionService.plaintextToRDF(model, parameters.getInput(),
					null, parameters.getPrefix(), nifVersion);
			return model;
		}
	}
}
