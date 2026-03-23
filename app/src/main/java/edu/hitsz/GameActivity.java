package edu.hitsz;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.AbstractGame;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";

    private AbstractGame gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageManager.init(getApplicationContext());

        String difficulty = "easy";
        if (getIntent() != null) {
            String extra = getIntent().getStringExtra(EXTRA_DIFFICULTY);
            if (extra != null && !extra.trim().isEmpty()) {
                difficulty = extra;
            }
        }

        gameView = Main.createGame(this, difficulty);
        FrameLayout root = new FrameLayout(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }
}
