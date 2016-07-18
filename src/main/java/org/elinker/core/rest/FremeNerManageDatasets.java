package org.elinker.core.rest;

import com.google.common.base.Strings;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.persistence.dao.DatasetMetadataDAO;
import eu.freme.common.persistence.repository.DatasetMetadataRepository;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.OwnedResourceManagingController;
import eu.freme.common.persistence.model.DatasetMetadata;

import org.apache.log4j.Logger;
import org.elinker.core.api.java.Config;
import org.elinker.core.api.scala.FremeNer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 08.04.2016.
 */
@RestController
@RequestMapping("/e-entity/freme-ner/datasets")
public class FremeNerManageDatasets extends OwnedResourceManagingController<DatasetMetadata>{

    @Value("${freme.ner.languages}")
    String languages = "";

    Set<String> SUPPORTED_LANGUAGES;

    Logger logger = Logger.getLogger(FremeNerManageDatasets.class);

    @Autowired
	Config fremeNerConfig;
    
    @PostConstruct
    public void init() {
        SUPPORTED_LANGUAGES = new HashSet<>();
        for (String lang : languages.split(",")) {
            SUPPORTED_LANGUAGES.add(lang);
        }
    }

    @Autowired
    org.elinker.core.api.java.FremeNer fremeNer;

    @Autowired
    JenaRDFConversionService jenaRDFConversionService;

    @Override
    protected DatasetMetadata createEntity(String body, Map<String, String> parameters, Map<String, String> headers) throws BadRequestException {

        String name = parameters.get(DatasetMetadata.getIdentifierName());
        if(Strings.isNullOrEmpty(name))
            throw new BadRequestException("No name provided! Please set the parameter \""+ DatasetMetadata.getIdentifierName() +"\" to a valid value.");

        if(getEntityDAO().findOneByIdentifierUnsecured(name)!=null)
            throw new BadRequestException("A dataset with the name \""+name+"\" exists already! Please provide another name.");

        //check if solr server is configured
    	if( !fremeNerConfig.isSolrURIEnabled()){
			throw new BadRequestException("FREME NER is not configured sufficiently for this call. Please add the configuration option"
					+ " \"freme.ner.solrURI.\"");
		}
        
        DatasetMetadata metadata = new DatasetMetadata(name);

        updateEntity(metadata, body, parameters, headers);

        return metadata;
    }

    @Override
    protected void updateEntity(DatasetMetadata datasetMetadata, String body, Map<String, String> parameters, Map<String, String> headers) throws BadRequestException {

        String language = parameters.get("language");
        if (language != null) {
            if (!SUPPORTED_LANGUAGES.contains(language)) {
                // The language specified with the langauge parameter is not
                // supported.
                throw new BadRequestException("Unsupported language.");
            }
        }

        String endpoint = parameters.get("endpoint");
        String sparql = parameters.get("sparql");
        // first check if user wants to submit data via SPARQL
        if (endpoint != null && sparql == null) {
            // endpoint specified, but not sparql => throw exception
            throw new BadRequestException(
                    "SPARQL endpoint was specified but the SPARQL query is empty.");
        }
        
        //check if solr server is configured
     	if( !fremeNerConfig.isSolrURIEnabled()){
			throw new BadRequestException("FREME NER is not configured sufficiently for this call. Please add the configuration option"
					+ " \"freme.ner.solrURI.\"");
		}

        NIFParameterSet nifParameters = this.normalizeNif(body,
                headers.get("accept"), headers.get("content-type"), parameters, true);

        String format = jenaRDFConversionService.getJenaType(nifParameters.getInformat());
        if(format==null){
            throw new BadRequestException("Bad input format "
                        + nifParameters.getInformat());
        }

        FremeNer.InputType inputType;

        // input from SPARQL
        if (endpoint != null) {
            inputType = new FremeNer.SparqlInput(sparql, endpoint);

        // text input
        } else if(!Strings.isNullOrEmpty(nifParameters.getInput())) {
            inputType = new FremeNer.TextInput(nifParameters.getInput());

        // empty (just update metadata: owner and visibility)
        }else{
            return;
        }

        long totalEntities = fremeNer.addToDataset(datasetMetadata.getName(), inputType, format, language,
                new String[] {});
        datasetMetadata.setTotalEntities(totalEntities);
    }

    @Override
    protected void preDelete(DatasetMetadata entity){
        fremeNer.deleteDataset(entity.getName());
    }
}
