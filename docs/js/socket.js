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
    };

    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    connection.onmessage = e => {
        msg++;
        if (msg == 1) {
            usr = JSON.parse(e.data);
            document.getElementById("level").innerHTML = nickname+'<span id="world-value" style="font-weight: normal;"> '+usr.initialQuidity.ouch.degree+'</span>';
        }
        else {

        }
        alert(e.data);
    };

    connection.onclose = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        document.getElementById("world-value").innerHTML = "offline";
        //document.getElementById('user-input').reset();
    }

}

