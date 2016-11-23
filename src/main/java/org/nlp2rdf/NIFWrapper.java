package org.nlp2rdf;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.freme.common.conversion.rdf.RDFConstants;
import org.elinker.core.api.process.Result;
import org.nlp2rdf.bean.NIFBean;
import org.nlp2rdf.bean.NIFType;
import org.nlp2rdf.nif20.impl.NIF20;
import org.nlp2rdf.nif21.impl.NIF21;
import org.nlp2rdf.parser.NIFParser;

import java.util.ArrayList;
import java.util.List;

public class NIFWrapper {

    private final String FREME_URL = "http://freme-project.eu/tools/freme-ner";

    private String version;

    private List<NIFBean> entities = new ArrayList<>();

    private NIFBean beanContext;

    private String baseURI;

    private Boolean classify;

    public NIFWrapper(String baseURI, String version, Boolean classify) {

        this.baseURI = baseURI;
        this.version = version;
        this.classify = classify;

        formatBaseURI();
    }

    public void context(String mention) {

        int beginIndex = 0;
        int endIndex = mention.length();

        NIFBean.NIFBeanBuilder contextBuilder = new NIFBean.NIFBeanBuilder();

        contextBuilder.context(baseURI, beginIndex, endIndex).mention(mention).nifType(NIFType.CONTEXT);

        beanContext = new NIFBean(contextBuilder);

    }


    private void formatBaseURI() {
        if (baseURI != null && !baseURI.isEmpty() &&
                !"/".equals(baseURI.substring(baseURI.length() - 1))) {
            baseURI = baseURI.concat("/");
        }
    }

    public void entity(Result result) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.annotator(FREME_URL).beginIndex(result.beginIndex()).endIndex(result.endIndex())
                .mention(result.mention());

        entity.context(baseURI, result.beginIndex(), result.endIndex());

        if (result.score().isDefined()) {
            entity.score((Double) result.score().get());
        }

        if (result.taIdentRef().isDefined()) {
            entity.taIdentRef(result.taIdentRef().get().toString());
        }

        if (classify && result.entityType() != null && !result.entityType().isEmpty()) {
            List<String> types = new ArrayList<>(1);
            types.add(result.entityType());
            entity.types(types);
        }

        entities.add(new NIFBean(entity));

    }


    public void entity(Result result, String[] otherTypes, String[] taClassRef) {

        NIFBean.NIFBeanBuilder entity = new NIFBean.NIFBeanBuilder();

        entity.annotator(FREME_URL).beginIndex(result.beginIndex()).endIndex(result.endIndex())
                .mention(result.mention()).context(baseURI, result.beginIndex(), result.endIndex());

        if (result.score().isDefined()) {
            entity.score((Double) result.score().get());
        }

        if (result.taIdentRef().isDefined()) {
            entity.taIdentRef(result.taIdentRef().get().toString());
        }

        if (taClassRef != null) {
            List<String> types = new ArrayList<>();

            if (otherTypes != null) {
                for (int i = 0; i < taClassRef.length; i++) {
                    types.add(taClassRef[i]);
                }
            }
            entity.taClassRef(types);
        }

        if (result.entityType() != null && !result.entityType().isEmpty()) {
            List<String> types = new ArrayList<>();
            types.add(result.entityType());

            if (otherTypes != null) {
                for (int i = 0; i < otherTypes.length; i++) {
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

    public String getNIF(String outputFormat, NIFParser parser) {
        List<NIFBean> entitiesToProcess = new ArrayList<>(entities.size() + 1);

        entitiesToProcess.add(beanContext);
        entitiesToProcess.addAll(entities);

        NIF nif;

        if (RDFConstants.nifVersion2_1.equalsIgnoreCase(version)) {
            nif = new NIF21(entitiesToProcess, parser);
        } else {
            nif = new NIF20(entitiesToProcess, parser);
        }

        return nif.getTurtle();
    }

    public String getNIF(String outputFormat) {
        List<NIFBean> entitiesToProcess = new ArrayList<>(entities.size() + 1);

        entitiesToProcess.add(beanContext);
        entitiesToProcess.addAll(entities);

        NIF nif;

        if (RDFConstants.nifVersion2_1.equalsIgnoreCase(version)) {
            nif = new NIF21(entitiesToProcess);
        } else {
            nif = new NIF20(entitiesToProcess);
        }

        return nif.getTurtle();
    }
}
