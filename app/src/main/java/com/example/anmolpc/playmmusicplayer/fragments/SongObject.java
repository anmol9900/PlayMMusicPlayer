package com.example.anmolpc.playmmusicplayer.fragments;

/**
 * Created by Anmol Pc on 4/10/2017.
 */
public class SongObject {

    private String songDuration;
    private String songTitle;
    private String songAuthor;
    private String songData;
    private String songAlbumid;

    public SongObject(String songTitle, String songAuthor, String songDuration,String songData,String songAlbumid) {
        this.songDuration = songDuration;
        this.songAuthor = songAuthor;
        this.songTitle = songTitle;
        this.songData = songData;
        this.songAlbumid = songAlbumid;
    }

    public String getSongDuration() {return songDuration;}

    public String getSongAuthor() {
        return songAuthor;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongData() {
        return songData;
    }

    public String getSongAlbumid() {
        return songAlbumid;
    }
}
