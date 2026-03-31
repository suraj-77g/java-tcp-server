# Java TCP Server Examples

This project demonstrates two different patterns for building a TCP server in Java: **Blocking I/O (BIO)** and **Non-blocking I/O (NIO)**.

## 1. Multi-threaded Blocking Server (BIO)
**File:** `src/main/java/com/example/SimpleTCPServer.java`

This version uses the traditional `java.net.ServerSocket`. It blocks the main thread while waiting for connections and spawns a new thread for every client connection.

### How to Run:
```bash
javac src/main/java/com/example/SimpleTCPServer.java
java -cp src/main/java com.example.SimpleTCPServer
```

---

## 2. Non-blocking Server (NIO)
**File:** `src/main/java/com/example/NonBlockingTCPServer.java`

This version uses `java.nio` (New I/O). It uses a single thread and a `Selector` to monitor multiple channels (connections). It only processes a channel when it actually has data ready to be read, making it much more scalable for thousands of concurrent connections.

### Key Concepts:
- **`ServerSocketChannel`**: A non-blocking version of `ServerSocket`.
- **`Selector`**: A multiplexer that watches multiple channels for events (`OP_ACCEPT`, `OP_READ`).
- **`ByteBuffer`**: Used to read and write data from channels.

### How to Run:
```bash
javac src/main/java/com/example/NonBlockingTCPServer.java
java -cp src/main/java com.example.NonBlockingTCPServer
```

---

## Testing Either Server
You can connect and send messages using `nc` (netcat):

```bash
echo "Hello Server" | nc localhost 8080
```

Or interactively via `telnet`:
```bash
telnet localhost 8080
```
