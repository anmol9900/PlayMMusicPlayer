package com.example.anmolpc.playmmusicplayer;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    //global variables
    public static MediaPlayer player;
    public static String data;
    public static final String PLAY_NEXT_ON_COMPLETION="MediaPlayer.OnCompletion";

    private final IBinder musicBind = new MusicBinder();
    private int resumeposition;
    private static int position;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    public static boolean musicBound=false,set=false;
    SharedPreferences preferences;
    SharedPreferences preferencesettings;
    SharedPreferences mostPlayedTracker;
    SharedPreferences.Editor mpEdit;
    ArrayList<SongObject> arrayListplayer;
    public static boolean smartShuffled=false;
    int volume = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Bundle b=intent.getExtras();
        data = b.getString("data");
        position=b.getInt("pos");
        initrun();
        Log.e("servicecheck","service bound");
        return musicBind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b=intent.getExtras();
        data = b.getString("data");
        position=b.getInt("pos");
        initrun();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestAudioFocus();
        registerBecomingNoisyReceiver();
        register_playNewAudio();
        callStateListener();
        preferences=this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
        preferencesettings=this.getSharedPreferences("playersettings", Context.MODE_PRIVATE);
        mostPlayedTracker=this.getSharedPreferences("mostplayed", Context.MODE_PRIVATE);
        mpEdit=mostPlayedTracker.edit();
        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        arrayListplayer = gson.fromJson(json, type);
    }

    @Override
    public boolean onUnbind(Intent intent){
        stopMedia();
        player.release();
        removeAudioFocus();
        musicBound=false;
        set=false;
        Log.e("servicecheck","service unbound");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent broadcastIntent = new Intent(PLAY_NEXT_ON_COMPLETION);
        sendBroadcast(broadcastIntent);
        }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(MainPlayerActivity.player_open)
        {
            Intent broadcastIntent = new Intent(MainPlayerActivity.smallplayerupdate);
            sendBroadcast(broadcastIntent);
        }
        if(smartShuffled)
        {

        }
        else {
            playMedia();
        }
    }

    public void playSong(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String filename = data.substring(data.lastIndexOf("/") + 1);
                int pos=filename.lastIndexOf(".");
                String name=filename.substring(0,pos);

                if(mostPlayedTracker.contains(name))
                {
                    MostPlayedObject obj=new MostPlayedObject();
                    Gson gson = new Gson();
                    String json = mostPlayedTracker.getString(name, null);
                    Type type = new TypeToken<MostPlayedObject>() {}.getType();
                    obj = gson.fromJson(json, type);
                    if (obj != null) {
                        int times=obj.getNumberOfPlays();
                        times++;
                        obj.setNumberOfPlays(times);
                    }
                    String ajson = gson.toJson(obj);
                    mpEdit.putString(name,ajson);
                    mpEdit.apply();
                }
                else
                {
                    MostPlayedObject obj=new MostPlayedObject();
                    obj.setNumberOfPlays(1);
                    obj.setPath(data);
                    Gson gson = new Gson();
                    String json = gson.toJson(obj);
                    mpEdit.putString(name,json);
                    mpEdit.apply();
                }
            }
        }).start();



        try {
            player = new MediaPlayer();
            player.reset();
            player.setDataSource(data);
            player.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (IllegalArgumentException e) {
            stopSelf();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            stopSelf();
            e.printStackTrace();
        } catch (IOException e) {
            stopSelf();
            e.printStackTrace();
        }
    }

    public void crossFadeSong()
    {
        try {
            player = new MediaPlayer();
            player.reset();
            player.setDataSource(data);
            player.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        smartShuffled=false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (player == null) playSong();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player.isPlaying()) player.stop();
                player.release();
                player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class MusicBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private void playMedia() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    private void pauseMedia() {
        if (player.isPlaying()) {
            player.pause();
            resumeposition = player.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!player.isPlaying()) {
            player.seekTo(resumeposition);
            player.start();
        }
    }

    private void stopMedia() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            stopMedia();
            player.release();
            musicBound=false;
        }
        removeAudioFocus();
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
//            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (player != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            if(smartShuffled)
            {
                if(player.isPlaying())
                {
                    player.release();
                    Bundle b = intent.getExtras();
                    data = b.getString("data");
                    position = b.getInt("pos");
                    croosinitRun();
                }
                else{
                    player.release();
                    Bundle b = intent.getExtras();
                    data = b.getString("data");
                    position = b.getInt("pos");
                    initrun();
                }
            }
            else {
                player.release();
                Bundle b = intent.getExtras();
                data = b.getString("data");
                position = b.getInt("pos");
                initrun();
            }
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    public void initrun()
    {
        playSong();
        musicBound=true;
    }

    public void croosinitRun()
    {
        musicBound=true;
        crossFadeSong();
    }

    public static int getPosition()
    {
        return position;
    }

    public static String getData(){return data;}

}
