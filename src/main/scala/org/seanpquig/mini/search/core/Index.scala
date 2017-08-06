package org.seanpquig.mini.search.core

case class Term(token: String) {
  def get: String = token
}

case class PostingsList(docsIds: Set[String])

case class Index(name: String, docs: Seq[Document]) {

  private val docStore: DocumentStore = new DocumentStore(docs)

  // build term dictionary from documents
  private val termDictionary: Map[Term, PostingsList] = {
    docs.flatMap(doc =>
      doc.text.split("\\s+")
        .map(token => (Term(token), doc.id))
    )
    .groupBy(_._1)
    .map { case (term, pairs) =>
      term -> PostingsList(pairs.map(_._2).toSet)
    }
  }

  def search(query: String): Iterable[Document] = {
    val queryTerm = Term(query)
    val hitIds = termDictionary.get(queryTerm).map(_.docsIds).getOrElse(Set())
    docStore.getDocs(hitIds)
  }

}
