package com.m2dl.shadock;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity implements View.OnTouchListener, SensorEventListener {

    public static int SPEED_ENNEMIES = 2;
    private GameView gameView;
    private TextView tv;
    private TextView tvScore;
    private Button replayButton;
    private Button scoreButton;
    private Button homeButton;

    public SharedPreferences.Editor editor;

    private SensorManager sensorManager = null;
    private Sensor light;

    private int background_color = Color.WHITE;
    private int ball_color = Color.BLACK;

    private final String guid = Accueil.sharedPreferences.getString("GUID", Accueil.guidNotRetrieve());

    // Access a Cloud Firestore instance from your Activity
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ObjectAnimator animHaut;
    private ObjectAnimator animBas;
    private ObjectAnimator animDroite;
    private ObjectAnimator animGauche;
    private boolean waintingInput = false;
    private boolean touched = false;
    private Handler mHandler;

    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            waintingInput = !waintingInput;
            if (waintingInput) {
                initAnim(Color.RED, 400);
                touched = false;
                mHandler.postDelayed(this, 400);
            } else {
                if (!touched) {
                    gameView.augmentSpeed();
                }
                mHandler.postDelayed(this, duration);
            }
        }
    };
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        editor = Accueil.sharedPreferences.edit();
        editor.putInt("BackgroundColor", background_color);
        editor.putInt("BallColor", ball_color);
        editor.apply();

        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView2);
        tvScore = findViewById(R.id.textViewScore);
        replayButton = findViewById(R.id.replayButton);
        homeButton = findViewById(R.id.homeButton);
        scoreButton = findViewById(R.id.scoreButton);
        gameView = findViewById(R.id.surfaceView);
        gameView.setActivity(this);
        gameView.setOnTouchListener(this);
        duration = 800;
        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, duration);
    }

    public void initAnim(int color, int duration) {
        RelativeLayout bordure = findViewById(R.id.bordure);
        LinearLayout bordureHaut = findViewById(R.id.bordureHaut);
        LinearLayout bordureBas = findViewById(R.id.bordureBas);
        LinearLayout bordureDroite = findViewById(R.id.bordureDroite);
        LinearLayout bordureGauche = findViewById(R.id.bordureGauche);
        bordure.setVisibility(VISIBLE);
        if (animBas != null) {
            animBas.cancel();
            animHaut.cancel();
            animDroite.cancel();
            animGauche.cancel();
        }
        animHaut = ObjectAnimator.ofInt(bordureHaut, "backgroundColor", Color.WHITE, color,
                Color.WHITE);
        animHaut.setDuration(duration);
        animHaut.setEvaluator(new ArgbEvaluator());
        animHaut.setRepeatMode(ValueAnimator.REVERSE);
        animHaut.setRepeatCount(Animation.ABSOLUTE);
        animBas = ObjectAnimator.ofInt(bordureBas, "backgroundColor", Color.WHITE, color,
                Color.WHITE);
        animBas.setDuration(duration);
        animBas.setEvaluator(new ArgbEvaluator());
        animBas.setRepeatMode(ValueAnimator.REVERSE);
        animBas.setRepeatCount(Animation.ABSOLUTE);
        animDroite = ObjectAnimator.ofInt(bordureDroite, "backgroundColor", Color.WHITE, color,
                Color.WHITE);
        animDroite.setDuration(duration);
        animDroite.setEvaluator(new ArgbEvaluator());
        animDroite.setRepeatMode(ValueAnimator.REVERSE);
        animDroite.setRepeatCount(Animation.ABSOLUTE);
        animGauche = ObjectAnimator.ofInt(bordureGauche, "backgroundColor", Color.WHITE, color,
                Color.WHITE);
        animGauche.setDuration(duration);
        animGauche.setEvaluator(new ArgbEvaluator());
        animGauche.setRepeatMode(ValueAnimator.REVERSE);
        animGauche.setRepeatCount(Animation.ABSOLUTE);
        animHaut.start();
        animBas.start();
        animDroite.start();
        animGauche.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getActionMasked();
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (!waintingInput) {
                    gameView.augmentSpeed();
                } else {
                    touched = true;
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this, light);
        super.onStop();
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
            System.out.println(light_value);
            if ( 0 <= light_value && light_value < 5) {
                background_color = Color.YELLOW;
                ball_color = Color.MAGENTA;
            } else if (5 <= light_value && light_value < 7) {
                background_color = Color.MAGENTA;
                ball_color = Color.GREEN;
            } else if (7 <= light_value && light_value < 10) {
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

    public void replay(View v) {
        finish();
        startActivity(getIntent());
    }

    public void seeScoreFromGame(View v) {
        Intent intent = new Intent(this, Score.class);
        startActivity(intent);
        finish();
    }
    public void seeHome(View v) {
        Intent intent = new Intent(this, Accueil.class);
        startActivity(intent);
        finish();
    }

    public void showFin(Integer score, String pseudo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String texte = pseudo + " votre score final est " + score;
                tv.setText(texte);
                tvScore.setVisibility(INVISIBLE);
                replayButton.setVisibility(VISIBLE);
                scoreButton.setVisibility(VISIBLE);
                homeButton.setVisibility(VISIBLE);
            }
        });
    }


    public void setTextTv(String s) {
        tv.setText(s);
    }
}