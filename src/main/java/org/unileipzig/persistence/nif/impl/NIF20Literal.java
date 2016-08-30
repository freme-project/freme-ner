package org.unileipzig.persistence.nif.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.unileipzig.persistence.nif.NIF20Format;
import org.unileipzig.persistence.nif.NIFLiteral;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF20Literal implements NIFLiteral, NIF20Format {

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> mention) {

        if (model.isPresent() && context.isPresent() && mention.isPresent()) {

            Resource contextRes = model.get().getResource(context.get().getContext());

            if (mention.get().isContext()) {

                contextRes.addLiteral(
                        model.get().getProperty(NIF_PROPERTY_ISSTRING),
                        mention.get().getMention());
                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_BEGININDEX),
                        model.get().createTypedLiteral(mention.get().getBeginIndex()));
                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_ENDINDEX),
                        model.get().createTypedLiteral(mention.get().getEndIndex()));

            } else if (mention.get().isMention()) {

                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_ANCHOR_OF),
                        mention.get().getMention());
                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_BEGININDEX),
                        model.get().createTypedLiteral(new Integer(mention.get().getBeginIndex())));
                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_ENDINDEX),
                        model.get().createTypedLiteral(new Integer(mention.get().getEndIndex())));

                if (mention.get().hasType()) {
                    contextRes.addProperty(
                            model.get().createProperty(RDF_PROPERTY_CLASS_REF),
                            model.get().createResource(mention.get().getType()));
                }

                if (mention.get().hasScore()) {
                    contextRes.addLiteral(
                            model.get().createProperty(RDF_PROPERTY_CONFIDENCE),
                            model.get().createTypedLiteral(mention.get().getScore()));
                }

            }
        }

    }


    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
