package edu.hitsz.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScoreSyncClient {

    public interface Listener {
        void onConnected();

        void onDisconnected(String reason);

        void onPeerScore(int score, boolean isDead);

        void onError(String error);
    }

    private static final String TYPE_HELLO = "hello";
    private static final String TYPE_SCORE = "score";
    private static final String TYPE_DEAD = "dead";

    private final String host;
    private final int port;
    private final String playerId;
    private final Listener listener;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readThread;
    private final Object closeLock = new Object();
    private volatile boolean disconnectedNotified = false;

    public ScoreSyncClient(String host, int port, String playerId, Listener listener) {
        this.host = host;
        this.port = port;
        this.playerId = playerId;
        this.listener = listener;
    }

    public void connect() {
        if (running.get()) {
            return;
        }
        readThread = new Thread(this::connectAndRead, "score-sync-client");
        readThread.start();
    }

    private void connectAndRead() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8")), true);
            running.set(true);
            if (listener != null) {
                listener.onConnected();
            }
            send(TYPE_HELLO, 0);

            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                handleIncoming(line);
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onError(e.getMessage() == null ? "network error" : e.getMessage());
            }
        } finally {
            closeInternal("connection closed");
        }
    }

    public void sendScore(int score) {
        send(TYPE_SCORE, score);
    }

    public void sendDead(int finalScore) {
        send(TYPE_DEAD, finalScore);
    }

    private void send(String type, int score) {
        if (!running.get()) {
            return;
        }
        sendExecutor.execute(() -> {
            if (writer == null) {
                return;
            }
            try {
                JSONObject json = new JSONObject();
                json.put("type", type);
                json.put("playerId", playerId);
                json.put("score", score);
                writer.println(json.toString());
                writer.flush();
            } catch (JSONException ignored) {
                // no-op
            }
        });
    }

    private void handleIncoming(String line) {
        try {
            JSONObject json = new JSONObject(line);
            String sender = json.optString("playerId", "");
            if (playerId.equals(sender)) {
                return;
            }
            String type = json.optString("type", "");
            int score = json.optInt("score", 0);
            if (TYPE_SCORE.equals(type) && listener != null) {
                listener.onPeerScore(score, false);
            } else if (TYPE_DEAD.equals(type) && listener != null) {
                listener.onPeerScore(score, true);
            }
        } catch (JSONException ignored) {
            // Ignore malformed lines from server.
        }
    }

    public void close() {
        closeInternal("closed by client");
    }

    private void closeInternal(String reason) {
        synchronized (closeLock) {
            running.set(false);
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                // no-op
            }
            if (writer != null) {
                writer.close();
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {
                // no-op
            }
            sendExecutor.shutdownNow();
            if (!disconnectedNotified && listener != null) {
                disconnectedNotified = true;
                listener.onDisconnected(reason);
            }
        }
    }
}
