package com.example.anmolpc.playmmusicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistObject;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistView extends AppCompatActivity implements AsyncResponse {

    RecyclerView rcyView;
    TextView listname,listnoofsongs;
    ImageView img;
    ImageButton backbtn;
    public static String name;
    AsyncResponse async= this;
    AlbumArtAsyncTask asynctask;
    String albid;
    MainPlayerActivity mpa=new MainPlayerActivity();
    Context context= MainPlayerActivity.context;
    SharedPreferences preferences=context.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor=preferences.edit();
    Gson gson = new Gson();
    ArrayList<SongObject> Objectlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_view);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        rcyView=(RecyclerView) findViewById(R.id.playlistsongs);
        listname=(TextView) findViewById(R.id.nameofplaylist);
        listnoofsongs=(TextView) findViewById(R.id.noofsongsplaylist);
        backbtn=(ImageButton) findViewById(R.id.playlistbackpress);
        img=(ImageView) findViewById(R.id.playlistart);
        Display disp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        int height = size.y;
        int layoutheight = (height * 40) / 100;
        img.getLayoutParams().height = layoutheight;
        rcyView.getLayoutParams().height = height-layoutheight;
        Gson gson = new Gson();
        Bundle bn=getIntent().getExtras();
        name=bn.getString("list");
        SharedPreferences preferences= context.getSharedPreferences(name, Context.MODE_PRIVATE);
        String json = preferences.getString("playlist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        Objectlist = gson.fromJson(json, type);


        listname.setText(name);
        listnoofsongs.setText(Objectlist.size()+" Songs");
        rcyView.setHasFixedSize(true);
        rcyView.addItemDecoration(new SimpleDividerItemDecoration(this));
        LinearLayoutManager lm=new LinearLayoutManager(getApplicationContext());
        rcyView.setLayoutManager(lm);
        SongAdapter adapter=new SongAdapter(this,Objectlist,"playlist");
        rcyView.setAdapter(adapter);

        List<String> albids=new ArrayList<>();
        for(SongObject so: Objectlist)
        {
            if(!so.getSongAlbumid().equals(null))
            {
                albids.add(so.getSongAlbumid());
            }
        }

        Random r = new Random();
        int Low = 0;
        int High = albids.size();
        int pos = r.nextInt(High - Low) + Low;

        albid = albids.get(pos);

        asynctask=new AlbumArtAsyncTask(getApplicationContext(),albid);
        asynctask.delegate=async;
        asynctask.execute();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
                backbtn.startAnimation(anim);
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
            img.setImageBitmap(output);
            img.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            img.setImageResource(R.drawable.placeholder);
            img.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
    }

    public class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {

            editor.clear();
            editor.commit();
            String json = gson.toJson(Objectlist);
            editor.putString("nowplaylist", json);
            editor.commit();

            return new Object();
        }
    }
}
