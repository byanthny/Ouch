var login = false; //Current state being displayed
var action;

function switchState() {
    document.getElementById("submit-button").classList.toggle("hidden");
    document.getElementById("search").classList.toggle("opacity");
    document.getElementById("user-input").classList.toggle("login");
    document.getElementById("exist-input").classList.toggle("login");
    document.getElementById("header").classList.toggle("login");
    document.getElementById("exist-input").classList.toggle("opacity");
    document.getElementById("exist-input").classList.toggle("disappear");
    document.getElementById("level").classList.toggle("opacity");
    document.getElementsByClassName("leaderboard")[0].classList.toggle("opacity");
    login = !login;
}

function switchSearch() {
    document.getElementById("search").classList.toggle("opacity");
    document.getElementById("search").classList.toggle("disappear");
    document.getElementById("chat").classList.toggle("opacity");
    document.getElementById("chat").classList.toggle("disappear");
}

function switchDark() {
    document.getElementById("ouch").classList.toggle("dark");
    document.getElementById("header").classList.toggle("dark");
}

function togglePopUp() {
    document.getElementById("help").classList.toggle("opacity");
    setTimeout(function () {
        document.getElementById("help").classList.toggle("hidden");
    }, 1000);
}

document.getElementsByClassName("close")[0].onclick = function() {
    togglePopUp();
};