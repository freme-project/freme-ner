package org.unileipzig.persistence.nif.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFAnnotationUnit;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF21AnnotationUnit implements NIFAnnotationUnit, NIF21Format {
    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> entity) {
        if (model.isPresent() && context.isPresent() && entity.isPresent() &&  entity.get().isMention()) {
            Resource contextRes = model.get().getResource(context.get().getContext(CONTEXT_FORMAT));

            Resource unitRes = model.get().createResource().addProperty(RDF.type, ResourceFactory.createResource(NIF_PROPERTY_ENTITY_OCCURRENCE));

            unitRes.addProperty(model.get().createProperty(RDF_PROPERTY_IDENTREF),  model.get().createResource(entity.get().getTaIdentRef()));

            if (entity.get().hasType()) {
                unitRes.addProperty(model.get().createProperty(RDF_PROPERTY_CLASS_REF), model.get().createResource(entity.get().getType()));
            }

            if (entity.get().hasOtherTypes()) {
                for (String ref : entity.get().getOtherTypes()) {
                    unitRes.addProperty(
                            model.get().createProperty(RDF_PROPERTY_CLASS_REF),
                            model.get().createResource(ref));
                }

            }
            if (entity.get().getScore() != null ) {
                unitRes.addProperty(model.get().createProperty(RDF_PROPERTY_CONFIDENCE), model.get().createTypedLiteral(entity.get().getScore()));
            }
            unitRes.addProperty(model.get().createProperty(RDF_PROPERTY_ANNOTATOR), model.get().createProperty(FREME_URL));

            contextRes.addProperty( model.get().createProperty(NIF_PROPERTY_ANNOTATION_UNIT), unitRes) ;


        }
    }
}