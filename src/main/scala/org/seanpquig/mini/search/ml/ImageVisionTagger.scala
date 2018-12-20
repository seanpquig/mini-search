package org.seanpquig.mini.search.ml

import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.nd4j.linalg.api.ndarray.INDArray

class ImageVisionTagger(modelPath: String) {

  private val model: ComputationGraph = KerasModelImport.importKerasModelAndWeights(modelPath)

  def predictTags(imgArray: INDArray): Array[Double] = {
    model.output(imgArray).flatMap(_.toDoubleVector)
  }

}
