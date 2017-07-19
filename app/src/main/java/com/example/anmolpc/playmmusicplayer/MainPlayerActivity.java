package com.example.anmolpc.playmmusicplayer;



import android.animation.Animator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.anmolpc.playmmusicplayer.MediaPlayerService.player;
import static com.example.anmolpc.playmmusicplayer.R.layout.small_player;
import static com.example.anmolpc.playmmusicplayer.playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO;

public class MainPlayerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Context context;
    private static final String TAG = MainPlayerActivity.class.getSimpleName();
    public static final String smallplayerupdate="small_player_setNewDATA";
    public static boolean player_open=false;
    private FragmentManager fragmentManager;
    private Fragment frag = null;
    GestureDetector gesturedetector = null;
    View nv;
    PopupWindow pw;
    Animation inAnimation;
    Animation outAnimation;
    public static long lastClickTime = 0;
    public static final long DOUBLE_CLICK_TIME_DELTA = 500;
    private GestureDetector gestureDetector;





    Intent i;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fadeinactivity,R.anim.fadeoutactivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context=this;
        register_setNewDATA();
        inAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        outAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        final View v= getWindow().getDecorView().getRootView();

//        fragmentManager = getSupportFragmentManager();
//        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        frag = new LibraryFragment();
//        fragmentTransaction.replace(R.id.headlines_fragment, frag);
//        fragmentTransaction.commit();



        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(!isDoubleClick()) {
                    if (isMyServiceRunning(MediaPlayerService.class)) {
                        player_open = true;
                        Animation a = AnimationUtils.loadAnimation(MainPlayerActivity.this, R.anim.fabanim);
                        fab.startAnimation(a);
                        a.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(final Animation animation) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        LayoutInflater inflater = (LayoutInflater) MainPlayerActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                                        nv = inflater.inflate(R.layout.small_player, null);
                                        pw = new PopupWindow(
                                                nv,
                                                DrawerLayout.LayoutParams.WRAP_CONTENT,
                                                DrawerLayout.LayoutParams.WRAP_CONTENT);

                                        if (Build.VERSION.SDK_INT >= 21) {
                                            pw.setElevation(5.0f);
                                        }

                                        final ImageButton next = (ImageButton) nv.findViewById(R.id.smallplay_nextbtn);
                                        next.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                next.startAnimation(inAnimation);
                                                int position = MediaPlayerService.getPosition();
                                                SharedPreferences preferences = MainPlayerActivity.this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
                                                Gson gson = new Gson();
                                                String json = preferences.getString("nowplaylist", null);
                                                Type type = new TypeToken<ArrayList<SongObject>>() {
                                                }.getType();
                                                final ArrayList<SongObject> arrayList = gson.fromJson(json, type);
                                                if (arrayList != null) {
                                                    if(position==arrayList.size()-1)
                                                    {
                                                        position=0;
                                                    }
                                                    else {
                                                        position++;
                                                    }
                                                }
                                                Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
                                                Bundle b = new Bundle();
                                                b.putString("data", arrayList.get(position).getSongData());
                                                b.putInt("pos", position);
                                                broadcastIntent.putExtras(b);
                                                sendBroadcast(broadcastIntent);
                                            }
                                        });
                                        final ImageButton prev = (ImageButton) nv.findViewById(R.id.smallplay_prevbtn);
                                        prev.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                prev.startAnimation(inAnimation);
                                                int position = MediaPlayerService.getPosition();
                                                SharedPreferences preferences = MainPlayerActivity.this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
                                                Gson gson = new Gson();
                                                String json = preferences.getString("nowplaylist", null);
                                                Type type = new TypeToken<ArrayList<SongObject>>() {
                                                }.getType();
                                                final ArrayList<SongObject> arrayList = gson.fromJson(json, type);
                                                if(position==0)
                                                {
                                                    if (arrayList != null) {
                                                        position=arrayList.size()-1;
                                                    }
                                                }
                                                else {
                                                    position--;
                                                }
                                                Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
                                                Bundle b = new Bundle();
                                                b.putString("data", arrayList.get(position).getSongData());
                                                b.putInt("pos", position);
                                                broadcastIntent.putExtras(b);
                                                sendBroadcast(broadcastIntent);
                                            }
                                        });

                                        final ImageButton play = (ImageButton) nv.findViewById(R.id.smallplay_playbtn);
                                        play.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (player.isPlaying()) {
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
                                                } else {
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

                                        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        pw.setAnimationStyle(R.style.DialogAnimation);

                                        smallPlayerInit(nv);
                                        pw.showAtLocation(v, Gravity.BOTTOM, 0, 15);
                                        view.setVisibility(View.INVISIBLE);

                                        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                player_open = false;
                                                view.setVisibility(View.VISIBLE);
                                                Animation a = AnimationUtils.loadAnimation(MainPlayerActivity.this, R.anim.fabaimin);
                                                view.startAnimation(a);
                                            }
                                        });


                                        nv.setOnTouchListener(new OnSwipeTouchListener(MainPlayerActivity.this){
                                            @Override
                                            public void onClick() {
                                                super.onClick();
                                                nv.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        i=new Intent(MainPlayerActivity.this,playeractivity.class);
                                                        i.putExtra("intent","callui");
                                                        startActivity(i);
                                                    }
                                                },100);
                                            }

                                            @Override
                                            public void onSwipeRight() {
                                                pw.dismiss();
                                            }
                                        });
                                    }
                                }, 200);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                i=new Intent(MainPlayerActivity.this,playeractivity.class);
                i.putExtra("intent","callui");
                startActivity(i);
                return true;
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(pw.isShowing()){
            pw.dismiss();
            player_open=false;
        }
        else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=0) {
            if (requestCode == 1) {
                Intent broadcastIntent = new Intent("recieveimagefilter");
                Bundle b = new Bundle();
                b.putString("data", String.valueOf(data.getData()));
                broadcastIntent.putExtras(b);
                sendBroadcast(broadcastIntent);
            } else if (requestCode == 2) {
                Intent broadcastIntent = new Intent("recieveimagealbumfilter");
                Bundle b = new Bundle();
                b.putString("data", String.valueOf(data.getData()));
                broadcastIntent.putExtras(b);
                sendBroadcast(broadcastIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.librarydraw) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void smallPlayerInit(View v)
    {
        CircleImageView img=(CircleImageView) v.findViewById(R.id.smallplay_image);
        TextView artist=(TextView) v.findViewById(R.id.smallplay_artist);
        TextView title=(TextView) v.findViewById(R.id.smallplay_title);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setSelected(true);
        title.setSingleLine(true);
        int position=MediaPlayerService.getPosition();
        SharedPreferences preferences=MainPlayerActivity.this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        final ArrayList<SongObject> arrayList = gson.fromJson(json, type);
        artist.setText(arrayList.get(position).getSongAuthor());
        title.setText(arrayList.get(position).getSongTitle());
        Bitmap bmp=PreloadData.getBitmapFromMemCache(arrayList.get(position).getSongAlbumid());
        if(bmp!=null)
        {
            img.setImageBitmap(bmp);
        }
        else{
            img.setImageResource(R.drawable.placeholder);
        }
    }

    public void setPlayerData(PopupWindow p)
    {
        CircleImageView img=(CircleImageView) p.getContentView().findViewById(R.id.smallplay_image);
        TextView artist=(TextView) p.getContentView().findViewById(R.id.smallplay_artist);
        TextView title=(TextView) p.getContentView().findViewById(R.id.smallplay_title);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setSelected(true);
        title.setSingleLine(true);
        ImageButton b=(ImageButton) p.getContentView().findViewById(R.id.smallplay_playbtn);
        b.setImageResource(R.drawable.pause);
        int position=MediaPlayerService.getPosition();
        SharedPreferences preferences=MainPlayerActivity.this.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("nowplaylist", null);
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        final ArrayList<SongObject> arrayList = gson.fromJson(json, type);
        artist.setText(arrayList.get(position).getSongAuthor());
        title.setText(arrayList.get(position).getSongTitle());
        Bitmap bmp=PreloadData.getBitmapFromMemCache(arrayList.get(position).getSongAlbumid());
        int centerX = (img.getLeft() + img.getRight()) / 2;
        int centerY = (img.getTop() + img.getBottom()) / 2;
        int startRadius = 0;
        int endRadius = Math.max(img.getWidth(), img.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(img, centerX, centerY, startRadius, endRadius);
        anim.start();
        if(bmp!=null)
        {
            img.setImageBitmap(bmp);
        }
        else{
            img.setImageResource(R.drawable.placeholder);
        }
    }

    private BroadcastReceiver setNewDATA = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(pw!=null) {
                nv.post(new Runnable() {
                    @Override
                    public void run() {
                        setPlayerData(pw);
                    }
                });
            }
        }
    };
    private void register_setNewDATA() {
        IntentFilter filter = new IntentFilter(smallplayerupdate);
        registerReceiver(setNewDATA, filter);
    }

    public static boolean isDoubleClick(){
        long clickTime = System.currentTimeMillis();
        if(clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
            lastClickTime = clickTime;
            return true;
        }
        lastClickTime = clickTime;
        return false;
    }
}
