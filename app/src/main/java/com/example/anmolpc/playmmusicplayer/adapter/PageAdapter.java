package com.example.anmolpc.playmmusicplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.anmolpc.playmmusicplayer.fragments.AlbumFragment;
import com.example.anmolpc.playmmusicplayer.fragments.ArtistFragment;
import com.example.anmolpc.playmmusicplayer.fragments.GenresFragment;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistFragment;
import com.example.anmolpc.playmmusicplayer.fragments.SongFragment;


public class PageAdapter extends FragmentPagerAdapter {

    private static final String TAG = PageAdapter.class.getCanonicalName();
    private static final int FRAGMENT_COUNT = 5;

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new SongFragment();
            case 1:
                return new ArtistFragment();
            case 2:
                return new AlbumFragment();
            case 3:
                return new PlaylistFragment();
            case 4:
                return new GenresFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Songs";
            case 1:
                return "Artists";
            case 2:
                return "Albums";
            case 3:
                return "Playlist";
            case 4:
                return "Genres";
        }
        return null;
    }
}
