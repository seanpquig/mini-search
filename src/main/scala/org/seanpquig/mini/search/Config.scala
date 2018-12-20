package org.seanpquig.mini.search

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  private val config: Config = ConfigFactory.load()

  val dataDir: String = config.getString("dataDir")
  val modelPath: String = config.getString("modelPath")
}
