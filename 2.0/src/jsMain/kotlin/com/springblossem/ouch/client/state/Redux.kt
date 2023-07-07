package com.springblossem.ouch.client.state

import com.springblossem.ouch.client.state.reducers.AuthAction
import com.springblossem.ouch.client.state.reducers.ExistenceAction
import com.springblossem.ouch.client.state.reducers.authReducer
import com.springblossem.ouch.client.state.reducers.existenceReducer
import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.Existence
import com.springblossem.ouch.common.Quiddity
import react.createContext
import redux.RAction

val context = createContext(StateContext())
  .apply { displayName = "AppContext" }

data class AppState(
  var existence: Existence? = null,
  var self: Quiddity? = null,
  var auth: Auth? = null,
)

class StateContext(init: AppState = AppState()) {

  private var stateHistory: List<AppState> = emptyList()

  var state: AppState = init
    private set(value) {
      stateHistory += field
      field = value
    }

  fun reduce(action: RAction) {
    this.state = this.state.reducer(action)
  }

  operator fun component1(): AppState = state

  private fun AppState.reducer(action: RAction): AppState = when (action) {
    is ExistenceAction ->
      this.copy(existence = existenceReducer(this.existence, action))

    is AuthAction      -> this.copy(auth = authReducer(this.auth, action))
    else               -> this
  }
}


