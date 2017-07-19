package com.example.anmolpc.playmmusicplayer.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
import com.example.anmolpc.playmmusicplayer.NowPlaying;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.SimpleDividerItemDecoration;
import com.example.anmolpc.playmmusicplayer.adapter.SongAdapter;
import com.example.anmolpc.playmmusicplayer.playeractivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class SongFragment extends Fragment {
    int n=0,pos=0;
    MainPlayerActivity mpa=new MainPlayerActivity();
    Context context= MainPlayerActivity.context;
    SharedPreferences preferences=context.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor=preferences.edit();
    LinearLayoutManager linearLayoutManager;
    RecyclerView songRecyclerView;
    Parcelable savedRecyclerLayoutState;
    Gson gson = new Gson();
    final List<SongObject> recentSongs = new ArrayList<SongObject>();
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";

    public SongFragment() {
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            songRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = songRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_fragment, container, false);
        songRecyclerView = (RecyclerView)view.findViewById(R.id.song_list);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        songRecyclerView.setLayoutManager(linearLayoutManager);
        songRecyclerView.setHasFixedSize(true);
        songRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));



       String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };
        final Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "TITLE ASC");
        while(cursor.moveToNext()) {
            long finaltimetxt=Long.parseLong(cursor.getString(4));
            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                            toMinutes(finaltimetxt));
            recentSongs.add(new SongObject(cursor.getString(1).toString(),cursor.getString(0).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursor.getString(3)));
        }

        final SongAdapter mAdapter = new SongAdapter(getActivity(), recentSongs,"none");
        songRecyclerView.setAdapter(mAdapter);
        return view;
    }
}
