var usr = 'user';
var id = 'id';


var url = 'wss://sim-ouch.herokuapp.com/ws?name=user&exID=id';

document.getElementById("submit-button").onclick = function() {
    usr = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;
    url = 'wss://sim-ouch.herokuapp.com/ws?name='+usr+'&exID='+id;
    alert("Submit button works" + url);
};

