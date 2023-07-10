package com.springblossem.ouch.client

import com.springblossem.ouch.client.state.AuthContext
import com.springblossem.ouch.client.state.AuthContextProvider
import com.springblossem.ouch.common.Auth
import kotlinx.browser.document
import react.FC
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.p
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import react.useContext
import kotlin.random.Random

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
  val context = useContext(AuthContext)
  var auth by context.authDel

  p {
    +(auth?.id?.toString() ?: "")
  }

  button {
    +"Update"
    onClick = {
      println("Updating Auth")
      auth = Auth(Random.nextInt(), "")
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
