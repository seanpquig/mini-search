package org.seanpquig.mini.search

import org.seanpquig.mini.search.core.{Document, Index}

/**
  * Entry point into all aspects and behavior of the search engine.
  * In general, client routes should be calling all functionality through this.
  */
object MiniSearch {

  private val indexLookup: Map[String, Index] = loadIndices

  private def loadIndices: Map[String, Index] = Map()

  def search(idxName: String, query: String): Seq[Document] = indexLookup(idxName).search(query)

}
