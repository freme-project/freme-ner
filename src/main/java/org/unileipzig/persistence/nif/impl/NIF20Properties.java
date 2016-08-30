package org.unileipzig.persistence.nif.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.unileipzig.persistence.nif.NIF20Format;
import org.unileipzig.persistence.nif.NIFProperties;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF20Properties implements NIFProperties, NIF20Format {

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> entity) {

        if (model.isPresent() && context.isPresent() && entity.isPresent()) {
            Resource contextRes = model.get().getResource(context.get().getContext());

           if (entity.get().isMention()) {
               fillMention(model, entity, contextRes);

            } else if (entity.get().isContext()) {

               fillContext(model, contextRes);
            }


        }
    }

    private void fillContext(Optional<Model> model, Resource contextRes) {
        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_STRING));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_CONTEXT));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_RFC5147));
    }

    private void fillMention(Optional<Model> model, Optional<NIFMention> entity, Resource contextRes) {

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_WORD));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_PHRASE));
        contextRes.addProperty(
                model.get().createProperty(NIF_PROPERTY_REFERENCE_CONTEXT),
                model.get().createResource(entity.get().getReferenceContext()));

        if (entity.get().hasTaIdentRef()) {
            contextRes.addProperty(
                    model.get().createProperty(RDF_PROPERTY_IDENTREF),
                    model.get().createResource(entity.get().getTaIdentRef()));
        }

        if (entity.get().hasType()) {
            contextRes.addProperty(
                    model.get().createProperty(RDF_PROPERTY_CLASS_REF),
                    model.get().createResource(entity.get().getType()));
        }

        if (entity.get().hasOtherTypes()) {
            for (String ref : entity.get().getOtherTypes()) {
                contextRes.addProperty(
                        model.get().createProperty(RDF_PROPERTY_CLASS_REF),
                        model.get().createResource(ref));
            }
        }
        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_STRING));

        contextRes.addProperty(
                RDF.type,
                model.get().createResource(NIF_PROPERTY_RFC5147));
    }

    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
