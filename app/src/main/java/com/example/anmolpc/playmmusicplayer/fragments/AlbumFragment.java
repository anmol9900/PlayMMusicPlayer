package com.example.anmolpc.playmmusicplayer.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.anmolpc.playmmusicplayer.AlbumSongsList;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerViewItemDecoraition;
import com.example.anmolpc.playmmusicplayer.adapter.AlbumAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anmol Pc on 4/10/2017.
 */

public class AlbumFragment extends Fragment {

    RecyclerView albumrcy;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);

        View view = inflater.inflate(R.layout.album_fragment, container, false);
        albumrcy=(RecyclerView) view.findViewById(R.id.albumrcyview);



        String[] projection = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        final Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        final List<AlbumObject> albums = new ArrayList<AlbumObject>();
        while(cursor.moveToNext()) {
            albums.add(new AlbumObject(cursor.getString(0).toString(),cursor.getString(2).toString(),cursor.getString(1).toString(),cursor.getString(3).toString()));
        }
        AlbumAdapter albumAdapter=new AlbumAdapter(getActivity(),albums);
        albumrcy.setLayoutManager(new GridLayoutManager(getContext(),2));
        albumrcy.setAdapter(albumAdapter);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = albumrcy.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            albumrcy.getLayoutManager().onRestoreInstanceState(listState);
        }
    }
}
