package com.m2dl.shadock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Score extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    public static final int MYTOP = 1;
    public static final int TOP100 = 2;
    public static final int LOCALSCORES = 3;
    private static final int NUM_PAGES = 3;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private ArrayList<String> top100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);


        viewPager = (ViewPager2) findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
        viewPager.setAdapter(pagerAdapter);


    }


    public void startGame(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void accueil(View view) {
        Intent intent = new Intent(this, Accueil.class);
        startActivity(intent);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }


        @Override
        public Fragment createFragment(int position) {
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            switch (position) {
                case 1:
                    return UserWorldScore.newInstance(MYTOP);
                case 2:
                    return UserWorldScore.newInstance(TOP100);
                case 0:
                    return UserWorldScore.newInstance(LOCALSCORES);
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}