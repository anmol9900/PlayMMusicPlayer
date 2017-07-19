package com.example.anmolpc.playmmusicplayer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.adapter.BitmapBrightness;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.anmolpc.playmmusicplayer.MediaPlayerService.player;

public class playeractivity extends AppCompatActivity implements AsyncResponse,SeekBar.OnSeekBarChangeListener,View.OnClickListener,View.OnLongClickListener,View.OnTouchListener {

    final public static Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");
    public static final String MEDIAPLAYER_PLAY_NEW_AUDIO = "com.example.anmolpc.playmmusicplayer.PlayNewAudio";
    public static final String MEDIAPLAYER_SET_AUDIO = "com.example.anmolpc.playmmusicplayer.setAudio";
    public static ImageButton play;
    public static TextView currenttime,finaltime;
    public static SeekBar seekbar,seekvolume;
    RelativeLayout option_player,seekwindow;
    ViewGroup root;
    public int mValue,mDur=1200,mDurTweak=50;
    KenBurnsView img;
    Window window;
    ImageButton shufffle,prev,next,repeat,nowplaylist,equilizer,smartshuffle,volume;
    TextView album,song,artist,next_song;
    LinearLayout playerui;
    int position;
    String intent;
    SharedPreferences preferences;
    SharedPreferences preferencesettings;
    ArrayList<SongObject> arrayListplayer;
    Display disp;
    Point size;
    Bitmap bmap = null,resource=null;
    AlbumArtAsyncTask asynctask;
    AsyncResponse async= this;
    ServiceConnection serviceConnection;
    Handler progressBarHandler = new Handler();
    Handler finalset=new Handler();
    AudioManager audioManager = null;
    boolean set_shuffled=false;
    int shuffledpos;
    boolean checkLongClick=false;
    private MediaPlayerService musicSrv;
    private Intent playIntent;
    private Handler repeatUpdateHandler = new Handler();
    private Handler rotateUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private Runnable SetFinalTime = new Runnable() {
        @Override
        public void run() {
            if(player!=null)
            {
                finaltime();
                finalset.removeCallbacks(SetFinalTime);
            }
        }
    };
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            if(player != null) {
                int startTime = 0;
                try {
                    startTime = player.getCurrentPosition();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (e instanceof IllegalStateException) {
                        startTime = 0;
                    }
                }
                String minformat, secformat;
                long min = TimeUnit.MILLISECONDS.toMinutes((long) startTime);
                long sec = TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) startTime));
                if (min < 10) {
                    minformat = "0%d";
                } else {
                    minformat = "%d";
                }
                if (sec < 10) {
                    secformat = "0%d";
                } else {
                    secformat = "%d";
                }
                currenttime.setText(String.format(minformat + ":" + secformat,
                        min, sec));
                seekbar.setProgress(startTime);
                progressBarHandler.postDelayed(this, 100);
            }
        }
    };
    private BroadcastReceiver OnCompletion = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (preferencesettings.contains("repeat")) {
                if (preferencesettings.getString("repeat", "").equals("one")) {
                    asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                    asynctask.delegate=async;
                    asynctask.execute();
                    playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                } else if (preferencesettings.getString("repeat", "").equals("none")) {
                    if (position == arrayListplayer.size()-1) {
                        player.stop();
                    } else {
                        if (preferencesettings.getString("shuffle", "").equals("on")) {
                            position = shuffledpos;
                            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                            asynctask.delegate=async;
                            asynctask.execute();
                            playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                        } else {
                            position++;
                            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                            asynctask.delegate=async;
                            asynctask.execute();
                            playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                        }
                    }
                }
                else{
                    if (preferencesettings.getString("shuffle", "").equals("on")) {
                        position = shuffledpos;
                        asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                        asynctask.delegate=async;
                        asynctask.execute();
                        playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                    } else {
                        if (position == arrayListplayer.size()-1) {
                            position = 0;
                            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                            asynctask.delegate=async;
                            asynctask.execute();
                            playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                        } else {
                            position++;
                            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                            asynctask.delegate=async;
                            asynctask.execute();
                            playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                        }
                    }
                }
            } else {
                position++;
                asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
                asynctask.delegate=async;
                asynctask.execute();
                playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
            }
        }
    };
    private BroadcastReceiver playNextShuffled = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {
            smartshuffle.clearAnimation();
            Gson gson = new Gson();
            String json = preferences.getString("nowplaylist", null);
            Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
            arrayListplayer = gson.fromJson(json, type);

            position++;
            asynctask = new AlbumArtAsyncTask(getApplicationContext(), arrayListplayer.get(position).getSongAlbumid());
            asynctask.delegate = async;
            asynctask.execute();
            playerstart(arrayListplayer.get(position).getSongData(), arrayListplayer.get(position).getSongAlbumid());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.MyTextViewStyle);
        setContentView(R.layout.activity_playeractivity);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.parseColor("#7F000000"));

        preferences=this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
        preferencesettings=this.getSharedPreferences("playersettings", Context.MODE_PRIVATE);

        final Animation outAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
        final Animation inAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        final SharedPreferences.Editor editor=preferencesettings.edit();

        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        arrayListplayer = gson.fromJson(json, type);

        //initalization
        shufffle=(ImageButton)findViewById(R.id.shuffle);
        prev=(ImageButton)findViewById(R.id.prevsong);
        next=(ImageButton)findViewById(R.id.nextsong);
        play=(ImageButton)findViewById(R.id.playsong);
        repeat=(ImageButton)findViewById(R.id.repeatsong);
        album=(TextView)findViewById(R.id.albumtxt);
        song=(TextView)findViewById(R.id.songtxt);
        artist=(TextView)findViewById(R.id.artisttxt);
        currenttime=(TextView)findViewById(R.id.starttimetxt);
        finaltime=(TextView)findViewById(R.id.endtimetxt);
        next_song=(TextView)findViewById(R.id.next_song_title);
        seekbar=(SeekBar)findViewById(R.id.songProgressBar);
        nowplaylist=(ImageButton)findViewById(R.id.nowplaylist);
        equilizer=(ImageButton) findViewById(R.id.equilizer);
        smartshuffle=(ImageButton) findViewById(R.id.smartshuffle);
        playerui=(LinearLayout) findViewById(R.id.allplayerui);
        root=(ViewGroup) findViewById(R.id.activity_playeractivity);
        option_player=(RelativeLayout) findViewById(R.id.player_options);
        volume = (ImageButton) findViewById(R.id.volume);
        seekwindow = (RelativeLayout) findViewById(R.id.seekwindow);
        seekvolume = (SeekBar) findViewById(R.id.seekvolume);
        seekbar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        seekvolume.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        seekbar.setOnSeekBarChangeListener(this);
        window = this.getWindow();
        disp = getWindowManager().getDefaultDisplay();
        size = new Point();
        disp.getSize(size);
        int height = size.y;
        int width = size.x;
        final int layoutheight = (height * 60) / 100;
        next_song.getLayoutParams().width = (width*40)/100;
        playerui.getLayoutParams().height = (height*40)/100;
        option_player.setY((height * 52) / 100);
        seekwindow.setY((height * 48) / 100);
        seekwindow.setX(width/2-80);
        seekwindow.getLayoutParams().width = (width*40)/100;
        img = (KenBurnsView) findViewById(R.id.albumartimgview);
        img.getLayoutParams().height=layoutheight;
        Bundle extras = getIntent().getExtras();
        register_OnCompletion();
        register_playNextShuffled();
        next.setOnClickListener(this);
        next.setOnLongClickListener(this);
        next.setOnTouchListener(this);
        prev.setOnClickListener(this);
        prev.setOnLongClickListener(this);
        prev.setOnTouchListener(this);
        img.setOnTouchListener(this);
        root.setOnTouchListener(this);


        intent = extras.getString("intent");
        if(intent.equals("callui")) {
            if (isMyServiceRunning(MediaPlayerService.class)) {
                position = MediaPlayerService.getPosition();
                asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid());
                asynctask.delegate = async;
                asynctask.execute();
                play.setImageResource(R.drawable.pause);
                progressBarHandler.postDelayed(UpdateSongTime,100);
                finalset.postDelayed(SetFinalTime,100);
                setData(arrayListplayer.get(position).getSongData());
            }
            else {
                position = 0;
                asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid());
                asynctask.delegate = async;
                asynctask.execute();
                play.setImageResource(R.drawable.pause);
                playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
            }
        }
        else {
            if (!isMyServiceRunning(MediaPlayerService.class)) {
                String data = extras.getString("data");
                final String albumid = extras.getString("albumid");
                position = extras.getInt("position");
                intent = extras.getString("intent");
                asynctask = new AlbumArtAsyncTask(getApplicationContext(), albumid);
                asynctask.delegate = async;
                asynctask.execute();
                playerstart(data, albumid);
            } else {
                String data = extras.getString("data");
                final String albumid = extras.getString("albumid");
                position = extras.getInt("position");
                asynctask = new AlbumArtAsyncTask(getApplicationContext(), albumid);
                asynctask.delegate = async;
                asynctask.execute();
                playerstart(data, albumid);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation a= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.opt_player_slidedown);
                option_player.startAnimation(a);
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        option_player.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        },3000);


        if(preferencesettings.getString("shuffle","").equals("on"))
        {
            shufffle.setImageResource(R.drawable.shuffleon);
            set_shuffled=true;
        }
        else
        {
            shufffle.setImageResource(R.drawable.shuffle);
        }

        if(preferencesettings.getString("repeat","").equals("one"))
        {
            repeat.setImageResource(R.drawable.replay1);
        }
        else if(preferencesettings.getString("repeat","").equals("all"))
        {
            repeat.setImageResource(R.drawable.replayall);
        }
        else
        {
            repeat.setImageResource(R.drawable.repeat);
        }

        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seekwindow.getVisibility() == View.GONE) {
                    Animation a=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.opt_player_slideup);
                    seekwindow.startAnimation(a);
                    seekwindow.setVisibility(View.VISIBLE);
                    try
                    {
                        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        seekvolume.setMax(audioManager
                                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                        seekvolume.setProgress(audioManager
                                .getStreamVolume(AudioManager.STREAM_MUSIC));


                        seekvolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                        {
                            @Override
                            public void onStopTrackingTouch(SeekBar arg0)
                            {
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar arg0)
                            {
                            }

                            @Override
                            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                            {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                        progress, 0);
                            }
                        });
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    Animation a=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.opt_player_slidedown);
                    seekwindow.startAnimation(a);
                    a.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            seekwindow.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        smartshuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioFile f;
                Tag tag;
                String genre = null;
                TagOptionSingleton.getInstance().setAndroid(true);
                File mp3File = new File(arrayListplayer.get(MediaPlayerService.getPosition()).getSongData());
                try {
                    f = AudioFileIO.read(mp3File);
                    tag = f.getTagOrCreateAndSetDefault();
                    genre = tag.getFirst(FieldKey.GENRE);
                } catch (CannotReadException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TagException e) {
                    e.printStackTrace();
                } catch (ReadOnlyFileException e) {
                    e.printStackTrace();
                } catch (InvalidAudioFrameException e) {
                    e.printStackTrace();
                }


                if(genre != null && !genre.isEmpty()) {
                    startSmartShuffledAnim();
                    Log.e("SendingGenre", genre);
                    Intent i = new Intent(getApplicationContext(), SmartShuffleService.class);
                    Bundle b = new Bundle();
                    b.putString("genre", genre);
                    i.putExtras(b);
                    startService(i);
                }
                else
                {
                    Log.e("SendingGenre","Empty");
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying())
                {
                    player.pause();
                    play.startAnimation(outAnimation);
                    outAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            play.setImageResource(R.drawable.play);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                }
                else {
                    player.start();
                    play.startAnimation(outAnimation);
                    outAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            play.setImageResource(R.drawable.pause);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        nowplaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowplaylist.startAnimation(outAnimation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i=new Intent(getApplicationContext(),NowPlaying.class);
                        i.putExtra("position",position);
                        startActivity(i);
                    }
                },100);
            }
        });
        equilizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                equilizer.startAnimation(outAnimation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i=new Intent(getApplicationContext(),Equilizer.class);
                        startActivity(i);
                    }
                },100);
            }
        });


        shufffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferencesettings.getString("shuffle", "").equals("off")) {
                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 20);
                    myAnim.setInterpolator(interpolator);
                    shufffle.startAnimation(myAnim);
                    Random r = new Random();
                    int Low = 0;
                    int High = arrayListplayer.size();
                    position = r.nextInt(High - Low) + Low;
                    asynctask = new AlbumArtAsyncTask(getApplicationContext(), arrayListplayer.get(position).getSongAlbumid());
                    asynctask.delegate = async;
                    asynctask.execute();
                    playerstart(arrayListplayer.get(position).getSongData(), arrayListplayer.get(position).getSongAlbumid());
                }
            }
        });

        shufffle.setLongClickable(true);
        shufffle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.longclick);
                shufffle.startAnimation(myAnim);
                if(preferencesettings.contains("shuffle")) {
                    if(preferencesettings.getString("shuffle","").equals("off")) {
                        shufffle.setImageResource(R.drawable.shuffleon);
                        editor.putString("shuffle", "on");
                        editor.commit();
                    }
                    else
                    {
                        shufffle.setImageResource(R.drawable.shuffle);
                        editor.putString("shuffle", "off");
                        editor.commit();
                    }
                }
                else {
                    shufffle.setImageResource(R.drawable.shuffleon);
                    editor.putString("shuffle","on");
                    editor.commit();
                }
                calcNextSong();
                return true;
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 20);
                myAnim.setInterpolator(interpolator);
                repeat.startAnimation(myAnim);
                if(preferencesettings.contains("repeat"))
                {
                    if(preferencesettings.getString("repeat","").equals("none"))
                    {
                        repeat.setImageResource(R.drawable.replay1);
                        editor.putString("repeat","one");
                        editor.commit();
                    }
                    else if(preferencesettings.getString("repeat","").equals("one"))
                    {
                        repeat.setImageResource(R.drawable.replayall);
                        editor.putString("repeat","all");
                        editor.commit();
                    }
                    else
                    {
                        repeat.setImageResource(R.drawable.repeat);
                        editor.putString("repeat","none");
                        editor.commit();
                    }
                }
                else
                {
                    repeat.setImageResource(R.drawable.replay1);
                    editor.putString("repeat","one");
                    editor.commit();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==next.getId())
        {
            if (preferencesettings.getString("shuffle", "").equals("on")) {
                position=shuffledpos;
            }
            else {
                if (position == (arrayListplayer.size()) - 1) {
                    position = 0;
                } else {
                    position++;
                }
            }
            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
            asynctask.delegate=async;
            asynctask.execute();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                    next.startAnimation(anim);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                    }
                }
            }, 20);
        }
        else if(v.getId()==prev.getId())
        {
            if (preferencesettings.getString("shuffle", "").equals("on")) {
                position=shuffledpos;
            }
            else {
                if (position == 0) {
                    position = arrayListplayer.size();
                    position--;
                } else {
                    position--;
                }
            }
            asynctask=new AlbumArtAsyncTask(getApplicationContext(),arrayListplayer.get(position).getSongAlbumid()) ;
            asynctask.delegate=async;
            asynctask.execute();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                    prev.startAnimation(anim);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        playerstart(arrayListplayer.get(position).getSongData(),arrayListplayer.get(position).getSongAlbumid());
                    }
                }
            }, 20);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId()==next.getId()) {
            checkLongClick = true;
            MotionEvent event = MotionEvent.obtain(
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + 100,
                    MotionEvent.ACTION_DOWN,
                    v.getX(),
                    v.getY(),
                    2
            );
            v.dispatchTouchEvent(event);
        }
        else if(v.getId()==prev.getId())
        {
            checkLongClick = true;
            MotionEvent event = MotionEvent.obtain(
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + 100,
                    MotionEvent.ACTION_DOWN,
                    v.getX(),
                    v.getY(),
                    2
            );
            v.dispatchTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        final View v=view;
        if(v.getId()==next.getId())
        {
            if(!checkLongClick)
            {
                return false;
            }
            else
            {
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f);
                final AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.setDuration(300);
                scaleUp.play(scaleUpX).with(scaleUpY);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    scaleUp.start();
                    MediaPlayerService.player.pause();
                    mAutoIncrement = true;
                    repeatUpdateHandler.post(new RptUpdater());
                    rotateUpdateHandler.post(new RepeatUpdater());
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) && mAutoIncrement){
                        v.setPressed(false);
                        mValue=500;
                        mDur=1200;
                        mDurTweak=50;
                        checkLongClick=false;
                        mAutoIncrement = false;
                        rotateUpdateHandler.removeCallbacksAndMessages(new RepeatUpdater());
                        repeatUpdateHandler.removeCallbacksAndMessages(new RptUpdater());
                        MediaPlayerService.player.start();

                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 1);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1);
                        final AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.setDuration(150);
                        scaleDown.play(scaleDownX).with(scaleDownY);
                        scaleDown.start();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scaleUp.cancel();
                                scaleUp.end();
                                scaleDown.cancel();
                                scaleDown.end();
                            }
                        },150);
                    }
                    return true;
                }
            }
        }
        else if(v.getId()==prev.getId())
        {
            if(!checkLongClick)
            {
                return false;
            }
            else
            {
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f);
                final AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.setDuration(300);
                scaleUp.play(scaleUpX).with(scaleUpY);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    scaleUp.start();
                    MediaPlayerService.player.pause();
                    mAutoDecrement = true;
                    repeatUpdateHandler.post(new RptUpdater());
                    rotateUpdateHandler.post(new RepeatUpdater());
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) && mAutoDecrement){
                        v.setPressed(false);
                        mValue=500;
                        mDur=1200;
                        mDurTweak=50;
                        checkLongClick=false;
                        mAutoDecrement = false;
                        rotateUpdateHandler.removeCallbacksAndMessages(new RepeatUpdater());
                        repeatUpdateHandler.removeCallbacksAndMessages(new RptUpdater());
                        MediaPlayerService.player.start();

                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 1);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1);
                        final AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.setDuration(150);
                        scaleDown.play(scaleDownX).with(scaleDownY);
                        scaleDown.start();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scaleUp.cancel();
                                scaleUp.end();
                                scaleDown.cancel();
                                scaleDown.end();
                            }
                        },150);
                    }
                    return true;
                }
            }
        }
        else if(v.getId()==img.getId())
        {

           int maxY=(int)img.getY() + ((img.getHeight()*70)/100);
            if(event.getAction()==MotionEvent.ACTION_DOWN)
            {
                int y=(int)event.getY();
                if(y>maxY)
                {
                    if(option_player.getVisibility()==View.GONE) {
                        Animation up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.opt_player_slideup);
                        option_player.startAnimation(up);
                        up.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                option_player.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                    else
                    {
                        Animation down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.opt_player_slidedown);
                        option_player.startAnimation(down);
                        seekwindow.startAnimation(down);
                        down.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                option_player.setVisibility(View.GONE);
                                seekwindow.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                }
            }
        }
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        progressBarHandler.removeCallbacks(UpdateSongTime);
        MediaPlayerService.set =false;
        if(intent.equals("albumlist"))
        {
            Intent i = new Intent(this,AlbumSongsList.class);
            startActivity(i);
        }
        else if(intent.equals("playlist"))
        {
            Intent i = new Intent(this,PlaylistView.class);
            startActivity(i);
        }
        else if(intent.equals("callui"))
        {
            Intent i = new Intent(this,MainPlayerActivity.class);
            startActivity(i);
        }
        else if(intent.equals("artistsongs"))
        {
            Intent i = new Intent(this,ArtistSongs.class);
            startActivity(i);
        }
        else if(intent.equals("genre"))
        {
            Intent i = new Intent(this,GenreView.class);
            startActivity(i);
        }
        else {
            Intent i = new Intent(this,MainPlayerActivity.class);
            startActivity(i);
        }
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (MediaPlayerService.musicBound) {
            unregisterReceiver(OnCompletion);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", MediaPlayerService.musicBound);
        savedInstanceState.putBoolean("SetState", MediaPlayerService.set);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MediaPlayerService.musicBound = savedInstanceState.getBoolean("ServiceState");
        MediaPlayerService.set = savedInstanceState.getBoolean("SetState");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void playerstart(final String data, final String albid)
    {
        play.setImageResource(R.drawable.pause);
        serviceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MediaPlayerService.musicBound = false;
        }
    };

        if(!MediaPlayerService.musicBound){
            playIntent = new Intent(this, MediaPlayerService.class);
            Bundle b=new Bundle();
            b.putString("data",data);
            b.putInt("pos",position);
            playIntent.putExtras(b);
            startService(playIntent);
            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
            Bundle b=new Bundle();
            b.putString("data",data);
            b.putInt("pos",position);
            broadcastIntent.putExtras(b);
            sendBroadcast(broadcastIntent);
        }

        setData(data);
        progressBarHandler.postDelayed(UpdateSongTime,100);
        finalset.postDelayed(SetFinalTime,100);
    }

    public void setData(String data)
    {
        if(MediaPlayerService.player!=null) {
            if (MediaPlayerService.player.isPlaying()) {
                play.setImageResource(R.drawable.pause);
            } else {
                play.setImageResource(R.drawable.play);
            }
        }
        //Setting Music Data
        try {
            Uri myUri = Uri.parse(data);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), myUri);
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null) {
                album.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            } else {
                album.setText(R.string.unknown);
            }
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null) {
                artist.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            } else {
                artist.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
            }
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null) {
                song.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            }else {
                String filename = data.substring(data.lastIndexOf("/") + 1);
                int pos=filename.lastIndexOf(".");
                song.setText(filename.substring(0,pos));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String filename = data.substring(data.lastIndexOf("/") + 1);
            int pos=filename.lastIndexOf(".");
            song.setText(filename.substring(0,pos));
        }
            calcNextSong();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void calcNextSong()
    {
        next_song.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        next_song.setSelected(true);
        next_song.setSingleLine(true);

        if (preferencesettings.getString("shuffle", "").equals("on"))
        {
            Random r = new Random();
            int Low = 1;
            int High = arrayListplayer.size();
            int nposition = r.nextInt(High - Low) + Low;
            if(position == nposition)
            {
                nposition = r.nextInt(High - Low) + Low;
            }
            String filename = arrayListplayer.get(nposition).getSongData().substring(arrayListplayer.get(nposition).getSongData().lastIndexOf("/") + 1);
            int pos=filename.lastIndexOf(".");
            next_song.setText(filename.substring(0,pos));
            shuffledpos=nposition;
        } else {
            int nposition;
            if(position == arrayListplayer.size()-1)
            {
                nposition=0;
            }
            else {
                nposition = position + 1;
            }

            String filename = arrayListplayer.get(nposition).getSongData().substring(arrayListplayer.get(nposition).getSongData().lastIndexOf("/") + 1);
            int pos=filename.lastIndexOf(".");
            next_song.setText(filename.substring(0,pos));
        }
    }

    public void finaltime()
    {
        seekbar.setMax(player.getDuration());
        String minformat,secformat;
        long finaltimetxt= player.getDuration();
        long min=TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
        long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                        toMinutes(finaltimetxt));
        if(min<10)
        {
            minformat="0%d";
        }
        else
        {
            minformat="%d";
        }
        if(sec<10)
        {
            secformat="0%d";
        }
        else {
            secformat="%d";
        }
        finaltime.setText(String.format(minformat+":"+secformat,
                min,sec));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            onBackPressed();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void processFinish(Bitmap output) {
        resource=null;
        resource=output;

        final int centerX = (img.getLeft() + img.getRight()) / 2;
        final int centerY = (img.getTop() + img.getBottom()) / 2;
        final int startRadius = 0;
        final int endRadius = Math.max(img.getWidth(), img.getHeight());


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animator anim = ViewAnimationUtils.createCircularReveal(img, centerX, centerY, startRadius, endRadius);
                anim.setDuration(300);
                anim.start();
                img.setImageBitmap(resource);
                RandomTransitionGenerator generator = new RandomTransitionGenerator(40000, new LinearInterpolator());
                img.setTransitionGenerator(generator);
            }
        },200);

        if(resource == null)
        {
            resource = BitmapFactory.decodeResource(getResources(),R.drawable.placeholder);
            BitmapBrightness bb=new BitmapBrightness();
            resource = bb.setBrightness(resource,-30);
            seekbar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
            View someView = findViewById(R.id.activity_playeractivity);
            View root = someView.getRootView();
            root.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else
        {
            Palette p = Palette.from(resource).generate();

            int color1 = 0,color2=0;
                List<Palette.Swatch> swatches;
                swatches = p.getSwatches();
                for (Palette.Swatch s : swatches) {
                    if (s != null) {
                        if (getLightness(s.getRgb()) > 0.5f) {
                            color1 = s.getRgb();
                        }
                        if(getLightness(s.getRgb()) < 0.5f){
                            color2 = s.getRgb();
                        }
                    }
                }
                if(color1==0)
                {
                    color1=Color.WHITE;
                }
                if(color2==0)
                {
                    color2=Color.BLACK;
                }
            seekbar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(color1, PorterDuff.Mode.MULTIPLY));
            bmap=Bitmap.createBitmap(size.x/2,size.y/2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmap);
            Paint paint = new Paint();
            paint.setShader(new LinearGradient (0, 0, 0, bmap.getHeight()/2, color2, color1, Shader.TileMode.MIRROR));
            canvas.drawPaint(paint);
            paint.setShader(new LinearGradient (0, 0, 0, bmap.getHeight()/2, color2, color1, Shader.TileMode.CLAMP));
            paint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
            canvas.drawRect(0, 0, bmap.getWidth(), bmap.getHeight()/2, paint);
                    BitmapBrightness bb=new BitmapBrightness();
                    bmap = bb.setBrightness(bmap,-100);

                    View someView = findViewById(R.id.activity_playeractivity);
                    View root = someView.getRootView();
                    root.setBackground(new BitmapDrawable(getResources(),bmap));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        player.pause();
        progressBarHandler.removeCallbacks(UpdateSongTime);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        progressBarHandler.removeCallbacks(UpdateSongTime);
        int currentPosition = seekBar.getProgress();

        // forward or backward to certain seconds
        player.seekTo(currentPosition);
        seekBar.setProgress(currentPosition);
        progressBarHandler.postDelayed(UpdateSongTime,100);
        if(!player.isPlaying())
        {
            player.start();
            play.setImageResource(R.drawable.pause);
        }
    }

    private void register_OnCompletion() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MediaPlayerService.PLAY_NEXT_ON_COMPLETION);
        registerReceiver(OnCompletion, filter);
    }

    public float getLightness(int color) {
        int red   = Color.red(color);
        int green = Color.green(color);
        int blue  = Color.blue(color);

        float hsl[] = new float[3];
        ColorUtils.RGBToHSL(red, green, blue, hsl);
        return hsl[2];
    }

    private void register_playNextShuffled() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter("play_next_smartshuffled");
        registerReceiver(playNextShuffled, filter);
    }

    public void startSmartShuffledAnim()
    {
        smartshuffle.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        Animation mAnimation=new AlphaAnimation(1,0);
        mAnimation.setDuration(250);
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new AccelerateInterpolator());
        smartshuffle.startAnimation(mAnimation);
    }

    private class RepeatUpdater implements Runnable{

        @Override
        public void run() {
            if( mAutoIncrement ) {
                ObjectAnimator rotator = ObjectAnimator.ofFloat(next,
                        "rotation", 0f, 360f);
                rotator.setRepeatCount(0);
                rotator.setInterpolator(new LinearInterpolator());
                if (mDur > 300 && mDurTweak < 350) {
                    mDur = mDur - (mDurTweak);
                    rotator.setDuration(mDur);
                    mDurTweak+=125;
                } else {
                    mDur = 300;
                    rotator.setDuration(mDur);
                }
                rotator.start();
                rotateUpdateHandler.postDelayed(new RepeatUpdater(), mDur);
            }
            else if( mAutoDecrement )
            {
                ObjectAnimator rotator = ObjectAnimator.ofFloat(prev,
                        "rotation", 360f, 0f);
                rotator.setRepeatCount(0);
                rotator.setInterpolator(new LinearInterpolator());
                if (mDur > 300 && mDurTweak < 350) {
                    mDur = mDur - (mDurTweak);
                    rotator.setDuration(mDur);
                    mDurTweak+=125;
                } else {
                    mDur = 300;
                    rotator.setDuration(mDur);
                }
                rotator.start();
                rotateUpdateHandler.postDelayed(new RepeatUpdater(), mDur);
            }
        }
    }

    private class RptUpdater implements Runnable {
        public void run() {
            if( mAutoIncrement ){
                mValue += 50; //change this value to control how much to forward
                MediaPlayerService.player.seekTo(MediaPlayerService.player.getCurrentPosition()+ mValue);
                repeatUpdateHandler.postDelayed( new RptUpdater(), 50 );
            } else if( mAutoDecrement ){
                mValue += 50; //change this value to control how much to rewind
                MediaPlayerService.player.seekTo(MediaPlayerService.player.getCurrentPosition()- mValue);
                repeatUpdateHandler.postDelayed( new RptUpdater(), 50 );
            }
        }
    }
}


