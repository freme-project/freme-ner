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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.freme.common.conversion.SerializationFormatMapper;
import eu.freme.common.exception.AccessDeniedException;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.persistence.dao.OwnedResourceDAO;
import eu.freme.common.persistence.model.DatasetMetadata;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.elinker.core.api.java.Config;
import org.elinker.core.api.java.FremeLabelMatch;
import org.elinker.core.api.java.FremeNer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static eu.freme.common.conversion.rdf.JenaRDFConversionService.JENA_TURTLE;
import static eu.freme.common.conversion.rdf.RDFConstants.ANCHOR_OF;
import static eu.freme.common.conversion.rdf.RDFConstants.NIF_PHRASE_TYPE;
import static eu.freme.common.conversion.rdf.RDFConstants.TURTLE;
import static eu.freme.common.conversion.rdf.RDFConstants.nifPrefix;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/e-entity/freme-ner")
public class FremeNerEnrichment extends BaseRestController {

    public static final String MODE_SPOT = "spot";
    public static final String MODE_CLASSIFY = "classify";
    public static final String MODE_LINK = "link";
    public static final Set<String> SUPPORTED_MODES = new HashSet<>(
            Arrays.asList(new String[]{MODE_SPOT, MODE_CLASSIFY, MODE_LINK}));
    @Value("${datasets.wandkey:default}")
    String wandKey;
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
    @Autowired
    private FremeLabelMatch fremeLabelMatch;

    @PostConstruct
    public void init() {
        SUPPORTED_LANGUAGES = new HashSet<>();
        for (String lang : languages.split(",")) {
            SUPPORTED_LANGUAGES.add(lang);
        }
    }

    // Submitting document for processing.
    @RequestMapping(value = "/documents", method = {
            RequestMethod.POST, RequestMethod.GET})
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
            @RequestBody(required = false) String postBody) {

        String linkingMethod = allParams.getOrDefault("linkingMethod", "");

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
                String datasets[] = dataset.split(",");
                for (String d : datasets) {
                    DatasetMetadata metadata = entityDAO.findOneByIdentifier(d);
                }
            }
        }

        int numLinks = 1;
        if (numLinksParam != null) {
            numLinks = Integer.parseInt(numLinksParam);
            if (numLinks > 5) {
                numLinks = 1;
            }
        }

        NIFParameterSet nifParameters = normalizeNif(postBody,
                acceptHeader, contentTypeHeader, allParams, false);

        Model inputModel;
        String plaintext;
        Statement firstPlaintextStm;
        try {
            inputModel = getRestHelper().convertInputToRDFModel(nifParameters);
            firstPlaintextStm = getRdfConversionService()
                    .extractFirstPlaintext(inputModel);
            plaintext = firstPlaintextStm.getObject().asLiteral().getString();
        } catch (Exception e) {
            logger.error(e);
            throw new BadRequestException(e.getLocalizedMessage());
        }

        String outputModel;
        try {
            if (rMode.contains(MODE_SPOT) && rMode.contains(MODE_CLASSIFY)
                    && rMode.contains(MODE_LINK)) {
                outputModel = fremeNer.spotLinkClassify(plaintext, language,
                        dataset, JENA_TURTLE, nifParameters.getPrefix(), numLinks,
                        domain, types, linkingMethod, nifParameters.getNifVersion());
            } else if (rMode.contains(MODE_SPOT)
                    && rMode.contains(MODE_CLASSIFY)) {
                outputModel = fremeNer.spotClassify(plaintext, language, JENA_TURTLE,
                        nifParameters.getPrefix(), nifParameters.getNifVersion());
            } else if (rMode.contains(MODE_SPOT) && rMode.contains(MODE_LINK)) {
                outputModel = fremeNer.spotLink(plaintext, language, dataset,
                        JENA_TURTLE, nifParameters.getPrefix(), numLinks, domain,
                        types, linkingMethod, nifParameters.getNifVersion());
            } else if (rMode.contains(MODE_SPOT)) {
                outputModel = fremeNer.spot(plaintext, language, JENA_TURTLE,
                        nifParameters.getPrefix(), nifParameters.getNifVersion());
            } else if (rMode.contains(MODE_LINK)) {
                if (nifParameters.getInformatString().equals(
                        SerializationFormatMapper.PLAINTEXT)) {
                    Resource plaintextSubject = firstPlaintextStm.getSubject();
                    plaintextSubject
                            .addLiteral(
                                    inputModel
                                            .createProperty(nifPrefix + ANCHOR_OF),
                                    plaintext);
                    plaintextSubject
                            .addProperty(
                                    RDF.type,
                                    inputModel
                                            .createResource(nifPrefix + NIF_PHRASE_TYPE));
                }
                String inputStr;
                try {
                    inputStr = serializeRDF(inputModel, TURTLE);
                } catch (Exception e) {
                    throw new InternalServerErrorException(
                            "Can not serialize inputModel to turtle.");
                }
                outputModel = fremeNer.link(inputStr, language, dataset, JENA_TURTLE,
                        nifParameters.getPrefix(), numLinks, domain, types,
                        linkingMethod, nifParameters.getNifVersion());
            } else {
                throw new InternalServerErrorException(
                        "Unknown mode combination: " + String.join(", ", rMode));
            }
        } catch (java.util.NoSuchElementException e) {
            throw new BadRequestException(e.getMessage());
        }

        try {
            Model enrichment = unserializeRDF(outputModel, TURTLE);
            enrichment.add(inputModel);
            return createSuccessResponse(enrichment,
                    nifParameters.getOutformatString());
        } catch (Exception e) {
            logger.error(e);
            throw new InternalServerErrorException();
        }

    }

    @RequestMapping(value = "/labelmatch", method = {RequestMethod.POST, RequestMethod.GET})
    public String annotate(@RequestBody @Valid FremeRequest fremeRequest) {

        init(fremeRequest);

        return fremeLabelMatch.annotate(fremeRequest);
    }


    private void init(FremeRequest fremeRequest) {

        NIFParameterSet nifParameters = normalizeNif(fremeRequest.getPostBody(),
                fremeRequest.getAcceptHeader(), fremeRequest.getContentTypeHeader(),
                fremeRequest.getAllParams(), Boolean.FALSE);

        fremeRequest.setNifVersion(nifParameters.getNifVersion());
        fremeRequest.setPrefix(nifParameters.getPrefix());

        try {

            Model inputModel = getRestHelper().convertInputToRDFModel(nifParameters);

            Statement firstPlaintextStm = getRdfConversionService()
                    .extractFirstPlaintext(inputModel);

            fremeRequest.setPlainText(firstPlaintextStm.getObject().asLiteral().getString());

        } catch (Exception e) {
            logger.error(e);
            throw new BadRequestException(e.getLocalizedMessage());
        }

    }

}
