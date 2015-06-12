package org.elinker.core.postag;

import edu.stanford.nlp.pipeline.Annotator;
import org.nlp2rdf.core.NIFParameters;
import org.nlp2rdf.implementation.stanfordcorenlp.StanfordWrapper;

/**
 * Created by nilesh on 03/06/15.
 */
public class StanfordPosTagWrapper extends StanfordWrapper2 {

    private final Annotator annotator;

    public StanfordPosTagWrapper(Annotator annotator) {
        this.annotator = annotator;
    }

    @Override
    protected Annotator buildAnnotator(NIFParameters nifParameters) {
        return this.annotator;
    }
}
