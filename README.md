# Akka http + akka persistance (mongodb) + event sourcing

This demo shows how to use akka persitance to store messages (mongodb) in sequence for event sourcing. 

Every 5 messages a snapshot is saved to avoid receiving too much messages.

The application recover the state from messages stored or snapshots.


## Requirements

Mongodb