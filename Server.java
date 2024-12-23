import java.io.*;
import java.net.*;

// Server

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

