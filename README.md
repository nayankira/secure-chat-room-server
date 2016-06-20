# secure-chat-room-server

secure-chat-room-client
This is a simple chat application, The chat system consists of two main distributed components: chat server and chat client. 
Chat clients are Java programs which can connect to a chat server.

The chat server is a Java application which can accept multiple incoming TCP connections. 
The chat server maintains a list of current chat rooms and chat clients can move between chat rooms.
Messages sent by a chat client are broadcast to all clients currently connected to the same chat room.

This application uses SSL (Secure Sockets Layer) to protect the communication between client and server 
and utilize the username and password to authenticate members.

Chat Server: In a nutshell, the chat server is primarily responsible for managing all the chat clients currently 
connected to the chat server and for distributing chat messages. The chat server listens for requests from clients 
for making new connections. When a client connects to the server: the server generates a unique id for the client 
which is guest followed by the smallest integer greater than 0 that is currently not in use by any other connected 
client, e.g. guest5 · · the server tells the client its id using an NewIdentity message the server adds the new 
client to the MainHall chat room ¨ this involves sending more messages which is explained later · The protocol that 
the chat server follows is specified exactly later in this project specification. All of the message types are specified 
exactly as well.

Client Command Line Interface: The client accepts input from standard input. Each line of input 
(terminated by a newline) is interpreted by the client as either a command or a message. 
If the line of input starts with a hash character "#" then it is interpreted as a command, otherwise it is interpreted as a message.

All protocol messages, i.e. sent between client and server, will be in the form of newline terminated JSON objects.

The functionalities involved in this project are: 
1. user authentication 
2. Default Id assignment 
3. Id change 
4. create, join, and delete chat rooms 
5. default room MainHall 
6. client request to display the list of rooms 
7. client request to display the list of members in room X 
8. the owner of room can kick out users 
9. the empty rooms will be deleted automatically 
10. quit from the application 11. automatically quit from the application when facing abrupt disconnection 
12.the owner of a room can block another user for a period of time

The application is executable as follows: 
java -jar chatserver.jar [-p port] 
java -jar chatclient.jar hostname [-p port]
