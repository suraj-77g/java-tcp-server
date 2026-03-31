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

    /**
     * Reads data from a client channel when it becomes readable.
     * 
     * Is this blocking or non-blocking?
     * The 'clientChannel.read(buffer)' call is NON-BLOCKING because we set 
     * 'configureBlocking(false)'. It returns immediately. If there's no data, 
     * it returns 0. If the client disconnected, it returns -1.
     * 
     * Is the single thread blocked when bytes are being read?
     * Technically, yes, but only for the very short time it takes the OS to copy 
     * bytes from its kernel buffers into our application's 'ByteBuffer'. This 
     * is extremely fast. However, the thread IS blocked from handling OTHER 
     * client events while it is executing this method.
     */
    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // Allocate a buffer to hold incoming data. In a real NIO server, 
        // you might reuse buffers to avoid frequent GC (Garbage Collection).
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        String address = clientChannel.getRemoteAddress().toString();

        // 1. NON-BLOCKING READ: Attempts to read data into the buffer.
        // Returns the number of bytes read (0 if none available, -1 if EOF).
        int bytesRead = clientChannel.read(buffer);
        
        if (bytesRead == -1) {
            // Client closed the connection (EOF)
            System.out.println("[SERVER] Client disconnected: " + address);
            clientChannel.close(); // Close the channel
            key.cancel();          // Stop the selector from watching this key
            return;
        }

        // 2. PREPARE FOR PROCESSING: 'flip()' switches the buffer from 
        // 'writing mode' (receiving from channel) to 'reading mode' (processing by us).
        buffer.flip();
        
        // 3. EXTRACT DATA: Read the bytes out of the buffer.
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String message = new String(data).trim();

        if (!message.isEmpty()) {
            System.out.println("[" + address + "] (NIO) says: " + message);
            
            // SHOULD WE USE A WORKER POOL?
            // If the processing of this message was slow (e.g., database query, 
            // complex calculation), we SHOULD hand it off to a separate 
            // ExecutorService (Worker Pool). This prevents the Selector thread 
            // from being stalled, ensuring the server stays responsive to 
            // other new connections and messages.
        }
    }
}
