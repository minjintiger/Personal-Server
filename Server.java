import java.io.*;
import java.net.*;

// Server
// 터미널 서버 (웹 서버 아님) --- 클라이언트 쌍방향 통신 
// 클라이언트가 서버에 접속할 떄 로그인/패스워드를 입력하고 서버에서는 이것을 확인하고 접속을 허용할건지 결정
// 클라이언트가 서버에 접속되면 서버 안에 있는 Shell.java가 실행되어 클라이언트가 명령어를 입력할 수 있게 해줌
// 그 결과를 클라이언트에게 보내줌.
// Ex) ls, touch ....
// ls ----> LICENSE README.md Server.java
// touch newfile.txt ---> ls ----> LICENSE README.md Server.java newfile.txt
//
//
//   Server ------------------------- Client
//           Connection Established
//               Login/Password
//            Connection Accepted
//
//   Server ------ Shell.java(실행) ------> Client
//   Server <----- Shell.java(명령) ------ Client
//   Server ------ Shell.java(결과) ------> Client
//   서버는 로그확인 가능할 수 있도록 로그 프린트

public class Server{

    public static void main(String[] args){

        // ./Server host(args[0]) port(args[1])
        if(args.length != 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        // Initialize host and port
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        // Socket 
        try{
            // bind to port
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Socket server is running on "+port);

            while(true){
                // waits until a client starts up and requests a connection on the host and port of this server.
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                // Create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
        
    }
}

// ClientHandler
class ClientHandler implements Runnable{

    private Socket clientSocket;

    public ClientHandler(Socket clienSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){

    }
}

// Shell
class Shell{

}

