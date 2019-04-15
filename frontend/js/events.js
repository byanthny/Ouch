/* Handles button clicks and other event listeners
 * events.js
 */

//Login Page

//User Inputs

/* When existence field has a keydown */
pass_input.addEventListener("keydown", function (event) {
    if (processInput()) {
        submit_button.click();
    }
});

/* When username field has a keydown */
user_input.addEventListener("keydown", function (event) {
    if (processInput()) {
        submit_button.click();
    }
});

/* Listens for keyup on user input and perform autocomplete */
user_input.addEventListener("keyup", function (event) {
    if (user_input.value.charAt(0) === "-") {
        if (!searching) {
            switchSearch();
        }
        if (!login) {
            autocompleteSearch(user_input.value);
        }
    } else if (searching) {
        switchSearch();
        searching = false;
    }
});

//Buttons

/* When reconnect Button  is clicked */
reconnect_button.onclick = function () {
    reconnecting = true;
    createConnection(url_ws + "?token=" + reconnect_token);
};

/*  When submit button is clicked */
submit_button.onclick = function () {

    //Get input
    nickname = user_input.value;
    id = pass_input.value;

    //Process input
    if (processInput(true)) {
        //If no existence was entered, make a new one
        if (id === "") {
            enteredOnce = true;
            createConnection(url_ws + '?name=' + nickname);
        }
        //if a previous existence was entered
        else {
            enteredOnce = true;
            createConnection(url_ws + '?name=' + nickname + '&exID=' + id.toUpperCase());
        }
    }
};

/* On popup close button clicked */
document.getElementsByClassName("close")[0].onclick = function () {
    togglePopUp();
    setTimeout(function () {
        help.classList.add("zindex");
    }, 1000);

};

/* On help icon button clicked */
helpicon.onclick = function () {
    help.classList.remove("zindex");
    togglePopUp();
};

login_style.onclick = function () {
    switchLoginType();
};

//Page load-in and load-out

/* Execute before page loads out */
document.onbeforeunload = function () {
    //disconnect from server
    connection.close();
};

/* One page load retrieve all actions from actions_url*/
window.onload = function () {
    //get actions from url and then load them
    getHTTP(url_actions,setActions);
    //get public status and load them
    getHTTP(url_status,setPublicExist);
};

//Timeout after inactivity

/* Begin watching */
function inactivityTime() {

    window.onload = resetTimer;
    document.onmousemove = resetTimer;
    document.onkeypress = resetTimer;

}

/* User was inactive */
function timedOut() {
    here = false;
    usr_disconnected = true;
    connection.close();
}

/* Reset inactive timer */
function resetTimer() {
    clearTimeout(timeout);
    here = true;
    timeout = setTimeout(timedOut, inactive)
}