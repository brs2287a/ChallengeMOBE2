package com.m2dl.ballgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private final Sensor sensor;
    private final int width;
    private final int height;
    private final long debut;
    private GameThread thread;
    private int x;
    private int y;
    private Direction direction;
    private SensorManager sensorManager;
    private int actualSpeed = 1;
    private double acceleration = 0;
    private int rayon = 50;
    private int score;
    private boolean dejaFini = false;
    private Handler mHandler;

    private int background_color;
    private int ball_color;

    private String pseudo = Accueil.sharedPreferences.getString("PlayerName", "");

    // un Runnable qui sera appelé par le timer
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            score = (int) (System.currentTimeMillis() / 1000 - debut);
            MainActivity.tvScore.setText("" + score);
            mHandler.postDelayed(this, 1000);
        }
    };

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        direction = randomDirection();

        background_color = Accueil.sharedPreferences.getInt("BackgroundColor", Color.BLACK);
        ball_color = Accueil.sharedPreferences.getInt("BallColor", Color.WHITE);

        thread = new GameThread(getHolder(), this);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        width = this.getResources().getDisplayMetrics().widthPixels;
        height = this.getResources().getDisplayMetrics().heightPixels;
        x = width / 2;
        y = height / 2;
        debut = System.currentTimeMillis() / 1000;
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    public void update() {
        background_color = Accueil.sharedPreferences.getInt("BackgroundColor", Color.BLACK);
        ball_color = Accueil.sharedPreferences.getInt("BallColor", Color.WHITE);

        if (!isFinDujeu()) {
            switch (direction) {
                case HAUT:
                    y = (int) Math.round(y + actualSpeed * acceleration);
                    break;
                case BAS:
                    y = (int) Math.round(y - actualSpeed * acceleration);
                    break;
                case DROITE:
                    x = (int) Math.round(x + actualSpeed * acceleration);
                    break;
                case GAUCHE:
                    x = (int) Math.round(x - actualSpeed * acceleration);
                    break;
            }
        }

    }

    private boolean isFinDujeu() {
        boolean fin = x + rayon > width || x - rayon < 0 || y - rayon < 0 || y + rayon > height;
        if (!dejaFini && fin) {
            //int score = (int) (System.currentTimeMillis() / 1000 - debut);
            String texte = pseudo + " votre score final est " + score;
            MainActivity.tv.setText(texte);
            MainActivity.tvScore.setVisibility(INVISIBLE);
            MainActivity.replayButton.setVisibility(VISIBLE);
            mHandler.removeCallbacks(mUpdateTimeTask);
            dejaFini = true;
        }

        return fin;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(this.background_color);
            Paint paint = new Paint();
            paint.setColor(this.ball_color);
            canvas.drawCircle(x, y, rayon,  paint);
        }
    }


    public static Direction randomDirection()  {
        // get an array of all the cards
        Direction[] directions=Direction.values();
            // this generates random numbers
        Random random = new Random();

        return directions[random.nextInt(directions.length)];

    }

    public static Direction randomDirection(Direction direction)  {
        ArrayList<Direction> directionArrayList = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if(dir != direction){
                directionArrayList.add(dir);
            }
        }

        // this generates random numbers
        Random random = new Random();

        return directionArrayList.get(random.nextInt(directionArrayList.size()));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float axisX = event.values[0];
        float axisY = event.values[1];
        switch (direction) {
            case HAUT:
                if (axisY > 7) {
                    acceleration = 4;
                } else if (axisY > 5) {
                    acceleration = 2;
                } else if (axisY > 3) {
                    acceleration = 1.5;
                } else if (axisY < -7) {
                    acceleration = 0.7;
                } else if (axisY < -5) {
                    acceleration = 0.9;
                } else if (axisY < -3) {
                    acceleration = 0.95;
                } else {
                    acceleration = 1;
                }
                break;
            case BAS:
                if (axisY > 7) {
                    acceleration = 0.7;
                } else if (axisY > 5) {
                    acceleration = 0.9;
                } else if (axisY > 3) {
                    acceleration = 0.95;
                } else if (axisY < -7) {
                    acceleration = 4;
                } else if (axisY < -5) {
                    acceleration = 2;
                } else if (axisY < -3) {
                    acceleration = 1.5;
                } else {
                    acceleration = 1;
                }
                break;
            case GAUCHE:
                if (axisX > 7) {
                    acceleration = 4;
                } else if (axisX > 5) {
                    acceleration = 2;
                } else if (axisX > 3) {
                    acceleration = 1.5;
                } else if (axisX < -7) {
                    acceleration = 0.7;
                } else if (axisX < -5) {
                    acceleration = 0.9;
                } else if (axisX < -3) {
                    acceleration = 0.95;
                } else {
                    acceleration = 1;
                }
                break;
            case DROITE:
                if (axisX > 7) {
                    acceleration = 0.7;
                } else if (axisX > 5) {
                    acceleration = 0.9;
                } else if (axisX > 3) {
                    acceleration = 0.95;
                } else if (axisX < -7) {
                    acceleration = 4;
                } else if (axisX < -5) {
                    acceleration = 2;
                } else if (axisX < -3) {
                    acceleration = 1.5;
                } else {
                    acceleration = 1;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getActualSpeed() {
        return actualSpeed;
    }

    public void setActualSpeed(int actualSpeed) {
        this.actualSpeed = actualSpeed;
    }
}
