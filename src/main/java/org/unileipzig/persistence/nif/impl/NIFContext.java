package org.unileipzig.persistence.nif.impl;


public class NIFContext {

    private String baseURI;

    private int beginIndex;

    private int endIndex;

    public NIFContext(String baseURI, int beginIndex, int endIndex) {
        this.baseURI = baseURI;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    public String getContext(String format) {
        return String.format(format, baseURI, beginIndex, endIndex);
    }

    public String getCollection() {
        return String.format("%s#collection", baseURI);
    }

}
