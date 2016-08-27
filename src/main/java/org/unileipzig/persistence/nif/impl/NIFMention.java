package org.unileipzig.persistence.nif.impl;

import java.util.HashMap;
import java.util.Map;

public class NIFMention {

    private String mention;

    private Integer beginIndex;

    private Integer endIndex;

    private String referenceContext;

    private NIFType nifType;

    private String type;

    private Double score;

    public Double getScore() {
        return score;
    }

    public NIFMention() {
        init();
    }

    public NIFMention(String mention, Integer beginIndex, Integer endIndex, NIFType nifType) {
        this.mention = mention;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.nifType = nifType;
        init();
    }

    private Map<String, String> entityTypes = new HashMap<String, String>(8);
    private void init() {
        entityTypes.put("PERSON", "http://nerd.eurecom.fr/ontology#Person");
        entityTypes.put("ORGANIZATION", "http://nerd.eurecom.fr/ontology#Organization");
        entityTypes.put("LOCATION", "http://nerd.eurecom.fr/ontology#Location");
        entityTypes.put("MISC", "http://www.w3.org/2002/07/owl#Thing");
        entityTypes.put("I-PER", "http://nerd.eurecom.fr/ontology#Person");
        entityTypes.put("I-ORG", "http://nerd.eurecom.fr/ontology#Organization");
        entityTypes.put("I-LOC", "http://nerd.eurecom.fr/ontology#Location");
        entityTypes.put("I-MISC", "http://www.w3.org/2002/07/owl#Thing");
    }

    public String getType() {
        return entityTypes.get(type);
    }

    public Boolean hasType() {
        return type != null && !type.isEmpty();
    }

    public Boolean hasScore() {
        return score != null;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public Integer getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(Integer beginIndex) {
        this.beginIndex = beginIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public String getReferenceContext() {
        return referenceContext;
    }

    public void setReferenceContext(String referenceContext) {
        this.referenceContext = referenceContext;
    }

    public Boolean isMention() {
        return NIFType.MENTION.equals(nifType);
    }

    public Boolean isContext() {
        return NIFType.CONTEXT.equals(nifType);
    }

    public NIFType getNifType() {
        return nifType;
    }

    public void setNifType(NIFType nifType) {
        this.nifType = nifType;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Map<String, String> getEntityTypes() {
        return entityTypes;
    }
}
