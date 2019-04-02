/* Handles all actions regarding styling
 * ex. changing to dark mode or animations
 * style.js
 */

//Is on login page?
var login = false;

//TODO load in actions from api
//Array of all possible actions
var action;

//HTML elements
var header = document.getElementById("header");
var exist_input = document.getElementById("exist-input");
var user_input = document.getElementById("user-input");
var chat = document.getElementById("chat");
var leaderboard = document.getElementsByClassName("leaderboard")[0];
var level = document.getElementById("level");
var box = document.getElementById("box");
var search = document.getElementById("search").classList.toggle("opacity");
var ouch = document.getElementById("ouch");
var submit_button = document.getElementById("submit-button");
var help = document.getElementById("help");
var indicator = document.getElementById("indicator");
var world_value = document.getElementById("world-value");

function switchState() {
    submit_button.classList.toggle("hidden");
    box.classList.toggle("opacity");
    user_input.classList.toggle("login");
    exist_input.classList.toggle("login");
    header.classList.toggle("login");
    exist_input.classList.toggle("opacity");
    exist_input.classList.toggle("disappear");
    level.classList.toggle("opacity");
    leaderboard.classList.toggle("opacity");
    login = !login;
}

function switchSearch() {
    search.classList.toggle("opacity");
    search.toggle("disappear");
    chat.classList.toggle("opacity");
    chat.classList.toggle("disappear");
}

function switchDark() {
    ouch.classList.toggle("dark");
    header.classList.toggle("dark");
    chat.classList.toggle("dark");
}

function togglePopUp() {
    help.classList.toggle("opacity");
    setTimeout(function () {
        help.classList.toggle("hidden");
    }, 1000);
}

document.getElementsByClassName("close")[0].onclick = function() {
    togglePopUp();
};

//TODO make scroll stay in place if you scroll up
//When called will scroll chat to bottom
function scrollBottom() {
    chat.scrollTop = chat.scrollHeight;
}

/* Reset Ouch back to the login screen. */
function reset() {
    switchState();
    indicator.classList.toggle("connected");
    world_value.innerHTML = "offline";
    leaderboard.innerHTML = "";
    chat.innerHTML = "";
    user_input.value = "";
}

/* Used to create shake animation on given element
 * @param the HTML element to be "shaken"
 */
function shake(toShake) {
    toShake.classList.add("shake");
    setTimeout(function () {
        toShake.classList.remove("shake");
    }, 1000);
}

function shakeUsername() {
    shake(user_input);
}

function shakeExist() {
    shake(exist_input);
}