/* Handles all interactions with the server
 * through web sockets
 * As well as and other interactions with websites/servers
 * socket.js
 */

//Web socket

/* Ping-Pong with server */
function heartbeat() {
    if (!connection) return;
    if (connection.readyState !== 1) return;
    if (!here) return;
    connection.send(ping_packet);
    setTimeout(heartbeat, 29000);
}

/* Handles communication to and from the server
 * As well as connection and disconnects
 * @param url to connect to
 */
function createConnection(endpoint) {
    switchLoading(true);
    inactivityTime();
    connection = new WebSocket(endpoint);

    //On connection open
    connection.onopen = function () {
        switchState();
        indicator.classList.toggle("await");
        world_value.innerHTML = "connecting";
        user_input.value = "";
        user_input.placeholder = "connecting to the Existence...";
        heartbeat();
        // keepConnectionOpen =  setTimeout((here ? isHere() : isNotHere()),30000);
    };

    //If  there is an  error
    connection.onerror = function (error) {
        //create error message
        alert('WebSocket error:' + error);
    };

    //On message received from socket
    connection.onmessage = function (e) {

        //parse the socket data
        var JSONdata = JSON.parse(e.data);
        //parse JSONdata.data
        var parsedData = JSON.parse(JSONdata.data);

        //Check if datatype is INIT
        switch (JSONdata.dataType) {
            case "INIT": //Init
                handleInit(parsedData);
                break;
            case "CHAT": //Chat message
                    addChat(parsedData.authorName, parsedData.content, "client");
                    scrollBottom();
                break;
            case "ENTER": //New user has entered
                //Add new user to leaderboard
                leaderboard.innerHTML += '<div class="data-leaderboard-cont"><p class="data-leaderboard '
                    + parsedData.id + '">' + parsedData.name + ' <span class="normal">'
                    + parsedData.ouch.degree + '</span></p></div>';
                //Announce user joined
                addChat(parsedData.name, "has joined the Existence.", "system");
                scrollBottom();
                break;
            case "EXIT": //New user has left
                //Remove user leaderboard item
                var quidleaderboard = document.getElementsByClassName(parsedData.id)[0];
                quidleaderboard.parentNode.removeChild(quidleaderboard);
                //Announce user left
                addChat(parsedData.name, "has left the Existence.", "system");
                scrollBottom();
                break;
            case "PING": //Ping
                break;
            default: //Other
                console.log("Unknown dataType");
                console.log(e.data);
                break;
        }
    };

    //On connection close
    connection.onclose = function (closeEvent) {
        //switch back to login
        reset();
        switch (closeEvent.code) {
            case close_code.ER_NO_NAME:
                shake(user_input);
                break;
            case close_code.ER_BAD_ID:
                exist_input.value = "";
                exist_input.placeholder = "Unknown ID";
                user_input.value = nickname;
                shake(exist_input);
                break;
            case close_code.ER_INTERNAL_GENERIC:
                alert("Sorry, an internal error occurred. Please try again");
                break;
            case close_code.ER_BAD_TOKEN:
                reconnect_token = null;
                break;
            default:
                if (!usr_disconnected) {
                    reconnect_button.onclick();
                    break;
                }
                usr_disconnected = false;
        }
        here = false;
    }
}

/* Retrieve JSON data from website
* @param url to retrieve JSON from
*/
function getHTTP(url) {

    fetch(url, {method: 'GET',  mode: 'cors'}).then(function (resp) {
       /* console.log(resp);
        resp.json(); // Transform the data into json
        console.log(resp.json());
    }).then(function (data) {
        return data; //return JSON data*/
    var jsonData = resp.json(); // there's always a body
    if (resp.status >= 200 && resp.status < 300) {
        console.log(resp);
        return jsonData;
    } else {
        console.log(jsonData.then(Promise.reject.bind(Promise)));
        return jsonData.then(Promise.reject.bind(Promise));
    }
    });
}

/* Load in actions from actions_url */
function loadActions() {
    var actions_data = getHTTP(url_actions);
    if (actions_data === "") {
        console.log("Error");
    } else {
        console.log(actions_data);
        actions = JSON.parse(actions_data);
        console.log(actions);
    }
}
