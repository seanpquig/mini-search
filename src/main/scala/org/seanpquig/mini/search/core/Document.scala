package org.seanpquig.mini.search.core

import scala.util.Random

case class Document(text: String, id: String = Random.nextString(16), titleOpt: Option[String] = None)

class DocumentStore(docs: Seq[Document]) {

  private val docLookup: Map[String, Document] = docs.map(doc => (doc.id, doc)).toMap

  def getDocs(ids: Seq[String]): Seq[Document] = {
    ids.map(docLookup)
  }

}
