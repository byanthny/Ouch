package com.springblossem.ouch.common

import kotlinx.serialization.Serializable

@Serializable
enum class Commands(val description: String) {
  BEAN("get beaned"),
  MUSIC("feel the ouchie vibes"),
  THEME("darkmode best mode"),
  EXIT("disconnect from the Existence"),
}

val Commands.invokation get() = "-${name}".lowercase()
