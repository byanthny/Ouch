package com.sim.ouch.web

import kotlinx.html.*
import kotlinx.html.stream.createHTML

enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map")
}

val ENDPONT_MAP = createHTML().apply {
    body {
        div {
            table {
                tr {
                    th { text("Endpoint") }
                    th { text("Method") }
                    th { text("Description") }
                    th { text("JSON") }
                }
                tr {
                    td { text("/") }
                    td { text("GET") }
                    td { text("redirects to ouch site") }
                    td { text("NA") }
                }
                tr {
                    td { text("/actions") }
                    td { text("GET") }
                    td { text("get a list of all actions") }
                    td {
                        code {
                            lang = "json"
                            text("[\"ACTION_NAME\", \"ACTION_NAME_2\",...]")
                        }
                    }
                }
                tr {
                    td { text("/status") }
                    td { text("GET") }
                    td { text("get a JSON summery of the state of ouch API") }
                    td {
                        code {
                            lang = "JSON"
                            text("""{ "dataType": "INTERNAL", "data": "{\"ex\":[],\"ses\":0}" }""")
                        }
                    }
                }
                tr {
                    td { text("/ws") }
                    td { text("WebSocket") }
                    td { text("connect to websocket") }
                }
            }
        }
    }
}.finalize()
