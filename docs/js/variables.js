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
//Temp init data
var usr;

//Socket
var url = 'wss://sim-ouch.herokuapp.com/ws?name=user';//&exID=id';
var connection;

var close_code = {
    ER_NO_NAME: 4004,
    ER_BAD_ID: 4005,
    ER_INTERNAL_GENERIC: 4010
};