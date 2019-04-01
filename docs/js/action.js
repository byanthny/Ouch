user_input.addEventListener("keydown", function(event) {
    //not login screen
    if(!login) {
        if (event.key === 13 || event.key === "Enter") {
            event.preventDefault();
            var input = user_input.value;
            if (input.charAt(0) === "-") {
                switch(input) {
                    case "-darkmode":
                        switchDark();
                        break;
                    case "-exit":
                        connection.close();
                    default:
                        console.log("action "+ input);
                }
            } else {
                //console.log("chat");
                if(connection!=null) {
                    connection.send('{"dataType":"CHAT","data":"'+input+'"}');
                }
            }
            user_input.value = "";
        }
    }
    //login screen
    else  {

    }
});