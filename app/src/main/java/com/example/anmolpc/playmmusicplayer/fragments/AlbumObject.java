package com.example.anmolpc.playmmusicplayer.fragments;

/**
 * Created by Anmol Pc on 4/15/2017.
 */

public class AlbumObject {
    private String AlbumCover;
    private String AlbumArtist;
    private String AlbumName;
    private String NumberOfSongs;

    public AlbumObject(String albumCover, String albumArtist, String albumName,String numberOfSongs) {
        this.AlbumCover = albumCover;
        this.AlbumArtist = albumArtist;
        this.AlbumName = albumName;
        this.NumberOfSongs=numberOfSongs;
    }

    public String getAlbumCover() {
        return AlbumCover;
    }

    public String getAlbumArtist() {
        return AlbumArtist;
    }

    public String getAlbumName() {
        return AlbumName;
    }
    public String getNumberOfSongs() {
        return NumberOfSongs;
    }
}
