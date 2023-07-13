package com.springblossem.ouch.client.pages

import com.springblossem.ouch.client.api.register
import com.springblossem.ouch.client.state.AuthContext
import com.springblossem.ouch.common.Failure
import com.springblossem.ouch.common.Success
import csstype.ClassName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.html.AutoComplete
import react.dom.html.ButtonType
import react.dom.html.InputHTMLAttributes
import react.dom.html.InputType.text
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input

@OptIn(DelicateCoroutinesApi::class)
val LoginPage = FC<Nothing> { _ ->
  var auth by useContext(AuthContext)
  var username by useState<String?>("username")
  var password by useState<String?>("password123")
  val submit = submit@{
    if (username == null || password == null) {
      println("no username and or password")
      return@submit
    }
    GlobalScope.launch {
      println("sending registration")
      when (val result = register(username!!, password!!)) {
        is Success -> auth = result.value
        is Failure -> TODO("display errors")
      }

    }
  }

  div {
    id = "commands"
    onSubmit = {
      it.preventDefault()
      submit()
    }
    form {
      +TextInput.create {
        id = "username-input"
        placeholder = "username"
        value = username
        onChange = { username = it.target.value }
      }
      +TextInput.create {
        id = "password-input"
        placeholder = "password"
        value = password
        onChange = { password = it.target.value }
      }
      button {
        id = "submit-button"
        type = ButtonType.submit
        className = ClassName("submit-button hidden")
        +"enter"
      }
    }
  }
}

external interface TextInputProps : Props, InputHTMLAttributes<HTMLInputElement>

val TextInput = FC<TextInputProps> { props ->
  //val (theme) = useContext(ThemeContext)

  input {
    id = props.id
    type = text
    autoComplete = props.autoComplete ?: AutoComplete.off
    value = props.value
    placeholder = props.placeholder
    onSubmit = props.onSubmit
    onChange = props.onChange
  }
}
