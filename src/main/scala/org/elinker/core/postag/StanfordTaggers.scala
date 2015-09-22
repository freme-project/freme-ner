package org.elinker.core.postag

import java.util

import com.hp.hpl.jena.ontology.{OntModel, Individual}
import edu.stanford.nlp.ling.{CoreLabel, CoreAnnotations}
import edu.stanford.nlp.pipeline.{Annotation, POSTaggerAnnotator}
import org.nlp2rdf.core.{RLOGSLF4JBinding, Span, Text2RDF, NIFParameters}
import org.nlp2rdf.core.vocab.{RLOGIndividuals, NIFDatatypeProperties}
import scala.collection.JavaConversions._

/**
 * Created by nilesh on 03/06/15.
 */
class StanfordTaggers(modelPaths: Map[String, String]) {
  val taggers = modelPaths.mapValues(modelPath => new StanfordPosTagWrapper(new POSTaggerAnnotator(modelPath, false)))

  def processText(context: Individual, inputModel: OntModel, outputModel: OntModel, nifParameters: NIFParameters, language: String): Unit = {
    taggers(language).processText(context, inputModel, outputModel, nifParameters)
  }
}
