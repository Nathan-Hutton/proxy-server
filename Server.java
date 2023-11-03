import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Server
{
    public static final int PORT = 8080;
    private static final Executor exec = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        try (
             ServerSocket serverSocket = new ServerSocket(PORT);
        ){
            while (true) {
                // Wait for clients to connect
                // The argument of Connection is the client socket
                Runnable task = new Connection(serverSocket.accept());
                exec.execute(task);
            }
        }
        catch (IOException e) {
            System.out.println("An IOException has occurred");
        }
    }
}
