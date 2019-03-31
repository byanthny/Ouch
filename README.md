<p align="center">
<img src="docs/imgs/logo.png" width="250"/>
</p>

## Quiddity 
   * Love state/function
   * You can set ouch up only and set love to whatever.
   * A state of Ouch that can't be turned off.
        * Max ouch, nothing works...
        * Ouch achievements
        * Ouch is a gradient
        * Play random Ouch Music (" **ouch** -music")
            * The Ouch Music should be to the varying degrees
        * Silence
        * A shower |||||
        * :(
        * Cry

---
## Thy Future
    * Make a discord bot
        * Ouch is increased by keywords and songs played
        * Ouch plays music and has "leaderboard"   
    * Make a web app
        * Multiplayer

---
## Internal

Login: 
    
        C -> Name, exID? => wss://sim-ouch.herokuapp.com/ws?name=user&exID=id
        S -> when: {
                no name     => close(4004, "No Name")
                invalid ID  => close(4005, "Unknown ID")
                no ID       => generate new DefaultExistence
                valid ID    => live existence
            }
            InitPacket as JSON 
            {
                existence: {...}
                actions: {...}
                (other data?...)
            }
            send(InitPacket)

Action: 
    
        C -> Action Name
        S -> updated Quidity (if actions affect Existence then send that)
