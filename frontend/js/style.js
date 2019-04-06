/* Handles all actions regarding styling
 * ex. changing to dark mode or animations
 * style.js
 */

//Switch States

/* Toggles between login and main page */
function switchState() {
    if (reconnect_token != null) {
        reconnect_button.classList.toggle("hidden");
    } else submit_button.classList.toggle("hidden");
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
    searching = !searching;
}

/* Toggles Ouch theme between dark and light mode */
function switchDark() {
    ouch.classList.toggle("dark");
    header.classList.toggle("dark");
    chat.classList.toggle("dark");
}

function togglePopUp() {
    help.classList.toggle("opacity");
}

/* Switches between loading screen */
function switchLoading(enter) {
    //TODO speed up animations
    if (enter) {
        commands.classList.toggle("opacity");

        setTimeout(function () {
            loading.classList.toggle("disappear");
            loading.classList.toggle("opacity");
        }, 1000);
    } else {
        loading.classList.toggle("opacity");
        loading.classList.toggle("disappear");

        setTimeout(function () {
            commands.classList.toggle("opacity");
        }, 1000);
    }
}

/* Switches on and off chat "more messages" indicator */
function switchChatIndic() {
    chatindic.classList.toggle("disappear");
    chatindic.classList.toggle("opacity");
}

//Chat

/* Brings chat to bottom */
var bottom = function () {
    chat.scrollTop = chat.scrollHeight;
};


/* Scroll chat to bottom
 * Or shows new message indicator if user is scrolling
 */
function scrollBottom() {

    //TODO make scroll stay in place if you scroll up
    //check previous
    //prevChatSize = chat.scrollHeight;
    //console.log(document.documentElement.clientWidth*.20 < chat.scrollHeight);
    console.log(+" " + prevChatSize + " " + chat.offsetTop); //- prevChatSize ===
    if ((chat.scrollTop === prevChatSize) && (document.documentElement.clientWidth * .20 < chat.scrollHeight)) {
        //if(!chatScrolling) {
        bottom;
        //console.log(document.documentElement.clientWidth*.20+ " "+ chat.scrollHeight);
    } else if (!(chat.scrollHeight < 0)) {
        switchChatIndic();
    }
    prevChatSize = chat.scrollHeight;
}

//Animations

/* Used to create shake animation on given element
 * @param the HTML element to be "shaken"
 */
function shake(toShake) {
    toShake.classList.add("shake");
    setTimeout(function () {
        toShake.classList.remove("shake");
    }, 1000);
}

//Web socket

/* Reset Ouch back to the login screen. */
function reset() {
    switchState();
    indicator.classList.toggle("connected");
    world_value.innerHTML = "offline";
    leaderboard.innerHTML = "";
    chat.innerHTML = "";
    user_input.value = "";
    user_input.placeholder = "username";
    enteredOnce = false;
    ouch.innerHTML = "Ouch";
    if(searching){
        switchSearch();
    }
}

/* Creates new chat message based on type
 * @param username
 * @param message content
 * @param type of message (system, user, or other)
 */
function addChat(name, content, type) {

    var html = "";

    if (type === "system") {
        html = '<div class="chat-msg-cont"><p class="chat-msg system"><span style="font-weight: bold;">'
            + name + '</span> ' + content + '</p></div>';
    } else if (name !== nickname && type === "client") {
        html =
            '<div class="chat-msg-cont"><p class="chat-msg ' + type + '"><span style="font-weight: bold;">'
            + name + ': </span>' + content + '</p></div>';
    } else if (name === nickname && type === "client") {
        html =
            '<div class="chat-msg-cont"><p class="chat-msg user">' + content + '</p></div>';
    }

    chat.innerHTML += html;

}

//Other

/* When chat indicator is clicked scroll to bottom */
function chatIndic() {
    scrollBottom();
    bottom();
}
