package org.seanpquig.mini.search.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import org.rocksdb.RocksDB
import org.seanpquig.mini.search.Config
import org.seanpquig.mini.search.core.analyzers.{Analyzer, AnalyzerPipeline, StandardAnalyzer}

case class Term(token: String)

// Class backed by underlying Set of Document Ids
case class PostingsList(docsIds: Set[String] = Set()) {
  def ++(other: PostingsList): PostingsList = PostingsList(docsIds ++ other.docsIds)
}

/**
  * Represents a search index that powers full-text search and document indexing and retrieval
  * @param name index name
  * @param docs initial documents to include in index
  * @param analyzers sequence of analyzers to apply to text in queries and document indexing
  */
case class Index(
    name: String,
    docs: Seq[Document],
    analyzers: Seq[Analyzer] = Seq(StandardAnalyzer())) extends DocumentAddable {

  private val docStore: DocumentStore = DocumentStore()
  private val analyzerPipeline = AnalyzerPipeline(analyzers)
  private val termDictionary = TermDictionary(analyzerPipeline = analyzerPipeline)

  // Add constructor documents
  addDocs(docs)

  def search(query: String): Iterable[TextDoc] = {
    val queryTerms = analyzerPipeline.analyze(query)
    val postings = termDictionary.getPostings(queryTerms)
    docStore.getDocs(postings.docsIds)
  }

  def addDoc(doc: Document): Unit = {
    // add to document store
    docStore.addDoc(doc)

    // update term dictionary
    termDictionary.addDoc(doc)
  }
}

/**
  * Data Structure that supports retrieval of PostingsLists of matching document IDs given a search Term.
  * It is backed by the persistent key-value store RocksDB.
  * @param docs initial documents to include in dictionary
  * @param analyzerPipeline pipeline of analyzers to apply during document addition
  */
case class TermDictionary(
    docs: Seq[Document] = Seq(),
    analyzerPipeline: AnalyzerPipeline) extends DocumentAddable {
  import TermDictionary._

  // Add constructor documents
  addDocs(docs)

  def getPostings(terms: Seq[Term]): PostingsList = {
    terms match {
      case Nil => PostingsList()
      case _ => terms.map(getPostings).reduce(_ ++ _)
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

  def addDoc(doc: Document): Unit = {
    val termPostingsMap: Map[Term, PostingsList] = (doc match {
      case TextDoc(text, _) => analyzerPipeline.analyze(text)
    }).map(term => (term, doc.id))
      .groupBy(_._1)
      .map { case (term, termIdPairs) =>
        term -> PostingsList(termIdPairs.map(_._2).toSet)
      }

    for ((term, postings) <- termPostingsMap) {
      val priorPostings = getPostings(term)
      putPostings(term, priorPostings ++ postings)
    }
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
  // Setup RocksDB
  val db: RocksDB = RocksDB.open(s"${Config.dataDir}/term_dictionaries")
}
