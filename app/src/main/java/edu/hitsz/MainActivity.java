package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.sound.SoundManager;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "game_settings";
    private static final String KEY_SOUND_ON = "sound_on";
    private static final String KEY_ONLINE_ON = "online_on";
    private static final String KEY_SERVER_HOST = "server_host";
    private static final String KEY_SERVER_PORT = "server_port";
    private static final String KEY_PLAYER_ID = "player_id";
    private static final String DEFAULT_SERVER_HOST = "10.0.2.2";
    private static final int DEFAULT_SERVER_PORT = 9999;
    private boolean soundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 持久化音效开关，确保菜单设置与游戏内行为一致。
        soundOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SOUND_ON, true);
        boolean onlineOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_ONLINE_ON, false);

        SwitchCompat soundSwitch = findViewById(R.id.switch_sound);
        SwitchCompat onlineSwitch = findViewById(R.id.switch_online);
        EditText hostInput = findViewById(R.id.et_server_host);
        EditText portInput = findViewById(R.id.et_server_port);
        EditText playerIdInput = findViewById(R.id.et_player_id);

        soundSwitch.setChecked(soundOn);
        onlineSwitch.setChecked(onlineOn);
        hostInput.setText(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_SERVER_HOST, DEFAULT_SERVER_HOST));
        portInput.setText(String.valueOf(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)));
        playerIdInput.setText(getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_PLAYER_ID, buildDefaultPlayerId()));

        SoundManager.setSoundOn(soundOn);
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundOn = isChecked;
            SoundManager.setSoundOn(isChecked);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_SOUND_ON, isChecked)
                    .apply();
        });

        updateOnlineInputVisibility(onlineOn, hostInput, portInput, playerIdInput);
        onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_ONLINE_ON, isChecked)
                    .apply();
            updateOnlineInputVisibility(isChecked, hostInput, portInput, playerIdInput);
        });

        findViewById(R.id.btn_easy).setOnClickListener(v -> startGame("easy", onlineSwitch, hostInput, portInput, playerIdInput));
        findViewById(R.id.btn_medium).setOnClickListener(v -> startGame("medium", onlineSwitch, hostInput, portInput, playerIdInput));
        findViewById(R.id.btn_hard).setOnClickListener(v -> startGame("hard", onlineSwitch, hostInput, portInput, playerIdInput));
        findViewById(R.id.btn_exit).setOnClickListener(v -> {
            moveTaskToBack(true);
            finishAffinity();
        });
    }

    private void startGame(
            String difficulty,
            SwitchCompat onlineSwitch,
            EditText hostInput,
            EditText portInput,
            EditText playerIdInput
    ) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameActivity.EXTRA_SOUND_ON, soundOn);

        boolean onlineMode = onlineSwitch.isChecked();
        intent.putExtra(GameActivity.EXTRA_ONLINE_MODE, onlineMode);
        if (onlineMode) {
            String host = safeText(hostInput.getText() == null ? null : hostInput.getText().toString(), DEFAULT_SERVER_HOST);
            int port = parsePort(portInput.getText() == null ? null : portInput.getText().toString(), DEFAULT_SERVER_PORT);
            String playerId = safeText(playerIdInput.getText() == null ? null : playerIdInput.getText().toString(), buildDefaultPlayerId());

            intent.putExtra(GameActivity.EXTRA_SERVER_HOST, host);
            intent.putExtra(GameActivity.EXTRA_SERVER_PORT, port);
            intent.putExtra(GameActivity.EXTRA_PLAYER_ID, playerId);

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_SERVER_HOST, host)
                    .putInt(KEY_SERVER_PORT, port)
                    .putString(KEY_PLAYER_ID, playerId)
                    .apply();
        }

        startActivity(intent);
    }

    private static void updateOnlineInputVisibility(
            boolean onlineOn,
            EditText hostInput,
            EditText portInput,
            EditText playerIdInput
    ) {
        int visibility = onlineOn ? View.VISIBLE : View.GONE;
        hostInput.setVisibility(visibility);
        portInput.setVisibility(visibility);
        playerIdInput.setVisibility(visibility);
    }

    private static String safeText(String text, String fallback) {
        if (TextUtils.isEmpty(text)) {
            return fallback;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static int parsePort(String text, int fallback) {
        if (TextUtils.isEmpty(text)) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(text.trim());
            return (parsed >= 1 && parsed <= 65535) ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String buildDefaultPlayerId() {
        long now = System.currentTimeMillis();
        return "player-" + (now % 10000);
    }
}
