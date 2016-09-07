package org.elinker.core.api.java.serialize;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.freme.common.conversion.rdf.RDFConstants;

import org.nlp2rdf.bean.NIFBean;
import org.nlp2rdf.bean.NIFContext;
import org.nlp2rdf.bean.NIFType;
import org.nlp2rdf.nif20.impl.NIF20;
import org.nlp2rdf.nif21.impl.NIF21;

import java.util.ArrayList;
import java.util.List;
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


    private Optional<NIFContext> buildContext(String prefix, int beginIndex, int endIndex) {

        NIFContext context = new NIFContext(prefix, beginIndex, endIndex);

        return Optional.of(context);
    }

    public Model createContext(String text, int beginIndex, int endIndex) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.mention(text).beginIndex(beginIndex).endIndex(endIndex).nifType(NIFType.CONTEXT);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(text,beginIndex, endIndex));

        return getModel(beans);
    }

    private Model getModel(List<NIFBean> beans) {
        if (isNIF20()) {
            return new NIF20(beans).getModel();
        } else if (isNIF21()) {
            return new NIF21(beans).getModel();
        }

        return new NIF20(beans).getModel();
    }

    public Model createMention(String mention, int beginIndex, int endIndex, String referenceContext) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();


        entity.context(prefix, beginIndex, endIndex).mention(mention).beginIndex(beginIndex).endIndex(endIndex).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention, beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createMentionWithType(String entityType, String mention, int beginIndex,
                                       int endIndex, String referenceContext) {
        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        List<String> types = new ArrayList<>();
        types.add(entityType);

        entity.context(prefix, beginIndex, endIndex).mention(mention).types(types).beginIndex(beginIndex).endIndex(endIndex).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention, beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createMentionWithScore(String mention, int beginIndex, int endIndex, double score,
                                        String referenceContext) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();


        entity.context(prefix, beginIndex, endIndex).mention(mention).beginIndex(beginIndex).endIndex(endIndex).score(score).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createMentionWithTypeAndScore(String entityType, String mention, int beginIndex, int endIndex, double score,
                                               String referenceContext) {
        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        List<String> types = new ArrayList<>();
        types.add(entityType);


        entity.context(prefix, beginIndex, endIndex).mention(mention).types(types).beginIndex(beginIndex).endIndex(endIndex).score(score).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createLink(String mention, int beginIndex, int endIndex, String taIdentRef, String referenceContext) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.context(prefix, beginIndex, endIndex).mention(mention).beginIndex(beginIndex).endIndex(endIndex).taIdentRef(taIdentRef).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createLinkWithType(String entityType, String[] otherTypes, String mention, int beginIndex, int endIndex,
                                    String taIdentRef, String referenceContext) {
        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        List<String> types = new ArrayList<>();
        types.add(entityType);

        if (otherTypes != null && otherTypes.length > 0) {
            for(int i=0; i < otherTypes.length; i++ ) {
                types.add(otherTypes[i]);
            }
        }

        entity.context(prefix, beginIndex, endIndex).mention(mention).types(types).beginIndex(beginIndex).endIndex(endIndex).taIdentRef(taIdentRef).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createLinkWithScore(String mention, int beginIndex, int endIndex, String taIdentRef, double score,
                                     String referenceContext) {
        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.context(prefix, beginIndex, endIndex).mention(mention).beginIndex(beginIndex).endIndex(endIndex).taIdentRef(taIdentRef).score(score).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    public Model createLinkWithTypeAndScore(String entityType, String[] otherTypes, String mention, int beginIndex,
                                            int endIndex, String taIdentRef, double score, String referenceContext) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        List<String> types = new ArrayList<>();
        types.add(entityType);

        if (otherTypes != null && otherTypes.length > 0) {
            for(int i=0; i < otherTypes.length; i++ ) {
                types.add(otherTypes[i]);
            }
        }

        entity.context(prefix, beginIndex, endIndex).mention(mention).types(types).beginIndex(beginIndex).endIndex(endIndex).taIdentRef(taIdentRef).score(score).referenceContext(referenceContext);

        NIFBean entityBean = new NIFBean(entity);

        List<NIFBean> beans = new ArrayList<>();
        beans.add(getContext(mention,beginIndex, endIndex));
        beans.add(entityBean);

        return getModel(beans);
    }

    private NIFBean getContext(String mention, int beginIndex, int endIndex) {
        NIFBean.NIFBeanBuilder contextBuilder = new NIFBean.NIFBeanBuilder();
        contextBuilder.mention(mention).context(prefix, beginIndex, endIndex).nifType(NIFType.CONTEXT);
        NIFBean beanContext = new NIFBean(contextBuilder);
        return beanContext;
    }


    public String getContextURI(Model contextModel) {
        StmtIterator iter = contextModel.listStatements(null, RDF.type, contextModel.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));

       try{
           return iter.nextStatement().getSubject().asResource().getURI();
       } catch (Exception e ){
           return "";
       }
    }
}
