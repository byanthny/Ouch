<p align="center">
<img src="docs/imgs/logo.png" width="250"/>
</p>

## Table of Contents
* [The Simulation](#the-simulation)
* [Project Description](#description)
* [Commands](#commands)
* [Technical Stuff](#technical-stuff)
    * [File Structure](#file-organization)
    * [Client-Server Communication](#client-server-communcation)
* [Updates](#updates)
---

## The Simulation
  Ah yes...there comes a point in person life were the simulation doesn't treat you so well. Sometimes I wonder what the `simulators` were thinking. 
  
  What are `simulators`? 
  
  Glad you asked, simulators are the controllers of our simulations, this simulation, every simulation. After all we live in a simulation, prove me wrong. 
  
  Now, we're going to define the elements of a **simulation**, in a way we see fit. There are no right or wrong answers here because... well we have no way of breaking the simulation. I mean if you know how to escape and contact the simulator(s), let me know. 
  
<p align="center">
<img src="docs/imgs/somethingspecial.jpg" width="250"/>
</p>
  
  Anyway, we have defined `Simulators` as the controllers of the simulation. 
  
  We will use `Existences` as the simulation instance. These `Existences` holds everything that is part of the simulation. For sake of simplicity we will have two options human and non-human to describe objects inside the simulation. 
  
  We will use `Quiddities` to describe the essence of a human. We will leave the definition of *human* up to you. 
  
  Anything that is non-human or not a `Quiddity`  we will call them `Infraquiddities`  or `InfraQuiddity`. 
  
  I haven't lost you yet, I hope. 
  
  Now why does this matter? Let's go back to feeling... *Ouch*. The feeling of *Ouch* is part of a `Quiddities` life, yes.
  
  ---
  #### Description
  Welcome to Ouch, a web app built like a simulation where you and your friends can exist in an `Existence` together. 
  
  In this `Existence` you can chat to each other and perform commands.. that well associate with your feeling of Ouch.
  
  You can increase you Ouch level and race up the leaderboard to see who is the most....yeah I know and much more!
 
  ## Commands
  
  | Commands  | Description |
  | ------------- | ------------- |
  | -theme  | Toggles between themes.  |
  | -exit  | Exit the simulation  |
  
  or https://sim-ouch.herokuapp.com/actions 

## Technical Stuff
Current status of Ouch API: https://sim-ouch.herokuapp.com/status 
Map of Ouch API: https://sim-ouch.herokuapp.com/map 
#### File Organization

```text
Ouch/
├── backend/ *source code for backend
│   ├── src/
│   └── gradle/wrapper
│
├── docs/ *Production files for Github Pages Website (minified)
│   ├── css/
│   ├── imgs/
│   └── js/
│
└── frontend/   *source code for frontend
    ├── js/
    └── less/

```

#### Client-Server Communication

Refer to: https://sim-ouch.herokuapp.com/map      
    
## Updates
* Reconnect on disconnect
* Removed annoying scrollbar
* Grunt works correctly
* Added loading screen while connecting
* Smoother animations
* Cleaned Up Code
 
---
