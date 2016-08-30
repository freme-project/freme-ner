package org.unileipzig.persistence.nif.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NIFMention {

    private String mention;

    private Integer beginIndex;

    private Integer endIndex;

    private NIFType nifType;

    private String type;

    private Double score;

    private String taIdentRef;

    private String referenceContext;

    private Map<String, String> entityTypes = new HashMap<String, String>(8);

    public NIFMention(NIFMentionBuilder builder) {
        this.mention = builder.mention;
        this.beginIndex = builder.beginIndex;
        this.endIndex = builder.endIndex;
        this.nifType = builder.nifType;
        this.type = builder.type;
        this.score = builder.score;
        this.taIdentRef = builder.taIdentRef;
        this.referenceContext = builder.referenceContext;
        init();
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

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

    public void setType(String type) {
        this.type = type;
    }

    public Boolean hasType() {
        return type != null && !type.isEmpty();
    }

    public Boolean hasTaIdentRef() {
        return taIdentRef != null && !taIdentRef.isEmpty();
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

    public Map<String, String> getEntityTypes() {
        return entityTypes;
    }

    public String getTaIdentRef() {
        return taIdentRef;
    }

    public void setTaIdentRef(String taIdentRef) {
        this.taIdentRef = taIdentRef;
    }

    public String getReferenceContext() {
        return referenceContext;
    }

    public void setReferenceContext(String referenceContext) {
        this.referenceContext = referenceContext;
    }

    public static class NIFMentionBuilder {

        private String mention;

        private Integer beginIndex;

        private Integer endIndex;

        private NIFType nifType = NIFType.MENTION;

        private String type;

        private Double score;

        private String taIdentRef;

        private String referenceContext;


        public NIFMentionBuilder init() {

            this.mention = null;
            this.beginIndex= null;
            this.endIndex= null;
            this.nifType = NIFType.MENTION;
            this.type= null;
            this.score= null;
            this.taIdentRef= null;

            return this;
        }


        public NIFMentionBuilder taIdentRef(String taIdentRef) {
            this.taIdentRef = taIdentRef;
            return this;
        }

        public NIFMentionBuilder mention(String mention) {
            this.mention = mention;
            return this;
        }

        public NIFMentionBuilder beginIndex(Integer beginIndex) {
            this.beginIndex = beginIndex;
            return this;
        }

        public NIFMentionBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }


        public NIFMentionBuilder nifType(NIFType nifType) {
            this.nifType = nifType;
            return this;
        }


        public NIFMentionBuilder type(String type) {
            this.type = type;
            return this;
        }


        public NIFMentionBuilder score(Double score) {
            this.score = score;
            return this;
        }

        public NIFMentionBuilder referenceContext(String referenceContext) {
            this.referenceContext = referenceContext;
            return this;
        }

        public Optional<NIFMention> build() {
            return Optional.of(new NIFMention(this));
        }


    }
}
