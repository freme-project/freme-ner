package org.elinker.core.postag

import java.util.Properties

import com.hp.hpl.jena.ontology.{Individual, OntModel}
import com.jamonapi.MonitorFactory
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.nlp2rdf.cli.ParameterParser
import org.nlp2rdf.core._
import org.nlp2rdf.core.vocab.{NIFOntClasses, RLOGIndividuals}

import scala.collection.JavaConversions._

/**
 * Created by nilesh on 03/06/15.
 */
class StanfordTaggers(modelPaths: Map[String, String]) {
  val taggers = modelPaths.mapValues(modelPath => new StanfordPosTagWrapper(
    {
      val properties = new Properties()
      properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
      properties.setProperty("model", modelPath)
      new StanfordCoreNLP(properties)
//      new POSTaggerAnnotator(modelPath, false)
    }))

  def processText(context: Individual, inputModel: OntModel, outputModel: OntModel, nifParameters: NIFParameters, language: String): Unit = {
    taggers(language).processText(context, inputModel, outputModel, nifParameters)
  }

  def tagText(text: String, language: String): String = {
//    val options = new util.HashMap[String, util.List[String]]()
//    options.put("f", List("text"))
//    options.put("i", List(text))
    val args = Array("-f", "text",  "-i", text)
    val parser = ParameterParser.getParser(args, "http://cli.nlp2rdf.org/stanfordcore#")
    ParameterParser.addCLIParameter(parser)
    val options = ParameterParser.getOption(parser, args)
//    parser.parse(args)
    val nifParameters = ParameterParser.parseOptions(options, true)

    //customize
    val model = nifParameters.getInputModel
    val mon = MonitorFactory.getTimeMonitor(this.getClass.getCanonicalName).start()
    var x = 0
    for(context <- model.listIndividuals(NIFOntClasses.Context.getOntClass(model))) {
      processText(context, model, model, nifParameters, language)
      x += 1
    }

    val finalMessage = "Annotated " + x + " nif:Context(s)  in " + mon.stop().getTotal + " ms.  (avg.:" + mon.getAvg + ") producing " + model.size + " triples"
    model.add(RLOGSLF4JBinding.log(nifParameters.getLogPrefix, finalMessage, RLOGIndividuals.DEBUG, this.getClass.getCanonicalName, null, null))
    model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/")
    NIFNamespaces.addNifPrefix(model)
    model.setNsPrefix("olia", "http://purl.org/olia/olia.owl#")
    model.setNsPrefix("p", nifParameters.getPrefix)
    model.write(System.out, Format.toJena(nifParameters.getOutputFormat))
    "fweremwkr"
  }
}

object StanfordTaggers {
  def main(args: Array[String]): Unit = {
    val tagger = new StanfordTaggers(Map(("en", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger")))
    tagger.tagText("Hello, this is a sentence about Barack Obama visiting Berlin.", "en")
  }
}
