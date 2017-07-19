package com.example.anmolpc.playmmusicplayer.fragments;

/**
 * Created by anmol9900 on 6/6/2017.
 */

public class GenreObject {
    private String genreid,genrename;

    public GenreObject(String id,String name){
        this.genreid=id;
        this.genrename=name;
    }


    public String getGenreid() {
        return genreid;
    }

    public String getGenrename() {
        return genrename;
    }
}
