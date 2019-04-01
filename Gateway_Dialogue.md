# Gateway Dialogue

### Preface

All data (at least until i realize im dumb and do something else) is sent within 
in a  ``Packet``. On the BackEnd, this is represented by:
```kotlin
data class Packet(val dataType: DataType, var data: Any)
```
To keep things consistent and to make things slightly simpler on the backend,
the ``data`` held within the ``Packet`` is defined as ``Any`` -- meaning it can 
be any object. The ``dataType`` variable is thus the way to see what type
of information lies in ``data``. When the ``Packet`` is sent, ``data`` is 
Stringifyed into JSON -- <sup>important part</sup> **this means all ``Packets``
when sent by the backend have a ``dataType`` and a JSON-STRING ``data``.**

## Connecting

1. Client connects to websocket at websocket endpoint

    a. ``wss://.../ws?name=NAME`` to create a new Existence. Makes existence and
    creates a ``Quidity`` with name of ``NAME``. Closes socket if ``name`` is
    blank or null.
    
    b. ``wss://.../ws?name=NAME&exID=EXISTENCE_ID`` to join an Existence.
    Attempts to make a new ``Quidity`` in the ``Existence`` matching 
    ``EXISTENCE_ID``. Closes the socket if no ``Existence`` was found.
    
2. On successful connection, Server sends the ``Existence`` and the ``Quidity``
associated with the new user:
At first it looks like this:
```json
{
  "dataType": "INIT",
  "data": "{\"existence\":{\"initialQuidity\":{\"ouch\":{\"degree\":0},\"name\":\"name\",\"id\":\"2V1HQG94VE\"},\"capacity\":-1,\"name\":\"gyauxo\",\"id\":\"S9PLVMQCL6\",\"status\":\"DRY\",\"quidities\":{\"2V1HQG94VE\":{\"ouch\":{\"degree\":0},\"name\":\"name\",\"id\":\"2V1HQG94VE\"}},\"infraQuidities\":{}},\"quidity\":{\"ouch\":{\"degree\":0},\"name\":\"name\",\"id\":\"2V1HQG94VE\"}}"
}
```
Parsing ``data`` again will result in:
```json
{
  "existence": {
    "initialQuidity": {
      "ouch": {
        "degree": 0
      },
      "name": "name",
      "id": "2V1HQG94VE"
    },
    "capacity": -1,
    "name": "gyauxo",
    "id": "S9PLVMQCL6",
    "status": "DRY",
    "quidities": {
      "2V1HQG94VE": {
        "ouch": {
          "degree": 0
        },
        "name": "name",
        "id": "2V1HQG94VE"
      }
    },
    "infraQuidities": {}
  },
  "quidity": {
    "ouch": {
      "degree": 0
    },
    "name": "name",
    "id": "2V1HQG94VE"
  }
}
```
**LET ME KNOW IF THIS CAUSES PROBLEMS AND I WILL MAKE CHANGES**

## Chat

1. Client sends a chat message:
```json
{
  "dataType": "CHAT",
  "data": "message_content"
}
```

2. Server distributes chat message to all clients (to be placed in the chat log)
```json
{
  "dataType": "CHAT",
  "data": "{\"authorName\":\"name\",\"content\":\"message content\"}"
}
```
Parsing ``data`` again gives the usable info:
```json
{
  "authorName": "name",
  "content": "message content"
}
```


## Actions (WIP)

1. Client requests to perform an action:
```json
{
  "dataType": "ACTION",
  "data": "action_name" // This might get more complex if actions have params
}
```

2. Server attempts to perform action and then sends updated quidity or Existence?
