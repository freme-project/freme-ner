package org.unileipzig.persistence.nif.impl;


import com.hp.hpl.jena.rdf.model.Model;
import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFPrefixes;
import org.unileipzig.persistence.nif.NIFVisitor;

import java.util.Optional;

public class NIF21Prefixes implements NIFPrefixes, NIF21Format {

    @Override
    public void accept(NIFVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void add(Optional<Model> model) {
        if (model.isPresent()) {
            model.get().setNsPrefix("nif", NIF_CORE_PREFIX);
            model.get().setNsPrefix("itsrdf", RDF_PREFIX);
            model.get().setNsPrefix("xsd", XML_PREFIX);
        }
    }
}
