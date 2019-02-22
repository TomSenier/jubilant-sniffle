import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {

    //A list of all the client sockets output streams.
    //If there are 3 clients connected, the server will have 3 output streams, each on a different socket.
    //If the server wants to send a message to a client, it writes to the outputStream for that client socket.
    //If the server wants to send a message to all clients, it can iterate through this ArrayList, writing the message to each client socket output stream.
    private ArrayList clientOutputStreams = new ArrayList<>();
    int port = 6789;

    public static void main(String[] args){

        Server server = new Server();
        server.go();

    }

    /**
     * Method responsible for setting up ServerSocket and then listening for incoming requests from clients.
     *
     * If a request is received, a new clientSocket is created for that client, along with a corresponding PrintWriter object to enable the server
     * to send messages to the client.
     *
     * Creates and starts new Thread that calls ClientHandler method on newly created clientSockets.
     *
     */
    public void go(){

        try{
            //Create a ServerSocket, which allows client requests to be sent to server at specified port.
            ServerSocket serverSock = new ServerSocket(port);

            while(true){
                //Waits for an incoming connection and accepts once it is received.
                //Each connection with a client requires the server to create a new socket (clientSocket)
                //Calling the .accept() method on the ServerSocket object creates a new socket which is used to communicate with client sending the request to the server.
                Socket clientSocket = serverSock.accept();
                //Create a writer so we can write to the socket outputStream (i.e. send messages to the client).
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                //Add this writer object to the list of clientOutputStreams (we want to keep track of every client we can communicate with).
                clientOutputStreams.add(writer);
                //Create a new thread, which executes ClientHandler method on the newly created clientSocket (i.e. the newly created connection with the client)
                Thread t = new Thread(new ClientHandler(clientSocket));
                //Start the thread
                t.start();
                System.out.println("Got a connection");

                //Go back to beginning of loop. i.e. wait for an incoming client request, accept it, create new connection, create new thread for connection etc...

                //serverSock.setSoTimeout(1000);
                //clientSocket.setSoTimeout(500);

            }

        }catch(IOException ex){

            ex.printStackTrace();

        }


    }


    /**
     * Class to be used once a clientSocket has been created for a new client.
     *
     * This class 'does' something with the new client.
     *
     * Currently what it 'does' is create a BufferedReader from the clientSocket's input stream (i.e. incoming to the server from the client).
     * This is done when a ClientHandler object is constructed.
     *
     * When a ClientHandler object is run() the BufferedReader 'listens' for a message from the clientSocket.
     * The message is then printed to the Server console.
     * The server then calls the tellEveryone() method on the message. This writes the method to the PrintWriters of all the clientSocket outputStreams (i.e. sends the message to all clients).
     */
    public class ClientHandler implements Runnable{
        //We already have a PrintWriter set up for the clientSocket, but each clientSocket also needs a BufferedReader so we can read-from the socket as well as write-to.
        BufferedReader reader;
        Socket sock;

        public ClientHandler(Socket clientSocket){

            try{
                sock = clientSocket;
                //Create an InputStreamReader that reads the inputStream of the clientSocket
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                //The inputStream is basically computer gibberish, so pass it to a BufferedReader which translates it to a readable String.
                reader = new BufferedReader(isReader);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        //This is what is run when we create and start() a new Thread with a ClientHandler object.
        public void run(){

            String message;

            try{//Reader waits for input from clientSocket inputStream (i.e. a message sent from the client)
                while ((message = reader.readLine())!=null){
                    System.out.println(message);
                    //Relay message to all clients
                    tellEveryone(message);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * Relays message to all clients with a clientOutputSteam
     * @param message Message to be sent
     */
    public void tellEveryone(String message){

        Iterator it = clientOutputStreams.iterator();
        while(it.hasNext()){

            try{
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }


    
}
