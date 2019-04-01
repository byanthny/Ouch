var nickname = 'user';
var id = 'id';

var usr;

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user';//&exID=id';
var connection;

var close_code = {
    ER_NO_NAME: 4004,
    ER_BAD_ID: 4005
};

//TODO  keep connection open
//TODO enter on  username  or existence

function play() {

    connection = new WebSocket(url);

    //On conncetion open
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
                leaderboard.innerHTML += '<div class="data-leaderboard ' + usersArray[quid].id +
                    '">' + usersArray[quid].name +
                    ' <span class="normal">' + usersArray[quid].ouch.degree +
                    '</span></div>';
            }

        }

        //if chat then add items
        else if (JSONdata.dataType === "CHAT") {

            //if message is from current user
            if (parsedData.authorName === nickname) {
                document.getElementById('chat').innerHTML +=
                    '<p class="chat-msg right">' + parsedData.content + '</p>';
            }
            //if message is from new user
            else {
                document.getElementById('chat').innerHTML +=
                    '<p class="chat-msg"><span style="font-weight: bold;">' + parsedData.authorName + ': </span>' + parsedData.content + '</p>';
            }

            //New user has entered
        } else if (JSONdata.dataType === "ENTER") {
            //Add new user to leaderboard
            leaderboard.innerHTML += '<div class="data-leaderboard ' + parsedData.id + '">' + parsedData.name + ' <span class="normal">' + parsedData.ouch.degree + '</span></div>';

            document.getElementById('chat').innerHTML +=
                '<p class="chat-msg system"><span style="font-weight: bold;">' +
                parsedData.name + '</span> has joined the Existence. </p>';

        } else if (JSONdata.dataType === "EXIT") {
            var quidleaderbaord = document.getElementByClassName(parsedData.id)[0]
            quidleaderbaord.parentNode.removeChild(quidleaderbaord);

            document.getElementById('chat').innerHTML +=
                '<p class="chat-msg system"><span style="font-weight: bold;">' +
                parsedData.name + '</span> has left the Existence.</p>';
        }

        //Some unkown dataType
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
