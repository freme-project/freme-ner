package org.elinker.core.api.java;


import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.elinker.core.api.process.Result;
import org.elinker.core.link.FremeEntityLinker;
import org.elinker.core.rest.FremeRequest;
import org.elinker.core.spotter.FremeSpotter;
import org.nlp2rdf.NIFWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.elinker.core.common.Symbols.*;

@Service
@RequiredArgsConstructor
public class FremeLabelMatch {

    @Autowired
    private FremeSpotter spotter;

    @Autowired
    private FremeEntityLinker fremeEntityLinker;


    public String annotate(FremeRequest request) {

        if (request.isSpotLinkClassify()) {
            return spotLinkClassify(request);
        } else if (request.isSpotClassify()) {
            return spotClassify(request);
        } else if (request.isSpotLink()) {
            return spotLink(request);
        } else if (request.isLink()) {
            return link(request);
        }

        return spot(request);
    }

    private List<Result> spots(String text) {
        Collection<Emit> spots =  spotter.parseText(text);
        List<Result> results = new ArrayList<>();
        if (!spots.isEmpty()) {
            spots.stream().forEach(spot-> {
                Result result = new Result(EMPTY , spot.getKeyword(), spot.getStart(),
                        spot.getEnd(), Option.apply(EMPTY), Option.apply(1d));
                results.add(result);
            });
        }
        return results;
    }

    private List<Result> links(List<String> datasets, Integer topN, String language, List<Result> results) {
        return fremeEntityLinker.link(datasets, topN, language, results);
    }

    private List<Result> classify(List<Result> results) {
        return results;
    }


    private String spot(FremeRequest request) {
        return toNIF(request, spots(request.getPlainText()));
    }

    private String toNIF(FremeRequest request, List<Result> results) {

        NIFWrapper nif = new NIFWrapper(request.getPrefix(), request.getNifVersion(), request.isClassify());
        nif.context(request.getPlainText());
        nif.entities(results);

        return nif.getNIF(request.getOutputFormat());
    }

    private String link(FremeRequest request) {

        List<Result> results = spots(request.getPlainText());

        results = links(request.datasets(), request.getNumLinksParam(), request.getLanguage(), results);

        return toNIF(request, results);
    }

    private String spotClassify(FremeRequest request) {

        List<Result> results = spots(request.getPlainText());

        results = classify(results);

        return toNIF(request, results);

    }

    private String spotLink(FremeRequest request) {

        List<Result> results = spots(request.getPlainText());

        results = links(request.datasets(), request.getNumLinksParam(), request.getLanguage(), results);

        return toNIF(request, results);

    }

    private String spotLinkClassify(FremeRequest request) {

        List<Result> results = spots(request.getPlainText());

        results = links(request.datasets(), request.getNumLinksParam(), request.getLanguage(), results);

        results = classify(results);

        return toNIF(request, results);

    }
}