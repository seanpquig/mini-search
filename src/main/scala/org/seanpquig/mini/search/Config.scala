package org.seanpquig.mini.search

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load()

  val dataDir: String = config.getString("dataDir")
  val modelPath: String = config.getString("modelPath")
}
