package com.example.anmolpc.playmmusicplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.anmolpc.playmmusicplayer.fragments.GenreObject;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.generic.AudioFileWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.KeyRep;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by anmol9900 on 6/15/2017.
 */

public class SmartShuffleService extends Service {

    String genre;
    Handler handle=new Handler();
    int count1,count2;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b=intent.getExtras();
        genre = b.getString("genre");
        Log.e("RecievingGenre",genre);
        initrun();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initrun() {
        FamiliesGeneres fg=new FamiliesGeneres();
        List<String> genres1=fg.getSimilarGeneres(genre);
        SharedPreferences preferences = this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        ArrayList<SongObject> arrayList = gson.fromJson(json, type);
        List<SongObject> songs=new ArrayList<>();


        if(!genres1.isEmpty()) {
            SongObject s;
            String[] projection = new String[]{
                    MediaStore.Audio.Genres._ID,
                    MediaStore.Audio.Genres.NAME};
            String sortOrder = MediaStore.Audio.Genres.NAME + " COLLATE NOCASE ASC";
            Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
            List<GenreObject> genres2 = new ArrayList<>();
            while (cursor.moveToNext()) {
                if (genres1.contains(cursor.getString(1))) {
                    genres2.add(new GenreObject(cursor.getString(0), cursor.getString(1)));
                }
            }

            String[] gprojection = {
                    MediaStore.Audio.Genres.Members.ARTIST,
                    MediaStore.Audio.Genres.Members.TITLE,
                    MediaStore.Audio.Genres.Members.DATA,
                    MediaStore.Audio.Genres.Members.ALBUM_ID,
                    MediaStore.Audio.Genres.Members.DURATION
            };
            String gselection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            for (GenreObject g : genres2) {
                Cursor gcursor = getApplicationContext().getContentResolver().query(
                        MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(g.getGenreid())),
                        gprojection,
                        gselection,
                        null,
                        "TITLE ASC");
                while (gcursor.moveToNext()) {
                    long finaltimetxt = Long.parseLong(gcursor.getString(4));
                    long min = TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                    long sec = TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(finaltimetxt));
                    songs.add(new SongObject(gcursor.getString(1),gcursor.getString(0), String.valueOf(min) + ":" + String.valueOf(sec), gcursor.getString(2), gcursor.getString(3)));
                }
            }
            int pos = MediaPlayerService.getPosition();
            int nposition = 0;
            if(songs.size()>1) {
                Random r = new Random();
                int Low = 1;
                int High = songs.size();
                nposition = r.nextInt(High - Low) + Low;
            }
            if (arrayList != null) {
                arrayList.add(pos + 1, songs.get(nposition));
            }
//            final String newData=songs.get(nposition).getSongData();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        byte[] b1=convert(MediaPlayerService.getData(),true);
//                        byte[] b2=convert(newData,false);
//
//                        if(count1!=count2 || count2!=count1)
//                        {
//                            Log.e("byte1","entered and started padding 0's");
//                            b1=Arrays.copyOf(b1,b2.length);
//                        }
//                        double[] d1=toDoubleArray(b1);
//                        double[] d2=toDoubleArray(b2);
//                        Log.e("byte1", String.valueOf(b1.length));
//                        Log.e("byte1", String.valueOf(b2.length));
//                        PearsonsCorrelation math = new PearsonsCorrelation();
//                        double correlation = math.correlation(d1, d2);
//                        Log.e("correlation", String.valueOf(correlation));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();



            NowPlaylistAsyncTask np = new NowPlaylistAsyncTask(arrayList, this);
            np.methodRun();
            MediaPlayerService.smartShuffled=true;

            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent broadcastIntent = new Intent("play_next_smartshuffled");
                    sendBroadcast(broadcastIntent);
                }
            },500);


        }
        else
        {
            Log.e("Genres","empty");
        }
        stopSelf();
    }

    public byte[] convert(String path,boolean y) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int i;
        while ((i = fis.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
            if(y) {
                count1++;
            }
            else
            {
                count2++;
            }
        }
        Log.e("byte1", String.valueOf(count1+"/"+count2));
        return baos.toByteArray();
    }

    public static double[] toDoubleArray(byte[] byteArray){
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(byteArray, i*times, times).getDouble();
        }
        return doubles;
    }

    public class FamiliesGeneres {

        public final List<String> alternative = new ArrayList<>(Arrays.asList(
                "Art Punk",
                "Alternative Rock",
                "College Rock",
                "Crossover Thrash",
                "Crust Punk",
                "Experimental Rock",
                "Folk Punk",
                "Goth / Gothic Rock",
                "Grunge",
                "Hardcore Punk",
                "Hard Rock",
                "Indie Rock",
                "Lo-fi",
                "New Wave",
                "Progressive Rock",
                "Punk",
                "Shoegaze",
                "Steampunk"));

        public final List<String> blues = new ArrayList<>(Arrays.asList(
                "Acoustic Blues",
                "Chicago Blues",
                "Classic Blues",
                "Contemporary Blues",
                "Country Blues",
                "Delta Blues",
                "Electric Blues",
                "Ragtime Blues"));

        public final List<String> classical = new ArrayList<>(Arrays.asList(
                "Avant-Garde",
                "Baroque",
                "Chamber Music",
                "Chant",
                "Choral",
                "Classical Crossover",
                "Contemporary Classical",
                "Early Music",
                "Expressionist",
                "High Classical",
                "Impressionist",
                "Medieval",
                "Minimalism",
                "Modern Composition",
                "Opera",
                "Orchestral",
                "Renaissance",
                "Romantic",
                "Romantic",
                "Wedding Music"));


        public final List<String> country = new ArrayList<>(Arrays.asList(
                "Alternative Country",
                "Americana",
                "Bluegrass",
                "Contemporary Bluegrass",
                "Contemporary Country",
                "Country Gospel",
                "Country Pop",
                "Honky Tonk",
                "Outlaw Country",
                "Traditional Bluegrass",
                "Traditional Country",
                "Urban Cowboy"
        ));

        public final List<String> dance = new ArrayList<>(Arrays.asList(
                "Club",
                "Club Dance",
                "Breakcore",
                "Breakbeat",
                "Breakstep",
                "Brostep",
                "Chillstep",
                "Deep House",
                "Dubstep",
                "Electro House",
                "Electroswing",
                "Exercise",
                "Future Garage",
                "Garage",
                "Glitch Hop",
                "Glitch Pop",
                "Grime",
                "Hardcore",
                "Hard Dance",
                "Hi-NRG",
                "Eurodance",
                "Horrorcore",
                "House",
                "Jackin House",
                "Jungle",
                "Drum’n’bass",
                "Liquid Dub",
                "Regstep",
                "Speedcore",
                "Techno",
                "Trance",
                "Trap"
        ));

        public final List<String> electronic = new ArrayList<>(Arrays.asList(
                "2-Step",
                "8bit",
                "Ambient",
                "Bassline",
                "Chillwave",
                "Chiptune",
                "Crunk",
                "Downtempo",
                "Drum & Bass",
                "Electro",
                "Electro-swing",
                "Electronica",
                "Electronic Rock",
                "Hardstyle",
                "IDM",
                "Experimental",
                "Industrial",
                "Trip Hop"
        ));

        public final List<String> hiphoprap = new ArrayList<>(Arrays.asList(
                "Alternative Rap",
                "Bounce",
                "Dirty South",
                "East Coast Rap",
                "Gangsta Rap",
                "Hardcore Rap",
                "Hip-Hop",
                "Hip-Hop/Rap",
                "Latin Rap",
                "Old School Rap",
                "Rap",
                "Turntablism",
                "Underground Rap",
                "West Coast Rap"
        ));


        public final List<String> inspirational = new ArrayList<>(Arrays.asList(
                "CCM",
                "Christian Metal",
                "Christian Pop",
                "Christian Rap",
                "Christian Rock",
                "Classic Christian",
                "Contemporary Gospel",
                "Gospel",
                "Christian & Gospel",
                "Praise & Worship",
                "Qawwali",
                "Southern Gospel",
                "Traditional Gospel"
        ));

        public final List<String> jazz = new ArrayList<>(Arrays.asList(
                "Acid Jazz",
                "Avant-Garde Jazz",
                "Bebop",
                "Big Band",
                "Blue Note",
                "Contemporary Jazz",
                "Cool",
                "Crossover Jazz",
                "Dixieland",
                "Ethio-jazz",
                "Fusion",
                "Gypsy Jazz",
                "Hard Bop",
                "Latin Jazz",
                "Mainstream Jazz",
                "Ragtime",
                "Smooth Jazz",
                "Trad Jazz"
        ));


        public final List<String> pop = new ArrayList<>(Arrays.asList(
                "Adult Contemporary",
                "Britpop",
                "Bubblegum Pop",
                "Chamber Pop",
                "Dance Pop",
                "Dream Pop",
                "Electro Pop",
                "Orchestral Pop",
                "Pop/Rock",
                "Pop Punk",
                "Power Pop",
                "Soft Rock",
                "Synthpop",
                "Teen Pop"
        ));

        public final List<String> rbsoulandReggae = new ArrayList<>(Arrays.asList(
                "Contemporary R&B",
                "Disco",
                "Doo Wop",
                "Funk",
                "Modern Soul",
                "Motown",
                "Neo-Soul",
                "Northern Soul",
                "Psychedelic Soul",
                "Quiet Storm",
                "Soul",
                "Soul Blues",
                "Southern Soul",
                "2-Tone",
                "Dancehall",
                "Dub",
                "Roots Reggae",
                "Ska"
        ));

        public final List<String> rock = new ArrayList<>(Arrays.asList(
                "Acid Rock",
                "Adult-Oriented Rock",
                "Afro Punk",
                "Adult Alternative",
                "Alternative Rock",
                "American Trad Rock",
                "Anatolian Rock",
                "Arena Rock",
                "Art Rock",
                "Blues-Rock",
                "British Invasion",
                "Cock Rock",
                "Death Metal",
                "Black Metal",
                "Doom Metal",
                "Glam Rock",
                "Gothic Metal",
                "Grind Core",
                "Hair Metal",
                "Hard Rock",
                "Math Metal",
                "Math Rock",
                "Metal",
                "Metal Core",
                "Noise Rock",
                "Jam Bands",
                "Post Punk",
                "Prog-Rock",
                "Art Rock",
                "Progressive Metal",
                "Psychedelic",
                "Rock & Roll",
                "Rockabilly",
                "Roots Rock",
                "Singer",
                "Songwriter",
                "Southern Rock",
                "Spazzcore",
                "Stoner Metal",
                "Surf",
                "Rock",
                "Technical Death Metal",
                "Tex-Mex",
                "Time Lord Rock",
                "Trash Metal"
        ));

        public final List<String> singersongwriter = new ArrayList<>(Arrays.asList(
                "Alternative Folk",
                "Contemporary Folk",
                "Contemporary Singer",
                "Songwriter",
                "Indie Folk",
                "Folk-Rock",
                "Love Song",
                "New Acoustic",
                "Traditional Folk"
        ));

        public final List<String> soundtrack = new ArrayList<>(Arrays.asList(
                "Foreign Cinema",
                "Movie Soundtrack",
                "Musicals",
                "Original Score",
                "Soundtrack",
                "TV Soundtrack",
                "Score"
        ));

        public final List<String> texmextejano  = new ArrayList<>(Arrays.asList(
                "Chicano",
                "Classic",
                "Conjunto",
                "Conjunto Progressive",
                "New Mex",
                "Tex-Mex"
        ));

        public final List<String> vocal  = new ArrayList<>(Arrays.asList(
                "A cappella",
                "Barbershop",
                "Doo-wop",
                "Gregorian Chant",
                "Standards",
                "Traditional Pop",
                "Vocal Jazz",
                "Vocal Pop"
        ));

        public final List<String> bollywood  = new ArrayList<>(Arrays.asList(
                "Hindustani",
                "Indian Ghazal",
                "Indian Pop",
                "Indian Rock",
                "Bollywood",
                "Punjabi",
                "Bollywood Songs",
                "Indian Pop/Single"
        ));



        public List<String> getSimilarGeneres(String genre)
        {
            List<List<String>> families=new ArrayList<>(Arrays.asList(alternative,blues,classical,country,dance,electronic,hiphoprap,inspirational,jazz,pop,rbsoulandReggae,rock,singersongwriter,soundtrack,texmextejano,vocal,bollywood));
            HashSet<String> genres=new HashSet<>();
            for (List<String> family:families) {
                for(String childs:family)
                {
                    boolean check=genre.toLowerCase().contains(childs.toLowerCase());
                    if(check)
                    {
                        genres.addAll(family);
                    }
                }
            }
            List<String> sortedgenres=new ArrayList<>();
            sortedgenres.addAll(genres);
            return sortedgenres;
        }
    }
}
