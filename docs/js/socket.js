var nickname = 'user';
var id = 'id';

var usr;
var msg = 0;

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
        msg = 0;
        switchState();
        document.getElementById("indicator").classList.toggle("connected");

        //get actions to action var

        //document.getElementById("world-value").innerHTML = id;
        //document.getElementById('indicator').reset();

        connection.send("{\"dataType\":\"CHAT\",\"data\":\"MESSAGE TEXT\"}");
    };

    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    connection.onmessage = e => {

        var JSONdata = JSON.parse(e.data);

        msg++;
        if (JSONdata.dataType == "EXISTENCE") {
            usr = JSONdata;
            alert(JSONdata.toString());
            document.getElementById("level").innerHTML = nickname+'<span id="world-value" style="font-weight: normal;"> '+usr.initialQuidity.ouch.degree+'</span>';
            //load in chat history
            //load in users
            //load in levels

        }
        else if (JSONdata.dataType == "CHAT"){
            //do something with chart message

            if(JSONdata.data.authorID == usr.initialQuidity.id) { //is current user make right
                document.getElementById('chat').innerHTML += '<p class="chat-msg right">'+JSONdata.data.content+'</p>';
            } else { //make left
                document.getElementById('chat').innerHTML += '<p class="chat-msg">'+JSONdata.data.content+'</p>';
            }
        } else {
            alert("idk wtf is going on");
        }
        //alert(e.data);
    };

    connection.onclose = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        document.getElementById("world-value").innerHTML = "offline";
        //document.getElementById('user-input').reset();
    }

}

