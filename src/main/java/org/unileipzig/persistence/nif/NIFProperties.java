package org.unileipzig.persistence.nif;


import com.hp.hpl.jena.rdf.model.Model;
import org.unileipzig.persistence.nif.impl.NIFContext;
import org.unileipzig.persistence.nif.impl.NIFMention;

import java.util.Optional;

public interface NIFProperties extends NIFFormat {

    void add(Optional<Model> model, Optional<NIFContext> context, Optional<NIFMention> entity);
}
