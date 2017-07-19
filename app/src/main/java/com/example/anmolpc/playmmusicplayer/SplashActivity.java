package com.example.anmolpc.playmmusicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    ImageView img;
    public static PreloadData pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        img=(ImageView)findViewById(R.id.splashimg);
        pd=new PreloadData(getApplicationContext());
        pd.preloadData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
        }
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.pulse);
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(anim);
        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.splashbounce);
        anim1.setStartOffset(2500);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 20);
        anim1.setInterpolator(interpolator);
        animationSet.addAnimation(anim1);
        img.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            img.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent startActivityIntent = new Intent(SplashActivity.this, MainPlayerActivity.class);
                startActivity(startActivityIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    final Animation anim = AnimationUtils.loadAnimation(this, R.anim.pulse);
                    final AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(anim);
                    Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.fadeoutactivity);
                    anim1.setStartOffset(2500);
                    animationSet.addAnimation(anim1);


                    img.startAnimation(animationSet);

                    ActionBar actionBar = getSupportActionBar();
                    if (null != actionBar) {
                        actionBar.hide();
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent startActivityIntent = new Intent(SplashActivity.this, MainPlayerActivity.class);
                            startActivity(startActivityIntent);
                            SplashActivity.this.finish();
                        }
                    }, SPLASH_DISPLAY_LENGTH);

                } else {

                    this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
