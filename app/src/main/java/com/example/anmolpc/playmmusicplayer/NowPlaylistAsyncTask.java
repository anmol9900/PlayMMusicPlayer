package com.example.anmolpc.playmmusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Anmol Pc on 5/12/2017.
 */

public class NowPlaylistAsyncTask{

    List<SongObject> objectlist;
    Context mcontext;

    public NowPlaylistAsyncTask(List<SongObject> Objectlist,Context context)
    {
        this.objectlist=Objectlist;
        this.mcontext=context;
    }

    public void methodRun()
    {
        new AsyncTaskPlaylist().execute();
    }

    private class AsyncTaskPlaylist extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            SharedPreferences preferences=mcontext.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            editor.clear();
            editor.commit();
            Gson gson = new Gson();
            String json = gson.toJson(objectlist);
            editor.putString("nowplaylist", json);
            editor.commit();
            return null;
        }
    }
    }
