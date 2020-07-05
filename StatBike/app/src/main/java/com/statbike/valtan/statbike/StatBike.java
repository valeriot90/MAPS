package com.statbike.valtan.statbike;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class StatBike extends Activity {

    private Handler splash;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the Title bar of this activity screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen_layout);
        final int durata = 1000;

        splash = new Handler();
        splash.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(StatBike.this,MainActivity.class);
                StatBike.this.startActivity(intent);
                finish();
            }
        }, durata);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        splash.removeCallbacksAndMessages(null);
    }
}
