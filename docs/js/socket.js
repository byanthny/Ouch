/* Handles all interactions with the server
 * through web sockets
 * socket.js
 */

var keepConnectionOpen =  setTimeout(checkOpen(), 3000); //900000

//TODO keep connection open, close after a certain amount of time and when connection is closed
function checkOpen() {
    console.log("I'm called");
    if(here) {
        console.log("sending heartbeat");
        connection.send(ping_packet);
        keepConnectionOpen;
    } else {
        //connection.close();
        console.log("I am not here");
        clearTimeout(keepConnectionOpen);
    }
};

//I am here
document.onmousemove = function(){here=true;};
document.onkeydown = function(){here=true;};

function play(endpoint) {

    connection = new WebSocket(endpoint);

    //On connection open
    connection.onopen = () => {
        switchState();
        indicator.classList.toggle("await");
        user_input.value = "";
        user_input.placeholder = "enter command or message";
        keepConnectionOpen;
    };

    //If  there is an  error
    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    //On message received from socket
    connection.onmessage = e => {

        //parse the socket data
        var JSONdata = JSON.parse(e.data);
        //parse JSONdata.data
        var parsedData = JSON.parse(JSONdata.data);

        //Check if datatype us INIT
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
            default:
                console.log("Unknown dataType");
                console.log(e.data);
                break;
        }
    };

    connection.onclose = (closeEvent) => {
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
                reconnect_button.onclick();
                break;
        }
        connection = null;
        here = false;
    }
}

//disconnect from server on page close or refresh
window.onbeforeunload = function () {
    connection.close();
}
