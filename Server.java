import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Terminal-style TCP server (NOT a web server).
 *
 * Safety note:
 * - This implementation does NOT execute arbitrary OS shell commands.
 * - Instead, it supports a small set of whitelisted commands handled in Java.
 *
 * Usage:
 *   javac Server.java
 *   java Server <host> <port>
 *
 * Example:
 *   java Server 0.0.0.0 5555
 */
public class Server {

    public static void main(String[] args) {
        // ./Server host(args[0]) port(args[1])
        if (args.length != 2) {
            System.out.println("Invalid number of arguments");
            System.out.println("Usage: java Server <host> <port>");
            return;
        }

        // Initialize host and port
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Port must be an integer");
            return;
        }

        // Bind to address + port (host was previously ignored in your code)
        InetAddress bindAddr;
        try {
            bindAddr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Invalid host: " + host);
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(bindAddr, port));
            System.out.println("Socket server is running on " + host + ":" + port);

            while (true) {
                // Wait until a client requests a connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler, "client-" + clientSocket.getPort());
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}

/**
 * Handles a single client connection.
 */
class ClientHandler implements Runnable {

    private final Socket clientSocket;

    // Fix: parameter name and assignment were wrong in your code
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();

        // Restrict server-side file operations to a safe working directory
        Path workDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Shell shell = new Shell(workDir);

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            log(clientId, "Connected");

            // Greeting
            writeLine(out, "Welcome. This is a terminal-style server (whitelisted commands only).");
            writeLine(out, "Please login.");

            // Authentication flow
            if (!authenticate(in, out, clientId)) {
                log(clientId, "Authentication failed; closing connection");
                writeLine(out, "AUTH FAIL");
                return;
            }

            log(clientId, "Authentication success");
            writeLine(out, "AUTH OK");
            writeLine(out, "Type 'help' to see available commands. Type 'exit' to quit.");

            // Command loop
            while (true) {
                write(out, "> "); // prompt (no newline)

                String line = in.readLine();
                if (line == null) {
                    log(clientId, "Client disconnected (EOF)");
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                log(clientId, "CMD: " + line);

                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    writeLine(out, "Bye.");
                    log(clientId, "Client requested exit");
                    break;
                }

                String result = shell.handleCommand(line);
                writeLine(out, result);
            }

        } catch (IOException e) {
            log(clientId, "I/O error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
            log(clientId, "Connection closed");
        }
    }

    /**
     * Simple demo authentication.
     * For real systems: store salted password hashes, rate-limit, and use TLS.
     */
    private boolean authenticate(BufferedReader in, BufferedWriter out, String clientId) throws IOException {
        // Demo credentials (replace with your own secure storage if needed)
        final String validUser = "user";
        final String validPass = "pass";

        // Basic rate limiting: max 3 attempts
        for (int attempt = 1; attempt <= 3; attempt++) {
            write(out, "Username: ");
            String username = in.readLine();
            if (username == null) return false;

            write(out, "Password: ");
            String password = in.readLine();
            if (password == null) return false;

            boolean ok = validUser.equals(username.trim()) && validPass.equals(password.trim());
            if (ok) return true;

            log(clientId, "Auth attempt " + attempt + " failed for username='" + username + "'");
            writeLine(out, "Invalid credentials. Attempts left: " + (3 - attempt));
        }
        return false;
    }

    private void write(BufferedWriter out, String s) throws IOException {
        out.write(s);
        out.flush();
    }

    private void writeLine(BufferedWriter out, String s) throws IOException {
        out.write(s);
        out.write("\n");
        out.flush();
    }

    private void log(String clientId, String msg) {
        String ts = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        System.out.println("[" + ts + "][" + clientId + "] " + msg);
    }
}

/**
 * A "safe shell" that supports only whitelisted commands implemented in Java.
 * No arbitrary OS command execution.
 */
class Shell {

    private final Path workDir;

    public Shell(Path workDir) {
        this.workDir = workDir;
    }

    public String handleCommand(String input) {
        String[] parts = splitArgs(input);
        if (parts.length == 0) return "";

        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help":
                    return help();
                case "echo":
                    return echo(parts);
                case "time":
                    return ZonedDateTime.now().toString();
                case "pwd":
                    return workDir.toString();
                case "ls":
                    return ls();
                case "touch":
                    return touch(parts);
                case "cat":
                    return cat(parts);
                default:
                    return "Unknown command: " + parts[0] + "\nType 'help' for the list of commands.";
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String help() {
        return String.join("\n",
            "Available commands (whitelisted):",
            "  help                 - show this help",
            "  echo <text>          - print text",
            "  time                 - show server time",
            "  pwd                  - show server working directory",
            "  ls                   - list files in working directory",
            "  touch <filename>     - create an empty file in working directory",
            "  cat <filename>       - print a text file from working directory",
            "  exit / quit          - disconnect"
        );
    }

    private String echo(String[] parts) {
        if (parts.length <= 1) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) sb.append(' ');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private String ls() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
            for (Path p : stream) {
                sb.append(p.getFileName().toString());
                if (Files.isDirectory(p)) sb.append("/");
                sb.append("\n");
            }
        }
        String out = sb.toString().trim();
        return out.isEmpty() ? "(empty)" : out;
    }

    private String touch(String[] parts) throws IOException {
        if (parts.length != 2) return "Usage: touch <filename>";

        Path target = safeResolve(parts[1]);
        if (Files.exists(target)) return "File already exists: " + target.getFileName();

        Files.createFile(target);
        return "Created: " + target.getFileName();
    }

    private String cat(String[] parts) throws IOException {
        if (parts.length != 2) return "Usage: cat <filename>";

        Path target = safeResolve(parts[1]);
        if (!Files.exists(target)) return "No such file: " + target.getFileName();
        if (Files.isDirectory(target)) return "Is a directory: " + target.getFileName();

        // Read as UTF-8 text; for binary files, this will look weird (intended).
        return Files.readString(target, StandardCharsets.UTF_8);
    }

    /**
     * Prevent path traversal (e.g., ../../etc/passwd).
     */
    private Path safeResolve(String filename) {
        Path resolved = workDir.resolve(filename).normalize();
        if (!resolved.startsWith(workDir)) {
            throw new SecurityException("Path traversal denied");
        }
        return resolved;
    }

    /**
     * Splits input into arguments, supporting basic quoted strings:
     *   echo "hello world" -> [echo, hello world]
     */
    private String[] splitArgs(String input) {
        // Minimal parser: handles quotes but not escape sequences
        java.util.List<String> args = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (!inQuotes && Character.isWhitespace(c)) {
                if (cur.length() > 0) {
                    args.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }

        if (cur.length() > 0) args.add(cur.toString());
        return args.toArray(new String[0]);
    }
}
