package org.unileipzig.persistence.nif.impl;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFResource;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF21Resource implements NIFResource, NIF21Format {

    @Override
    public void add(Optional<Model> model, Optional<NIFContext> context) {
        if (model.isPresent() && context.isPresent()) {

            model.get().createResource(context.get().getCollection());

            model.get().createResource(context.get().getContext(CONTEXT_FORMAT));
        }
    }

    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }
}
