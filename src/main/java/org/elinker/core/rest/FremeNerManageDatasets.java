package org.elinker.core.rest;

import java.util.*;

import javax.annotation.PostConstruct;

import eu.freme.common.persistence.model.DatasetMetadata;
import org.apache.log4j.Logger;
import org.elinker.core.api.java.FremeNer;
import org.elinker.core.api.scala.FremeNer.TextInput;
import org.elinker.core.api.scala.FremeNer.InputType;
import org.elinker.core.api.scala.FremeNer.SparqlInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.exception.NotFoundException;
import eu.freme.common.persistence.dao.DatasetMetadataDAO;
import eu.freme.common.persistence.repository.DatasetMetadataRepository;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;

@RestController
public class FremeNerManageDatasets extends BaseRestController {

	@Value("${freme.ner.languages}")
	String languages = "";

	Set<String> SUPPORTED_LANGUAGES;

	Logger logger = Logger.getLogger(FremeNerManageDatasets.class);

	@Autowired
	DatasetMetadataRepository datasetRepo;

	@Autowired
	DatasetMetadataDAO datasetMetadataDao;

	@PostConstruct
	public void init() {
		SUPPORTED_LANGUAGES = new HashSet<>();
		for (String lang : languages.split(",")) {
			SUPPORTED_LANGUAGES.add(lang);
		}
	}

	@Autowired
	FremeNer fremeNer;

	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = { RequestMethod.POST })
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> createDataset(
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "description", required = true) String description,
			@RequestParam(value = "language", required = false) String language,
			// @RequestParam(value = "informat", required = false) String
			// informat,
			// @RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "endpoint", required = false) String endpoint,
			@RequestParam(value = "sparql", required = false) String sparql,
			@RequestParam Map<String, String> allParams,
			@RequestBody(required = false) String postBody) {

		try {
			if (language != null) {
				if (!SUPPORTED_LANGUAGES.contains(language)) {
					// The language specified with the langauge parameter is not
					// supported.
					throw new BadRequestException("Unsupported language.");
				}
			}

			// first check if user wants to submit data via SPARQL
			if (endpoint != null && sparql == null) {
				// endpoint specified, but not sparql => throw exception
				throw new BadRequestException(
						"SPARQL endpoint was specified but the SPARQL query is empty.");
			}

			if (datasetRepo.findOneByName(name) != null) {
				throw new BadRequestException("A datasetMetadata with name \"" + name
						+ "\" already exists.");
			}

			NIFParameterSet nifParameters = this.normalizeNif(postBody,
					acceptHeader, contentTypeHeader, allParams, false);

			String format = null;
			switch (nifParameters.getInformat()) {
			case TURTLE:
				format = "TTL";
				break;
			case JSON_LD:
				format = "JSON-LD";
				break;
			case RDF_XML:
				format = "RDF/XML";
				break;
			case N_TRIPLES:
				format = "N-TRIPLES";
				break;
			case N3:
				format = "N3";
				break;
			default:
				throw new BadRequestException("Bad input format "
						+ nifParameters.getInformat());
			}

			InputType inputType;

			if (endpoint != null) {
				// input from SPARQL
				inputType = new SparqlInput(sparql, endpoint);
			} else {
				// text input
				inputType = new TextInput(nifParameters.getInput());
			}

			fremeNer.addDataset(name, inputType, description, format, language,
					new String[] {});
			return new ResponseEntity<String>(HttpStatus.OK);

		} catch (Exception e) {

			if (e instanceof org.elinker.core.api.process.Datasets.DatasetAlreadyExistsException) {
				throw new BadRequestException("DatasetMetadata already exists");
			}

			logger.error(e.getMessage(), e);
			throw new InternalServerErrorException();
		}
	}

	// Updating datasetMetadata for use in the e-Entity service.
	// curl -v
	// "http://localhost:8080/e-entity/freme-ner/datasets/test?language=en" -X
	// PUT
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = { RequestMethod.PUT })
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> updateDataset(
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
			@PathVariable(value = "name") String name,
			@RequestParam(value = "language") String language,
			@RequestParam Map<String, String> allParams,
			@RequestBody(required = false) String postBody) {

		try {

			if (!SUPPORTED_LANGUAGES.contains(language)) {
				// The language specified with the langauge parameter is not
				// supported.
				throw new BadRequestException("Unsupported language.");
			}

			NIFParameterSet nifParameters = this.normalizeNif(postBody, null,
					contentTypeHeader, allParams, false);

			DatasetMetadata ds = datasetRepo.findOneByName(name);
			if (ds == null) {
				throw new NotFoundException(
						"Could not find a datasetMetadata with name \"" + name + "\"");
			}

			String format = null;
			switch (nifParameters.getInformat()) {
			case TURTLE:
				format = "TTL";
				break;
			case JSON_LD:
				format = "JSON-LD";
				break;
			case RDF_XML:
				format = "RDF/XML";
				break;
			case N_TRIPLES:
				format = "N-TRIPLES";
				break;
			case N3:
				format = "N3";
				break;
			}

			fremeNer.updateDataset(name, new TextInput(postBody), ds.getDescription(), format,
					language, new String[] {});
			return new ResponseEntity<String>(HttpStatus.OK);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			if (e instanceof org.elinker.core.api.process.Datasets.DatasetDoesNotExistException) {
				throw new BadRequestException("DatasetMetadata does not exist");
			} else {
				throw new InternalServerErrorException();
			}
		}
	}

	// Removing a specific datasetMetadata.
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = { RequestMethod.DELETE })
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public ResponseEntity<String> removeDataset(
			@PathVariable(value = "name") String name) {

		try {
			fremeNer.deleteDataset(name);
			return new ResponseEntity<String>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e);
			if (e instanceof org.elinker.core.api.process.Datasets.DatasetDoesNotExistException) {
				throw new BadRequestException("DatasetMetadata does not exist");
			} else {
				throw new InternalServerErrorException();
			}
		}
	}

	// Get info about a specific datasetMetadata.
	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = { RequestMethod.GET })
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public DatasetMetadata getDataset(@PathVariable(value = "name") String name) {

		DatasetMetadata ds = datasetRepo.findOneByName(name);
		if (ds == null) {
			throw new NotFoundException("datasetMetadata \"" + name + "\" not found.");
		} else {
			return ds;
		}
	}

	// Get info about all datasets.
	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = { RequestMethod.GET })
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	public List<DatasetMetadata> getDatasets() {
		List<DatasetMetadata> result = new ArrayList<>();
		for (DatasetMetadata datasetMetadata : datasetRepo.findAll()){
			result.add(datasetMetadata);
		}
		return result;
		//return datasetRepo.findAll();
	}
}
