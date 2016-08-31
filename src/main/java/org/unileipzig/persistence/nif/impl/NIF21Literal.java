package org.unileipzig.persistence.nif.impl;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFLiteral;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF21Literal implements NIFLiteral, NIF21Format {

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> mention) {

        if (model.isPresent() && context.isPresent() && mention.isPresent()) {

            Resource contextRes = model.get().getResource(context.get().getContext(CONTEXT_FORMAT));

            if (mention.get().isContext()) {

                contextRes.addLiteral(
                        model.get().getProperty(NIF_PROPERTY_ISSTRING),
                        mention.get().getMention());

                model.get().add(contextRes, model.get().createProperty(NIF_PROPERTY_BEGININDEX),
                        mention.get().getBeginIndex().toString(), XSDDatatype.XSDnonNegativeInteger);

                model.get().add(contextRes, model.get().createProperty(NIF_PROPERTY_ENDINDEX),
                        mention.get().getEndIndex().toString(), XSDDatatype.XSDnonNegativeInteger);

            } else if (mention.get().isMention()) {

                contextRes.addLiteral(
                        model.get().createProperty(NIF_PROPERTY_ANCHOR_OF),
                        mention.get().getMention());

                model.get().add(contextRes, model.get().createProperty(NIF_PROPERTY_BEGININDEX),
                        mention.get().getBeginIndex().toString(), XSDDatatype.XSDnonNegativeInteger);

                model.get().add(contextRes, model.get().createProperty(NIF_PROPERTY_ENDINDEX),
                        mention.get().getEndIndex().toString(), XSDDatatype.XSDnonNegativeInteger);


            }
        }

    }


    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
