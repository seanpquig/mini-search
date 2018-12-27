package org.seanpquig.mini.search.ml

import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.nd4j.linalg.api.ndarray.INDArray
import org.seanpquig.mini.search.Config
import spray.json._

import scala.io.Source


case class ImagenetClass(name: String, description: String)

case class ImagenetPrediction(name: String, description: String, prob: Double)

class InceptionVisionTagger(override val modelPath: String) extends VisionTagger {
  override def imgWidth: Int = 299
  override def imgHeight: Int = imgWidth
}

class XceptionVisionTagger(override val modelPath: String) extends VisionTagger {
  override def imgWidth = 299
  override def imgHeight = imgWidth
}

class NASNetVisionTagger(override val modelPath: String) extends VisionTagger {
  override def imgWidth = 331
  override def imgHeight = imgWidth
}

class NASNetMobileTagger(override val modelPath: String) extends VisionTagger {
  override def imgWidth = 224
  override def imgHeight = imgWidth
}

trait VisionTagger extends DefaultJsonProtocol {
  def modelPath: String

  def imgWidth: Int
  def imgHeight: Int

  private val graph: ComputationGraph = KerasModelImport.importKerasModelAndWeights(
    modelPath, Array(imgWidth, imgHeight, 3), false
  )

  val imagenetClasses: List[ImagenetClass] = loadImagenetClasses(Config.imagenetClassPath)

  private def loadImagenetClasses(filePath: String): List[ImagenetClass] = {
    val bufferedSource = Source.fromFile(Config.imagenetClassPath)
    val jsonStr = bufferedSource.mkString
    bufferedSource.close()

    val rawData = jsonStr.parseJson.convertTo[Map[String, List[String]]]
    rawData.toList.map { case (k, v) => (k.toInt, v) }
      .sortBy(_._1)
      .map { case (_, l) => ImagenetClass(name = l(0), description = l(1))}
  }

  def preProcessImage(img: INDArray): INDArray = img.div(127.5).sub(1)

  def imageToTags(imgPath: String): Array[ImagenetPrediction] = {
    val imgLoader = new NativeImageLoader(299, 299, 3)
    val img = imgLoader.asMatrix(imgPath)
    val preProcess = preProcessImage(img)

    predictTags(preProcess)
  }

  def predictTags(img: INDArray): Array[ImagenetPrediction] = {
    val preds = graph.outputSingle(img).toDoubleVector

    preds.zip(imagenetClasses)
      .map { case (p, cls) => ImagenetPrediction(cls.name, cls.description, p) }
      .filter(_.prob > 0.01)
      .sortWith(_.prob > _.prob)
  }
}
