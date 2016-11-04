package org.elinker.core.api.java.utils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Fetch RDF types for a given resource
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class SPARQLProcessor {
    private String endpoint = null;

    public SPARQLProcessor(String endpointURI) {
        this.endpoint = endpointURI;
    }

    private static final String GET_TYPES = "SELECT ?type WHERE { <%s> " +
            "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . }";

    private static final String GET_MOST_SPECIFIC_TYPES =  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "SELECT ?type WHERE { <%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .  " +
            "FILTER NOT EXISTS { <%s> a ?subtype . ?subtype rdfs:subClassOf|owl:equivalentClass ?type } " +
            "FILTER regex(str(?type), \"dbpedia.org/ontology/\") }";

    public Set<String> getTypes(String resource) {

        String sparqlQueryString = String.format(GET_TYPES, resource);

        Set<String> types = getTypesFromSPARQL(sparqlQueryString);

        return types;

    }

    private Set<String> getTypesFromSPARQL(String sparqlQueryString) {

        Query query = QueryFactory.create(sparqlQueryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.endpoint, query);
        Set<String> types = new HashSet<>();

        ResultSet results = qexec.execSelect();
        while(results.hasNext()) {
            QuerySolution qs = results.next();
            Resource type = qs.getResource("?type");
            types.add(type.getURI());
        }

        qexec.close();
        return types;
    }

    public Set<String> getMostSpecificTypeFromDBpediaOntology(String resource) {

        String sparqlQueryString = String.format(GET_MOST_SPECIFIC_TYPES, resource, resource);

        Set<String> types = getTypesFromSPARQL(sparqlQueryString);

        return types;

    }

}
