package org.unileipzig.persistence.nif;


import com.hp.hpl.jena.rdf.model.Model;

public interface NIFModel  extends NIFFormat {

    Model create();

}
