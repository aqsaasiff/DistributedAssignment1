import java.io.*;
import java.net.*;
import java.util.*;

public class CollaborativeWhiteboardServer {
    private static final int PORT = 12345; // port for the server
    private static List<ClientHandler> clients = new ArrayList<>(); // list of connected clients

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // create a new handler for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to broadcast messages to all clients
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // inner class for handling individual client connections
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String role;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.role = clients.isEmpty() ? "admin" : "user"; // first client becomes admin
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                sendMessage("Welcome! You are connected as " + role);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    CollaborativeWhiteboardServer.broadcast(message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void closeConnection() {
            try {
                socket.close();
                clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
