/* Handles all interactions with the server
 * through web sockets
 * socket.js
 */
/*
var keepConnectionOpen;//30000

//TODO keep connection open, close after a certain amount of time and when connection is closed
function checkOpen() {
    console.log("I'm called");
    if(here) {
        console.log("sending heartbeat");
        this.connection.send("");
        keepConnectionOpen = setTimeout(checkOpen(), 30000);
    } else {
        //connection.close();
        console.log("I am not here");
        clearTimeout(keepConnectionOpen);
    }
};
*/
function heartbeat() {
    if (!connection) return;
    if (connection.readyState !== 1) return;
    if(!here) return;
    connection.send(ping_packet);
    setTimeout(heartbeat, 29000);
}

//TODO detect inactivity
//TODO speed up animations
function inactivityTime() {

    window.onload = resetTimer;
    // DOM Events
    document.onmousemove = resetTimer;
    document.onkeypress = resetTimer;

}

function logout() {
    console.log("BYE");
    here = false;
    usr_disconnected  = true;
    connection.close();
    //location.href = 'logout.html'
}

function resetTimer() {
    clearTimeout(time);
    here = true;
    time = setTimeout(logout, 30000)
    // 1000 milliseconds = 1 second
}

function play(endpoint) {
    inactivityTime();
    connection = new WebSocket(endpoint);

    //On connection open
    connection.onopen = function() {

        switchState();
        switchLoading(true);
        indicator.classList.toggle("await");
        user_input.value = "";
        user_input.placeholder = "connecting to the Existence...";
        heartbeat();
       // keepConnectionOpen =  setTimeout((here ? isHere() : isNotHere()),30000);
    };

    //If  there is an  error
    connection.onerror = function(error) {
        //create error message
        alert('WebSocket error:'+error);
    };

    //On message received from socket
    connection.onmessage = function(e) {

        //parse the socket data
        var JSONdata = JSON.parse(e.data);
        //parse JSONdata.data
        var parsedData = JSON.parse(JSONdata.data);

        //Check if datatype is INIT
        switch (JSONdata.dataType) {
            case "INIT":
                handleInit(parsedData);
                break;
            case "CHAT": //if message is from current user
                if (parsedData.authorName === nickname) {
                    addChat("", parsedData.content, "user");
                    scrollBottom();
                }
                //if message is from new user
                else {
                    addChat(parsedData.authorName, parsedData.content, "other");
                    scrollBottom();
                }
                //New user has entered
                break;
            case "ENTER": //Add new user to leaderboard
                leaderboard.innerHTML += '<div class="data-leaderboard-cont"><p class="data-leaderboard ' + parsedData.id + '">' + parsedData.name + ' <span class="normal">' + parsedData.ouch.degree + '</span></p></div>';

                addChat(parsedData.name, "has joined the Existence.", "system");
                scrollBottom();
                break;
            case "EXIT":
                var quidleaderboard = document.getElementsByClassName(parsedData.id)[0]
                quidleaderboard.parentNode.removeChild(quidleaderboard);
                addChat(parsedData.name, "has left the Existence.", "system");
                scrollBottom();
                break;
            case "PING":
                break;
            default:
                console.log("Unknown dataType");
                console.log(e.data);
                break;
        }
    };

    connection.onclose = function(closeEvent) {
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
                if(!usr_disconnected) {
                    reconnect_button.onclick();
                    break;
                }
                usr_disconnected = false;
        }
       // connection;
        here = false;
    }
}

//disconnect from server on page close or refresh
document.onbeforeunload = function () {
    connection.close();
};

document.onload = function () {
    loadActions();
};
