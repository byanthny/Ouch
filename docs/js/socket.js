var nickname = 'user';
var id = 'id';

var usr;

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user';//&exID=id';

//On button click update connected and stuff
document.getElementById("submit-button").onclick = function() {
    nickname = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;
    url = 'wss://sim-ouch.herokuapp.com/ws?name='+nickname;//+'&exID='+id;
    play();
    //alert(url);
};

var elements;

function play() {

    var connection = new WebSocket(url);

    connection.onopen = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");

        //get actions to action var

        //document.getElementById("world-value").innerHTML = id;
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
            document.getElementById("level").innerHTML = nickname+'<span id="world-value" style="font-weight: normal;"> '+usr.existence.initialQuidity.ouch.degree+'</span>';
            //load in chat history
            //load in users
/*
            for(int i = 0; i < usr.data.) {

            }*/

            //load in levels

        }
        //if chat then add items
        else if (JSONdata.dataType == "CHAT"){

            //if message is from current user
            if(parsedData.authorName == usr.existence.initialQuidity.name) { //is current user make right
                document.getElementById('chat').innerHTML += '<p class="chat-msg right">'+parsedData.content+'</p>';
            } else { //if message is from new user
                document.getElementById('chat').innerHTML += '<p class="chat-msg">'+parsedData.content+'</p>';
            }
        } else {
            alert("idk wtf is going on");
            alert(e.data);
        }
    };

    connection.onclose = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        document.getElementById("world-value").innerHTML = "offline";
        //document.getElementById('user-input').reset();
    }

}

