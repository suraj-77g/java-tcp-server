package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * A non-blocking TCP server using Java NIO (Selectors, Channels, Buffers).
 * This server uses a single thread to handle multiple connections simultaneously.
 */
public class NonBlockingTCPServer {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try (
            // 1. Open a ServerSocketChannel and bind it
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            // 2. Open a Selector
            Selector selector = Selector.open()
        ) {
            serverChannel.bind(new InetSocketAddress(PORT));
            // 3. CRITICAL: Set the channel to non-blocking mode
            serverChannel.configureBlocking(false);
            
            // 4. Register the channel with the selector for 'accept' events
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Non-blocking TCP Server started on port " + PORT);

            while (true) {
                // 5. Block until at least one event is ready
                selector.select();

                // 6. Get the keys for the events that occurred
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        // 7. Handle a new client connection
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        // 8. Handle a client sending data
                        handleRead(key);
                    }
                    
                    // 9. Remove the key from the set as we've handled it
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] " + e.getMessage());
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        String address = clientChannel.getRemoteAddress().toString();
        
        System.out.println("\n[SERVER] New connection from: " + address);
        
        // Register the client for 'read' events
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        String address = clientChannel.getRemoteAddress().toString();

        int bytesRead = clientChannel.read(buffer);
        
        if (bytesRead == -1) {
            // Client closed the connection
            System.out.println("[SERVER] Client disconnected: " + address);
            clientChannel.close();
            key.cancel();
            return;
        }

        // Flip the buffer to prepare for reading from it
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String message = new String(data).trim();

        if (!message.isEmpty()) {
            System.out.println("[" + address + "] (NIO) says: " + message);
        }
    }
}
