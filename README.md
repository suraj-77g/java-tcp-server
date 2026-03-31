# Simple Java TCP Server

A basic multi-threaded TCP server implemented in Java to demonstrate core networking concepts like `ServerSocket`, threading with `ExecutorService`, and stream-based I/O.

## Features
- **Multi-threaded**: Uses a cached thread pool to handle multiple client connections concurrently.
- **Message Logging**: Prints received messages from clients to the console, prefixed with the client's address.
- **Resource Management**: Properly closes client sockets and input streams.

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher.

### Compilation
Compile the source code from the project root:
```bash
javac src/main/java/com/example/SimpleTCPServer.java
```

### Running the Server
Start the server on port 8080:
```bash
java -cp src/main/java com.example.SimpleTCPServer
```

### Testing the Server
You can connect to the server using `telnet` or `netcat` (`nc`):

**Using Netcat:**
```bash
echo "Hello from client" | nc localhost 8080
```

**Using Telnet:**
```bash
telnet localhost 8080
# Type your message and press Enter
```

## How it Works
1. The server initializes a `ServerSocket` on port 8080.
2. It enters an infinite loop, waiting for client connections using `accept()`.
3. When a client connects, it submits a `ClientHandler` task to an `ExecutorService`.
4. The `ClientHandler` reads the input stream line-by-line and prints the content to the console until the client disconnects.
