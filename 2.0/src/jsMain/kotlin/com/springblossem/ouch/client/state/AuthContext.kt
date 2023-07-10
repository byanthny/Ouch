package com.springblossem.ouch.client.state

import com.springblossem.ouch.common.Auth
import react.*

// sealed class AContextContainer<T>(val reducer: Reducer<T, Any>)

data class ContextContainer(
  val authDel: StateInstance<Auth?>,
  val boolDel: StateInstance<Boolean>
)
//private var stateHistory: List<AppState> = emptyList()

val AuthContext = createContext<ContextContainer>()



val AuthContextProvider = FC<PropsWithChildren> {
  val auth = useState<Auth>()
  val beans = useState(false)

  +AuthContext.Provider.create {
    value = ContextContainer(auth, beans)
    children = it.children
  }
}

