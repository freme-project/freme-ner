package org.elinker.serialize;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class NIFConverter {

    private String baseURI = "http://www.freme-project.eu/data/#char=";

    public Model createContext(
            String text,
            int beginIndex,
            int endIndex
    ) {
        Model model = ModelFactory.createDefaultModel();

        // Add some prefixes for nicer output.
        model.setNsPrefix("nif", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#");
        model.setNsPrefix("itsrdf", "http://www.w3.org/2005/11/its/rdf#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        String contextURI = baseURI + beginIndex+","+endIndex;

        // Create a resource for the context.
        Resource contextRes = model.createResource(contextURI);

        contextRes.addProperty(
                RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String"));
        contextRes.addProperty(
                RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
        contextRes.addProperty(
                RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#RFC5147String"));
        contextRes.addLiteral(
                model.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString"),
                text);
        contextRes.addLiteral(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex"),
                model.createTypedLiteral(new Integer(beginIndex)));
        contextRes.addLiteral(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex"),
                model.createTypedLiteral(new Integer(endIndex)));

        return model;
    }

    public Model createMention(
            String mention,
            int beginIndex,
            int endIndex,
            String taIdentRef,
            String referenceContext
    ) {

        Model model = ModelFactory.createDefaultModel();

        String mentionURI = baseURI + beginIndex+","+endIndex;

        // Create a resource for the mention.
        Resource stringRes = model.createResource(mentionURI);

        stringRes.addProperty( RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Word"));
        stringRes.addProperty( RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase"));
        stringRes.addProperty( RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String"));
        stringRes.addProperty( RDF.type,
                model.createResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#RFC5147String"));
        stringRes.addLiteral(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf"),
                mention);
        stringRes.addLiteral(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex"),
                model.createTypedLiteral(new Integer(beginIndex)));
        stringRes.addLiteral(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex"),
                model.createTypedLiteral(new Integer(endIndex)));
        // Add the link identifier.
        stringRes.addProperty(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taIdentRef"),
                model.createResource(taIdentRef));
        // Add the link to the context document.
        stringRes.addProperty(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext"),
                model.createResource(referenceContext));

        return model;
    }

    public String getContextURI(Model contextModel) {
        StmtIterator iter = contextModel.listStatements(null, RDF.type, contextModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
        return iter.nextStatement().getSubject().asResource().getURI();
    }
}
