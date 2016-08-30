package org.elinker.core.api.java.serialize;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.unileipzig.persistence.nif.NIFVisitor;
import org.unileipzig.persistence.nif.impl.*;

import java.util.Optional;

/**
 * Creates NIF Jena models for documents and annotations (spotted entity mentions, linked entites, classification etc.)
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 *         http://dojchinovski.mk
 */
public class NIFConverter {

    private String prefix = null;
    private String version = null;


    public NIFConverter(String version, String prefix) {
        this.prefix = prefix;
        this.version = version;
    }

    private Boolean isNIF20() {
        return RDFConstants.nifVersion2_0.equals(version);
    }

    private Boolean isNIF21() {
        return RDFConstants.nifVersion2_1.equals(version);
    }

    private Model nif20(Optional<NIFContext> context, Optional<NIFMention> entity) {

        NIFVisitor nifVisitor = new NIF20CreateContext(context, entity);
        NIF20 nif20 = new NIF20();
        nif20.accept(nifVisitor);

        return nifVisitor.getModel().get();

    }

    private Model nif21(Optional<NIFContext> context, Optional<NIFMention> entity) {

        NIFVisitor nifVisitor = new NIF21CreateContext(context, entity);
        NIF21 nif21 = new NIF21();
        nif21.accept(nifVisitor);

        return nifVisitor.getModel().get();
    }


    private Optional<NIFContext> buildContext(String prefix, int beginIndex, int endIndex) {

        NIFContext context = new NIFContext(prefix, beginIndex, endIndex);

        return Optional.of(context);
    }

    public Model createContext(String text, int beginIndex, int endIndex) {

        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(text)
                .beginIndex(beginIndex).endIndex(endIndex).nifType(NIFType.CONTEXT).build();

        return getModel(nifContext, nifMention);
    }

    private Model getModel(Optional<NIFContext> nifContext, Optional<NIFMention> nifMention) {
        if (isNIF20()) {
            return nif20(nifContext, nifMention);
        } else if (isNIF21()) {
            return nif21(nifContext, nifMention);
        }

        return nif20(nifContext, nifMention);
    }

    public Model createMention(String mention, int beginIndex, int endIndex, String referenceContext) {

        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createMentionWithType(String entityType, String mention, int beginIndex,
                                       int endIndex, String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .type(entityType).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createMentionWithScore(String mention, int beginIndex, int endIndex, double score,
                                        String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).referenceContext(referenceContext).endIndex(endIndex).build();

        return getModel(nifContext, nifMention);
    }

    public Model createMentionWithTypeAndScore(String entityType, String mention, int beginIndex, int endIndex, double score,
                                               String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .type(entityType).score(score).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createLink(String mention, int beginIndex, int endIndex, String taIdentRef, String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention =  new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .taIdentRef(taIdentRef).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createLinkWithType(String entityType, String[] otherTypes, String mention, int beginIndex, int endIndex,
                                    String taIdentRef, String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .type(entityType).taIdentRef(taIdentRef).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createLinkWithScore(String mention, int beginIndex, int endIndex, String taIdentRef, double score,
                                     String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .score(score).taIdentRef(taIdentRef).referenceContext(referenceContext).build();

        return getModel(nifContext, nifMention);
    }

    public Model createLinkWithTypeAndScore(String entityType, String[] otherTypes, String mention, int beginIndex,
                                            int endIndex, String taIdentRef, double score, String referenceContext) {
        Optional<NIFContext> nifContext = buildContext(prefix, beginIndex, endIndex);
        Optional<NIFMention> nifMention = new NIFMention.NIFMentionBuilder().init().mention(mention)
                .beginIndex(beginIndex).endIndex(endIndex)
                .type(entityType).taIdentRef(taIdentRef).referenceContext(referenceContext).score(score).build();

        return getModel(nifContext, nifMention);
    }

    public String getContextURI(Model contextModel) {
        StmtIterator iter = contextModel.listStatements(null, RDF.type, contextModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
        return iter.nextStatement().getSubject().asResource().getURI();
    }
}
