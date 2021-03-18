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
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class Accueil extends Activity {

    public static SharedPreferences sharedPreferences;
    public static int SIZE_HIGHSCORE = 10;
    public SharedPreferences.Editor editor;

    private EditText player_name;
    private TextView name;
    private Button play;
    private Button score;

    private String guid;

    protected static String guidNotRetrieve(){
        String storedGuid = Accueil.sharedPreferences.getString("GUID", "");
        if(storedGuid.equals("")){
            storedGuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString("GUID", storedGuid).apply();
        }
        return storedGuid;
    }

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

        name = findViewById(R.id.game_name);
        name.setVisibility(View.VISIBLE);
        play = findViewById(R.id.play_button);
        play.setVisibility(View.VISIBLE);
        score = findViewById(R.id.score_button);
        score.setVisibility(View.VISIBLE);

        guid = sharedPreferences.getString("GUID", guidNotRetrieve());
    }

    public void startGame(View view) {
        String pseudo = player_name.getText().toString();
        if (pseudo.equals("")) {
            pseudo = "Player 1";
        }
        editor = sharedPreferences.edit();
        editor.putString("PlayerName", pseudo);
        editor.putString("GUID", guid);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void seeScore(View view) {
        Intent intent = new Intent(this, Score.class);
        startActivity(intent);
        finish();
    }

    public static boolean saveArray(List<Integer> list, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("local", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("scores" + "_size", list.size());
        for (int i = 0; i < list.size(); i++)
            editor.putInt("scores" + "_" + i, list.get(i));
        return editor.commit();
    }

    public static List<Integer> loadArray(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("local", 0);
        int size = prefs.getInt("scores" + "_size", 0);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++)
            list.add(prefs.getInt("scores" + "_" + i, -1));
        Collections.sort(list, Collections.reverseOrder());
        return list;
    }
}