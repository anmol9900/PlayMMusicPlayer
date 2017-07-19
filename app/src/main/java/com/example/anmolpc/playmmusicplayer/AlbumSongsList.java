package com.example.anmolpc.playmmusicplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.adapter.fastblur;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.anmolpc.playmmusicplayer.MainPlayerActivity.context;
import static com.example.anmolpc.playmmusicplayer.playeractivity.sArtworkUri;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AlbumSongsList extends AppCompatActivity implements AsyncResponse {
    ImageView img;
    RecyclerView rcysonglist;
    ImageButton bckBtn;
    TextView album,albumartist,albumyear;
    SharedPreferences preferences=context.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    Gson gson = new Gson();
    AsyncResponse async= this;
    AlbumArtAsyncTask asynctask;
    final List<SongObject> albumSongs = new ArrayList<SongObject>();
    int pos=0;
     public static boolean active = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_songs_list);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Display disp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        int height = size.y;
        int layoutheight = (height * 40) / 100;
        img = (ImageView) findViewById(R.id.albumartalbumlist);
        img.getLayoutParams().height = layoutheight;
        rcysonglist=(RecyclerView) findViewById(R.id.albumsonglist);
        rcysonglist.getLayoutParams().height=height-layoutheight;
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcysonglist.setLayoutManager(linearLayoutManager);
        rcysonglist.setHasFixedSize(true);
        rcysonglist.addItemDecoration(new SimpleDividerItemDecoration(this));
        album=(TextView)findViewById(R.id.albumnamesonglist);
        albumartist=(TextView)findViewById(R.id.artistsonglist);
        albumyear=(TextView)findViewById(R.id.albumyear);

        Bundle incomData=getIntent().getExtras();
        final String albumid=incomData.getString("albumid");

        asynctask=new AlbumArtAsyncTask(getApplicationContext(),albumid);
        asynctask.delegate=async;
        asynctask.execute();


        String selection = "is_music != 0";
        selection = selection + " and album_id = " + albumid;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";


            Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor cursor = this.getContentResolver().query(urinew, projection, selection, null, sortOrder);
        cursor.moveToFirst();
        album.setText(cursor.getString(4));
        albumartist.setText(cursor.getString(1));
        albumyear.setText(cursor.getString(5));
        cursor.moveToPosition(-1);
        while(cursor.moveToNext()) {
            long finaltimetxt=Long.parseLong(cursor.getString(3));
            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                            toMinutes(finaltimetxt));
            albumSongs.add(new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albumid));
        }
        final SongAdapter mAdapter = new SongAdapter(this, albumSongs,"albumlist");
        rcysonglist.setAdapter(mAdapter);

        bckBtn=(ImageButton)findViewById(R.id.backPress);
        bckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
                bckBtn.startAnimation(anim);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1)
        {
            Intent broadcastIntent = new Intent("recieveimagefilter");
            Bundle b=new Bundle();
            b.putString("data", String.valueOf(data.getData()));
            broadcastIntent.putExtras(b);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active=true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active=false;
    }

    @Override
    public void processFinish(Bitmap output) {
        if(output!=null) {
            img.setImageBitmap(output);
            img.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            img.setImageResource(R.drawable.placeholder);
            img.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);
        }
    }

    class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {

            editor.clear();
            editor.commit();
            String json = gson.toJson(albumSongs);
            editor.putString("nowplaylist", json);
            editor.commit();

            return new Object();
        }
    }

}
