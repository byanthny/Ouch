// Client -> Server packets

function makeChatMessage(text) {
    return '{"dataType":"CHAT","data":"' + text + '"}';
}

// Server -> Client packets

function handleInit(init_packet) {
    //Save init data to usr in case needed later
    var existence = init_packet.existence;
    var quiddity  = init_packet.quiddity;
    reconnect_token = init_packet.token;

    //Update level with data received
    level.innerHTML = quiddity.name
        + '<span id="world-value" style="font-weight: normal;"> '
        + quiddity.ouch.degree + '</span>';
    //Set existence ID
    world_value.innerHTML = init_packet.existence._id;
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
                + usersArray[quid].ouch.degree +
            '</span></p></div>';
    }
}
