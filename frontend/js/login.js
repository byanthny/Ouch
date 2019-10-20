function login() {
    var r = fetch('http://localhost:7000/login', {
        method: "POST",
        mode: "cors",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Basic " + btoa('jono:pass') // todo replace
            // with input
        }
    }).then(function (resp) {
        console.log(resp)
    });
}
