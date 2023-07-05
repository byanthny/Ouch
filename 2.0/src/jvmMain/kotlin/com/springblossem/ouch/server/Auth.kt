package com.springblossem.ouch.server

import at.favre.lib.crypto.bcrypt.BCrypt

fun String.encrypt(): Result<String> =
  runCatching { BCrypt.withDefaults().hashToString(12, toCharArray()) }

fun verify(incoming: String, hash: String): Result<Boolean> =
  runCatching {
    BCrypt
      .verifyer()
      .verify(incoming.toCharArray(), hash.toCharArray())
      .verified
  }
