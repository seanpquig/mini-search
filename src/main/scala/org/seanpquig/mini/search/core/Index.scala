package org.seanpquig.mini.search.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.typesafe.config.{Config, ConfigFactory}
import org.rocksdb.RocksDB

case class Term(token: String)

case class PostingsList(docsIds: Set[String] = Set()) {
  def ++(other: PostingsList): PostingsList = PostingsList(docsIds ++ other.docsIds)
}

case class Index(name: String, docs: Seq[Document]) {

  private val docStore: DocumentStore = DocumentStore(docs)
  private val termDictionary = TermDictionary(docs)

  def search(query: String): Iterable[Document] = {
    val queryTerm = Term(query)
    val postings = termDictionary.getPostings(queryTerm)
    docStore.getDocs(postings.docsIds)
  }

  def addDoc(doc: Document): Unit = {
    // add to document store
    docStore.putDoc(doc)

    // update term dictionary
    termDictionary.addDoc(doc)
  }

}

case class TermDictionary(docs: Seq[Document]) {
  import TermDictionary._

  for (doc <- docs) {
    addDoc(doc)
  }

  def addDoc(doc: Document): Unit = {
    val termPostingsMap = doc.text.split("\\s+").map(token => (Term(token), doc.id))
      .groupBy(_._1)
      .map { case (term, pairs) =>
        term -> PostingsList(pairs.map(_._2).toSet)
      }

    for ((term, postings) <- termPostingsMap) {
      val priorPostings = getPostings(term)
      putPostings(term, priorPostings ++ postings)
    }
  }

  def getPostings(term: Term): PostingsList = {
    Option(db.get(term.token.getBytes)).map { bytes =>
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val postings = ois.readObject().asInstanceOf[PostingsList]
      ois.close()
      postings
    }.getOrElse(PostingsList())
  }

  def putPostings(term: Term, postings: PostingsList): Unit = {
    val stream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(postings)
    oos.close()

    // put postings in RocksDB
    db.put(term.token.getBytes, stream.toByteArray)
  }

}

object TermDictionary {
  val config: Config = ConfigFactory.load()
  val dataDir: String = config.getString("dataDir")

  // Setup RocksDB
  val db: RocksDB = RocksDB.open(s"$dataDir/term_dictionaries")
}
