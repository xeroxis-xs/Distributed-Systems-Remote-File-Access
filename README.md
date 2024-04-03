### Server

From project directory run:

```shell
javac server/Server.java
java server.Server
```

### Client

#### Set up C++ in Visual Code

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
