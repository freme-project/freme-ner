package org.elinker.core.api.java.serialize;

import java.util.ArrayList;

/**
 * Created by nilesh on 14/01/16.
 */
public class Document {
    private ArrayList<EntityMention> entities;
    private String text;

    public Document(ArrayList<EntityMention> entities, String text) {
        this.entities = entities;
        this.text = text;
    }

    public ArrayList<EntityMention> getEntities() {
        return entities;
    }

    public String getText() {
        return text;
    }
}
