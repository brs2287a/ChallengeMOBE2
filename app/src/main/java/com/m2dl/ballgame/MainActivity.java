package com.m2dl.ballgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity implements View.OnTouchListener {
    private GameView gameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        gameView = new GameView(this);
        gameView.setOnTouchListener(this);
        setContentView(gameView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                switch (gameView.direction) {
                    case HAUT:
                        gameView.direction = gameView.randomDirection(gameView.direction);
                        break;
                    case BAS:
                        gameView.direction = gameView.randomDirection(gameView.direction);
                        break;
                    case DROITE:
                        gameView.direction = gameView.randomDirection(gameView.direction);
                        break;
                    case GAUCHE:
                        gameView.direction = gameView.randomDirection(gameView.direction);

                        break;
                }
                break;
        }
        return true;
    }

}