/* Stores all variables
 * variables.js
 */

//HTML elements
var header = document.getElementById("header");
var exist_input = document.getElementById("exist-input");
var user_input = document.getElementById("user-input");
var chat = document.getElementById("chat");
var leaderboard = document.getElementsByClassName("leaderboard")[0];
var level = document.getElementById("level");
var box = document.getElementById("box");
var search = document.getElementById("search");
var ouch = document.getElementById("ouch");
var submit_button = document.getElementById("submit-button");
var reconnect_button = document.getElementById("reconnect-button");
var help = document.getElementById("help");
var indicator = document.getElementById("indicator");
var world_value = document.getElementById("world-value");

//Array of all possible actions
//TODO load in actions from api
var actions;

//Login In
//Is on login page?
var login = false;

//Username and id from Login Input
var nickname = 'user';
var id = 'id';

//Server Communication
//if user is active
var here = true;

//Temp init data
var usr;

// Base endpoint
var url_base = 'sim-ouch.herokuapp.com';
//Socket
var url_ws = 'wss://' + url_base + '/ws';
var url_actions = 'https://'+url_base+'/actions';
var connection;
var reconnect_token = null;

var close_code = {
    ER_NO_NAME  : 4005,
    ER_EX_NOT_FOUND : 4004,
    ER_Q_NOT_FOUND : 4040,
    ER_BAD_TOKEN : 4007,
    ER_INTERNAL_GENERIC : 4010
};

var ping_packet = '{"dataType":"PING","data":"PING"}';

var  usr_disconnected = false;
