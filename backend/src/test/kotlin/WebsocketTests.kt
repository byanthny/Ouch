import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import org.junit.Before
import org.junit.Test

@KtorExperimentalAPI
class WebsocketTests {

    lateinit var client: HttpClient
    private val endpoint = "localhost"
    private val port = 7_000

    @Before
    @UnstableDefault
    @ImplicitReflectionSerializer
    fun initClient() {
        client = HttpClient(CIO).config { install(WebSockets) }
    }

    @Test
    @ImplicitReflectionSerializer
    fun connect() = runBlocking {
        client.webSocket(
            HttpMethod.Get,
            "127.0.0.1",
            System.getenv("PORT")?.toIntOrNull() ?: 7_000,
            "/ws",
            { parameter("name", "test_name") }
        ) {
            when (val frame = incoming.receive()) {
                is Frame.Text -> {
                    println(frame.readText())
                    return@webSocket
                }
            }
        }
    }
}
