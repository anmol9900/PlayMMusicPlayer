package com.example.anmolpc.playmmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.fragments.SongFragment;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ArtistSongs extends AppCompatActivity implements AsyncResponse {

    SharedPreferences preferences=MainPlayerActivity.context.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor=preferences.edit();
    Gson gson = new Gson();
    ImageView img;
    ImageButton back;
    RecyclerView explv;
    TextView artistname,numberofsngs;
    String albid;
    Cursor cursor;
    AsyncResponse async= this;
    AlbumArtAsyncTask asynctask;
    List<SongObject> list = new ArrayList<SongObject>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Display disp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        int height = size.y;
        int layoutheight = (height * 40) / 100;
        img=(ImageView) findViewById(R.id.artistallsongart);
        artistname=(TextView) findViewById(R.id.allalbumnamesonglist);
        numberofsngs=(TextView) findViewById(R.id.allartistnumbersngs);
        back=(ImageButton) findViewById(R.id.allbackPress);
        explv=(RecyclerView) findViewById(R.id.expdlst);
        img.getLayoutParams().height = layoutheight;
        explv.getLayoutParams().height=height-layoutheight;

        Bundle incomData=getIntent().getExtras();
        String artist=incomData.getString("artist");
        artistname.setText(artist);
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
        };
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        final Cursor cursoralbum=getApplicationContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artist}, sortOrderalbum);
        while (cursoralbum.moveToNext())
        {
            if(!cursoralbum.getString(0).equals(""))
            {
                albid=cursoralbum.getString(0);
            }
            String selection = "is_music != 0";
            selection = selection + " and album_id = " + cursoralbum.getString(0);
            cursor = this.getContentResolver().query(urinew, projection, selection, null, sortOrder);
            while(cursor.moveToNext()) {
                long finaltimetxt=Long.parseLong(cursor.getString(3));
                long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(finaltimetxt));
                list.add(new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursoralbum.getString(0)));
            }
        }

        numberofsngs.setText(list.size()+" Songs");
        SongAdapter songAdapter=new SongAdapter(this,list,"artistsongs");
        LinearLayoutManager lm=new LinearLayoutManager(this);
        explv.setLayoutManager(lm);
        explv.setAdapter(songAdapter);
        explv.setHasFixedSize(true);
        explv.addItemDecoration(new SimpleDividerItemDecoration(this));
        asynctask=new AlbumArtAsyncTask(getApplicationContext(),albid);
        asynctask.delegate=async;
        asynctask.execute();

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


    class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {

            editor.clear();
            editor.commit();
            String json = gson.toJson(list);
            editor.putString("nowplaylist", json);
            editor.commit();

            return new Object();
        }
    }
}
