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
        + ' <span id="world-value" class="normal">'
        + quiddity.ouch.level + '</span>';
    //Set existence ID
    world_value.innerHTML = init_packet.existence._id;

    user_input.placeholder = "enter command or message";
    indicator.classList.toggle("await");
    indicator.classList.toggle("connected");

    //load in quids in leaderboard
    var usersArray = existence.quidities;

    //Add quids to leaderboard
    for (var quid in usersArray) {
        leaderboard.innerHTML +=
            '<div class="data-leaderboard-cont">' +
            '<p class="data-leaderboard ' + usersArray[quid].id + '">'
            + usersArray[quid].name +
            ' <span class="normal">'
            + usersArray[quid].ouch.level +
            '</span></p></div>';
    }

    // Load the chat history
    var chat_log = existence.chat.history;
    for  (var i = 0; i < chat_log.length; i++) {
        var message = chat_log[i];
        addChat(message.authorName, message.content, "client");
    }

    setTimeout(function () {
        switchLoading(false);
    }, 1000);
}

function handleQuid (id, name, level) {
    if (name === nickname) {
        world_value.innerHTML = level;
    }
    var quidleaderboard = document.getElementsByClassName(id)[0].childNodes[0];
    quidleaderboard.innerHTML = level;
}
