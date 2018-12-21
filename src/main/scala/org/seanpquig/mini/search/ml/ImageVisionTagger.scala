package org.seanpquig.mini.search.ml

import java.io.File

import org.datavec.image.loader.ImageLoader
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.seanpquig.mini.search.Config
import spray.json._

import scala.io.Source


case class ImagenetClass(name: String, description: String)
case class ImagenetPrediction(name: String, description: String, prob: Double)


class ImageVisionTagger(modelPath: String) extends DefaultJsonProtocol {

  private val model: ComputationGraph = KerasModelImport.importKerasModelAndWeights(
    modelPath,
    Array(299, 299, 3),
    false
  )

  val imagenetClasses: List[ImagenetClass] = loadImagenetClasses(Config.imagenetClassPath)

  def loadImagenetClasses(filePath: String): List[ImagenetClass] = {
    val bufferedSource = Source.fromFile(Config.imagenetClassPath)
    val jsonStr = bufferedSource.mkString
    bufferedSource.close()

    val rawData = jsonStr.parseJson.convertTo[Map[String, List[String]]]
    rawData.toList.sortBy(_._1).map { case (_, l) =>
      ImagenetClass(name = l.head, description = l(1))
    }
  }

  def imageToTags(imgPath: String): Array[ImagenetPrediction] = {
    val imgLoader = new ImageLoader(299, 299, 3)
    val imgArray = imgLoader.asImageMatrix(new File(imgPath)).getImage

    predictTags(imgArray)
  }

  def predictTags(imgArray: INDArray): Array[ImagenetPrediction] = {
    val singleImgDataset = Nd4j.repeat(imgArray, 1)
    val preds = model.outputSingle(singleImgDataset).toDoubleVector

    preds.zip(imagenetClasses)
      .map { case (p, cls) => ImagenetPrediction(cls.name, cls.description, p) }
      .sortWith(_.prob > _.prob)
  }

}
