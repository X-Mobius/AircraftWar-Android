import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple relay server for Lab6 online battle.
 * It forwards each line to all connected peers except sender.
 */
public class ScoreSyncServer {

    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {
        int port = 9999;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                port = 9999;
            }
        }
        new ScoreSyncServer().start(port);
    }

    private void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ScoreSyncServer started on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                ClientSession session = new ClientSession(client);
                sessions.add(session);
                new Thread(session, "session-" + client.getPort()).start();
            }
        }
    }

    private void broadcast(String message, ClientSession source) {
        for (ClientSession session : sessions) {
            if (session != source) {
                session.send(message);
            }
        }
    }

    private class ClientSession implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        ClientSession(Socket socket) {
            this.socket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8")), true);
            } catch (IOException e) {
                close();
            }
        }

        @Override
        public void run() {
            if (in == null || out == null) {
                return;
            }
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    broadcast(line, this);
                }
            } catch (IOException ignored) {
                // no-op
            } finally {
                sessions.remove(this);
                close();
            }
        }

        void send(String message) {
            if (out != null) {
                out.println(message);
                out.flush();
            }
        }

        void close() {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
                // no-op
            }
            if (out != null) {
                out.close();
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {
                // no-op
            }
        }
    }
}
