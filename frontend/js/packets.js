/*  Creates packets to be sent to web socket
 *  Handles packets sent from socket
 */

// Client -> Server packets

/* Creates chat package to send to server
 * @param content of message
 */
function makeChatMessage(content) {
    return '{"dataType":"CHAT","data":"' + content + '"}';
}

// Server -> Client packets

/* Handles processing from initial packet from web socket
 * @param initial packet parsed
 */
function handleInit(init_packet) {
    //Save init data to usr in case needed later
    var existence = init_packet.existence;
    var quiddity = init_packet.quiddity;
    reconnect_token = init_packet.token;

    //Update level with data received
    level.innerHTML = quiddity.name
        + '<span id="world-value" style="font-weight: normal;"> '
        + quiddity.ouch.degree + '</span>';
    //Set existence ID
    world_value.innerHTML = init_packet.existence._id;

    user_input.placeholder = "enter command or message";
    indicator.classList.toggle("await");
    indicator.classList.toggle("connected");
    setTimeout(function () {
        switchLoading(false);
    }, 1000);

    //load in quids in leaderboard
    var usersArray = existence.quidities;

    //Add quids to leaderboard
    for (var quid in usersArray) {
        leaderboard.innerHTML +=
            '<div class="data-leaderboard-cont">' +
            '<p class="data-leaderboard ' + usersArray[quid].id + '">'
            + usersArray[quid].name +
            ' <span class="normal">'
            + usersArray[quid].ouch.degree +
            '</span></p></div>';
    }

    // Load the chat history
    var chat_log = existence.chat.history;

    for (var message in chat_log) {
        addChat(message.authorName, message.content, "user")
    }
}
