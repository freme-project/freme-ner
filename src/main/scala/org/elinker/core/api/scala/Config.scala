package org.elinker.core.api.scala

/**
 * Config class for initializing an FremeNER instance.
 *
 * Created by nilesh on 12/10/15.
 *
 * @param languages Array of ISO language codes (eg. en, de, fr)
 * @param modelsLocation Directory where ner models are loaded
 * @param solrURI SOLR URI for the entity linker
 * @param mysqlURI MySQL URI for managing dataset metadata
 */
class Config(val languages: Array[String], val modelsLocation: String, val solrURI: String, val mysqlURI: String) {
  val modelFiles = for(lang <- languages) yield (lang, modelsLocation + "wikiner-" + lang + "-ner-model.ser.gz")
}
