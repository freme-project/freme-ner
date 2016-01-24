package org.elinker.core.api.java.serialize;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class NIFConverter {

    private String baseURI = null;
    private Map<String, String> entityTypes = new HashMap<String, String>();

    public NIFConverter(String prefix) {
        this.baseURI = prefix + "#char=";
        entityTypes.put("PERSON", "http://nerd.eurecom.fr/ontology#Person");
        entityTypes.put("ORGANIZATION", "http://nerd.eurecom.fr/ontology#Organization");
        entityTypes.put("LOCATION", "http://nerd.eurecom.fr/ontology#Location");
        entityTypes.put("MISC", "http://www.w3.org/2002/07/owl#Thing");
        entityTypes.put("I-PER", "http://nerd.eurecom.fr/ontology#Person");
        entityTypes.put("I-ORG", "http://nerd.eurecom.fr/ontology#Organization");
        entityTypes.put("I-LOC", "http://nerd.eurecom.fr/ontology#Location");
        entityTypes.put("I-MISC", "http://www.w3.org/2002/07/owl#Thing");
    }

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
        // Add the link to the context document.
        stringRes.addProperty(
                model.createProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#referenceContext"),
                model.createResource(referenceContext));

        return model;
    }

    public Model createMentionWithType(
            String entityType,
            String mention,
            int beginIndex,
            int endIndex,
            String referenceContext
    ) {
        Model model = createMention(mention, beginIndex, endIndex, referenceContext);
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Resource stringRes = model.getResource(mentionURI);

        // Add the entity type.
        stringRes.addProperty(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taClassRef"),
                model.createResource(entityTypes.get(entityType)));

        return model;
    }

    public Model createMentionWithScore(
            String mention,
            int beginIndex,
            int endIndex,
            double score,
            String referenceContext
    ) {
        Model model = createMention(mention, beginIndex, endIndex, referenceContext);
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Resource stringRes = model.getResource(mentionURI);

        // Add the confidence/relevance score.
        stringRes.addLiteral(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taConfidence"),
                model.createTypedLiteral(new Double(score)));

        return model;
    }

    public Model createMentionWithTypeAndScore(
            String entityType,
            String mention,
            int beginIndex,
            int endIndex,
            double score,
            String referenceContext
    ) {
        Model model = createMentionWithType(entityType, mention, beginIndex, endIndex, referenceContext);
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Resource stringRes = model.getResource(mentionURI);

        // Add the confidence/relevance score.
        stringRes.addLiteral(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taConfidence"),
                model.createTypedLiteral(new Double(score)));

        return model;
    }

    public Model createLink(
            String mention,
            int beginIndex,
            int endIndex,
            String taIdentRef,
            String referenceContext
    ) {
        Model model = createMention(mention, beginIndex, endIndex, referenceContext);
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Resource stringRes = model.getResource(mentionURI);

        // Add the link identifier.
        stringRes.addProperty(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taIdentRef"),
                model.createResource(taIdentRef));

        return model;
    }

    public Model createLinkWithType(
            String entityType,
            String[] otherTypes,
            String mention,
            int beginIndex,
            int endIndex,
            String taIdentRef,
            String referenceContext
    ) {
        Model model = createMentionWithType(entityType, mention, beginIndex, endIndex, referenceContext);
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Resource stringRes = model.getResource(mentionURI);

        // Add the link identifier.
        stringRes.addProperty(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taIdentRef"),
                model.createResource(taIdentRef));

        // Add other types.
        for(String type: otherTypes) {
            stringRes.addProperty(
                    model.createProperty("http://www.w3.org/2005/11/its/rdf#taClassRef"),
                    model.createResource(type));
        }

        return model;
    }

    public Model createLinkWithScore(
            String mention,
            int beginIndex,
            int endIndex,
            String taIdentRef,
            double score,
            String referenceContext
    ) {
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Model model = createLink(mention, beginIndex, endIndex, taIdentRef, referenceContext);
        Resource stringRes = model.getResource(mentionURI);

        // Add the confidence/relevance score.
        stringRes.addLiteral(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taConfidence"),
                model.createTypedLiteral(new Double(score)));

        return model;
    }

    public Model createLinkWithTypeAndScore(
            String entityType,
            String[] otherTypes,
            String mention,
            int beginIndex,
            int endIndex,
            String taIdentRef,
            double score,
            String referenceContext
    ) {
        String mentionURI = baseURI + beginIndex+","+endIndex;
        Model model = createLinkWithType(entityType, otherTypes, mention, beginIndex, endIndex, taIdentRef, referenceContext);
        Resource stringRes = model.getResource(mentionURI);

        // Add the confidence/relevance score.
        stringRes.addLiteral(
                model.createProperty("http://www.w3.org/2005/11/its/rdf#taConfidence"),
                model.createTypedLiteral(new Double(score)));

        return model;
    }

    public String getContextURI(Model contextModel) {
        StmtIterator iter = contextModel.listStatements(null, RDF.type, contextModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
        return iter.nextStatement().getSubject().asResource().getURI();
    }
}
