package org.seanpquig.mini.search.core.analyzers

import org.seanpquig.mini.search.core.Term

/**
  * High-level interface for all analyzers, which take a
  * String of text and convert them to a sequence of terms.
  */
trait Analyzer {
  val name: String
  def analyze(text: String): Seq[Term]
}

// useful methods and data to be used by various analyzers
object AnalyzerUtil {
  val defaultStopWords = Set("a", "an", "and", "are", "in", "the")
}

/**
  * Basic Analyzer that lowercases text, strips punctuation, splits on whitespace, and filters stop words
  * @param stopWords
  */
case class StandardAnalyzer(stopWords: Set[String] = AnalyzerUtil.defaultStopWords) extends Analyzer {
  val name = "StandardAnalyzer"

  def analyze(text: String): Seq[Term] = {
    val lowercaseText = text.toLowerCase
    val strippedPunctText = lowercaseText.replaceAll("\\p{Punct}" , "")
    val terms = strippedPunctText.split("\\s+").map(Term)
    terms.filter(t => !stopWords.contains(t.token))
  }
}

/**
  * Pipeline that allows you to pass text through multiple analyzers
  * @param analyzers
  */
case class AnalyzerPipeline(analyzers: Seq[Analyzer]) {

  /**
    * Pass text through all analyzers in the pipeline
    * @param text string to analyze
    * @return tokens
    */
  def analyze(text: String): Seq[Term] = {
    analyzers.foldLeft(Seq(Term(text)))(
      (terms, analyzer) => terms.flatMap(t => analyzer.analyze(t.token))
    )
  }
}
