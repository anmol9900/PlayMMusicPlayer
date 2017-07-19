package com.example.anmolpc.playmmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NowPlaying extends AppCompatActivity {

   public static RecyclerView songRecyclerView;
    String data[],albumid[],track,artist;
    String selection = "is_music != 0";
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        this.setTitle("Now Playing");
        SharedPreferences preferences=this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);

        songRecyclerView = (RecyclerView)findViewById(R.id.nowplaylistview);
       final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        songRecyclerView.setLayoutManager(linearLayoutManager);
        songRecyclerView.setHasFixedSize(true);
        songRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        Bundle extras = getIntent().getExtras();
        final int position=extras.getInt("position");

        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        final ArrayList<SongObject> arrayList = gson.fromJson(json, type);


        SongAdapter adapter=new SongAdapter(this,arrayList,"nowplaying");
        songRecyclerView.setAdapter(adapter);
        songRecyclerView.scrollToPosition(position);
    }
}
