# Personal-Server

This is a simple terminal-based TCP server written in Java.  
It is not a web server.

A client connects to the server over a raw TCP socket, enters a username and password, and if authentication succeeds, the client can send commands to the server. The server processes those commands and sends the results back to the client.

This project is meant to demonstrate basic client–server communication, authentication, multithreading, and command handling in Java, without executing arbitrary OS shell commands.

---

What this server does

- Listens on a TCP port using ServerSocket
- Accepts multiple clients (one thread per client)
- Requires username/password login before interaction
- Provides a restricted, command-based interface
- Executes only whitelisted commands implemented in Java
- Logs client connections, commands, and disconnects on the server side
- Restricts all file operations to a single working directory

---

Available commands

After logging in, the client can run:

help  
Shows available commands

echo <text>  
Echoes text back to the client

time  
Returns the server time

pwd  
Shows the server working directory

ls  
Lists files in the working directory

touch <filename>  
Creates an empty file in the working directory

cat <filename>  
Prints the contents of a text file

exit / quit  
Closes the connection

Only these commands are supported. Arbitrary shell commands (bash, rm, cd, etc.) are intentionally not allowed.

---

Authentication

Credentials are hard-coded for now:

Username: user  
Password: pass

This is intentional for simplicity and can be changed in the authentication logic.

---

Project structure

All code is in a single file:

Server.java

- Server: starts the TCP server and accepts connections
- ClientHandler: handles authentication and per-client interaction
- Shell: processes and executes whitelisted commands

---

Requirements

- Windows
- Java JDK 17+ (JDK 21 recommended)
- Git Bash or PowerShell
- Optional: Nmap (for ncat client connections)

---

Compile

javac Server.java

---

Run the server

java Server 0.0.0.0 5555

Using 0.0.0.0 listens on all interfaces.  
Using 127.0.0.1 limits connections to the local machine.

---

Connect as a client

Using ncat:

ncat 127.0.0.1 5555

Log in with the credentials above when prompted.

---

Notes

- This is not a remote shell
- No OS command execution
- No encryption or TLS
- Authentication is minimal and for learning purposes
- Intended for personal projects and educational use, not production deployment
