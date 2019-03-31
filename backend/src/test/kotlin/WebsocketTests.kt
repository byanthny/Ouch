import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import org.junit.Before
import org.junit.Test

class WebsocketTests {

    lateinit var client: HttpClient
    private val endpoint = "localhost"
    private val port = 7_000

    @Before
    fun initClient() {
        client = HttpClient(CIO).config { install(WebSockets) }
    }

}
