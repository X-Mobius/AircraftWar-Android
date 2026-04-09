package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.AbstractGame;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;
import edu.hitsz.sound.SoundManager;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND_ON = "sound_on";

    private AbstractGame gameView;
    private boolean gameOverHandled = false;
    private ScoreDao scoreDao;
    private final Handler gameUiHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == AbstractGame.MSG_GAME_OVER) {
            showGameOverDialog(msg.arg1);
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
        if (getIntent() != null) {
            String extra = getIntent().getStringExtra(EXTRA_DIFFICULTY);
            if (extra != null && !extra.trim().isEmpty()) {
                difficulty = extra;
            }
            soundOn = getIntent().getBooleanExtra(EXTRA_SOUND_ON, true);
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
        setContentView(root);
    }

    private void showGameOverDialog(int finalScore) {
        if (gameOverHandled || isFinishing() || isDestroyed()) {
            return;
        }
        gameOverHandled = true;

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
                    if (scoreDao.existsPlayerName(playerName)) {
                        Toast.makeText(this, "玩家名已存在，请换一个名字", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveScoreAndOpenRank(playerName, finalScore);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void saveScoreAndOpenRank(String playerName, int finalScore) {
        scoreDao.addRecord(new ScoreRecord(playerName, finalScore));
        startActivity(new Intent(this, RankActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        SoundManager.release();
        super.onDestroy();
    }
}
