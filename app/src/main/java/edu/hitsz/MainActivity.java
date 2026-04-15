package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.sound.SoundManager;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "game_settings";
    private static final String KEY_SOUND_ON = "sound_on";
    private boolean soundOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 持久化音效开关，确保菜单设置与游戏内行为一致。
        soundOn = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SOUND_ON, true);
        SwitchCompat soundSwitch = findViewById(R.id.switch_sound);
        soundSwitch.setChecked(soundOn);
        SoundManager.setSoundOn(soundOn);
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundOn = isChecked;
            SoundManager.setSoundOn(isChecked);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_SOUND_ON, isChecked)
                    .apply();
        });

        // 主菜单只负责选择难度并传递参数，实际游戏生命周期由 GameActivity 管理。
        findViewById(R.id.btn_easy).setOnClickListener(v -> startGame("easy"));
        findViewById(R.id.btn_medium).setOnClickListener(v -> startGame("medium"));
        findViewById(R.id.btn_hard).setOnClickListener(v -> startGame("hard"));
        findViewById(R.id.btn_exit).setOnClickListener(v -> {
            moveTaskToBack(true);
            finishAffinity();
        });
    }

    private void startGame(String difficulty) {
        // 封装启动参数，避免在菜单页耦合游戏细节。
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameActivity.EXTRA_SOUND_ON, soundOn);
        startActivity(intent);
    }
}
