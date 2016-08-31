package org.unileipzig.persistence.nif.impl;

import org.unileipzig.persistence.nif.NIF21Format;
import org.unileipzig.persistence.nif.NIFFormat;
import org.unileipzig.persistence.nif.NIFVisitor;


public class NIF21 implements NIF21Format {
    NIFFormat[] elements;


    public NIF21() {
        this.elements = new NIFFormat[]{new NIF21Model(), new NIF21Resource(), new NIF21Prefixes(), new NIF21Properties(), new NIF21Literal(), new NIF21AnnotationUnit()};
    }

    @Override
    public void accept(NIFVisitor visitor) {
        for (NIFFormat elem : elements) {
            elem.accept(visitor);
        }
    }
}
