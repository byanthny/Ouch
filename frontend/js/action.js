function checkInput(clicked) {

    here = true;

    //not login screen
    if (event.key === 13 || event.key === "Enter" || clicked === true) {
        var input = user_input.value;

        //not lpgin screen
        if (!login) {
            event.preventDefault();
            //If no input
            if(input === "") {
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
                //console.log("chat");
                if (connection != null) {
                    connection.send(makeChatMessage(input));
                }
            }
            user_input.value = "";
        }

        //login screen
        else {
            if (input === "") {
                shake(user_input);
                return false;
            } else {
                return true;
            }
        }
    }
}

user_input.addEventListener("keydown", function (event) {
    if(checkInput()) {
        submit_button.click();
    }
});

exist_input.addEventListener("keydown", function (event) {
    if (event.key === 13 || event.key === "Enter") {
        if(checkInput()) {
            submit_button.click();
        }
    }
});

//On button click update connected and stuff
// Reconnect
submit_button.onclick = function () {

    nickname = user_input.value;
    id = exist_input.value;

    if (checkInput(true)) {
        if (id === "") {
            play(url_ws + '?name=' + nickname);
        } else {
            play(url_ws + '?name=' + nickname + '&exID=' + id);
        }
    }
};

reconnect_button.onclick = function () {
    play(url_ws + "?token=" + reconnect_token);
};

document.onmousemove = function(){
    here = true;
};

//Get HTTP and return JSON
function getHTTP(url) {
    fetch(url).then(function(resp) { resp.json();}) // Transform the data into json
        .then(function(data) {
            console.log(data);
            return data;
        });
}

function loadActions() {
    var actions_data = getHTTP(url_actions);
    if (actions_data === "") {
        console.log("Error");
    }
    else  {
        //actions = JSON.parse(actions_data);
        console.log(actions);
    }
}