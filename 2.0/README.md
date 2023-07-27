```yaml
old
client -(name, exID)-> ws://existenceID

new
client -(name, pass)-> ws://home
ws:// -(auth, existences)-> client
client -(exID)-> ws::existenceID
```

Relationships
```yaml
Auth <= Quiddity <- Existence 
```
