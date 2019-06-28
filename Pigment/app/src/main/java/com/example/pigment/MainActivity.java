package com.example.pigment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;
    static ViewPager viewPager;

    public static ArrayList<String> answers = new ArrayList<>();
    public static ArrayList<String> normal = new ArrayList<>();
    public static ArrayList<String> protanopia = new ArrayList<>();
    public static ArrayList<String> deuteranopia = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        // Check if we need to display our OnboardingFragment
        if (!sharedPreferences.getBoolean(
                DiagnosticActivity.COMPLETED_TEST, false)) {
            // The user hasn't seen the OnboardingFragment yet, so show it
            startActivity(new Intent(this, DiagnosticActivity.class));
        }

        int setIndex = 1;

        if(savedInstanceState != null) {
            answers = savedInstanceState.getStringArrayList("answers");
            normal = savedInstanceState.getStringArrayList("normal");
            protanopia = savedInstanceState.getStringArrayList("protanopia");
            deuteranopia = savedInstanceState.getStringArrayList("deuteranopia");
        }

        if (getIntent().hasExtra("answers")) {
            answers = getIntent().getExtras().getStringArrayList("answers");
            normal = getIntent().getExtras().getStringArrayList("normal");
            protanopia = getIntent().getExtras().getStringArrayList("protanopia");
            deuteranopia = getIntent().getExtras().getStringArrayList("deuteranopia");
            setIndex = 0;
        }

        viewPager =  findViewById(R.id.viewPager);

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem(setIndex);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putStringArrayList("answers", answers);
        outState.putStringArrayList("normal", normal);
        outState.putStringArrayList("protanopia", protanopia);
        outState.putStringArrayList("deuternopia", deuteranopia);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
             switch(position){
                 case 0:
                     Bundle bundle = new Bundle();
                     bundle.putStringArrayList("answers", MainActivity.answers);
                     bundle.putStringArrayList("normal", MainActivity.normal);
                     bundle.putStringArrayList("protanopia", MainActivity.protanopia);
                     bundle.putStringArrayList("deuteranopia", MainActivity.deuteranopia);
                     ResultsActivity raf = ResultsActivity.newInstance();
                     raf.setArguments(bundle);
                     return raf;
                 case 1:
                     return CameraFragment.newInstance();
             }
            return null;
        }

        @Override
        public int getCount() {
            return 2 ;
        }
    }

}
