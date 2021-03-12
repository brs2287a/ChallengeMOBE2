package com.m2dl.ballgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;


public class Accueil extends Activity {

    public static SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    private EditText player_name;
    private TextView name;
    private Button play;
    private Button score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

        setContentView(R.layout.activity_accueil);
        player_name = findViewById(R.id.player_name);
        player_name.setVisibility(View.VISIBLE);

        editor = sharedPreferences.edit();
        editor.putString("PlayerName", player_name.getText().toString());
        editor.apply();

        name = findViewById(R.id.game_name);
        name.setVisibility(View.VISIBLE);
        play = findViewById(R.id.play_button);
        play.setVisibility(View.VISIBLE);
        score = findViewById(R.id.score_button);
        score.setVisibility(View.VISIBLE);
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void seeScore(View view) {
        Intent intent = new Intent(this, Score.class);
        startActivity(intent);
    }
}