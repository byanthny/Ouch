var nickname = 'user';
var id = 'id';

var usr;

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user';//&exID=id';
var connection;

var close_code = {
    ER_NO_NAME: 4004,
    ER_BAD_ID: 4005
};

//TODO keep connection open, close after a certain amount of time and when connection is closed

function play() {

    connection = new WebSocket(url);

    //On connection open
    connection.onopen = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");

        document.getElementById('user-input').value = "";
        document.getElementById('user-input').placeholder = "enter command or message";

    };

    //If  there is an  error
    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    //On message recieved from socket
    connection.onmessage = e => {

        //parse the scoket data
        var JSONdata = JSON.parse(e.data);
        //parse JSONdata.data
        var parsedData = JSON.parse(JSONdata.data);

        //Check if datatype us INIT
        if (JSONdata.dataType == "INIT") {
            //Save init data to usr in case needed later
            usr = parsedData;

            //Update level with data received
            document.getElementById("level").innerHTML = nickname + '<span id="world-value" style="font-weight: normal;"> '
                + parsedData.existence.initialQuidity.ouch.degree + '</span>';
            //Set existence ID
            document.getElementById("world-value").innerHTML = parsedData.existence.id;

            //load in quids in leaderboard
            var usersArray = parsedData.existence.quidities;

            //Add quids to leaderboard
            for (var quid in usersArray) {
                leaderboard.innerHTML += '<div class="data-leaderboard-cont"><p class="data-leaderboard ' + usersArray[quid].id +
                    '">' + usersArray[quid].name +
                    ' <span class="normal">' + usersArray[quid].ouch.degree +
                    '</span></p></div>';
            }

        }

        //if chat then add items
        else if (JSONdata.dataType === "CHAT") {

            //if message is from current user
            if (parsedData.authorName === nickname) {
                document.getElementById('chat').innerHTML +=
                    '<div class="chat-msg-cont"><p class="chat-msg right">' + parsedData.content + '</p></div>';
            }
            //if message is from new user
            else {
                document.getElementById('chat').innerHTML +=
                    '<div class="chat-msg-cont"><p class="chat-msg"><span style="font-weight: bold;">' + parsedData.authorName + ': </span>' + parsedData.content + '</p></div>';
            }

            //New user has entered
        } else if (JSONdata.dataType === "ENTER") {
            //Add new user to leaderboard
            leaderboard.innerHTML += '<div class="data-leaderboard-cont"><p class="data-leaderboard ' + parsedData.id + '">' + parsedData.name + ' <span class="normal">' + parsedData.ouch.degree + '</span></p></div>';

            document.getElementById('chat').innerHTML +=
                '<div class="chat-msg-cont"><p class="chat-msg system"><span style="font-weight: bold;">' +
                parsedData.name + '</span> has joined the Existence. </p></div>';

        } else if (JSONdata.dataType === "EXIT") {
            var quidleaderbaord = document.getElementsByClassName(parsedData.id)[0]
            quidleaderbaord.parentNode.removeChild(quidleaderbaord);

            document.getElementById('chat').innerHTML +=
                '<div class="chat-msg-cont"><p class="chat-msg system"><span style="font-weight: bold;">' +
                parsedData.name + '</span> has left the Existence.</p></div>';
        }

        //Some unknown dataType
        else {
            console.log("Unkown dataType");
            console.log(e.data);
        }
    };

    connection.onclose = (closeEvent) => {
        //switch back to login
        reset();
        if (closeEvent.code === close_code.ER_BAD_ID) {
            document.getElementById('exist-input').placeholder = "Unknown ID";
            document.getElementById('user-input').value = nickname;

            shakeExist();

        } else if (closeEvent.code === close_code.ER_NO_NAME) {
            //document.getElementById('user-input').placeholder = "Must provide a name";
        }

        connection = null;
    }
}

//disconnect from server on page close or refresh
window.onbeforeunload = function(){
    connection.close();
}
