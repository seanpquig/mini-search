package org.seanpquig.mini.search.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.util.hashing.MurmurHash3

import com.typesafe.config.{Config, ConfigFactory}
import org.rocksdb.RocksDB

case class Document(
    text: String,
    title: Option[String] = None) {

  val id: String = {
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
case class DocumentStore(docs: Seq[Document] = Seq()) extends DocumentAddable {
  import DocumentStore._

  // Add constructor documents
  addDocs(docs)

  def getDocs(ids: Iterable[String]): Iterable[Document] = ids.flatMap(getDoc)

  def getDoc(id: String): Option[Document] = {
    Option(db.get(id.getBytes)).map { bytes =>
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val doc = ois.readObject().asInstanceOf[Document]
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
  val config: Config = ConfigFactory.load()
  val dataDir: String = config.getString("dataDir")

  // Setup RocksDB
  val db: RocksDB = RocksDB.open(s"$dataDir/doc_stores")
}
