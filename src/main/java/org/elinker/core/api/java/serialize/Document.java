package org.elinker.core.api.java.serialize;

import java.util.List;

/**
 * Created by nilesh on 14/01/16.
 */
public class Document {
    private List<EntityMention> entities;
    private String text;

    public Document(List<EntityMention> entities, String text) {
        this.entities = entities;
        this.text = text;
    }

    public List<EntityMention> getEntities() {
        return entities;
    }

    public String getText() {
        return text;
    }
}
