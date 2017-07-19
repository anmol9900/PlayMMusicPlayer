package com.example.anmolpc.playmmusicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GenreView extends AppCompatActivity implements AsyncResponse {
    RecyclerView genrercyview;
    ImageButton back;
    TextView genrename;
    ImageView genreimage;
    String id,name,albid;
    List<SongObject> songs=new ArrayList<>();
    AsyncResponse async= this;
    AlbumArtAsyncTask asynctask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_view);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        genrercyview=(RecyclerView) findViewById(R.id.genresongs);
        back=(ImageButton) findViewById(R.id.genrebackpress);
        genrename=(TextView) findViewById(R.id.nameofgenre);
        genreimage=(ImageView) findViewById(R.id.genreart);
        Display disp = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Point size = new Point();
        disp.getSize(size);
        int height = size.y;
        int width = size.x;
        genreimage.getLayoutParams().height= (height*40)/100;
        genrercyview.getLayoutParams().height=((height*60)/100);
        ViewGroup.MarginLayoutParams backparams = (ViewGroup.MarginLayoutParams) back.getLayoutParams();
        backparams.leftMargin = Math.round(((width*5)/100) / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        backparams.topMargin = Math.round(((height*15)/100)/ ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        back.setLayoutParams(backparams);
        ViewGroup.MarginLayoutParams nameparams = (ViewGroup.MarginLayoutParams) genrename.getLayoutParams();
        nameparams.leftMargin = Math.round(((width*5)/100) / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        nameparams.topMargin = Math.round(((height*60)/100)/ ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        genrename.setLayoutParams(nameparams);
        Bundle b=getIntent().getExtras();
        id=b.getString("id");
        name=b.getString("name");

        genrename.setText(name);
        List<String> albids=new ArrayList<>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Genres.Members.ARTIST,
                MediaStore.Audio.Genres.Members.TITLE,
                MediaStore.Audio.Genres.Members.DATA,
                MediaStore.Audio.Genres.Members.ALBUM_ID,
                MediaStore.Audio.Genres.Members.DURATION
        };
        final Cursor cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(id)),
                projection,
                selection,
                null,
                "TITLE ASC");
        while (cursor.moveToNext()){
            albids.add(cursor.getString(3));
            long finaltimetxt=Long.parseLong(cursor.getString(4));
            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                            toMinutes(finaltimetxt));
            String fmin,fsec;
            if(min<10)
            {
                fmin="0"+min;
            }
            else {
                fmin= String.valueOf(min);
            }
            if(sec<10)
            {
                fsec="0"+sec;
            }
            else
            {
                fsec= String.valueOf(sec);
            }
            songs.add(new SongObject(cursor.getString(1), cursor.getString(0),fmin+":"+fsec,cursor.getString(2),cursor.getString(3)));
            Log.e("value",cursor.getString(1)+cursor.getString(0)+fmin+":"+fsec+cursor.getString(2)+cursor.getString(3));

        }

        Random r = new Random();
        int Low = 0;
        int High = albids.size();
        int pos = r.nextInt(High - Low) + Low;

        albid = albids.get(pos);

        asynctask=new AlbumArtAsyncTask(getApplicationContext(),albid);
        asynctask.delegate=async;
        asynctask.execute();

        LinearLayoutManager lm=new LinearLayoutManager(getApplicationContext());
        genrercyview.addItemDecoration(new SimpleDividerItemDecoration(this));
        genrercyview.setLayoutManager(lm);
        SongAdapter adapter=new SongAdapter(this,songs,"genre");
        genrercyview.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
                back.startAnimation(anim);
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(this,MainPlayerActivity.class);
        startActivity(i);
    }

    @Override
    public void processFinish(Bitmap output) {
        if(output!=null)
        {
            genreimage.setImageBitmap(output);
            genreimage.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            genreimage.setImageResource(R.drawable.placeholder);
            genreimage.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
    }
}
