package com.example.anmolpc.playmmusicplayer.adapter;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anmolpc.playmmusicplayer.MediaPlayerService;
import com.example.anmolpc.playmmusicplayer.NowPlaylistAsyncTask;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.example.anmolpc.playmmusicplayer.playeractivity;
import com.google.gson.reflect.TypeToken;


import com.bumptech.glide.Glide;
import com.example.anmolpc.playmmusicplayer.AlbumSongsList;
import com.example.anmolpc.playmmusicplayer.ArtistSongs;
import com.example.anmolpc.playmmusicplayer.PreloadData;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.RecyclerViewItemDecoraition;
import com.example.anmolpc.playmmusicplayer.SplashActivity;
import com.example.anmolpc.playmmusicplayer.fragments.AlbumObject;
import com.example.anmolpc.playmmusicplayer.fragments.ArtistFragment;
import com.example.anmolpc.playmmusicplayer.fragments.ArtistObject;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistObject;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.anmolpc.playmmusicplayer.playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO;
import static com.example.anmolpc.playmmusicplayer.playeractivity.sArtworkUri;

/**
 * Created by Anmol Pc on 4/19/2017.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    Context mContext;
    List<ArtistObject> artistObjects;
    String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
    String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
    Cursor cursoralbum;
    int ttlnumofsngs=0,Pos=0,ttlalbums=0;
    ArtistInnerAdapter adapter;
    Map<Integer,List<AlbumObject>> map=new HashMap<>();
    Gson gson=new Gson();



    public ArtistAdapter(Context context, List<ArtistObject> artistObjectList)
    {
        this.mContext=context;
        this.artistObjects=artistObjectList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView artistname, totalnumberofsongs, artistalbum;
        public RelativeLayout options,expandview;
        public ImageButton expand;
        RecyclerView recyclerView;
        boolean checkrcy = false;
        public LinearLayout lladd;

        public ViewHolder(View itemView) {
            super(itemView);
            artistname = (TextView) itemView.findViewById(R.id.artistNametxt);
            expand = (ImageButton) itemView.findViewById(R.id.expand);
            artistalbum = (TextView) itemView.findViewById(R.id.artstalbums);
            totalnumberofsongs = (TextView) itemView.findViewById(R.id.artstnumberofsngs);
            expandview = (RelativeLayout) itemView.findViewById(R.id.expandview);
            options = (RelativeLayout) itemView.findViewById(R.id.artistoptions);
            lladd = (LinearLayout) itemView.findViewById(R.id.rcyAdd);
            itemView.setOnClickListener(this);
            options.setOnClickListener(this);
            expandview.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == expandview.getId()) {
                recyclerView = new RecyclerView(mContext);
                if (checkrcy) {
                    ObjectAnimator rotator = ObjectAnimator.ofFloat(expand,
                            "rotation", 180f, 360f);
                    rotator.setRepeatCount(0);
                    rotator.setDuration(350);
                    rotator.setInterpolator(new AnticipateOvershootInterpolator());
                    rotator.start();

                    Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.removeview);
                    lladd.startAnimation(anim);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            checkrcy = false;
                            lladd.removeAllViews();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    ObjectAnimator rotator = ObjectAnimator.ofFloat(expand,
                            "rotation", 0f, 180f);
                    rotator.setRepeatCount(0);
                    rotator.setDuration(350);
                    rotator.setInterpolator(new AnticipateOvershootInterpolator());
                    rotator.start();
                    checkrcy = true;
                    recyclerView.setLayoutManager(new GridLayoutManager(mContext, 1, GridLayoutManager.HORIZONTAL, false));
                    recyclerView.setNestedScrollingEnabled(false);
                    recyclerView.setAdapter(null);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setHorizontalFadingEdgeEnabled(true);
                    AlbumAdapter adapternew = new AlbumAdapter(mContext, map.get(getAdapterPosition()));
                    recyclerView.setAdapter(adapternew);
                    recyclerView.setVisibility(View.GONE);
                    lladd.addView(recyclerView);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setVisibility(View.VISIBLE);
                            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.scaleview);
                            recyclerView.startAnimation(anim);
                        }
                    });
                }
            } else if (v.getId() == options.getId()) {
                PopupMenu popupMenu = new PopupMenu(mContext, options);
                popupMenu.inflate(R.menu.options_menu_artist);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addtoplay:
                                CustomDialogClass cd = new CustomDialogClass(mContext, artistObjects.get(getAdapterPosition()).getAlbumArtist());
                                cd.show();
                                break;
                            case R.id.playnow:
                                playAllSongsNow(artistObjects.get(getAdapterPosition()).getAlbumArtist());
                                break;
                            case R.id.playnext:
                                playnextSongs(artistObjects.get(getAdapterPosition()).getAlbumArtist());
                                break;
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        int roweffected = 0;
                                        final String[] projection = {MediaStore.Audio.Media.DATA};
                                        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
                                        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                        String[] projectionalbum = new String[]{MediaStore.Audio.Albums._ID};
                                        String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
                                        final Cursor cursoralbum = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artistObjects.get(getAdapterPosition()).getAlbumArtist()}, sortOrderalbum);
                                        while (cursoralbum.moveToNext()) {
                                            String selection = "is_music != 0";
                                            selection = selection + " and album_id = " + cursoralbum.getString(0);
                                            Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                                            while (cursor.moveToNext()) {
                                                File f = new File(cursor.getString(0));
                                                Boolean deleted = f.delete();
                                                if (deleted) {
                                                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(cursor.getString(0));
                                                    roweffected = mContext.getContentResolver().delete(uri,
                                                            MediaStore.MediaColumns.DATA + "=\"" + cursor.getString(0) + "\"",
                                                            null);
                                                }
                                            }
                                        }
                                        if (roweffected > 0) {
                                            artistObjects.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                            Toast.makeText(mContext, "Artist Deleted!!", Toast.LENGTH_SHORT).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });

                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog alert = builder.create();
                                alert.show();
                                break;
                            case R.id.share:
                                final String[] projection = {MediaStore.Audio.Media.DATA};
                                final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
                                Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projectionalbum = new String[]{MediaStore.Audio.Albums._ID};
                                String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
                                final Cursor cursoralbum = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artistObjects.get(getAdapterPosition()).getAlbumArtist()}, sortOrderalbum);
                                ArrayList<Uri> files = new ArrayList<Uri>();
                                while (cursoralbum.moveToNext()) {
                                    String selection = "is_music != 0";
                                    selection = selection + " and album_id = " + cursoralbum.getString(0);
                                    Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                                    while (cursor.moveToNext()) {
                                        File file = new File(cursor.getString(0));
                                        Uri uri = Uri.fromFile(file);
                                        files.add(uri);
                                    }
                                }
                                Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                share.setType("*/*");
                                share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                                mContext.startActivity(Intent.createChooser(share, "Share Audio Files"));
                                break;
                        }
                        return true;
                    }
                });
            } else {
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mContext, ArtistSongs.class);
                        intent.putExtra("artist", artistObjects.get(getAdapterPosition()).getAlbumArtist());
                        mContext.startActivity(intent);
                    }
                }, 150);
            }
        }
    }

    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_list_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ArtistAdapter.ViewHolder holder, int position) {
        final List<AlbumObject> albums = new ArrayList<AlbumObject>();
        ttlnumofsngs=0;
        ttlalbums=0;
        Pos=position;
        cursoralbum=mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artistObjects.get(position).getAlbumArtist()}, sortOrderalbum);
        while (cursoralbum.moveToNext())
        {
            albums.add(new AlbumObject(cursoralbum.getString(0),cursoralbum.getString(2),cursoralbum.getString(1),cursoralbum.getString(3)));
            ttlnumofsngs+=Integer.parseInt(cursoralbum.getString(3));
            ttlalbums++;
        }
        cursoralbum.moveToFirst();
        holder.artistname.setText(artistObjects.get(position).getAlbumArtist());
        holder.totalnumberofsongs.setText(String.valueOf(ttlnumofsngs)+" Songs");
        holder.artistalbum.setText(String.valueOf(ttlalbums)+" Albums, ");
        map.put(position,albums);
    }

    @Override
    public int getItemCount() {
        return artistObjects.size();
    }

    public class CustomDialogClass extends Dialog {
        ArrayList<PlaylistObject> Objectlist;
        Button createplay;
        RecyclerView lst;
        TextView empty;
        String artist;
        final String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        public CustomDialogClass(@NonNull Context context,String artist) {
            super(context);
            this.artist=artist;
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.playlist_dialog);
            final SharedPreferences preferences = mContext.getSharedPreferences("playlists", Context.MODE_PRIVATE);

            String json = preferences.getString("list", null);
            Type type = new TypeToken<ArrayList<PlaylistObject>>() {
            }.getType();
            Objectlist = gson.fromJson(json, type);
            Objectlist.remove(0);
            Objectlist.remove(0);
            createplay = (Button) findViewById(R.id.createplaylist);
            lst = (RecyclerView) findViewById(R.id.allplaylists);
            empty = (TextView) findViewById(R.id.emptyplaylist);
            if (Objectlist.isEmpty()) {
                lst.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            } else {
                final PlayListAdapter adapter = new PlayListAdapter(mContext, Objectlist,"other");
                lst.setAdapter(adapter);
                lst.setLayoutManager(new LinearLayoutManager(mContext));
                lst.setHasFixedSize(true);
                lst.addOnItemTouchListener(new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SharedPreferences addplay= view.getContext().getSharedPreferences(Objectlist.get(position).getPlaylistTitle(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor addplayedit=addplay.edit();
                        String json = addplay.getString("playlist", null);
                        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
                        List<SongObject> newplay = gson.fromJson(json, type);
                        String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
                        String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
                        final Cursor cursoralbum=mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artist}, sortOrderalbum);
                        while (cursoralbum.moveToNext())
                        {
                            String selection = "is_music != 0";
                            selection = selection + " and album_id = " + cursoralbum.getString(0);
                           Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                            while(cursor.moveToNext()) {
                                long finaltimetxt=Long.parseLong(cursor.getString(3));
                                long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                                long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes(finaltimetxt));
                                newplay.add(new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursoralbum.getString(0)));
                            }
                        }
                        addplayedit.clear();
                        addplayedit.commit();
                        String newplayjson = gson.toJson(newplay);
                        addplayedit.putString("playlist", newplayjson);
                        addplayedit.commit();
                        Toast.makeText(view.getContext(),"Artist Added!!",Toast.LENGTH_SHORT).show();
                        cancel();
                    }
                }));
            }

            createplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreatePlaylistDialog cdd=new CreatePlaylistDialog(mContext,artist);
                    cdd.show();
                    cancel();
                }

            });
        }
    }

    protected class CreatePlaylistDialog extends Dialog {
        String artist;
        final String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        public CreatePlaylistDialog(Context context,String artist) {
            super(context);
            this.artist=artist;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.createplaylist_layout);

            final EditText input=(EditText)findViewById(R.id.inputName);
            Button ybtn=(Button) findViewById(R.id.yesbtn);
            Button nbtn=(Button) findViewById(R.id.nobtn);
            ybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = input.getText().toString();
                    SharedPreferences pref = mContext.getSharedPreferences("playlists", Context.MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor=pref.edit();
                    Gson gson = new Gson();
                    String json = pref.getString("list", null);
                    Type type = new TypeToken<ArrayList<PlaylistObject>>() {}.getType();
                    final ArrayList<PlaylistObject> newlst = gson.fromJson(json, type);
                    newlst.add(new PlaylistObject(name));
                    String njson = gson.toJson(newlst);
                    prefEditor.clear();
                    prefEditor.commit();
                    prefEditor.putString("list", njson);
                    prefEditor.commit();

                    SharedPreferences playlist=mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
                    SharedPreferences.Editor playedit=playlist.edit();
                    final List<SongObject> newplaylist = new ArrayList<SongObject>();
                    String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
                    String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
                    final Cursor cursoralbum=mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artist}, sortOrderalbum);
                    while (cursoralbum.moveToNext())
                    {
                        String selection = "is_music != 0";
                        selection = selection + " and album_id = " + cursoralbum.getString(0);
                        Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                        while(cursor.moveToNext()) {
                            long finaltimetxt=Long.parseLong(cursor.getString(3));
                            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes(finaltimetxt));
                            newplaylist.add(new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursoralbum.getString(0)));
                        }
                    }
                    String jsonsong = gson.toJson(newplaylist);
                    playedit.putString("playlist", jsonsong);
                    playedit.commit();
                    Toast.makeText(v.getContext(),"Playlist Created!!",Toast.LENGTH_SHORT).show();
                    cancel();
                }
            });

            nbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });
        }
    }

    public void playAllSongsNow(String artist)
    {
        final String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final List<SongObject> allsongs = new ArrayList<SongObject>();
        String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        final Cursor cursoralbum=mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artist}, sortOrderalbum);
        while (cursoralbum.moveToNext())
        {
            String selection = "is_music != 0";
            selection = selection + " and album_id = " + cursoralbum.getString(0);
            Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
            while(cursor.moveToNext()) {
                long finaltimetxt=Long.parseLong(cursor.getString(3));
                long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(finaltimetxt));
                allsongs.add(new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursoralbum.getString(0)));
            }
        }

        if(!MediaPlayerService.musicBound){
            Intent i = new Intent(mContext, MediaPlayerService.class);
            Bundle b=new Bundle();
            b.putString("data",allsongs.get(0).getSongData());
            b.putInt("pos",0);
            i.putExtras(b);
            mContext.startService(i);
        }
        else
        {
            Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
            Bundle b=new Bundle();
            b.putString("data",allsongs.get(0).getSongData());
            b.putInt("pos",0);
            broadcastIntent.putExtras(b);
            mContext.sendBroadcast(broadcastIntent);
        }

        NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allsongs,mContext);
        np.methodRun();
        Toast.makeText(mContext,"Artist Played!!",Toast.LENGTH_SHORT).show();
    }

    public void playnextSongs(String artist)
    {
        final String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM_ID
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projectionalbum = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        String sortOrderalbum = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        final Cursor cursoralbum=mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projectionalbum,MediaStore.Audio.Media.ARTIST+ "=?", new String[]{artist}, sortOrderalbum);


        if(MediaPlayerService.musicBound) {
            SharedPreferences preferences = mContext.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = preferences.getString("nowplaylist", null);
            Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
            ArrayList<SongObject> arrayList = gson.fromJson(json, type);
            int i=1;
            while (cursoralbum.moveToNext())
            {
                String selection = "is_music != 0";
                selection = selection + " and album_id = " + cursoralbum.getString(0);
                Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                while(cursor.moveToNext()) {
                    long finaltimetxt=Long.parseLong(cursor.getString(3));
                    long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                    long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(finaltimetxt));
                    arrayList.add(MediaPlayerService.getPosition() + i, new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursor.getString(6)));
                    i++;
                }
            }
            NowPlaylistAsyncTask np = new NowPlaylistAsyncTask(arrayList, mContext);
            np.methodRun();
        }
        else {
            final List<SongObject> arrayList=new ArrayList<SongObject>();
            while (cursoralbum.moveToNext())
            {
                String selection = "is_music != 0";
                selection = selection + " and album_id = " + cursoralbum.getString(0);
                Cursor cursor = mContext.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                while(cursor.moveToNext()) {
                    long finaltimetxt=Long.parseLong(cursor.getString(3));
                    long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                    long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(finaltimetxt));
                    arrayList.add(new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),cursoralbum.getString(0)));
                }
            }
            NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(arrayList,mContext);
            np.methodRun();
            android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(mContext,playeractivity.class);
                    i.putExtra("data", arrayList.get(0).getSongData());
                    i.putExtra("albumid",arrayList.get(0).getSongAlbumid());
                    i.putExtra("position",0);
                    i.putExtra("intent","other");
                    mContext.startActivity(i);
                }
            },100);
        }

    }
}
