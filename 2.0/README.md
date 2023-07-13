```yaml
old
client -(name, exID)-> ws://existenceID

new'
client -(name, pass)-> http://
http:// -(authData)-> client

new
client -(name, pass)-> ws://home
client -(exID)-> ws::existenceID
```

Relationships
```yaml
Auth <= Quiddity <- Existence 
```
