package com.m2dl.shadock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Regles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regles);
    }

    public void seeAcceuil(View view) {
        Intent intent = new Intent(this, Accueil.class);
        startActivity(intent);
        finish();
    }
}