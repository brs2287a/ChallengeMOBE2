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

    private static final int HAUTEUR_LIGNE_VIDE = 150;
    private final Sensor sensor;
    private final int width;
    private final int height;
    private final long debut;
    private final GameThread thread;
    private int x;
    private int y;
    private int xEnnemy = 0;
    private Direction direction;
    private final SensorManager sensorManager;
    private int actualSpeed = 1;
    private double acceleration = 0;
    private final int rayon = 50;
    private int score;
    private boolean dejaFini = false;
    private final Handler mHandler;
    private final Handler mHandlerUpdateEnnemy;
    private final Handler mHandlerSpawnEnnemy;
    private Ennemy highestEnnemy;
    private final boolean timeToSpawn = false;

    private int background_color;
    private int ball_color;


    private final ArrayList<Ennemy> ennemies;

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

    private final Runnable mUpdateTimeEnemy = new Runnable() {
        public void run() {
            updateEnnemies();
            mHandlerUpdateEnnemy.postDelayed(this, 300);
        }
    };

    private void updateEnnemies() {
        for (Ennemy ennemy : ennemies) {
            ennemy.updatePosition(height);
            if (ennemy == highestEnnemy) {
                if (ennemy.getY() > HAUTEUR_LIGNE_VIDE) {
                    spawEnemies();
                }
            }
        }
    }

    private final Runnable mUpdateTimeEnemySpawn = new Runnable() {
        public void run() {
            spawEnemies();
        }
    };

    private void spawEnemies() {
        xEnnemy = 0;
        for (int i = 0; i < 15; ++i) {
            Ennemy ennemy = new Ennemy(xEnnemy);
            xEnnemy = xEnnemy + 120;
            ennemies.add(ennemy);
        }
        Ennemy ennemy = new Ennemy(xEnnemy);
        xEnnemy = xEnnemy + 120;
        ennemies.add(ennemy);
        highestEnnemy = ennemy;
    }

    public ArrayList<Ennemy> getEnnemies() {
        return ennemies;
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        direction = randomDirection();

        background_color = Accueil.sharedPreferences.getInt("BackgroundColor", Color.BLACK);
        ball_color = Accueil.sharedPreferences.getInt("BallColor", Color.WHITE);


        ennemies = new ArrayList<>();

        thread = new GameThread(getHolder(), this);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        width = this.getResources().getDisplayMetrics().widthPixels;
        height = this.getResources().getDisplayMetrics().heightPixels;
        x = width / 2;
        y = height / 2;
        debut = System.currentTimeMillis() / 100;
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 100);
        mHandlerSpawnEnnemy = new Handler();
        mHandlerSpawnEnnemy.postDelayed(mUpdateTimeEnemySpawn, 100);
        mHandlerUpdateEnnemy = new Handler();
        mHandlerUpdateEnnemy.postDelayed(mUpdateTimeEnemy, 100);
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
            paint.setColor(this.ball_color);
            canvas.drawCircle(x, y, rayon,  paint);
        }
        drawEnnemy(canvas);
        for (Ennemy e: getEnnemies()) {
            e.updatePosition(getHeight());
        }
    }

    public void drawEnnemy(Canvas canvas) {
        int nbMax = (width / (rayon * 2)) - 2;

        Paint paint = new Paint();
        paint.setColor(Color.rgb(0, 0, 255));

        for (Ennemy e : ennemies) {
            canvas.drawCircle(e.getX(), e.getY(), rayon, paint);
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
