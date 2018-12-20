package org.seanpquig.mini.search.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import org.rocksdb.RocksDB
import org.seanpquig.mini.search.Config

import scala.util.hashing.MurmurHash3

sealed trait Document {
  val id: String
}

case class TextDoc(
    text: String,
    title: Option[String] = None) extends Document {

  override val id: String = {
    val stringToHash = s"${title.getOrElse("")}^%&$text"
    val hash = MurmurHash3.stringHash(stringToHash)
    Math.abs(hash).toString
  }
}

// interface for the ability to add Documents to an object
trait DocumentAddable {
  def addDocs(docs: Seq[Document]): Unit = {
    for (doc <- docs) {
      addDoc(doc)
    }
  }

  def addDoc(doc: Document): Unit
}

/**
  * Key-value lookup that supports document retrieval by document ID.
  * It is backed by the persistent key-value store RocksDB.
  * @param docs initial documents to include in store
  */
case class DocumentStore(docs: Seq[TextDoc] = Seq()) extends DocumentAddable {
  import DocumentStore._

  // Add constructor documents
  addDocs(docs)

  def getDocs(ids: Iterable[String]): Iterable[TextDoc] = ids.flatMap(getDoc)

  def getDoc(id: String): Option[TextDoc] = {
    Option(db.get(id.getBytes)).map { bytes =>
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val doc = ois.readObject().asInstanceOf[TextDoc]
      ois.close()
      doc
    }
  }

  def addDoc(doc: Document): Unit = {
    val stream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(doc)
    oos.close()

    // put doc in RocksDB
    db.put(doc.id.getBytes, stream.toByteArray)
  }
}

object DocumentStore {
  // Setup RocksDB
  val db: RocksDB = RocksDB.open(s"${Config.dataDir}/doc_stores")
}
