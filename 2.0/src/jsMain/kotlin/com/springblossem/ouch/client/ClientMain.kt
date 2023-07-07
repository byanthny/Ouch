package com.springblossem.ouch.client

import com.springblossem.ouch.client.state.AppState
import com.springblossem.ouch.client.state.AuthContext
import com.springblossem.ouch.client.state.AuthContextProvider
import com.springblossem.ouch.client.state.context
import com.springblossem.ouch.common.Auth
import kotlinx.browser.document
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.p
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import kotlin.js.Date

fun main() {
  // setup react
  val container = document.getElementById("root")!!
  document.body!!.appendChild(container)
  val root = createRoot(container)
  println("root established")

  // render
  root.render(RootComponent.create())
}

val RootComponent = FC<Nothing> { +AuthContextProvider.create { +App.create() } }

val App = FC<Nothing> {
  var auth by useContext(AuthContext)

  p {
    +(auth?.id?.toString() ?: "")
  }

  button {
    +"Update"
    onClick = {
      println("Updating Auth")
      auth = Auth(Date.now().toInt(), "")
    }
  }

  // setup router
  BrowserRouter {
    Routes {
      Route {
        path = "/"
        element = Home.create { name = "Page1" }
      }
      Route {
        path = "/2"
        element = Home.create { name = "Page2" }
      }
    }
  }
}
