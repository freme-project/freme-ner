package org.elinker.core.api.scala

import eu.freme.common.persistence.dao.DatasetSimpleDAO

/**
 * Config class for initializing an FremeNER instance.
 *
 * Created by nilesh on 12/10/15.
 *
 * @param languages Array of ISO language codes (eg. en, de, fr)
 * @param modelsLocation Directory where ner models are loaded
 * @param solrURI SOLR URI for the entity linker
 * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
 * @param datasetDAO Spring DAO for managing datasets
 */
class Config(val languages: Array[String],
             val modelsLocation: String,
             val solrURI: String,
             val domainsFile: String,
             val datasetDAO: DatasetSimpleDAO) {
  println(languages.mkString("\n"))
  val modelFiles = for(lang <- languages) yield {
    if(lang == "en") (lang, "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz") else
    (lang, modelsLocation + "wikiner-" + lang + "-ner-model.ser.gz")
  }
}
