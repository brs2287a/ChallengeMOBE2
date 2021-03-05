package com.m2dl.ballgame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnTouchListener, SensorEventListener {
    private GameView gameView;
    static TextView tv;

    public static SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    private SensorManager sensorManager = null;
    private Sensor light;

    private int background_color = Color.WHITE;
    private int ball_color = Color.BLACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("BackgroundColor", background_color);
        editor.putInt("BallColor", ball_color);
        editor.apply();

        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView2);
        gameView = findViewById(R.id.surfaceView);
        gameView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getActionMasked();
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                gameView.setActualSpeed(gameView.getActualSpeed()+1);
                switch (gameView.getDirection()) {
                    case HAUT:
                        gameView.setDirection(gameView.randomDirection(gameView.getDirection()));
                        break;
                    case BAS:
                        gameView.setDirection(gameView.randomDirection(gameView.getDirection()));
                        break;
                    case DROITE:
                        gameView.setDirection(gameView.randomDirection(gameView.getDirection()));
                        break;
                    case GAUCHE:
                        gameView.setDirection(gameView.randomDirection(gameView.getDirection()));

                        break;
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, light);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float light_value = event.values[0];
            if ( 0 <= light_value && light_value < 10000) {
                background_color = Color.YELLOW;
                ball_color = Color.MAGENTA;
            } else if (10000 <= light_value && light_value < 20000) {
                background_color = Color.MAGENTA;
                ball_color = Color.GREEN;
            } else if (20000 <= light_value && light_value < 30000) {
                background_color = Color.GREEN;
                ball_color = Color.BLUE;
            } else {
                background_color = Color.BLUE;
                ball_color = Color.RED;
            }
            editor.putInt("BackgroundColor", background_color);
            editor.putInt("BallColor", ball_color);
            editor.apply();
        }
    }

}