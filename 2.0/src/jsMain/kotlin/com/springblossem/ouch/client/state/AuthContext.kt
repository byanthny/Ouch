package com.springblossem.ouch.client.state

import com.springblossem.ouch.common.Auth
import react.*

val AuthContext = createContext<StateInstance<Auth?>>()
  .apply { displayName = "AuthContext" }

val AuthContextProvider = FC<PropsWithChildren> {
  val auth = useState<Auth>()

  +AuthContext.Provider.create {
    value = auth
    children = it.children
  }
}

