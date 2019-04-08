import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import org.junit.Before

class WebsocketTests {

    lateinit var client: HttpClient
    private val endpoint = "localhost"
    private val port = 7_000

    @Before
    fun initClient() {
        client = HttpClient(CIO).config { install(WebSockets) }
    }
}
