package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_easy).setOnClickListener(v -> startGame("easy"));
        findViewById(R.id.btn_medium).setOnClickListener(v -> startGame("medium"));
        findViewById(R.id.btn_hard).setOnClickListener(v -> startGame("hard"));
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}
