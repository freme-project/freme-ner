//
///**
// * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
// * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
// * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.elinker.core.rest;
//
//import com.hp.hpl.jena.rdf.model.*;
//import com.hp.hpl.jena.vocabulary.RDF;
//
//import eu.freme.common.conversion.rdf.RDFConstants;
//import eu.freme.common.rest.BaseRestController;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.ByteArrayInputStream;
//import java.net.URI;
//import java.net.URLEncoder;
//import java.util.*;
//
//import org.springframework.web.client.HttpClientErrorException;
//
//@RestController
//public class CopyOfFremeNerEnrichment extends BaseRestController {
//    
//
//    @Value("${datasets.wandkey:default}")
//    String wandKey;
//    
//    @Autowired
//    EEntityService entityAPI;
//        
//    @Autowired
//    TemplateDAO templateDAO;
//        
//    @Autowired
//    DataEnricher dataEnricher;
//    
//    @Value("${freme.ner.api-endpoint:http://rv2622.1blu.de:8081/api}")
//    String fremeNerEndpoint;
//
//    public final Set<String> SUPPORTED_LANGUAGES = new HashSet<>(Arrays.asList(new String[]{
//            "en", "de", "nl", "it", "fr", "es", "ru"
//    }));
//
//
//        // Submitting document for processing.
//	@RequestMapping(value = "/e-entity/freme-ner/documents", method = {
//            RequestMethod.POST, RequestMethod.GET })
//	public ResponseEntity<String> execute(
//			/*@RequestParam(value = "input", required = false) String input,
//			@RequestParam(value = "i", required = false) String i,
//			@RequestParam(value = "informat", required = false) String informat,
//			@RequestParam(value = "f", required = false) String f,
//			@RequestParam(value = "outformat", required = false) String outformat,
//			@RequestParam(value = "o", required = false) String o,
//			@RequestParam(value = "prefix", required = false) String prefix,
//			@RequestParam(value = "p", required = false) String p,
//			*/
//            @RequestHeader(value = "Accept", required = false) String acceptHeader,
//			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
//			@RequestParam(value = "language", required = true) String language,
//			@RequestParam(value = "dataset", required = true) String dataset,
//			@RequestParam(value = "numLinks", required = false) String numLinksParam,
//			@RequestParam(value = "enrichement", required = false) String enrichementType,
//			@RequestParam(value = "mode", required = false) String mode,
//			@RequestParam(value = "domain", required = false) String domain,
//			@RequestParam(value = "types", required = false) String types,
//			@RequestParam(value = "datasetKey", required = false) String datasetKey,
//            @RequestParam Map<String,String> allParams,
//            @RequestBody(required = false) String postBody) {
//        try {
//            
////            System.out.println(domain);
////            System.out.println(types);
//            
//            // Check the language parameter.
//           if(!SUPPORTED_LANGUAGES.contains(language)){
//                    // The language specified with the langauge parameter is not supported.
//                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
//            }
//
//            if(dataset.equals("wand")) {
//                if(datasetKey != null) {
//                    if(datasetKey.equals(wandKey)) {
//                        // The user has access right to the dataset.
//                    } else {
//                        throw new eu.freme.broker.exception.AccessDeniedException("You dont have access right for this dataset" + wandKey);
//                    }
//                } else {
//                    throw new eu.freme.broker.exception.AccessDeniedException("You dont have access right for this dataset");
//                }
//            }
//
//
//           
//           
//            ArrayList<String> rMode = new ArrayList<>();
//            
//            // Check the MODE parameter.
//            if(mode != null) {
//                String[] modes = mode.split(",");
//                for(String m : modes) {
//                    if(m.equals("spot") 
//                            || m.equals("classify") 
//                            || m.equals("link")
//                            || m.equals("all")) {
//                        // OK, the mode is supported.
//                        rMode.add(m);
//                    } else {
//                        // The mode specified is not supported.
//                        throw new eu.freme.broker.exception.BadRequestException("Unsupported mode: " + m);
//                    }
//                }
//                
//                if(rMode.contains("classify") && !rMode.contains("spot")) {
//                    throw new eu.freme.broker.exception.BadRequestException("Unsupported mode combination: classification must be performed in combination with spotting.");
//                }
//                
//                if(rMode.contains("all")) {
//                    rMode.clear();
//                    rMode.add("all");
//                }
//                
//            } else {
//                // OK, perform all.
//                rMode.add("all");
//            }
//            
//            int numLinks = 1;
//            // Check the dataset parameter.
//            if(numLinksParam != null) {
//                numLinks = Integer.parseInt(numLinksParam);
//                if(numLinks > 5) {
//                    numLinks = 1;
//                }
//            }
//            
//            //NIFParameterSet parameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
//            NIFParameterSet nifParameters = this.normalizeNif(postBody,acceptHeader,contentTypeHeader,allParams,false);
//
//            Model inModel = ModelFactory.createDefaultModel();
//            Model outModel = ModelFactory.createDefaultModel();
//            outModel.setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-de", "http://de.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-nl", "http://nl.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-es", "http://es.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-it", "http://it.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-fr", "http://fr.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbpedia-ru", "http://ru.dbpedia.org/resource/");
//            outModel.setNsPrefix("dbc", "http://dbpedia.org/resource/Category:");
//            outModel.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
//            outModel.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
//            outModel.setNsPrefix("freme-onto", "http://freme-project.eu/ns#");
//
//            String docForProcessing = null;
//            
//            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
//                // input is sent as value of the input parameter
//                docForProcessing = nifParameters.getInput();
//                
////                if(rMode.size() == 1 && rMode.contains("link")) {
////                    throw new eu.freme.broker.exception.BadRequestException("Unsupported mode combination: you must provide NIF in order to perform only linking.");
////                }
//                
//            } else {
//                // input is sent as body of the request
//
//                if(rMode.size() == 1 && rMode.contains("link")) {
//                    docForProcessing = postBody;
//                } else {
//                    inModel = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
//
//                    StmtIterator iter = inModel.listStatements(null, RDF.type, inModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
//
//                    boolean textFound = false;
//                    String tmpPrefix = "http://freme-project.eu/#";
//                    // The first nif:Context with assigned nif:isString will be processed.
//                    while(!textFound) {
//                        Resource contextRes = iter.nextStatement().getSubject();
//                        tmpPrefix = contextRes.getURI().split("#")[0];
//                        nifParameters.setPrefix(tmpPrefix);
//                        Statement isStringStm = contextRes.getProperty(inModel.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"));
//                        if(isStringStm != null) {
//                            docForProcessing = isStringStm.getObject().asLiteral().getString();
//                            textFound = true;
//                        }                    
//                    }
//                }
//                
//                if(docForProcessing == null) {
//                    throw new eu.freme.broker.exception.BadRequestException("No content to process.");
//                }
//            }
//            
//
//                String fremeNERRes = entityAPI.callFremeNER(docForProcessing, language, nifParameters.getPrefix(), dataset, numLinks, rMode, nifParameters.getInformat().contentType(), domain, types);
//                outModel.read(new ByteArrayInputStream(fremeNERRes.getBytes()), null, "TTL");
//                outModel.add(inModel);
//                HashMap<String, String> templateParams = new HashMap<>();
//                if(enrichementType != null) {
//                    if(enrichementType.equals("dbpedia-categories")) {
//                        Template template = templateDAO.findOneById(300);
//                        outModel = dataEnricher.enrichWithTemplate(outModel, template, templateParams);
//                    }
//                }
//
//            return createSuccessResponse(outModel,  nifParameters.getOutformat());
//            } catch (BadRequestException e) {
//                logger.error(e.getMessage(), e);
//                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
//            } catch (eu.freme.eservices.eentity.exceptions.ExternalServiceFailedException e) {
//                logger.error(e.getMessage(), e);
//                throw new ExternalServiceFailedException();
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
//            }
//        }
//
//        // Submitting dataset for use in the e-Entity service.
//        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/?name=test&language=en" -X POST
//	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
//            RequestMethod.POST })
//	public ResponseEntity<String> createDataset(
//            @RequestHeader(value = "Accept", required = false) String acceptHeader,
//            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
//			@RequestParam(value = "name", required = true) String name,
//			@RequestParam(value = "description", required = true) String description,
//			@RequestParam(value = "language", required = false) String language,
//			//@RequestParam(value = "informat", required = false) String informat,
//			//@RequestParam(value = "f", required = false) String f,
//			@RequestParam(value = "endpoint", required = false) String endpoint,
//			@RequestParam(value = "sparql", required = false) String sparql,
//            @RequestParam Map<String,String> allParams,
//            @RequestBody(required = false) String postBody) {
//
//        try {
//            if(language != null) {
//                if (!SUPPORTED_LANGUAGES.contains(language)) {
//                    // The language specified with the langauge parameter is not supported.
//                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
//                }
//            }
//
//            // first check if user wants to submit data via SPARQL
//            if (endpoint != null && sparql == null) {
//                // endpoint specified, but not sparql => throw exception
//                throw new eu.freme.broker.exception.BadRequestException("SPARQL endpoint was specified but not a SPARQL query.");
//            }
//
//            NIFParameterSet nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, true);
//
//            String format = null;
//            switch (nifParameters.getInformat()) {
//                case TURTLE:
//                    format = "TTL";
//                    break;
//                case JSON_LD:
//                    format = "JSON-LD";
//                    break;
//                case RDF_XML:
//                    format = "RDF/XML";
//                    break;
//                case N_TRIPLES:
//                    format = "N-TRIPLES";
//                    break;
//                case N3:
//                    format = "N3";
//                    break;
//            }
//
//            if (endpoint != null) {
//                // fed via SPARQL endpoint
//                return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                        + "&name=" + name
//                        + "&description=" + URLEncoder.encode(description, "UTF-8")
//                        + "&language=" + language
//                        + "&endpoint=" + endpoint
//                        + "&sparql=" + URLEncoder.encode(sparql, "UTF-8"), HttpMethod.POST, null);
//            } else {
//                // datasets is sent
//                if(language != null) {
//                    return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                            + "&name=" + name
//                            + "&description=" + URLEncoder.encode(description, "UTF-8")
//                            + "&language=" + language, HttpMethod.POST, nifParameters.getInput());
//                } else {
//                    return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                            + "&name=" + name
//                            + "&description=" + URLEncoder.encode(description, "UTF-8") , HttpMethod.POST, nifParameters.getInput());
//                }
//            }
//        } catch(Exception e){
//            logger.error(e.getMessage(), e);
//            throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
//        }
//    }
//        
//        // Updating dataset for use in the e-Entity service.
//        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test?language=en" -X PUT
//	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
//            RequestMethod.PUT })
//	public ResponseEntity<String> updateDataset(
//			@RequestHeader(value = "Content-Type", required=false) String contentTypeHeader,
//			@PathVariable(value = "name") String name,
//			@RequestParam(value = "language") String language,
//			//@RequestParam(value = "informat", required = false) String informat,
//			//@RequestParam(value = "f", required = false) String f,
//            @RequestParam Map<String,String> allParams,
//            @RequestBody(required = false) String postBody) {
//
//            try {
//
//                if (!SUPPORTED_LANGUAGES.contains(language)) {
//                    // The language specified with the langauge parameter is not supported.
//                    throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
//                }
//
//                NIFParameterSet nifParameters = this.normalizeNif(postBody, null, contentTypeHeader, allParams, false);
//
//                String format = null;
//                switch (nifParameters.getInformat()) {
//                    case TURTLE:
//                        format = "TTL";
//                        break;
//                    case JSON_LD:
//                        format = "JSON-LD";
//                        break;
//                    case RDF_XML:
//                        format = "RDF/XML";
//                        break;
//                    case N_TRIPLES:
//                        format = "N-TRIPLES";
//                        break;
//                    case N3:
//                        format = "N3";
//                        break;
//                }
//
//                return callBackend(fremeNerEndpoint+"/datasets/" + name + "?format=" + format
//                    + "&language=" + language, HttpMethod.PUT, nifParameters.getInput());
//            }catch(Exception e){
//                logger.error(e.getMessage(), e);
//                throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
//            }
//        }
//        
//        // Get info about a specific dataset.
//        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test
//	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
//            RequestMethod.GET })
//	public ResponseEntity<String> getDataset(
//            @PathVariable(value = "name") String name) {
//            return callBackend(fremeNerEndpoint+"/datasets/"+name, HttpMethod.GET, null);
//        }
//
//        // Get info about all available datasets.
//        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets
//	@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
//            RequestMethod.GET })
//	public ResponseEntity<String> getAllDatasets() {
//            return callBackend(fremeNerEndpoint+"/datasets", HttpMethod.GET, null);
//        }
//        
//        // Removing a specific dataset.
//        // curl -v "http://localhost:8080/e-entity/freme-ner/datasets/test" -X DELETE
//	@RequestMapping(value = "/e-entity/freme-ner/datasets/{name}", method = {
//            RequestMethod.DELETE })
//	public ResponseEntity<String> removeDataset(
//			@PathVariable(value = "name") String name) {
//            return callBackend(fremeNerEndpoint+"/datasets/"+name, HttpMethod.DELETE, null);
//        }
//
//
//    private ResponseEntity<String> callBackend(String uri, HttpMethod method, String body) {
//        
//        RestTemplate restTemplate = new RestTemplate();
//        try {
//            if(body == null) {
//                return restTemplate.exchange(new URI(uri), method, null, String.class);
//            } else {
//                ResponseEntity<String> response = restTemplate.exchange(new URI(uri), method, new HttpEntity<String>(body), String.class);
//                
//                if(response.getStatusCode() == HttpStatus.CONFLICT) {
//                    throw new eu.freme.broker.exception.BadRequestException("Dataset with this name already existis and it cannot be created.");
//                } else {
//                    return response;
//                }
////                return restTemplate.exchange(new URI(uri), method, new HttpEntity<String>(body), String.class);
//            }
//        } catch (HttpClientErrorException rce) {
//            if(rce.getStatusCode() == HttpStatus.CONFLICT) {
//                throw new eu.freme.broker.exception.BadRequestException("Dataset with this name already existis and it cannot be created.");
//            } else {
//                throw new eu.freme.broker.exception.ExternalServiceFailedException(rce.getMessage());
//            }
//        } catch (RestClientException rce) {
//            logger.error("failed", rce);
//            throw new eu.freme.broker.exception.ExternalServiceFailedException(rce.getMessage());
//        } catch (Exception e) {
//            logger.error("failed", e);
//            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}
