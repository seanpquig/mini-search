package org.seanpquig.mini.search.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.util.Random

import com.typesafe.config.{Config, ConfigFactory}
import org.rocksdb.RocksDB

case class Document(
  text: String,
  id: String = Random.alphanumeric.take(16).mkString,
  title: Option[String] = None
)

case class DocumentStore(docs: Seq[Document]) {
  import DocumentStore._

  for (doc <- docs) {
    putDoc(doc)
  }

  private val docLookup: Map[String, Document] = docs.map(doc => (doc.id, doc)).toMap

  def getDocs(ids: Iterable[String]): Iterable[Document] = ids.flatMap(getDoc)

  def getDoc(id: String): Option[Document] = {
    Option(db.get(id.getBytes)).map { bytes =>
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val doc = ois.readObject().asInstanceOf[Document]
      ois.close()
      doc
    }
  }

  def putDoc(doc: Document): Unit = {
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
