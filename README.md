# NTU SC4051 Distributed Systems Project
## Remote File Server
This project aims to implement a system for remote file access based on client-server architecture using UDP communication. The system was implemented with two programming languages: the client program with `C++` and the server program with `Java`.
The server provides five remote file services for the clients:
- Read a file
- Insert content into a file
- Monitor updates of a file
- Delete a file
- Append a file into a file


### Server
---
To run the server program, make sure to have `JDK` installed.

To compile, from project directory run:

```shell
javac server/Server.java
```
To start the server, from project directory run:
```shell
java server.Server
```

### Client
---
You can either run the client program directly from Terminal or Visual Studio Code:
#### Run C++ directly from Terminal
To run the client program, make sure to have `gcc` installed.

To compile, from project directory run:
```shell
g++ client/Main.cpp client/Handler.cpp client/Client.cpp utils/ConsoleUI.cpp utils/Marshaller.cpp utils/UserInputReader.cpp -o client/Main.exe -lws2_32
```
To start the client, from project directory run:
```shell
client/Main.exe <serverAddress> <serverPort> <clientPort> <freshnessInterval>
```
E.g.
```shell
client/Main.exe 127.0.0.1 12345 65535 1
```
#### Run C++ with Visual Studio Code
Set up C++ in Visual Studio Code

- Video Tutorial : [Link](https://www.youtube.com/watch?v=DMWD7wfhgNY)

In command prompt (without <>):

```shell
build_client.bat <serverAddress> <serverPort> <clientPort> <freshnessInterval>
```

#### Possible Issues:

- Segmentation fault when running bat file

Open Terminal in Folder and run `./Main.exe <serverAddress> <serverPort> <clientPort> <freshnessInterval>`

#### Debugging

Press F5 in any .cpp files, debugging is possible by clicking on breakpoint
