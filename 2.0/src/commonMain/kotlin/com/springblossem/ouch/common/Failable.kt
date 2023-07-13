package com.springblossem.ouch.common

sealed interface Failable<T> {

  val value: T?
  val success: Boolean
}

val <T> Failable<T>.failed: Boolean get() = !success

data class Success<T>(override val value: T) : Failable<T> {

  override val success: Boolean = true
}

data class Failure<T>(val message: String? = null) : Failable<T> {

  override val value: T? = null
  override val success: Boolean = false
}
