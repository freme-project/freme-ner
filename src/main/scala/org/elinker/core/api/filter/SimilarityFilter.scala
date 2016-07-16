package org.elinker.core.api.filter

import com.rockymadden.stringmetric.similarity.JaroWinklerMetric
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.elinker.core.api.process.Result
import scala.collection.JavaConversions._


class SimilarityFilter(solr: HttpSolrClient) {

  val threshold = 0.95

  val SURFACE_FORM_SIMILARITY = "SurfaceFormSimilarity1"


  /**
    * Remove false-positive entities using SOLR index and string similarity
    *
    * @param entity   - entity
    * @param datasets  - SOLR dataset
    * @param language - i18n language
    * @return
    */
  def filterByStringSimilarity(entity: Result, datasets: String, language: String): Boolean = {

    val query = new SolrQuery()
    val taIdentRef = entity.taIdentRef.getOrElse("")
    val datasetInClause = datasets.replace(",", "\" \"")

    query.set("q", s"""dataset:("$datasetInClause") AND resource:"$taIdentRef" AND (language:"$language" OR language:"xx")""")
    query.set("sort", "score desc, count desc")

    val response = solr.query("elinker", query)
    response.getResults.exists(x => computeSimilarity(entity.mention, x.get("label").toString))
  }

  /**
    * Compute string similarity using JaroWinkler Metric
    *
    * @param surfaceFormA Entity mention
    * @param surfaceFormB Solr label
    * @return
    */
  private def computeSimilarity(surfaceFormA: String, surfaceFormB: String): Boolean = {
    if (surfaceFormA.isEmpty || surfaceFormB.isEmpty) return false
    JaroWinklerMetric.compare(surfaceFormA, surfaceFormB).get > threshold
  }

}