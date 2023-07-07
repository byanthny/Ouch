package com.springblossem.ouch.client.state.reducers

import com.springblossem.ouch.common.Existence
import redux.RAction

sealed interface ExistenceAction : RAction

fun existenceReducer(existence: Existence? = null, action: ExistenceAction): Existence? =
  when (action) {
    else -> existence
  }
