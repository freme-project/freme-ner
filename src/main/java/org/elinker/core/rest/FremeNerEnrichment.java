/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elinker.core.rest;

import com.hp.hpl.jena.rdf.model.*;

import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.AccessDeniedException;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.persistence.dao.OwnedResourceDAO;
import eu.freme.common.persistence.model.DatasetMetadata;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;

import org.apache.log4j.Logger;
import org.elinker.core.api.java.FremeNer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import javax.annotation.PostConstruct;

@RestController
public class FremeNerEnrichment extends BaseRestController {

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

	Logger logger = Logger.getLogger(FremeNerEnrichment.class);

	@Value("${freme.ner.languages}")
	String languages = "";

	Set<String> SUPPORTED_LANGUAGES;

	public static final String MODE_SPOT = "spot";
	public static final String MODE_CLASSIFY = "classify";
	public static final String MODE_LINK = "link";

	public static final Set<String> SUPPORTED_MODES = new HashSet<>(Arrays.asList(new String[]{
			MODE_SPOT, MODE_CLASSIFY, MODE_LINK
	}));

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
			@RequestParam(value = "language", required = true) String language,
			@RequestParam(value = "dataset", required = false) String dataset,
			@RequestParam(value = "numLinks", required = false) String numLinksParam,
			@RequestParam(value = "enrichement", required = false) String enrichementType,
			@RequestParam(value = "mode", required = false) String mode,
			@RequestParam(value = "domain", required = false) String domain,
			@RequestParam(value = "types", required = false) String types,
			@RequestParam(value = "datasetKey", required = false) String datasetKey,
			@RequestParam Map<String, String> allParams,
			@RequestBody(required = false) String postBody) {

		// Check the language parameter.
		if (!SUPPORTED_LANGUAGES.contains(language)) {
			// The language specified with the langauge parameter is not
			// supported.
			throw new BadRequestException("Unsupported language.");
		}

		ArrayList<String> rMode = new ArrayList<>();

		// Check the MODE parameter.
		if (mode != null) {
			String[] modes = mode.split(",");
			for (String m : modes) {
				m = m.trim();
				if (SUPPORTED_MODES.contains (m) || m.equals("all")){
					// OK, the mode is supported.
					rMode.add(m);
				} else {
					// The mode specified is not supported.
					throw new BadRequestException("Unsupported mode: " + m);
				}
			}
		}
		if(rMode.isEmpty() || rMode.contains("all")){
			rMode.clear();
			rMode.add(MODE_SPOT);
			rMode.add(MODE_CLASSIFY);
			rMode.add(MODE_LINK);
		}
		if (rMode.contains(MODE_CLASSIFY) && !rMode.contains(MODE_SPOT)) {
			throw new BadRequestException(
					"Unsupported mode combination: classification must be performed in combination with spotting.");
		}

		if(rMode.contains(MODE_LINK)) {
			// TODO: remove this, after dataset security is fully implemented
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
				DatasetMetadata metadata = entityDAO.findOneByIdentifier(dataset);
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

		Model model = null;
		String plaintext;
		try {
			model = restHelper.convertInputToRDFModel(nifParameters);
			plaintext = rdfConversionService.extractFirstPlaintext(model)
					.getObject().asLiteral().toString();
		} catch (Exception e) {
			logger.error(e);
			throw new BadRequestException("Cannot parse NIF input");
		}

		String rdf = null;
		if(rMode.contains(MODE_SPOT) && rMode.contains(MODE_CLASSIFY) && rMode.contains(MODE_LINK)){
			rdf = fremeNer.spotLinkClassify(plaintext, language, dataset,
					"TTL", nifParameters.getPrefix(), numLinks, domain, types);
		}else if(rMode.contains(MODE_SPOT) && rMode.contains(MODE_CLASSIFY)){
			rdf = fremeNer.spotClassify(plaintext, language, "TTL",
					nifParameters.getPrefix());
		}else if(rMode.contains(MODE_SPOT) && rMode.contains(MODE_LINK)){
			rdf = fremeNer.spotLink(plaintext, language, datasetKey, "TTL",
					nifParameters.getPrefix(), numLinks, domain, types);
		}else if(rMode.contains(MODE_SPOT)){
			rdf = fremeNer.spot(plaintext, language, "TTL",
					nifParameters.getPrefix());
		}else if(rMode.contains(MODE_LINK)){
			rdf = fremeNer.link(plaintext, language, dataset,
					"TTL", nifParameters.getPrefix(), numLinks, domain, types);
		}else {
			throw new InternalServerErrorException("Unknown mode combination");
		}

		try {
			Model enrichment = rdfConversionService.unserializeRDF(rdf,
					RDFSerialization.TURTLE);
			return restHelper.createSuccessResponse(enrichment,
					nifParameters.getOutformat());
		} catch (Exception e) {
			logger.error(e);
			throw new InternalServerErrorException();
		}

	}

}
