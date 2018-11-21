package org.seanpquig.mini.search

import org.seanpquig.mini.search.core.{Document, Index, IndexInfo, IndicesResponse}

/**
  * Entry point into all aspects and behavior of the search engine.
  * In general, client routes should be calling all functionality through this.
  */
object MiniSearch {

  private val testIndex = Index(
    name = "test-index",
    docs = List(
      Document("The cat in the hat jumped over the hill.", title = Option("The Cat in the Hat")),
      Document("Cute animals everywhere; cats, dogs, lambs."),
      Document("Are we over the proverbial quantitative easing hill?")
    )
  )

  private val indexLookup: Map[String, Index] = loadIndices()

  private def loadIndices(): Map[String, Index] = Map(testIndex.name -> testIndex)

  /**
    * Search an index by name. None result implies that index does not exist.
    * @param idxName
    * @param query
    * @return optional result documents
    */
  def search(idxName: String, query: String): Option[Iterable[Document]] = {
    indexLookup.get(idxName).map(_.search(query))
  }

  def index(idxName: String, docs: Iterable[Document]) = ???

  def indicesResponse(): IndicesResponse = IndicesResponse(
    indexLookup.map { case (_, idx) =>
      IndexInfo(name = idx.name, idx.docs.length)
    }
  )

}
