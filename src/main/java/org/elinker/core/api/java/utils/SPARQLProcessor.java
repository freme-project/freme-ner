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
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class SPARQLProcessor {
    private static SPARQLProcessor instance = null;

    public static SPARQLProcessor getInstance() {
        if(instance == null){
            instance = new SPARQLProcessor();
        }
        return instance;
    }

    public Set<String> getTypes(String resource) {
        String sparqlQueryString =
                "SELECT ?type WHERE { <" + resource + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . }";

        Query query = QueryFactory.create(sparqlQueryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
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

}
