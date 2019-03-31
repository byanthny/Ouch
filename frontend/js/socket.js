var usr = 'user';
var id = 'id';

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user&exID=id';

//On button click update connected and stuff
document.getElementById("submit-button").onclick = function() {
    usr = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;
    url = 'wss://sim-ouch.herokuapp.com/ws?name='+usr+'&exID='+id;
    play();
    //alert(url);
};

var elements;

function play() {

    var connection = new WebSocket(url);

    connection.onopen = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        //document.getElementById('indicator').reset();
    };

    connection.onerror = error => {
        //create error message
        alert(`WebSocket error: ${error}`);
    };

    connection.onmessage = e => {

        alert(e.data);
    };

    connection.onclose = () => {
        switchState();
        document.getElementById("indicator").classList.toggle("connected");
        //document.getElementById('user-input').reset();
    }

}

