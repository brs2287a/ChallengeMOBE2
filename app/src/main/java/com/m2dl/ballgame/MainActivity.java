package com.m2dl.ballgame;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements View.OnTouchListener {
    private GameView gameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.surfaceView);
        gameView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getActionMasked();
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