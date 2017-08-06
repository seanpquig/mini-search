package org.seanpquig.mini.search.core

import scala.util.Random

case class Document(
  text: String,
  id: String = Random.alphanumeric.take(16).mkString,
  title: Option[String] = None
)

class DocumentStore(docs: Seq[Document]) {

  private val docLookup: Map[String, Document] = docs.map(doc => (doc.id, doc)).toMap

  def getDocs(ids: Iterable[String]): Iterable[Document] = ids.map(docLookup)

  def getDoc(id: String): Document = docLookup(id)

}
