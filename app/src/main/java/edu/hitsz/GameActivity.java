package edu.hitsz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicBoolean;

import edu.hitsz.application.AbstractGame;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.network.ScoreSyncClient;
import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;
import edu.hitsz.sound.SoundManager;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND_ON = "sound_on";
    public static final String EXTRA_ONLINE_MODE = "online_mode";
    public static final String EXTRA_SERVER_HOST = "server_host";
    public static final String EXTRA_SERVER_PORT = "server_port";
    public static final String EXTRA_PLAYER_ID = "player_id";

    private AbstractGame gameView;
    private ScoreDao scoreDao;

    private boolean finalResultHandled = false;
    private boolean onlineMode = false;
    private boolean remoteDead = false;
    private boolean peerDisconnected = false;
    private int localFinalScore = -1;
    private int remoteScore = 0;
    private int remoteFinalScore = -1;
    private int lastSyncedScore = Integer.MIN_VALUE;
    private String playerId = "player";

    private TextView onlineScoreView;
    private AlertDialog waitingDialog;
    private ScoreSyncClient scoreSyncClient;
    private final AtomicBoolean saveInProgress = new AtomicBoolean(false);

    private final Handler scoreSyncHandler = new Handler(Looper.getMainLooper());
    private final Runnable scoreSyncTask = new Runnable() {
        @Override
        public void run() {
            if (gameView == null || scoreSyncClient == null || finalResultHandled) {
                return;
            }
            int localScore = localFinalScore >= 0 ? localFinalScore : gameView.getScore();
            if (localFinalScore < 0 && localScore != lastSyncedScore) {
                scoreSyncClient.sendScore(localScore);
                lastSyncedScore = localScore;
            }
            updateOnlineScoreView(localScore);
            scoreSyncHandler.postDelayed(this, 250L);
        }
    };

    private final Handler gameUiHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == AbstractGame.MSG_GAME_OVER) {
            onLocalGameOver(msg.arg1);
            return true;
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageManager.init(getApplicationContext());
        SoundManager.init(getApplicationContext());

        String difficulty = "easy";
        boolean soundOn = true;
        String serverHost = "10.0.2.2";
        int serverPort = 9999;

        if (getIntent() != null) {
            String extra = getIntent().getStringExtra(EXTRA_DIFFICULTY);
            if (!TextUtils.isEmpty(extra)) {
                difficulty = extra;
            }
            soundOn = getIntent().getBooleanExtra(EXTRA_SOUND_ON, true);
            onlineMode = getIntent().getBooleanExtra(EXTRA_ONLINE_MODE, false);

            String extraHost = getIntent().getStringExtra(EXTRA_SERVER_HOST);
            if (!TextUtils.isEmpty(extraHost)) {
                serverHost = extraHost.trim();
            }
            serverPort = getIntent().getIntExtra(EXTRA_SERVER_PORT, 9999);

            String extraPlayerId = getIntent().getStringExtra(EXTRA_PLAYER_ID);
            if (!TextUtils.isEmpty(extraPlayerId)) {
                playerId = extraPlayerId.trim();
            }
        }

        if (TextUtils.isEmpty(playerId)) {
            playerId = buildDefaultPlayerId();
        }

        SoundManager.setSoundOn(soundOn);
        scoreDao = new ScoreDaoImpl(this);

        gameView = Main.createGame(this, difficulty);
        gameView.setUiHandler(gameUiHandler);

        FrameLayout root = new FrameLayout(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        if (onlineMode) {
            attachOnlineScoreView(root);
        }

        setContentView(root);

        if (onlineMode) {
            initScoreSync(serverHost, serverPort, playerId);
        }
    }

    private void onLocalGameOver(int finalScore) {
        if (finalResultHandled || isFinishing() || isDestroyed()) {
            return;
        }

        localFinalScore = finalScore;

        if (!onlineMode) {
            showSinglePlayerGameOverDialog(finalScore);
            return;
        }

        if (scoreSyncClient != null) {
            scoreSyncClient.sendDead(finalScore);
        }

        if (remoteDead || peerDisconnected) {
            dismissWaitingDialog();
            showOnlineResultDialog();
        } else {
            showWaitingDialog();
        }
        updateOnlineScoreView(finalScore);
    }

    private void showSinglePlayerGameOverDialog(int finalScore) {
        if (finalResultHandled || isFinishing() || isDestroyed()) {
            return;
        }
        finalResultHandled = true;

        EditText input = new EditText(this);
        input.setHint(R.string.game_over_input_hint);
        input.setSingleLine();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.game_over_title)
                .setMessage(getString(R.string.game_over_message, finalScore))
                .setView(input)
                .setCancelable(false)
                .setPositiveButton(R.string.game_over_confirm, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String playerName = input.getText() == null ? "" : input.getText().toString().trim();
            if (TextUtils.isEmpty(playerName)) {
                playerName = "Player";
            }
            saveScoreAndOpenRank(playerName, finalScore, dialog);
        }));

        dialog.show();
    }

    private void showOnlineResultDialog() {
        if (finalResultHandled || isFinishing() || isDestroyed()) {
            return;
        }
        finalResultHandled = true;

        int myScore = Math.max(0, localFinalScore);
        int peerScore = remoteDead ? remoteFinalScore : remoteScore;
        String peerInfo = remoteDead
                ? String.valueOf(peerScore)
                : (peerDisconnected ? "N/A (disconnected)" : String.valueOf(peerScore));
        String result = calcResult(myScore, peerScore, remoteDead);

        if (scoreSyncClient != null) {
            scoreSyncClient.close();
        }

        EditText input = new EditText(this);
        input.setHint(R.string.game_over_input_hint);
        input.setSingleLine();

        String message = "My Score: " + myScore
                + "\nPeer Score: " + peerInfo
                + "\nResult: " + result
                + "\nEnter player name to save local score";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Online Battle Over")
                .setMessage(message)
                .setView(input)
                .setCancelable(false)
                .setPositiveButton(R.string.game_over_confirm, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String playerName = input.getText() == null ? "" : input.getText().toString().trim();
            if (TextUtils.isEmpty(playerName)) {
                playerName = "Player";
            }
            saveScoreAndOpenRank(playerName, myScore, dialog);
        }));

        dialog.show();
    }

    private static String calcResult(int myScore, int peerScore, boolean peerFinished) {
        if (!peerFinished) {
            return "peer status unavailable";
        }
        if (myScore > peerScore) {
            return "WIN";
        }
        if (myScore < peerScore) {
            return "LOSE";
        }
        return "DRAW";
    }

    private void showWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            return;
        }
        waitingDialog = new AlertDialog.Builder(this)
                .setTitle("You are down")
                .setMessage("Waiting for opponent to finish...")
                .setCancelable(false)
                .create();
        waitingDialog.show();
    }

    private void dismissWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
        }
        waitingDialog = null;
    }

    private void attachOnlineScoreView(FrameLayout root) {
        onlineScoreView = new TextView(this);
        onlineScoreView.setTextSize(15f);
        onlineScoreView.setTextColor(Color.WHITE);
        onlineScoreView.setBackgroundColor(0x55000000);
        onlineScoreView.setPadding(24, 18, 24, 18);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.topMargin = 24;
        params.rightMargin = 24;
        root.addView(onlineScoreView, params);

        updateOnlineScoreView(0);
    }

    private void initScoreSync(String host, int port, String myPlayerId) {
        scoreSyncClient = new ScoreSyncClient(host, port, myPlayerId, new ScoreSyncClient.Listener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> Toast.makeText(GameActivity.this,
                        "Connected to battle server", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDisconnected(String reason) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || finalResultHandled) {
                        return;
                    }
                    peerDisconnected = true;
                    if (!TextUtils.isEmpty(reason)) {
                        Toast.makeText(GameActivity.this,
                                "Connection closed: " + reason,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (localFinalScore >= 0) {
                        dismissWaitingDialog();
                        showOnlineResultDialog();
                    }
                });
            }

            @Override
            public void onPeerScore(int score, boolean isDead) {
                runOnUiThread(() -> {
                    remoteScore = score;
                    if (isDead) {
                        remoteDead = true;
                        remoteFinalScore = score;
                        Toast.makeText(GameActivity.this,
                                "Opponent eliminated, final score: " + score,
                                Toast.LENGTH_SHORT).show();
                        if (localFinalScore >= 0 && !finalResultHandled) {
                            dismissWaitingDialog();
                            showOnlineResultDialog();
                            return;
                        }
                    }
                    int localScore = localFinalScore >= 0
                            ? localFinalScore
                            : (gameView == null ? 0 : gameView.getScore());
                    updateOnlineScoreView(localScore);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(GameActivity.this,
                        "Network error: " + error,
                        Toast.LENGTH_SHORT).show());
            }
        });

        scoreSyncClient.connect();
        scoreSyncHandler.post(scoreSyncTask);
    }

    private void updateOnlineScoreView(int localScore) {
        if (onlineScoreView == null) {
            return;
        }
        String peerStatus = remoteDead ? "DEAD" : (peerDisconnected ? "DISCONNECTED" : "ALIVE");
        onlineScoreView.setText("Me: " + localScore
                + "\nPeer: " + remoteScore
                + "\nStatus: " + peerStatus);
    }

    private static String buildDefaultPlayerId() {
        return "player-" + (System.currentTimeMillis() % 10000);
    }

    private void saveScoreAndOpenRank(String playerName, int finalScore, AlertDialog dialog) {
        if (!saveInProgress.compareAndSet(false, true)) {
            return;
        }

        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }

        String normalizedName = TextUtils.isEmpty(playerName) ? "Player" : playerName.trim();

        Thread saveThread = new Thread(() -> {
            try {
                if (scoreDao.existsPlayerName(normalizedName)) {
                    runOnUiThread(() -> {
                        saveInProgress.set(false);
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(GameActivity.this,
                                "Name already exists. Please choose another one.",
                                Toast.LENGTH_SHORT).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    });
                    return;
                }

                scoreDao.addRecord(new ScoreRecord(normalizedName, finalScore));

                runOnUiThread(() -> {
                    saveInProgress.set(false);
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    startActivity(new Intent(GameActivity.this, RankActivity.class));
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    saveInProgress.set(false);
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    Toast.makeText(GameActivity.this,
                            "Save failed. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                });
            }
        }, "save-score-thread");
        saveThread.start();
    }

    @Override
    protected void onDestroy() {
        scoreSyncHandler.removeCallbacksAndMessages(null);
        dismissWaitingDialog();
        if (scoreSyncClient != null) {
            scoreSyncClient.close();
        }
        SoundManager.release();
        super.onDestroy();
    }
}