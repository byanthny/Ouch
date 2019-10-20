# Ouch Backend README

## Websocket Connection Pattern

The Websocket API endpoint in `/ws`. When initially connecting, there are 2
URL Query parameters, one of which is required and the other being optional.
The first and required parameter is the `name` which assigns the client's
display name. The second and optional parameter is the target `Existence`
ID, this parameter should be given as `exID`. A connection request will end up
looking like this:

    /ws?name=NAME&exID=EXISTENCE_ID

Once the connection is made, one of two things will occur:
- If an `exID` is provided the user is added to the Existence associated
with the parameter.
- If no `exID` is given, the user is added to the first public Existence which
is not full. If there is no available public Existence, a new one will be 
created.

Once the user has joined an Existence, a JWT is generated for their session.
The token is sent to the client and then their addition to the Existence is
broadcast to the other `Entities` in the Existence.

### Reconnection Pattern

The Websocket accepts a heart-beat pattern with the client to maintain the 
connection, but after the idle timeout the session will be closed. The session
token can be used to restart a timed-out session. The process is the same as
[the connection pattern](#websocket-connection-pattern) however instead of the
`name` and `exID` query parameters, a `token` parameter is sent with the session
token as it's value. 
    
    /ws?token=JWT_TOKEN
    
If the token is validated then the session information is passed to the client
and they are re-added to the Existence.

## Internal Structure

Things to track:

    Existence
        Entity
            Token
            Session
        Chat
    WsContext
        Token
