var usr = 'user';
var id = 'id';

var url = 'wss://sim-ouch.herokuapp.com/ws?name=user&exID=id';

var connection;

//On button click update connected and stuff
document.getElementById("submit-button").onclick = function() {
    usr = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;
    url = 'wss://sim-ouch.herokuapp.com/ws?name='+usr+'&exID='+id;

    connection = new WebSocket(url);
    //alert(url);
};
connection.onopen = () => {
    //do stuff and switch
    switchState();
    id = document.getElementById('user-input').reset();
}

connection.onerror = error => {
    //create error message
    alert(`WebSocket error: ${error}`);
}

