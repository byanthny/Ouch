function switchState() {
    document.getElementById("submit-button").classList.toggle("hidden");
    document.getElementById("search").classList.toggle("opacity");
    document.getElementById("user-input").classList.toggle("login");
    document.getElementById("header").classList.toggle("login");
}

function switchDark() {
    document.getElementById("ouch").classList.toggle("dark");
    document.getElementById("header").classList.toggle("dark");
}