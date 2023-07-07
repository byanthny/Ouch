package com.springblossem.ouch.client.state.reducers

import com.springblossem.ouch.common.Auth
import redux.RAction

sealed interface AuthAction : RAction

data class UpdateId(val id: Int): AuthAction

fun authReducer(auth: Auth? = null, action: AuthAction): Auth? =
  when (action) {
    is UpdateId -> auth?.copy(action.id)
    else -> auth
  }
