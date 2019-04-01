//Actions listnen  or enter

user_input.addEventListener("keydown", function(event) {
    // Number 13 is the "Enter" key on the keyboard
    if(!login) {
        if (event.key === 13 || event.key === "Enter") {
            event.preventDefault();
            var input = user_input.value;
            if (input.charAt(0) === "-") {
                console.log("action");
            } else {
                //console.log("chat");
                if(connection!=null) {
                    connection.send('{"dataType":"CHAT","data":"'+input+'"}');
                }
            }
            user_input.value = "";
        }
    }
});