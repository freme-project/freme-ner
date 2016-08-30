package org.unileipzig.persistence.nif;

import com.hp.hpl.jena.rdf.model.Model;

import java.util.Optional;

public interface NIFVisitor {

    Optional<Model> getModel();

    void visit(NIFModel model);

    void visit(NIFPrefixes prefixes);

    void visit(NIFProperties properties);

    void visit(NIFLiteral literal);

    void visit(NIFResource resource);
}
