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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.LinkedList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    private final Sensor sensor;
    private GameThread thread;
    private int x = 0;
    private LinkedList<Integer> ys = new LinkedList<>();
    private SensorManager sensorManager;
    private int actualSpeed = 5;
    private double acceleration = 0;
    private Direction direction = Direction.DROITE;

    // on défini un handler qui représentera notre timer :
    private Handler mHandler;

    // un Runnable qui sera appelé par le timer
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            ys.add(ys.getLast() + 200);
            mHandler.postDelayed(this, 1000);
        }
    };

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        ys.add(MainActivity.sharedPref.getInt("valeur_y", 0));
        thread = new GameThread(getHolder(), this);
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 1000);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
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
        x = (int) Math.round(x + actualSpeed * acceleration) % 700;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.rgb(250, 0, 0));
            for (Integer yCurr : ys) {
                canvas.drawRect(x, yCurr, x + 100, yCurr + 100, paint);
            }
        }
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
}
