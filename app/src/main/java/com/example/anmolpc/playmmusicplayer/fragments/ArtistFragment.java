package com.example.anmolpc.playmmusicplayer.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.anmolpc.playmmusicplayer.AlbumSongsList;
import com.example.anmolpc.playmmusicplayer.ArtistSongs;
import com.example.anmolpc.playmmusicplayer.PreCachingLayoutManager;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.SimpleDividerItemDecoration;
import com.example.anmolpc.playmmusicplayer.adapter.ArtistAdapter;
import com.example.anmolpc.playmmusicplayer.playeractivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Handler;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;

public class ArtistFragment extends Fragment {

    RecyclerView rcyartistlist;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    List<ArtistObject> artists = new ArrayList<ArtistObject>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        View v=inflater.inflate(R.layout.artist_fragment,container,false);
        rcyartistlist=(RecyclerView)v.findViewById(R.id.artistlist);

        rcyartistlist.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcyartistlist.setHasFixedSize(true);
        rcyartistlist.setDrawingCacheEnabled(true);
        rcyartistlist.setItemViewCacheSize(20);
        rcyartistlist.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rcyartistlist.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projection = new String[] {"DISTINCT " + MediaStore.Audio.Media.ARTIST};
                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC";
                Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
                while(cursor != null && cursor.moveToNext()) {
                    artists.add(new ArtistObject(cursor.getString(0)));
                }
            }
        }).start();
        ArtistAdapter adapter=new ArtistAdapter(getActivity(),artists);
        rcyartistlist.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            rcyartistlist.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = rcyartistlist.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }
}
