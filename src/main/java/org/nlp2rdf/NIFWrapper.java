package org.nlp2rdf;


import eu.freme.common.conversion.rdf.RDFConstants;
import org.elinker.core.api.process.Result;
import org.nlp2rdf.bean.NIFBean;
import org.nlp2rdf.bean.NIFType;
import org.nlp2rdf.nif20.impl.NIF20;
import org.nlp2rdf.nif21.impl.NIF21;

import java.util.ArrayList;
import java.util.List;

public class NIFWrapper {

    private final String FREME_URL = "http://freme-project.eu/tools/freme-ner";

    private String version;

    private List<NIFBean> entities = new ArrayList<>();

    private NIFBean beanContext;

    private String baseURI;

    public NIFWrapper(String baseURI, String version) {

        this.baseURI = baseURI;
        this.version = version;
    }

    public void context(String mention) {

        int beginIndex = 0;
        int endIndex = mention.length();

        NIFBean.NIFBeanBuilder contextBuilder = new NIFBean.NIFBeanBuilder();

        contextBuilder.context(baseURI, beginIndex, endIndex).mention(mention).nifType(NIFType.CONTEXT);

        beanContext = new NIFBean(contextBuilder);

    }

    public void entity(Result result) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.annotator(FREME_URL).beginIndex(result.beginIndex()).endIndex(result.endIndex())
                .mention(result.mention()).context(baseURI, result.beginIndex(), result.endIndex());

        if (result.score().isDefined()) {
            entity.score((Double) result.score().get());
        }

        if (result.taIdentRef().isDefined()) {
            entity.taIdentRef(result.taIdentRef().get().toString());
        }

        if (result.entityType() != null  && !result.entityType().isEmpty()) {
            List<String> types = new ArrayList<>(1);
            types.add(result.entityType());
            entity.types(types);
        }

        entities.add(new NIFBean(entity));

    }


    public void entity(Result result, String[] otherTypes) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.annotator(FREME_URL).beginIndex(result.beginIndex()).endIndex(result.endIndex())
                .mention(result.mention()).context(baseURI, result.beginIndex(), result.endIndex());

        if (result.score().isDefined()) {
            entity.score((Double) result.score().get());
        }

        if (result.taIdentRef().isDefined()) {
            entity.taIdentRef(result.taIdentRef().get().toString());
        }

        if (result.entityType() != null  && !result.entityType().isEmpty() ) {
            List<String> types = new ArrayList<>();
            types.add(result.entityType());

            if (otherTypes != null) {
                for (int i=0 ; i < otherTypes.length ; i++) {
                    types.add(otherTypes[i]);
                }
            }
            entity.types(types);
        }

        entities.add(new NIFBean(entity));

    }

    public void entities(List<Result> results) {

        results.stream().forEach(result -> entity(result));

    }

    public String getNIF(String outputFormat) {
        List<NIFBean> entitiesToProcess = new ArrayList<>(entities.size() + 1);

        entitiesToProcess.add(beanContext);
        entitiesToProcess.addAll(entities);

        NIF nif;

         if (RDFConstants.nifVersion2_1.equalsIgnoreCase(version)) {
             nif =  new NIF21(entitiesToProcess);
         } else {
             nif = new NIF20(entitiesToProcess);
         }

        return nif.getTurtle();
    }
}
