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
            Resource contextRes = model.get().getResource(context.get().getContext());

            contextRes.addProperty(
                    RDF.type,
                    model.get().createResource(NIF_PROPERTY_STRING));
            contextRes.addProperty(
                    RDF.type,
                    model.get().createResource(NIF_PROPERTY_OFFSETBASEDSTRING));

            if (entity.get().isContext()) {
                contextRes.addProperty(
                        RDF.type,
                        model.get().createResource(NIF_PROPERTY_CONTEXT));
            } else if (entity.get().isMention()) {
                contextRes.addProperty(
                        RDF.type,
                        model.get().createResource(NIF_PROPERTY_WORD));
                contextRes.addProperty(
                        RDF.type,
                        model.get().createResource(NIF_PROPERTY_PHRASE));
                contextRes.addProperty(
                        model.get().createProperty(NIF_PROPERTY_REFERENCE_CONTEXT),
                        model.get().createResource(context.get().getContext()));
            }
        }
    }

    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
