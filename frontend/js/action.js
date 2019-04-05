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
                switch (input) {
                    case "-theme":
                        switchDark();
                        break;
                    case "-exit":
                        usr_disconnected = true;
                        connection.close();
                        break;
                    default:
                        console.log("action " + input);
                }
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