
# MultiChat 🤖💬

The intention of this excercise was to refactor a more or less functional project.

Focus was to build in concurrency functionality and the MVC Pattern and also to restruct the code with inheritance.

[GitHub Link to the project](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr)

## Authors
We are a small team of Computer Science students from the Zurich University of Applied Sciences.

- [@unverjoh](https://github.zhaw.ch/unveryoh)
- [@jasarard](https://github.zhaw.ch/jasarard)
- [@gabricyr](https://github.zhaw.ch/gabricyr)


## Configuration
The project is build with [Gradle](https://gradle.org/) and available on GitHub through the link above.
The user interface is build in [JavaFX](https://openjfx.io/). 

To start the project you have to manually start each client (with gradle run class [Client](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/client/src/main/java/ch/zhaw/pm2/multichat/client/Client.java)).

Then make gradle run for the [Server](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/server/src/main/java/ch/zhaw/pm2/multichat/server/Server.java) only once even for multiple clients!
## Documentation of the refactoring process
The given code was very unstructured and far away from the clean code ideas.
The functionality was mostly given. The only thing was that not more then one client could connect to the server which resulted in the fact that a dialog via the chat window was impossible. 

### Approach

All issues are documented in the [GitHub Issue register (see "Closed" for closed issues)](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/issues) .  
The issues are split up in [Functional](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/.github/ISSUE_TEMPLATE/functional-bug.md) and [Structural](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/.github/ISSUE_TEMPLATE/structural-bug.md) issues.
To make it clear we defined two [issue templates](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/tree/main/.github/ISSUE_TEMPLATE).

In a first attempt we fixed the fundamental functional issues like the connection of multiple clients to the server. After this we structured the given code with [inheritance](https://en.wikipedia.org/wiki/Inheritance_(object-oriented_programming))
and the [MVC Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller).

A quick overview of the refactoring process:

    1. Fix fundamental functional issues
    2. Fix structural issues (inheritance and MVC)
    3. refactoring wokring code (new appeard issues due to already did refactoring)

### Structure

Please check out the [class diagram](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/tree/main/diagram) of the project!

#### Inheritance:

We realized that [ClientConnectionHandler](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/client/src/main/java/ch/zhaw/pm2/multichat/client/ClientConnectionHandler.java) and [ServerConnectionHandler](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/server/src/main/java/ch/zhaw/pm2/multichat/server/ServerConnectionHandler.java)
had code duplication which we then solved with a super class called [ConnectionHandler](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/protocol/src/main/java/ch/zhaw/pm2/multichat/protocol/ConnectionHandler.java).
We choosed this structure because of the already mentioned code duplication. 

#### MVC:

We realized that [ClientConnectionHandler](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/client/src/main/java/ch/zhaw/pm2/multichat/client/ClientConnectionHandler.java) manipulated [ChatWindowController](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/client/src/main/java/ch/zhaw/pm2/multichat/client/ChatWindowController.java)
and there was a class ClientMessageList which also manipulated ChatWindowController. This injured the MVC Pattern. Therefore we created a new class called [ClientInfo](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/blob/main/client/src/main/java/ch/zhaw/pm2/multichat/client/ClientInfo.java) which represents the model of the MVC Pattern.
Within the class ClientInfo we binded the properties of the FXML-File which then gets updated if something changes. This optimizes the code because like this only one class updated the appearance of the GUI which matches the guidlines of MVC. 

#### Why this structure?

Which the choosed structure we eliminated codeduplication of ClientConnectionHandler and ServerConnectionHandler which optimizes the adaptability of changes in the future and also secures better maintainability.
It is also a step to meet the Clean Code rules. 

The same approach is reached with the fix of the MVC Pattern due to the implementation of the new class ClientInfo. 

#### Future Implementations:

##### Implementation 1:
In this [issue](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/issues/13) we described the problem that you must know the username of the client you want to send a direct message to.
In a further refactoring process we could implement the output of all current registred clients. Or even a search function wich the real name which implies then that the client must registrate himself. 

---

##### Implementation 2:

The design of the GUI is very basic it described in this [issue](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/issues/30). In the future the implementation of a CSS file for design would be a considerable implementation.

---

##### Implementation 3:

In this [issue](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/issues/20) we described that only the received messages are shown und the conversation history.
The sent messages should also appear in the conversation history to complete the dialog history. 

---

##### Implementation 4:

An implementation could be that the server gets started automaticlly if the first clients wants to connect to a server.
A friend of him could then connect to the running server by entering the host name. Now you can connect onl to de default server. 
## Bugs
All known and not implemented bugs are listed as [open issues](https://github.zhaw.ch/PM2-IT21aWIN-fame-rayi-wahl/Uebung-hk2-unveryoh-jasarard-gabricyr/issues). 
