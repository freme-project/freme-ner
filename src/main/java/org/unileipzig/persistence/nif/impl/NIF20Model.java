package org.unileipzig.persistence.nif.impl;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.unileipzig.persistence.nif.NIFModel;
import org.unileipzig.persistence.nif.NIFVisitor;

public class NIF20Model implements NIFModel {
    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Model create() {
        return ModelFactory.createDefaultModel();
    }
}
