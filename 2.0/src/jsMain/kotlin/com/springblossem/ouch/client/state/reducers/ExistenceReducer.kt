package com.springblossem.ouch.client.state.reducers

import com.springblossem.ouch.common.Existence

sealed interface ExistenceAction

fun existenceReducer(existence: Existence? = null, action: ExistenceAction): Existence? =
  when (action) {
    else -> existence
  }
