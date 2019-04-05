/* Handles button clicks and other event listeners
 * events.js
 */

//Login Page

//User Inputs

/* When existence field has a keydown */
exist_input.addEventListener("keydown", function (event) {
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

user_input.addEventListener("keyup", function (event) {
    if(user_input.value.charAt(0)==="-" && !searching) {
        switchSearch();
    } else {
        if(searching) {
            switchSearch();
        }
        searching = false;
        console.log(searching);
    }
    if(!login) {
        autocompleteSearch(user_input.value);
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
    id = exist_input.value;

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
};

//Page load-in and load-out

/* Execute before page loads out */
document.onbeforeunload = function () {
    //disconnect from server
    connection.close();
};

/* One page load retrieve all actions from actions_url*/
document.onload = function () {
    loadActions();
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