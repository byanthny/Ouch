<p align="center">
<img src="docs/imgs/logo.png" width="250"/>
</p>

## The Simulation
  Ah yes...there comes a point in person life were the simulation doesn't treat you so well. Sometimes I wonder what the `simulators` were thinking. 
  
  What are `simulators`? 
  
  Glad you asked, simulators are the controllers of our simulations, this simulation, every simulation. After all we live in a simulation, prove me wrong. 
  
  Now, we're going to define the elements of a **simulation**, in a way we see fit. There are no right or wrong answers here because... well we have no way of breaking the simulation. I mean if you know how to escape and contact the simulator(s), let me know. 
  
  Anyway, we have defined `Simulators` as the controllers of the simulation. 
  
  We will use `Existences` as the simulation instance. These `Existences` holds everything that is part of the simulation. For sake of simplicity we will have two options human and non-human to describe objects inside the simulation. 
  
  We will use `Quiddities` to describe the essence of a human. We will leave the definition of *human* up to you. 
  
  Anything that is non-human or not a `Quiddity`  we will call them `Infraquiddities`  or `InfraQuiddity`. 
  
  I haven't lost you yet, I hope. 
  
  Now why does this matter? Let's go back to feeling... *Ouch*. The feeling of *Ouch* is part of a `Quiddities` life, yes.
  
  ---
  
  Welcome to Ouch, a web app build like a simulation where you and your friends can exist in an `Existence` together. 
  
  In this `Existence` you can chat to each other and perform commands.. that well associate with your feeling of Ouch.
  
  You can increase you Ouch level and race up the leaderboard to see who is the most....yeah I know and much more!
  
  ---
  
  ## Commands
  
    "-darmode" Toggles dark mode.
    "-exit" Exit the simulation
    "-something"
    

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

## Future Ideas
 * Ouch is increased by keywords and songs played
 
---
## Technical Stuff

#### File Organization

    Working on it

#### Client-Server Communcation

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
