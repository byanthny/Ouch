```yaml
old
client -(name, exID)-> ws://existenceID

new'
client -(name, pass)-> http://
http:// -(authData)-> client

new A
client -(name, pass, exID)-> ws://existenceID
-| single connection to single existence
-| will be "blind" to any other "known" existences

new B
client -(name, pass)-> ws://home
client -(exID)-> ws::existenceID
```

Relationships
```yaml
Auth <= Quiddity <- Existence 
```
