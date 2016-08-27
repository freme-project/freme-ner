package org.unileipzig.persistence.nif;

import com.hp.hpl.jena.rdf.model.Model;
import org.unileipzig.persistence.nif.impl.NIFContext;

import java.util.Optional;

public interface NIFResource extends NIFFormat {

    void add(Optional<Model> model, Optional<NIFContext> context);
}
