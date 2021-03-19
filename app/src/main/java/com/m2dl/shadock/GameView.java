package com.m2dl.shadock;

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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private final Sensor sensor;
    private final int width;
    private final int height;
    private final long debut;
    private final GameThread thread;
    private int x;
    private final int y;
    private Direction direction;
    private final SensorManager sensorManager;
    private int actualSpeed = 1;
    private double acceleration = 0;
    private final int rayon = 50;
    private int score;
    private boolean dejaFini = false;
    private final Handler mHandler;

    private final int background_color;
    private final int ball_color;

    // Access a Cloud Firestore instance from your Activity
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final String pseudo = Accueil.sharedPreferences.getString("PlayerName", "Player 1");
    private final String guid = Accueil.sharedPreferences.getString("GUID", Accueil.guidNotRetrieve());
    private MainActivity activity;
    // un Runnable qui sera appelé par le timer
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            score = (int) (System.currentTimeMillis() / 100 - debut);
            activity.setTextTv("" + score);
            mHandler.postDelayed(this, 100);
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
        y = height - (height / 15);
        debut = System.currentTimeMillis() / 100;
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 100);
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
        if (!isFinDujeu()) {
            x = (int) Math.round(x + actualSpeed * acceleration);
        }

    }

    private boolean isFinDujeu() {
        boolean fin = x + rayon > width || x - rayon < 0 || y - rayon < 0 || y + rayon > height;
        if (!dejaFini && fin) {
            dejaFini = true;
            mHandler.removeCallbacks(mUpdateTimeTask);
            activity.showFin(score, pseudo);
            registerScore(score, pseudo);
        }

        return fin;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            canvas.drawCircle(x, y, rayon, paint);
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
        if (axisX > 7) {
            acceleration = -6;
        } else if (axisX > 5) {
            acceleration = -4;
        } else if (axisX > 3) {
            acceleration = -2;
        } else if (axisX > 1) {
            acceleration = -1;
        } else if (axisX < -7) {
            acceleration = 6;
        } else if (axisX < -5) {
            acceleration = 4;
        } else if (axisX < -3) {
            acceleration = 2;
        } else if (axisX < -1) {
            acceleration = 1;
        } else {
            acceleration = 0;
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

    public void setActivity(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    private void registerScore(int score, String pseudo) {
        List<Integer> scores = Accueil.loadArray(getContext().getApplicationContext());
        if (scores.isEmpty() || scores.get(0) < score) {
            sendScore(score, pseudo);
        }
        int lastIndex = scores.size() - 1;
        if (scores.size() < Accueil.SIZE_HIGHSCORE) {
            scores.add(score);
            Accueil.saveArray(scores, getContext().getApplicationContext());
        } else if (!scores.isEmpty() && score > scores.get(lastIndex)) {
            scores.remove(lastIndex);
            scores.add(score);
            Accueil.saveArray(scores, getContext().getApplicationContext());
        }
    }

    private void sendScore(Integer score, String pseudo) {
        Map<String, Object> scoreEnregistrement = new HashMap<>();

        scoreEnregistrement.put("date", new Date());
        scoreEnregistrement.put("score", score);
        scoreEnregistrement.put("user", pseudo);

        db.collection("score").document(guid)
                .set(scoreEnregistrement)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}
