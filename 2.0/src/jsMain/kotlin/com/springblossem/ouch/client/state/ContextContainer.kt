package com.springblossem.ouch.client.state

//data class ContextContainer<T>(val state: StateInstance<T>)

/*

data class ContextContainer(
  val authDel: StateInstance<Auth?>,
  val boolDel: StateInstance<Boolean>
)

val AuthContext = createContext<ContextContainer>()

val AuthContextProvider = FC<PropsWithChildren> {
  val auth = useState<Auth>()
  val beans = useState(false)

  +AuthContext.Provider.create {
    value = ContextContainer(auth, beans)
    children = it.children
  }
}


 */
