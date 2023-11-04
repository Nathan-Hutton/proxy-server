import java.io.*;
import java.net.*;

public class Handler
{
    // This is invoked by a different thread
    public void process(Socket clientSocket) throws java.io.IOException {
        // For reading from client
        BufferedReader fromClient = null;
        // For writing to origin server
        DataOutputStream dataOut = null;
        // For reading from origin server
        BufferedInputStream bufferedIn = null;
        // For writing to client
        BufferedOutputStream bufferedOut = null;
        Socket originSocket = null;


        try {
            // =================================
            // Read and parse the client request
            // =================================
            fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientRequest = fromClient.readLine();
            
            // This is a special case where the request is for a favicon with no host name
            // Handling this would add more complexity than it's worth
            if (clientRequest.equals("GET /favicon.ico HTTP/1.1"))
                return;

            String originHost = clientRequest.split("/")[1].split(" ")[0];
            String resource = clientRequest.split(originHost)[1];
            
            // If we're requesting default documents
            if (resource.charAt(0) == ' ')
                resource = "/ HTTP/1.1";

            // ==============================
            // Write request to origin server
            // ==============================
            // Create a socket for the origin server
            originSocket = new Socket(originHost, 80);
            
            // Write request to origin server
            dataOut = new DataOutputStream(originSocket.getOutputStream());
            String request;
            request = "GET " + resource + "\r\nHost: " + originHost + "\r\nConnection: close\r\n\r\n";
            dataOut.writeBytes(request);
            dataOut.flush();

            // ============================================================
            // Read response back from origin server and write it to client
            // ============================================================
            bufferedIn = new BufferedInputStream(originSocket.getInputStream());
            bufferedOut = new BufferedOutputStream(clientSocket.getOutputStream());
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = bufferedIn.read(buffer)) != -1) {
                bufferedOut.write(buffer, 0, bytesRead);
                bufferedOut.flush();
            }
        }
        // We always get an UnknownHostException for the favicon.ico
        // It doesn't show up in the browser
        catch (IOException ioe) {
            System.out.println(ioe);
        }
        finally {
            if (fromClient != null)
                fromClient.close();
            if (originSocket != null)
                originSocket.close();
            if (dataOut != null)
                dataOut.close();
            if (bufferedIn != null)
                bufferedIn.close();
            if (bufferedOut != null)
                bufferedOut.close();
            if (clientSocket != null)
                clientSocket.close();
        }
    }
}
