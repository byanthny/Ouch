/* Handles other functions and processing
 * Mainly helper methods
 */

/* When input is sent from username or existence field
 * process it based on current state (e.x. login or commands page)
 * Only accepts input if through enter button or button clicked (using param).
 * @param if clicked button was clicked instead of key down
 */
function processInput(clicked) {

    here = true;

    //not login screen
    if (event.key === 13 || event.key === "Enter" || clicked === true) {
        var input = user_input.value;

        //not login screen
        if (!login) {
            event.preventDefault();
            //If no input
            if (input === "") {
                shake(user_input);
            }
            //If command
            else if (input.charAt(0) === "-") {
                processCommands(input);
            }
            //If chat
            else {
                //Check to make sure websocket is open
                if (connection.readyState == 1) {
                    //console.log("sent chat message, connection status: "+ connection.readyState);
                    connection.send(makeChatMessage(input));
                } else {
                    console.log("web socket is not connected");
                }
            }
            //Clear user input
            user_input.value = "";
        }

        //login screen
        else {
            //If  input is empty or  not alphanumeric don't accept
            if (input === "" || !input.match(allowedInput)) {
                shake(user_input);
                return false;
            } else {
                //if used to prevent spamming
                if (!enteredOnce) {
                    return true;
                }
            }
        }
    }
}

function processCommands (command) {
    switch (command) {
        case "-theme":
            switchDark();
            break;
        case "-bean":
            ouch.innerHTML = '<object data="imgs/bean.svg" type="image/svg+xml" style="width: 200px;"></object>';
            break;
        case "-exit":
            usr_disconnected = true;
            connection.close();
            break;
        default:
            console.log("action " + command);
    }
}

//Search Autocomplete

/* Autocomplete funct */
function autocompleteSearch(currentInput) {

    console.log("Im called");

    search.innerHTML = "";
    var count =  0;
    for (var i = 0; i < testActions.length; i++) {
        if(testActions[i].includes(currentInput)) {
            count++;
            search.innerHTML += '<div class="search-item">'+testActions[i]+'</div>';
        }
    }
    if(count === 0) {
        search.innerHTML += '<div class="search-item">No Matches</div>'
    }

    for (var i = 0; i < search_items.length; i++) {
        search_items[i].onclick = function (event) {
            autofill(event.target.innerHTML);
        }
    }

    searching = true;

}

function autofill(input) {
    //searching = false;
    user_input.value = "";
    processCommands(input);
    switchSearch();
}
