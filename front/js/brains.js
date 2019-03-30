function switchState() {
    document.getElementByClassName("submit-button").classList.toggle("hidden");
    document.getElementByClassName("search").classList.toggle("hidden");
    document.getElementByClassName("user-input").classList.toggle("login");
    document.getElementByClassName("header").classList.toggle("login");
}

function switchDark() {
    document.getElementByClassName("ouch").classList.toggle("dark");
    document.getElementByClassName("header").classList.toggle("dark");
}