package com.springblossem.ouch.common.api

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.Existence
import com.springblossem.ouch.common.Quiddity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface WsMissive

@Serializable
sealed interface WsRequest

@Serializable
sealed interface WsResponse

@Serializable
sealed interface UserInfo : WsMissive {

  @Serializable
  @SerialName("userinfo_request_own")
  object RequestOwn : UserInfo, WsRequest

  @Serializable
  data class Response(val auth: Auth) : UserInfo, WsResponse
}

/**
 * Requests to
 */
@Serializable
sealed interface ExistenceInfo : WsMissive {

  @Serializable
  data class Request(val id: Int) : ExistenceInfo, WsRequest

  @Serializable
  data class Response(val existence: Existence) : ExistenceInfo, WsResponse

  @Serializable
  @SerialName("existence_request_own")
  object RequestOwn : ExistenceInfo, WsRequest

  @Serializable
  data class ResponseOwn(
    val existences: Set<Existence>,
    val quiddities: Set<Quiddity>
  ) : ExistenceInfo, WsResponse
}


