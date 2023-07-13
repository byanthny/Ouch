package com.springblossem.ouch.client

import com.springblossem.ouch.client.pages.LoginPage
import com.springblossem.ouch.client.state.AuthContextProvider
import com.springblossem.ouch.client.state.ThemeProvider
import kotlinx.browser.document
import react.FC
import react.create
import react.dom.client.createRoot
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter

fun main() {
  // setup react
  val container = document.getElementById("root")!!
  document.body!!.appendChild(container)
  val root = createRoot(container)
  root.render(RootComponent.create())
}

val RootComponent = FC<Nothing> {
  AuthContextProvider {
    +ThemeProvider.create {
      +HashRouter.create {
        Routes {
          Route {
            path = "/"
            element = LoginPage.create { }
          }
        }
      }
    }
  }
}
