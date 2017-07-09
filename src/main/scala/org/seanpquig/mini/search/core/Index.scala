package org.seanpquig.mini.search.core

case class Term(token: String) {
  def get: String = token
}

case class PostingsList(docsIds: Set[String])

class Index(name: String, termDictionary: Map[Term, PostingsList], docStore: DocumentStore) {

  def search(query: String): Seq[Document] = Seq()

}
