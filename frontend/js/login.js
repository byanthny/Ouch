function login() {
    fetch("http://localhost:7000/auth/new", {
        method: "POST",
        mode: "cors",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": "Basic " + btoa('jono:passing')
        },
        body: "A=1"
    })
}
