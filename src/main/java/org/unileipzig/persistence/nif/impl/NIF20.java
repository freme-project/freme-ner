package org.unileipzig.persistence.nif.impl;

import org.unileipzig.persistence.nif.NIF20Format;
import org.unileipzig.persistence.nif.NIFFormat;
import org.unileipzig.persistence.nif.NIFVisitor;


public class NIF20 implements NIF20Format {
    NIFFormat[] elements;


    public NIF20() {
        this.elements = new NIFFormat[]{new NIF20Model(), new NIF20Resource(), new NIF20Prefixes(), new NIF20Properties(), new NIF20Literal()};
    }

    @Override
    public void accept(NIFVisitor visitor) {
        for (NIFFormat elem : elements) {
            elem.accept(visitor);
        }
    }
}
