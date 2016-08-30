package org.unileipzig.persistence.nif;

public interface NIF21Format extends NIFFormat {

    String XML_PREFIX = "http://www.w3.org/2001/XMLSchema#";

    String RDF_PREFIX = "http://www.w3.org/2005/11/its/rdf#";

    String RDF_PROPERTY_CLASS_REF = "taClassRef";

    String RDF_PROPERTY_CONFIDENCE = "taConfidence";


    String NIF_CORE_PREFIX = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";

    String NIF_PROPERTY_ISSTRING = "isString";

    String NIF_PROPERTY_OFFSETBASEDSTRING = "OffsetBasedString";

    String NIF_PROPERTY_CONTEXT = "Context";

    String NIF_PROPERTY_WORD = "Word";

    String NIF_PROPERTY_PHRASE = "Phrase";

    String NIF_PROPERTY_STRING = "String";

    String NIF_PROPERTY_BEGININDEX = "beginIndex";

    String NIF_PROPERTY_ENDINDEX = "endIndex";

    String NIF_PROPERTY_ANCHOR_OF = "anchorOf";

    String NIF_PROPERTY_REFERENCE_CONTEXT = "referenceContext";

    String NIF_PROPERTY_CONTEXT_STRING_REF = "contextStringRef";

    String NIF_PROPERTY_TEXT_SPAN_ANNOTATION = "TextSpanAnnotation";

    String NIF_PROPERTY_ENTITY_OCURRENCE = "EntityOccurrence";

    String NIF_PROPERTY_TERM_OCURRENCE = "TermOccurrence";

}
