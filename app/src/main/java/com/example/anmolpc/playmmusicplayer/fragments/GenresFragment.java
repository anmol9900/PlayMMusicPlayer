package com.example.anmolpc.playmmusicplayer.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.SimpleDividerItemDecoration;
import com.example.anmolpc.playmmusicplayer.adapter.GenreAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anmol9900 on 6/6/2017.
 */

public class GenresFragment extends Fragment {

    RecyclerView rcygenre;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        View v=inflater.inflate(R.layout.genres_fragment,container,false);
        rcygenre=(RecyclerView) v.findViewById(R.id.genrercy);

        String[] projection = new String[] {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME};
        String sortOrder = MediaStore.Audio.Genres.NAME + " COLLATE NOCASE ASC";
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
        List<GenreObject> genres= new ArrayList<>();
        while (cursor.moveToNext()){
            genres.add(new GenreObject(cursor.getString(0),cursor.getString(1)));
        }

        GenreAdapter genreAdapter=new GenreAdapter(getActivity(),genres);
        rcygenre.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcygenre.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        rcygenre.setAdapter(genreAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            rcygenre.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = rcygenre.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }
}
