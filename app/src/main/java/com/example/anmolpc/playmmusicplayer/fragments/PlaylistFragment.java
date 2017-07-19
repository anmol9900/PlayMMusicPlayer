package com.example.anmolpc.playmmusicplayer.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anmolpc.playmmusicplayer.MainPlayerActivity;
import com.example.anmolpc.playmmusicplayer.PlaylistView;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.SimpleDividerItemDecoration;
import com.example.anmolpc.playmmusicplayer.adapter.PlayListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Anmol Pc on 4/10/2017.
 */
public class PlaylistFragment extends Fragment {

    MainPlayerActivity mpa=new MainPlayerActivity();
    Context context= MainPlayerActivity.context;
    SharedPreferences preferences=context.getSharedPreferences("playlists", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor=preferences.edit();
    RecyclerView rcy;
    Gson gson = new Gson();
    PlayListAdapter pla;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_playlists, container, false);
        rcy=(RecyclerView) view.findViewById(R.id.rcyplaylists);
        rcy.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        if(!preferences.contains("list"))
        {
            List<PlaylistObject> lst=new ArrayList<>();
            lst.add(new PlaylistObject("Most Played"));
            lst.add(new PlaylistObject("Recently Added"));
            String json = gson.toJson(lst);
            editor.putString("list", json);
            editor.commit();
        }


        SharedPreferences pref=context.getSharedPreferences("Recently Added", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=pref.edit();
        edit.clear();
        edit.commit();
        final List<SongObject> recentSongs = new ArrayList<SongObject>();
        String selection = MediaStore.Audio.Media.DATE_ADDED + ">" + (System.currentTimeMillis() / 1000 - 14*60*60*24);
        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED
        };
        final Cursor cursor = getActivity().managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DATE_ADDED+" DESC");


        while(cursor.moveToNext()) {
            long min = 0,sec=0;
            String time;
            if(cursor.getString(4)!=null) {
                long finaltimetxt = Long.parseLong(cursor.getString(4));
               min = TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                sec= TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(finaltimetxt));
                time=String.valueOf(min) + ":" + String.valueOf(sec);
                recentSongs.add(new SongObject(cursor.getString(1).toString(), cursor.getString(0).toString(), time, cursor.getString(2), cursor.getString(3)));
            }
        }

        String jsonsong = gson.toJson(recentSongs);
        edit.putString("playlist", jsonsong);
        edit.commit();

        Gson gson = new Gson();
        String json = preferences.getString("list", null);
        Type type = new TypeToken<ArrayList<PlaylistObject>>() {}.getType();
        final ArrayList<PlaylistObject> Objectlist = gson.fromJson(json, type);

        rcy.setHasFixedSize(true);
        LinearLayoutManager lm=new LinearLayoutManager(getActivity());
        rcy.setLayoutManager(lm);
        pla=new PlayListAdapter(getActivity(),Objectlist,"playlist");
        rcy.setAdapter(pla);

        return view;
    }
}
