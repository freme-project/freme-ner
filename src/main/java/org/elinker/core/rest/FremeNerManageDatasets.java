//package org.elinker.core.rest;
//
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class FremeNerManageDatasets {
//@RequestMapping(value = "/e-entity/freme-ner/datasets", method = {
//      RequestMethod.POST })
//public ResponseEntity<String> createDataset(
//      @RequestHeader(value = "Accept", required = false) String acceptHeader,
//      @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
//		@RequestParam(value = "name", required = true) String name,
//		@RequestParam(value = "description", required = true) String description,
//		@RequestParam(value = "language", required = false) String language,
//		//@RequestParam(value = "informat", required = false) String informat,
//		//@RequestParam(value = "f", required = false) String f,
//		@RequestParam(value = "endpoint", required = false) String endpoint,
//		@RequestParam(value = "sparql", required = false) String sparql,
//      @RequestParam Map<String,String> allParams,
//      @RequestBody(required = false) String postBody) {
//
//  try {
//      if(language != null) {
//          if (!SUPPORTED_LANGUAGES.contains(language)) {
//              // The language specified with the langauge parameter is not supported.
//              throw new eu.freme.broker.exception.BadRequestException("Unsupported language.");
//          }
//      }
//
//      // first check if user wants to submit data via SPARQL
//      if (endpoint != null && sparql == null) {
//          // endpoint specified, but not sparql => throw exception
//          throw new eu.freme.broker.exception.BadRequestException("SPARQL endpoint was specified but not a SPARQL query.");
//      }
//
//      NIFParameterSet nifParameters = this.normalizeNif(postBody, acceptHeader, contentTypeHeader, allParams, true);
//
//      String format = null;
//      switch (nifParameters.getInformat()) {
//          case TURTLE:
//              format = "TTL";
//              break;
//          case JSON_LD:
//              format = "JSON-LD";
//              break;
//          case RDF_XML:
//              format = "RDF/XML";
//              break;
//          case N_TRIPLES:
//              format = "N-TRIPLES";
//              break;
//          case N3:
//              format = "N3";
//              break;
//      }
//
//      if (endpoint != null) {
//          // fed via SPARQL endpoint
//          return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                  + "&name=" + name
//                  + "&description=" + URLEncoder.encode(description, "UTF-8")
//                  + "&language=" + language
//                  + "&endpoint=" + endpoint
//                  + "&sparql=" + URLEncoder.encode(sparql, "UTF-8"), HttpMethod.POST, null);
//      } else {
//          // datasets is sent
//          if(language != null) {
//              return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                      + "&name=" + name
//                      + "&description=" + URLEncoder.encode(description, "UTF-8")
//                      + "&language=" + language, HttpMethod.POST, nifParameters.getInput());
//          } else {
//              return callBackend(fremeNerEndpoint+"/datasets?format=" + format
//                      + "&name=" + name
//                      + "&description=" + URLEncoder.encode(description, "UTF-8") , HttpMethod.POST, nifParameters.getInput());
//          }
//      }
//  } catch(Exception e){
//      logger.error(e.getMessage(), e);
//      throw new eu.freme.broker.exception.BadRequestException(e.getMessage());
//  }
//}
//}
