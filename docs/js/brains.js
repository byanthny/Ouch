function switchState() {
    document.getElementById("search").classList.toggle("opacity");
    document.getElementById("user-input").classList.toggle("login");
    document.getElementById("header").classList.toggle("login");
}

function switchDark() {
    document.getElementById("ouch").classList.toggle("dark");
    document.getElementById("header").classList.toggle("dark");
}

var span = document.getElementsByClassName("close")[0];

span.onclick = function() {
    document.getElementById("help").classList.toggle("opacity");
    setTimeout(function (){
        document.getElementById("help").classList.toggle("hidden");
    }, 1000);
}