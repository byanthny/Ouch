package com.springblossem.ouch.common

import com.springblossem.ouch.common.api.ExistenceInfo
import com.springblossem.ouch.common.api.UserInfo
import com.springblossem.ouch.common.api.WsMissive
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val JsonConfig = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
  encodeDefaults = true
  serializersModule = SerializersModule {
    polymorphic(WsMissive::class) {
      subclass(UserInfo.RequestOwn::class)
      subclass(UserInfo.Response::class)
      subclass(ExistenceInfo.Request::class)
      subclass(ExistenceInfo.RequestOwn::class)
      subclass(ExistenceInfo.Response::class)
      subclass(ExistenceInfo.ResponseOwn::class)
    }
  }
}
