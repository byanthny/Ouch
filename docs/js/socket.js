var nickname = 'user';
var id = 'id';

var usr;

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user';//&exID=id';
var connection;

//On button click update connected and stuff
document.getElementById("submit-button").onclick = function() {

    nickname = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;

    document.getElementById('user-input').value = "";
    document.getElementById('user-input').placeholder = "enter command or message";

    if (id == "") {
        url = 'wss://sim-ouch.herokuapp.com/ws?name='+nickname;
    } else {
        url = 'wss://sim-ouch.herokuapp.com/ws?name='+nickname+'&exID='+id;
    }

    play();
};

var elements;

function play() {

    connection = new WebSocket(url);

    connection.onopen = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");

        //document.getElementById('indicator').reset();

        //test sending message
        connection.send("{\"dataType\":\"CHAT\",\"data\":\"MESSAGE TEXT\"}");
    };

    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    connection.onmessage = e => {

        //parse the data
        var JSONdata = JSON.parse(e.data);
        var parsedData = JSON.parse(JSONdata.data);

        //IF begining then save user and update stuff
        if (JSONdata.dataType == "INIT") {
            usr = parsedData;
            document.getElementById("level").innerHTML = nickname+'<span id="world-value" style="font-weight: normal;"> '+parsedData.existence.initialQuidity.ouch.degree+'</span>';
            document.getElementById("world-value").innerHTML = parsedData.existence.id;

            /*
            //load in chat history
            var chatHistory = parsedData.existence.quidities;

            for(var quid in usersArray) {
                leaderboard.innerHTML += '<div class="data-leaderboard">'+usersArray[quid].name+' <span class="normal">'+usersArray[quid].ouch.degree+'</span></div><br></br>';
            }*/

            //load in users in leaderboard
            var usersArray = parsedData.existence.quidities;

            for(var quid in usersArray) {
                leaderboard.innerHTML += '<div class="data-leaderboard '+usersArray[quid].id+'">'+usersArray[quid].name+' <span class="normal">'+usersArray[quid].ouch.degree+'</span></div><br></br>';
            }

        }
        //if chat then add items
        else if (JSONdata.dataType == "CHAT"){

            //if message is from current user
            if(parsedData.authorName == nickname) { //is current user make right
                document.getElementById('chat').innerHTML += '<p class="chat-msg right">'+parsedData.content+'</p>';
            } else { //if message is from new user
                document.getElementById('chat').innerHTML += '<p class="chat-msg">'+parsedData.content+'</p>';
            }
        } else if (JSONdata.dataType == "ENTER") {
            leaderboard.innerHTML += '<div class="data-leaderboard '+parsedData.id+'">'+parsedData.name+' <span class="normal">'+parsedData.ouch.degree+'</span></div>';

        } else if (JSONdata.dataType == "EXIT") {
            alert(parsedData.id + "Left");
            document.getElementByClassName(parsedData.id)[0].display = none;
        }

        else {
            alert("idk wtf is going on");
            alert(e.data);
        }
    };

    connection.onclose = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        document.getElementById("world-value").innerHTML = "offline";
        connection = null;
        //document.getElementById('user-input').reset();
    }

}

