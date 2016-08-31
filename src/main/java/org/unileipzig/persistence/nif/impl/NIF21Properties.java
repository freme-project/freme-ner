package org.unileipzig.persistence.nif.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFProperties;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF21Properties implements NIFProperties, NIF21Format {

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> entity) {

        if (model.isPresent() && context.isPresent() && entity.isPresent()) {
            Resource contextRes = model.get().getResource(context.get().getContext(CONTEXT_FORMAT));

           if (entity.get().isMention()) {
               fillMention(model, entity, contextRes);
            } else if (entity.get().isContext()) {
               fillResourceCollection (model, context);
               fillContext(model, contextRes);
            }
        }
    }


    private void fillResourceCollection(Optional<Model> model, Optional<NIFContext> context) {

        Resource resource = model.get().getResource(context.get().getCollection());

        resource.addProperty(RDF.type, model.get().createResource(NIF_PROPERTY_CONTEXT_COLLECTION));

        resource.addProperty(
                model.get().createProperty(NIF_PROPERTY_HAS_CONTEXT),
                model.get().createResource(context.get().getContext(CONTEXT_FORMAT)));


        resource.addProperty(
                model.get().createProperty(NIF_PROPERTY_CONFORMS_TO),
                model.get().createResource(context.get().getContext(NIF_21)));
    }

    private void fillContext(Optional<Model> model, Resource contextRes) {


        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_CONTEXT));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_OFFSETBASEDSTRING));

    }

    private void fillMention(Optional<Model> model, Optional<NIFMention> entity, Resource contextRes) {

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_OFFSETBASEDSTRING));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_PHRASE));

        contextRes.addProperty(
                model.get().createProperty(NIF_PROPERTY_REFERENCE_CONTEXT),
                model.get().createResource(entity.get().getReferenceContext()));

    }

    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
