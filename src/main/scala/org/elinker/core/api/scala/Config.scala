package org.elinker.core.api.scala

/**
 * Config class for initializing an FremeNER instance.
 *
 * Created by nilesh on 12/10/15.
 *
 * @param languages Array of ISO language codes (eg. en, de, fr)
 * @param modelsLocation Directory where ner models are loaded
 * @param solrURI SOLR URI for the entity linker
 * @param databaseUri Database connection URI for managing dataset metadata
 * @param dbpediaInstanceTypesFile Location of dbpedia instance types transitive dataset
 * @param domainsFile CSV file with first column having domain name, rest of the columns with corresponding types
 */
class Config(val languages: Array[String],
             val modelsLocation: String,
             val solrURI: String,
             val databaseUri: String,
             val dbpediaInstanceTypesFile: String,
             val domainsFile: String) {
  val modelFiles = for(lang <- languages) yield {
    if(lang == "en") (lang, "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz") else
    (lang, modelsLocation + "wikiner-" + lang + "-ner-model.ser.gz")
  }
}
