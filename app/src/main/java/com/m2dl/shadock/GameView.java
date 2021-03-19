package com.m2dl.shadock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.res.ResourcesCompat;

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

    private static final int HAUTEUR_LIGNE_VIDE = 500;
    private static long SPEED = 300;
    private final Sensor sensor;
    private final int width;
    private final int height;
    private long debut;
    private GameThread thread;
    private int x;
    private final int y;
    private int xEnnemy = 0;
    private Direction direction;
    private final SensorManager sensorManager;
    private int actualSpeed = 1;
    private double acceleration = 0;
    private final int rayon = 50;
    private int score;
    private boolean dejaFini = false;
    private Handler mHandler;
    private Handler mHandlerUpdateEnnemy;
    private final Handler mHandlerInit;
    private Ennemy highestEnnemy;

    private int indexOne = 0;
    private int indexTwo = 1;
    private int indexThree = 4;
    private int indexFour = 5;

    private boolean fin = false;

    private int background_color;
    private final int ball_color;

    private boolean endList = false;
    private int nbMax;
    private int cpt = 3;

    private final ArrayList<Ennemy> ennemies;
    private final ArrayList<Bonus> bonuses;

    private final Drawable mCustomImage;
    private final Drawable shadokPumpOne;
    private final Drawable shadokPumpTwo;
    private final Drawable shadokTired;
    private final Drawable fuseeGibi;
    private final Drawable passoire;

    private boolean pump = true;
    private boolean rythm = true;
    private boolean tired = false;

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
            updateBonus();
            mHandlerUpdateEnnemy.postDelayed(this, GameView.SPEED);
        }
    };

    private final Runnable mInitTask = new Runnable() {
        public void run() {
            if (cpt > 0) {
                activity.setTextTv("" + cpt);
                --cpt;
                mHandlerInit.postDelayed(this, 1000);
            } else if (cpt == 0) {
                activity.setTextTv("GO");
                --cpt;
                mHandlerInit.postDelayed(this, 1000);
            } else {
                launchGame();
            }
        }
    };

    private void updateEnnemies() {
        int i = 0;
        while (i < ennemies.size()) {
            ennemies.get(i).updatePosition(height);
            if (ennemies.get(i) == highestEnnemy) {
                if (ennemies.get(i).getY() > HAUTEUR_LIGNE_VIDE) {
                    spawEnemies();
                }
            }
            if (ennemies.get(i).updatePosition(height)) {
                ennemies.remove(ennemies.get(i));
            } else {
                i++;
            }
        }

    }

    public void augmentSpeed() {
        if (GameView.SPEED > 10) {
            GameView.SPEED -= 10;
        }
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);

        background_color = Accueil.sharedPreferences.getInt("BackgroundColor", Color.WHITE);
        ball_color = Accueil.sharedPreferences.getInt("BallColor", Color.WHITE);


        ennemies = new ArrayList<>();
        bonuses = new ArrayList<>();

        thread = new GameThread(getHolder(), this);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        width = this.getResources().getDisplayMetrics().widthPixels;
        height = this.getResources().getDisplayMetrics().heightPixels;
        x = width / 2;
        y = height - (height / 15);
        mHandlerInit = new Handler();
        mHandlerInit.postDelayed(mInitTask, 0);
        mCustomImage = ResourcesCompat.getDrawable(getResources(), R.drawable.fusee_shadocks_resized, null);
        shadokPumpOne = ResourcesCompat.getDrawable(getResources(), R.drawable.pump_way_one, null);
        shadokPumpTwo = ResourcesCompat.getDrawable(getResources(), R.drawable.pump_way_two, null);
        shadokTired = ResourcesCompat.getDrawable(getResources(), R.drawable.tiringpump, null);
        fuseeGibi = ResourcesCompat.getDrawable(getResources(), R.drawable.fusee_gibi, null);
        passoire = ResourcesCompat.getDrawable(getResources(), R.drawable.passoire_alpha, null);

    }

    private void launchGame() {
        thread = new GameThread(getHolder(), this);
        debut = System.currentTimeMillis() / 100;
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 100);
        spawEnemies();
        spawBonuses();
        mHandlerUpdateEnnemy = new Handler();
        mHandlerUpdateEnnemy.postDelayed(mUpdateTimeEnemy, 50);
        GameView.SPEED = 200;
    }

    private void updateBonus() {
        int i = 0;
        while (i < bonuses.size()) {
            if (bonuses.get(i).updatePosition(height)) {
                bonuses.remove(bonuses.get(i));
                spawBonuses();
            } else {
                i++;
            }
        }

    }

    private void spawEnemies() {

        nbMax = (width / (rayon * 2));
        System.out.println(nbMax);
        xEnnemy = 0;
        if(indexOne==0){
            endList = false;
        }else if(indexOne != 0){
            endList=true;
        }
        for (int i = 0; i < nbMax-1; ++i) {
            Ennemy ennemy = new Ennemy(xEnnemy);
            xEnnemy = xEnnemy + 120;
            if (indexOne != i && indexTwo!=i && indexThree!=i && indexFour!=i)
                ennemies.add(ennemy);
        }
        Ennemy ennemy = new Ennemy(xEnnemy);
        xEnnemy = xEnnemy + 120;
        ennemies.add(ennemy);
        highestEnnemy = ennemy;
        if(endList){
            indexOne = 0;
            indexTwo= 1;
            indexThree=4;
            indexFour=5;
        }else if(!endList){
            indexOne = 2;
            indexTwo= 3;
            indexThree=6;
            indexFour=7;
        }
    }

    private void spawBonuses() {
        Bonus bonus = new Bonus(width, 200);
        bonuses.add(bonus);
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
        background_color = Accueil.sharedPreferences.getInt("BackgroundColor", Color.WHITE);
        for (Ennemy e: ennemies) {
            if ((e.getX() - 100 <= x && x <= e.getX() + 100) && (e.getY() - 100 <= y && y <= e.getY() + 100))
                fin = true;
        }
        /*for(int i=0; i<bonuses.size();++i) {
            if ((bonuses.get(i).getX() - 100 <= x && x <= bonuses.get(i).getX() + 100) && (bonuses.get(i).getY() - 100 <= y && y <= bonuses.get(i).getY() + 100)) {
                System.out.println(score);
                score = score + 100;
                GameView.SPEED = 300;
                System.out.println(score);
                bonuses.remove(bonuses.get(i));
                spawBonuses();
            }
        }*/
        if (!isFinDujeu()) {
            int newValue = (int) Math.round(x + actualSpeed * acceleration);
            x = newValue - 50 < 0 || newValue + 50 > width ? x : newValue;
        }

    }

    private boolean isFinDujeu() {
        if (!dejaFini && fin) {
            pump = false;
            rythm = false;
            tired = true;
            dejaFini = true;
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandlerUpdateEnnemy.removeCallbacks(mUpdateTimeEnemy);
            ennemies.clear();
            bonuses.clear();
            activity.showFin(score, pseudo);
            registerScore(score, pseudo);
        }

        return fin;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(this.background_color);
            Rect shadok = new Rect(x - 50, y - 75, x + 50, y + 75);
            mCustomImage.setBounds(shadok);
            mCustomImage.draw(canvas);

            if (rythm) {
                if (pump) {
                    shadokPumpOne.setBounds(new Rect(100, 100, 200, 200));
                    shadokPumpOne.draw(canvas);
                    pump = false;
                } else {
                    shadokPumpTwo.setBounds(new Rect(100, 100, 200, 200));
                    shadokPumpTwo.draw(canvas);
                    pump = true;
                }
            }

            if (tired) {
                shadokTired.setBounds(new Rect(150, 500, 650, 900));
                shadokTired.draw(canvas);
            }

            drawEnnemy(canvas, shadok);
            drawBonus(canvas, shadok);
        }
    }

    public void drawEnnemy(Canvas canvas, Rect shadok) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(0, 0, 255));
        for (Ennemy e : ennemies) {
            Rect giby = new Rect(e.getX(), e.getY(), e.getX() + 125, e.getY() + 200);
            if (giby.intersect(shadok)) {
                fin = true;
            }
            fuseeGibi.setBounds(giby);
            fuseeGibi.draw(canvas);
            //canvas.drawCircle(e.getX(), e.getY(), rayon, paint);
        }
    }

    public void drawBonus(Canvas canvas, Rect shadok) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(0, 255,0));
        for (int i=0; i<bonuses.size();++i) {
            Rect passoireImg = new Rect(bonuses.get(i).getX(), bonuses.get(i).getY(), bonuses.get(i).getX() + 60, bonuses.get(i).getY() + 60);
            if (passoireImg.intersect(shadok)) {
                score = score + 100;
                GameView.SPEED = 300;
                bonuses.remove(bonuses.get(i));
                spawBonuses();
            }
            passoire.setBounds(passoireImg);
            passoire.draw(canvas);
            //canvas.drawCircle(e.getX(), e.getY(), rayon, paint);
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
            acceleration = -8;
        } else if (axisX > 5) {
            acceleration = -6;
        } else if (axisX > 3) {
            acceleration = -3;
        } else if (axisX > 1) {
            acceleration = -2;
        } else if (axisX < -7) {
            acceleration = 8;
        } else if (axisX < -5) {
            acceleration = 6;
        } else if (axisX < -3) {
            acceleration = 3;
        } else if (axisX < -1) {
            acceleration = 2;
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
