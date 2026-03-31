package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple multi-threaded TCP server that listens for connections
 * and prints received messages to the console.
 */
public class SimpleTCPServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // Create a cached thread pool to handle multiple clients concurrently
        ExecutorService executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP Server started on port " + PORT);
            System.out.println("Waiting for connections...");

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getRemoteSocketAddress().toString();
                System.out.println("\n[SERVER] New client connected: " + clientAddress);

                // Handle the client connection in a separate thread
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("[SERVER ERROR] " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Runnable task that handles an individual client connection.
     */
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                // Read lines from the client until they disconnect
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[" + clientAddress + "] says: " + inputLine);
                }
            } catch (Exception e) {
                System.err.println("[CLIENT ERROR] (" + clientAddress + "): " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("[SERVER] Client disconnected: " + clientAddress);
                } catch (Exception e) {
                    System.err.println("[SOCKET CLOSE ERROR] " + e.getMessage());
                }
            }
        }
    }
}
