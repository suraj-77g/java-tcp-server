# GEMINI.md - Project Context

## Project Overview
This is a Java-based project that demonstrates two fundamental patterns for building TCP servers:
1.  **Multi-threaded Blocking I/O (BIO):** Implemented in `SimpleTCPServer.java`. It uses a traditional `ServerSocket` and spawns a new thread (via a cached thread pool) for each client connection.
2.  **Non-blocking I/O (NIO):** Implemented in `NonBlockingTCPServer.java`. It utilizes `java.nio` components like `ServerSocketChannel`, `Selector`, and `ByteBuffer` to handle multiple concurrent connections within a single thread.

Both servers listen on port `8080` by default and log client messages to the console.

## Project Structure
- `src/main/java/com/example/`: Contains the source code.
  - `SimpleTCPServer.java`: The BIO implementation.
  - `NonBlockingTCPServer.java`: The NIO implementation.
- `README.md`: Provides basic descriptions and run commands.

## Building and Running

### Prerequisites
- Java Development Kit (JDK) installed and available in the PATH.

### Compilation
To compile both server implementations:
```bash
javac src/main/java/com/example/*.java
```

### Running the Blocking Server
```bash
java -cp src/main/java com.example.SimpleTCPServer
```

### Running the Non-blocking Server
```bash
java -cp src/main/java com.example.NonBlockingTCPServer
```

### Testing the Servers
You can use `netcat` (`nc`) or `telnet` to connect to the servers:
```bash
# Using netcat
echo "Hello from client" | nc localhost 8080

# Using telnet
telnet localhost 8080
```

## Development Conventions
- **Language:** Java (standard library only, no external dependencies like Maven or Gradle).
- **Package Structure:** Follows `com.example` naming convention.
- **Error Handling:** Uses `try-with-resources` for automatic resource management (sockets, channels, selectors). Errors are logged to `System.err`.
- **Concurrency (BIO):** Uses `ExecutorService` (specifically `Executors.newCachedThreadPool()`) to manage client handler threads.
- **Concurrency (NIO):** Uses a single-threaded event loop with a `Selector` to multiplex I/O operations.
- **Style:** Clear, documented code with comments explaining key networking and I/O concepts.
