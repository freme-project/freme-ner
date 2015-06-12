package org.elinker.core.postag;

/***************************************************************************/
/*  Copyright (C) 2010-2011, Sebastian Hellmann                            */
/*  Note: If you need parts of NLP2RDF in another licence due to licence   */
/*  incompatibility, please mail hellmann@informatik.uni-leipzig.de        */
/*                                                                         */
/*  This file is part of NLP2RDF.                                          */
/*                                                                         */
/*  NLP2RDF is free software; you can redistribute it and/or modify        */
/*  it under the terms of the GNU General Public License as published by   */
/*  the Free Software Foundation; either version 3 of the License, or      */
/*  (at your option) any later version.                                    */
/*                                                                         */
/*  NLP2RDF is distributed in the hope that it will be useful,             */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           */
/*  GNU General Public License for more details.                           */
/*                                                                         */
/*  You should have received a copy of the GNU General Public License      */
/*  along with this program. If not, see <http://www.gnu.org/licenses/>.   */
/***************************************************************************/


import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.OWL;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.nlp2rdf.core.NIFParameters;
import org.nlp2rdf.core.RLOGSLF4JBinding;
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.Text2RDF;
import org.nlp2rdf.core.urischemes.URIScheme;
import org.nlp2rdf.core.vocab.NIFAnnotationProperties;
import org.nlp2rdf.core.vocab.NIFDatatypeProperties;
import org.nlp2rdf.core.vocab.NIFObjectProperties;
import org.nlp2rdf.core.vocab.RLOGIndividuals;
import org.nlp2rdf.vm.dep.StanfordSimple;
import org.nlp2rdf.vm.olia.models.Penn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;


/**
 * The basic code was taken from the ClearTK Project
 * http://code.google.com/p/cleartk
 * who have written a UIMA wrapper.
 * The original file by Steven Bethard can be found here:
 * http://code.google.com/p/cleartk/source/browse/trunk/cleartk-stanford-corenlp/src/main/java/org/cleartk/stanford/StanfordCoreNLPAnnotator.java
 * Licence http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * <p/>
 * Debug with  echo -n "This is a sentence." | mvn compile exec:java -e  -Dexec.mainClass="org.nlp2rdf.implementation.stanfordcore.StanfordCoreCLI" -Dexec.args="-f text -i -" | less
 *
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */

public class StanfordWrapper2 {
    private static Logger log = LoggerFactory.getLogger(StanfordWrapper2.class);

    protected Annotator buildAnnotator(NIFParameters nifParameters) {
        /**
         * Prepare Stanford
         **/
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        //props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner"); // ner,  dcoref");
        //props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); // ner,  dcoref");
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); // ner,  dcoref");
        if (nifParameters.getConfig() != null) {
            //TODO implement proper config parsing
        }

        return new StanfordCoreNLP(props);
    }

    public void processText( Individual context, OntModel inputModel, OntModel outputModel, NIFParameters nifParameters) {
        String contextString = context.getPropertyValue(NIFDatatypeProperties.isString.getDatatypeProperty(inputModel)).asLiteral().getString();
        String prefix = nifParameters.getPrefix();
        URIScheme urischeme = nifParameters.getUriScheme();

        Annotator pipeline = buildAnnotator(nifParameters);

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(contextString);
        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        //get all the sentences and words and read it in an intermediate structure
        //NOTE: this can be greatly optimized of course
        // for now it is just simple and cheap to implement it like this
        int wordCount = 0;
        TreeMap<Span, List<Span>> tokenizedText = new TreeMap<Span, List<Span>>();
        for (CoreMap sentence : sentences) {
            Span sentenceSpan = new Span(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
            List<Span> wordSpans = new ArrayList<Span>();
            for (CoreLabel coreLabel : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                wordSpans.add(new Span(coreLabel.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), coreLabel.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)));
                wordCount++;
            }
            tokenizedText.put(sentenceSpan, wordSpans);
        }

        /**
         * Basic Model Setup
         **/
        //get parameters for the URIGenerator
        Text2RDF text2RDF = new Text2RDF();
        text2RDF.generateNIFModel(prefix, context, urischeme, outputModel, tokenizedText);
        outputModel.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix(), "Finished creating " + tokenizedText.size() + " sentence(s) with " + wordCount + " word(s) ", RLOGIndividuals.DEBUG, this.getClass().getCanonicalName(), null, null));
        // text2RDF.addNextAndPreviousProperties(prefix,urischeme,model);

        // traversing the words in the current sentence
        // a CoreLabel is a CoreMap with additional token-specific methods
        for (CoreMap sentence : sentences) {


            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                Span wordSpan = new Span(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
                //the word should exist already
                Individual wordIndividual = outputModel.getIndividual(urischeme.generate(prefix, contextString, wordSpan));

                if (wordIndividual == null) {
                    log.error("SKIPPING: word was not found in the model: " + urischeme.generate(prefix, contextString, wordSpan));
                    continue;
                }
                /********************************
                 * Lemma
                 ******/

                if(token.get(CoreAnnotations.LemmaAnnotation.class)!=null) {
                    wordIndividual.addProperty(NIFDatatypeProperties.lemma.getDatatypeProperty(outputModel), token.get(CoreAnnotations.LemmaAnnotation.class));
                }

                /********************************
                 * POS tag
                 ******/
                outputModel.setNsPrefix("olia", "http://purl.org/olia/olia.owl#");
                // this is the POS tag of the token
                String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                List<String> oliaIndividual = (List<String>) Penn.hasTag.get(posTag);
                if (oliaIndividual != null) {

                    for (String s : oliaIndividual) {
                        wordIndividual.addProperty(NIFObjectProperties.oliaLink.getObjectProperty(outputModel), outputModel.createIndividual(s, OWL.Thing));
                        List<String> pennlinks = (List<String>) Penn.links.get(s);
                        if (pennlinks != null) {
                            for (String oc : pennlinks) {
                                wordIndividual.addProperty(NIFAnnotationProperties.oliaCategory.getAnnotationProperty(outputModel), outputModel.createClass(oc));
                            }
                        } else {
                            outputModel.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix(), "missing oliaLinks for " + s, RLOGIndividuals.ERROR, this.getClass().getCanonicalName(), null, null));
                        }
                    }
                } else {
                    outputModel.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix(), "missing oliaLinks for " + posTag, RLOGIndividuals.ERROR, this.getClass().getCanonicalName(), null, null));

                }
            }

            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

            if (dependencies != null) {
                //time to add the prefix
                StanfordSimple.addStanfordSimplePrefix(outputModel);



                // create relation annotations for each Stanford dependency
                for (SemanticGraphEdge stanfordEdge : dependencies.edgeIterable()) {

                    Span govSpan = new Span(stanfordEdge.getGovernor().get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), stanfordEdge.getGovernor().get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
                    Span depSpan = new Span(stanfordEdge.getDependent().get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), stanfordEdge.getDependent().get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
                    //String relationType = stanfordEdge.getRelation().toString();

                    String[] edgeURIs = StanfordSimple.getURIforEdgeLabel(stanfordEdge.getRelation().toString());
                    //ObjectProperty relation = model.createObjectProperty(new CStringInst().generate(prefix, contextString, new Span[]{}));
                    ObjectProperty relation = null;
                    switch (edgeURIs.length) {
                        case 1:
                            relation = outputModel.createObjectProperty(edgeURIs[0]);

                            break;
                        case 2:
                            relation = outputModel.createObjectProperty(edgeURIs[0]);
                            relation.addSubProperty(outputModel.createObjectProperty(edgeURIs[1]));
                            break;
                        default:
                            String message = "Empty edge label, no URI written: " + edgeURIs;
                            outputModel.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix(), message, RLOGIndividuals.ERROR, this.getClass().getCanonicalName(), null, null));
                            continue;

                    }

                    Individual gov = text2RDF.createCStringIndividual(prefix, context, govSpan, urischeme, outputModel);
                    Individual dep = text2RDF.createCStringIndividual(prefix, context, depSpan, urischeme, outputModel);
                    gov.addProperty(relation, dep);
                    relation.addSuperProperty(NIFObjectProperties.inter.getObjectProperty(outputModel));
                    relation.addSuperProperty(NIFObjectProperties.dependency.getObjectProperty(outputModel));

                    if (gov == null || dep == null) {
                        String message = "SKIPPING Either gov or dep was null for the dependencies\n" + "gov: " + gov + "\ndep: " + dep;
                        outputModel.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix(), message, RLOGIndividuals.ERROR, this.getClass().getCanonicalName(), null, null));
                        continue;
                    }

                    //  List<String> oliaIndividual = (List<String>) Stanford.hasTag.get(stanfordEdge.getRelation().getShortName());

                    /** for (String s : oliaIndividual) {
                     relation.addProperty(NIFAnnotationProperties.oliaPropLink.getAnnotationProperty(model), model.createIndividual(s, OWL.Thing));
                     for (String oc : (List<String>) Stanford.links.get(s)) {
                     relation.addProperty(NIFAnnotationProperties.oliaCategory.getAnnotationProperty(model), oc);
                     }
                     if (((List<String>) Stanford.links.get(s)).isEmpty()) {
                     log.error("missing links for: " + s);
                     }
                     } **/


                    /* Individual relation = null;//dependency.getOLiAIndividualForTag(relationType);
                        //in an ideal world, all used tags should also be in OLiA, this tends to be null sometimes
                        if (relation == null) {
                            log.error("reltype was null for: " + relationType);
                            continue;
                        }
                        ObjectProperty dependencyRelation = model.createObjectProperty(relation.getURI());
                        //add the property from governer to dependent
                        gov.addProperty(dependencyRelation, dep);
                        Set<String> classUris = dependency.getClassURIsForTag(relationType);
                        for (String cl : classUris) {
                            if (!cl.startsWith("http://purl.org/olia/stanford.owl")) {
                                continue;
                            }
                            //add the property from governer to dependent
                            ObjectProperty nn = model.createObjectProperty(cl);
                            gov.addProperty(nn, dep);
                            dependencyRelation.addSuperProperty(nn);
                            //copy and transform the hierarchy
                            //removed for 2.0
                            //OLiAOntology.classHierarchy2PropertyHierarchy(dependency.getHierarchy(cl), model, "http://purl.org/olia/stanford.owl");
                        }
                    }*/

                }
            }//end sentences
            /**************
             * Syntax Tree
             * */

            //Tree tree = sentence.get(TreeAnnotation.class);
            //if (tree != null) {
            //removed for 2.0
            //processTree(tree, urigenerator, prefix, text, model);
            //}

        }

    }
}

//log.info("Added lemma, pos, olia having " + (diff.size() - size) + " more triples.");
//size = diff.size();
//log.info("Added dependencies: " + (diff.size() - size) + " more triples.");
//size = diff.size();


/**public void processTree(Tree currentNode, URIGenerator uriGenerator, String prefix, String text, OntModel model) {
 // String tag = currentNode.label().value();
 //log.info("Current Node :" + currentNode);
 //log.info("Label: " + currentNode.label() + "");
 //log.info("Label Value: " + currentNode.label().value() + "");
 //log.info("Preterminal: " + currentNode.isPreTerminal() + "");
 //log.info("Index: " + ((CoreLabel) currentNode.label()).get(CharacterOffsetBeginAnnotation.class) + "");
 if (currentNode.isLeaf()) {
 //the node is a leaf and belongs in the realm of pos tagging
 } else {
 Phrase p = new Text2RDF().createStringAnnotationForClass(Phrase.class, prefix, text, getSpan(currentNode), uriGenerator, model);
 List<Tree> children = currentNode.getChildrenAsList();
 for (Tree child : children) {
 /* if (false && child.isPreTerminal()) {
 //skip preterminals
 log.debug("skipping preterminal: "+currentNode);
 log.debug("label: "+currentNode.label());
 child = child.getChild(0);
 Word childTerminal = new Text2RDF().createStringAnnotationForClass(Word.class, prefix, text, getSpan(child), uriGenerator, model);
 p.addChild(childTerminal);
 *
 Phrase childPhrase = new Text2RDF().createStringAnnotationForClass(Phrase.class, prefix, text, getSpan(child), uriGenerator, model);
 p.addChild(childPhrase);
 processTree(child, uriGenerator, prefix, text, model);
 log.info("Current Node :" + currentNode);
 log.info("Label: " + currentNode.label() + "");
 log.info("Label Value: " + currentNode.label().value() + "");
 log.info("Preterminal: " + currentNode.isPreTerminal() + "");
 //log.info("Index: " + ((CoreLabel) currentNode.label()).get(CharacterOffsetBeginAnnotation.class) + "");
 //adding syntax classes from olia and olia-top
 String tag = ((CoreLabel) currentNode.label()).get(CategoryAnnotation.class);
 Set<String> classes = penn_syntax.getClassURIsForTag(tag);
 for (String classUri : classes) {
 log.info("found: " + classUri + " for: " + tag);
 OntModel hierarchy = penn_syntax.getHierarchy(classUri);
 for (ExtendedIterator<OntClass> it = hierarchy.listClasses(); it.hasNext(); ) {
 OntClass oc = it.next();
 p.addOntClass(model.createResource(oc.getURI()));
 }
 //Copy the hierarchy
 model.add(hierarchy);
 }
 }
 }
 } */

