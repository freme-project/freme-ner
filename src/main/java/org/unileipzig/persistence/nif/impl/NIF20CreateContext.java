package org.unileipzig.persistence.nif.impl;


import com.hp.hpl.jena.rdf.model.Model;
import org.unileipzig.persistence.nif.*;

import java.util.Optional;

public class NIF20CreateContext implements NIFVisitor {

    private Optional<Model> model;

    private Optional<NIFContext> context;

    private Optional<NIFMention> entity;

    public NIF20CreateContext(Optional<NIFContext> context, Optional<NIFMention> entity) {
        this.context = context;
        this.entity = entity;
    }

    @Override
    public Optional<Model> getModel() {
        return model;
    }

    @Override
    public void visit(NIFModel model) {
        this.model = Optional.of(model.create());
    }

    @Override
    public void visit(NIFPrefixes prefixes) {
        prefixes.add(model);
    }

    @Override
    public void visit(NIFResource resource) {
        resource.add(model, context);
    }

    @Override
    public void visit(NIF21AnnotationUnit anotationUnit) {
    }

    @Override
    public void visit(NIFProperties properties) {
        properties.add(model, context, entity);
    }

    @Override
    public void visit(NIFLiteral literal) {
        literal.add(model, context, entity);
    }


}
