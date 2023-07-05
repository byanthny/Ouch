package com.springblossem.ouch.common

import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@Serializable
object ApplicationInfo {

  data class Author(val name: String, val url: String)

  const val version = "2.0.0"
  const val site = "https://anthnyd.github.io/Ouch"
  val jono = Author("Jonathan Augustine", "https://JonoAugustine.com")
  val anthony = Author("Anthony Das", "https://github.com/anthnyd")
}

@Serializable
object Configuration {

  /** The duration after last activity at which an [Existence] becomes dormant */
  val DORMANT_AGE = 31.days
}
