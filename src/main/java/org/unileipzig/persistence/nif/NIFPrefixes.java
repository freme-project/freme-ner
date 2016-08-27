package org.unileipzig.persistence.nif;

import com.hp.hpl.jena.rdf.model.Model;

import java.util.Optional;

public interface NIFPrefixes extends NIFFormat {

    void add(Optional<Model> model);

}
