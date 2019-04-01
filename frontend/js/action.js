function checkInput(clicked) {
    //not login screen
    if (event.key === 13 || event.key === "Enter" || clicked === true) {
        var input = user_input.value;

        //not lpgin screen
        if (!login) {
            event.preventDefault();
            if (input.charAt(0) === "-") {
                switch (input) {
                    case "-darkmode":
                        switchDark();
                        break;
                    case "-exit":
                        connection.close();
                    default:
                        console.log("action " + input);
                }
            } else {
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
                shakeUsername();
                return false;
            } else {
                return true;
            }
        }
    }
}

user_input.addEventListener("keydown", function (event) {
    if(checkInput()) {
        document.getElementById("submit-button").click();
    }
});

exist_input.addEventListener("keydown", function (event) {
    if (event.key === 13 || event.key === "Enter") {
        if(checkInput()) {
            document.getElementById("submit-button").click();
        }
    }
});

//On button click update connected and stuff
document.getElementById("submit-button").onclick = function () {

    nickname = document.getElementById('user-input').value;
    id = document.getElementById('exist-input').value;

    if (checkInput(true)) {

        if (id === "") {
            url = 'wss://sim-ouch.herokuapp.com/ws?name=' + nickname;
        } else {
            url = 'wss://sim-ouch.herokuapp.com/ws?name=' + nickname + '&exID=' + id;
        }

        play();
    }
};