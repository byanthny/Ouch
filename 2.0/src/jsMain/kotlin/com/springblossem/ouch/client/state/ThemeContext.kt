package com.springblossem.ouch.client.state

import csstype.Color
import react.*

data class Colors(val primary: Color, val secondary: Color)

sealed interface Theme {

  val colors: Colors
}

object LightTheme : Theme {

  override val colors: Colors = Colors(Color("#b5f8ff"), Color("forestgreen"))
}

object DarkTheme : Theme {

  override val colors: Colors = Colors(Color("#333333"), Color("green"))
}

class ThemeContextContainer(private val themeDel: StateInstance<Theme>) {

  val theme: Theme get() = themeDel.component1()

  operator fun component1(): Theme = theme

  fun toggle() {
    var theme by themeDel
    theme = when (theme) {
      is LightTheme -> DarkTheme
      is DarkTheme  -> LightTheme
    }
  }
}

val ThemeContext = createContext<ThemeContextContainer>()
  .apply { displayName = "ThemeContext" }

val ThemeProvider = FC<PropsWithChildren> {
  val theme = useState<Theme>(LightTheme)

  +ThemeContext.Provider.create {
    value = ThemeContextContainer(theme)
    children = it.children
  }
}
