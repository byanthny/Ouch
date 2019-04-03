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
                        connection.close();
                    default:
                        console.log("action " + input);
                }
            }
            //If chat
            else {
                //console.log("chat");
                if (connection != null) {
                    connection.send('{"dataType":"CHAT","data":"' + input + '"}');
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
document.getElementById("submit-button").onclick = function () {

    nickname = user_input.value;
    id = exist_input.value;

    if (checkInput(true)) {

        if (id === "") {
            url = 'wss://sim-ouch.herokuapp.com/ws?name=' + nickname;
        } else {
            url = 'wss://sim-ouch.herokuapp.com/ws?name=' + nickname + '&exID=' + id;
        }

        play();
    }
};

document.onmousemove = function(){
    here = true;
}