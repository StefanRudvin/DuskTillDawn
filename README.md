# DuskTillDawn
A MUD game created using the Java RMI for CS3524 Distributed Systems and Security


![Game Screen](https://github.com/StefanRudvin/DuskTillDawn/blob/master/screenshots/IntroScreen.png)

## Requirements

* Java 1.8
* Java RMI
* RmiRegistry

## Instructions

1. make mud -B

2. rmiregistry 50010

3. java mud.MUDServerMainline 50010 50014

4. java mud.MUDClient [your hostname] 50010

## Features

CGS D:
* Implemented MUD game server, remote interface with implementation and a server mainline
* User can move in a direction

CGS C: 
* User can move to all directions (North/East/South/West)
* User can see other objects and players in the MUD
* User can pick up things in the MUD

CGS B:
* Three instances of MUDs are created on startup
* User is shown all MUDs at startup and has the option of choosing which one to join

CGS A5:
* User can create new MUDs with custom names at runtime with 'createmud' 
* Number of total players, number of players per MUD and number of total MUDs is restricted

CGS A4-A1:
* Amount of MUDs created can be changed by a constant in the server (i.e. user can create any number of MUDs)
* User can drop items
* User can see contents of his/her inventory
* User can exit game, which drops his/her items on the ground
* User can see a help menu which shows available commands
***
* User can see which MUDs are available at any time
* User can create a new MUD while playing the game
* User can change MUD at any time
* User can query total number of players, MUDs and number of players in current MUD during runtime
***
* Server and client has a polling solution, which times out and exits clients that have quit
* Users are notified when other users exit, are timed out or enter the game
***
* User can change maximum limits of total players, players per MUD and MUDs at any time


### Game Screen

![Game Screen](https://github.com/StefanRudvin/DuskTillDawn/blob/master/screenshots/GameScreen.png)
