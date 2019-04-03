/* Handles all actions regarding styling
 * ex. changing to dark mode or animations
 * style.js
 */

/* Toggles between login and main page */
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


/* Toggles predictive search panel */
function switchSearch() {
    search.classList.toggle("opacity");
    search.classList.toggle("disappear");
    chat.classList.toggle("opacity");
    chat.classList.toggle("disappear");
}

/* Toggles Ouch theme between dark and light mode */
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
/* Will scroll chat window to bottom */
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
/* Creates new chat message based on type
 * @param
 */
function addChat(name, content, type) {

    var  html = "";

    if(type === "system") {
        html = '<div class="chat-msg-cont"><p class="chat-msg system"><span style="font-weight: bold;">'
            + name + '</span> '+content+'</p></div>';
    } else if (type === "other") {
        html =
            '<div class="chat-msg-cont"><p class="chat-msg '+type+'"><span style="font-weight: bold;">'
            + name + ': </span>' + content + '</p></div>';
    } else if (type === "user") {
        html =
            '<div class="chat-msg-cont"><p class="chat-msg user">' + content + '</p></div>';
    }

    chat.innerHTML += html;

}