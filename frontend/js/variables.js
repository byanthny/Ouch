/* Stores all variables
 * variables.js
 */

//HTML elements
//
var header = document.getElementById("header");
var pass_input = document.getElementById("pass-input");
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
var loading = document.getElementById("loading");
var commands = document.getElementById("commands");
var chatindic = document.getElementById("chat-indic");
var helpicon = document.getElementById("help-icon");
var login_status = document.getElementById("login-status");
var login_style = document.getElementById("login-type-toggle");
var actions_help = document.getElementById("actions-help");
var help_background = document.getElementById("help");

var search_items = document.getElementsByClassName("search-item");
var search_items = document.getElementsByClassName("panel");

//Login In
//

//Login message
var existing_msg = "have an existing login?";
var new_exist_msg= "dont have an account?";

//Is on login page?
var login = false;
var enteredOnce = false;

//Username and id from Login Input
var nickname = 'user';
var id = 'id';

//Input allowed for username
var allowedInput = /^[a-z0-9]+$/i;


//Server Communication
//

// Base endpoint
var url_base = 'ouchie.herokuapp.com';

//Socket
var url_ws = 'wss://' + url_base + '/ws';
var url_actions = 'https://' + url_base + '/actions';
var url_status = 'https://' + url_base + '/status';
var connection;
var reconnect_token = null;

var close_code = {
    ER_NO_NAME: 4005,
    ER_EX_NOT_FOUND: 4004,
    ER_Q_NOT_FOUND: 4040,
    ER_BAD_TOKEN: 4007,
    ER_INTERNAL_GENERIC: 4010
};

var ping_packet = '{"dataType":"PING","data":"PING"}';

var usr_disconnected = false;
var reconnecting = false;

//Array of all possible actions
var actions;
//all public existences
var public_exist;
var  searching = false;


//Events
//

//if user is active
var here = true;

//Timeout timer
var timeout;
//How long stand-by
var inactive = 30000;
