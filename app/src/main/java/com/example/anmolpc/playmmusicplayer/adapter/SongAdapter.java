package com.example.anmolpc.playmmusicplayer.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anmolpc.playmmusicplayer.AlbumArtAsyncTask;
import com.example.anmolpc.playmmusicplayer.ArtistSongs;
import com.example.anmolpc.playmmusicplayer.AsyncResponse;
import com.example.anmolpc.playmmusicplayer.MainPlayerActivity;
import com.example.anmolpc.playmmusicplayer.MediaPlayerService;
import com.example.anmolpc.playmmusicplayer.NowPlaylistAsyncTask;
import com.example.anmolpc.playmmusicplayer.PlaylistView;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistObject;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.example.anmolpc.playmmusicplayer.playeractivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.R.attr.id;
import static android.content.ContentValues.TAG;
import static com.example.anmolpc.playmmusicplayer.playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>{
    private Context context;
    private List<SongObject> allSongs;
    private String newIntent;
    MediaPlayerService serv;
    Gson gson = new Gson();
    public SongAdapter(Context context, List<SongObject> allSongs,String intent) {
        this.context = context;
        this.allSongs = allSongs;
        this.newIntent=intent;
    }
    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_list_layout, parent, false);
        return new SongViewHolder(view);
    }
    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        SongObject songs = allSongs.get(position);
        holder.songTitle.setText(songs.getSongTitle());
        holder.songAuthor.setText(songs.getSongAuthor());
        holder.duration.setText(songs.getSongDuration());
    }
    @Override
    public int getItemCount() {
        return allSongs.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView songTitle;
        public TextView songAuthor,duration;
        public LinearLayout ll;
        public RelativeLayout options;
        public SongViewHolder(View itemView, TextView songTitle, TextView songAuthor, TextView duration) {
            super(itemView);
            this.songTitle = songTitle;
            this.songAuthor = songAuthor;
            this.duration = duration;
        }
        public SongViewHolder(View itemView) {
            super(itemView);
            songTitle = (TextView)itemView.findViewById(R.id.song_title);
            songAuthor = (TextView)itemView.findViewById(R.id.song_author);
            duration = (TextView)itemView.findViewById(R.id.duration);
            options = (RelativeLayout) itemView.findViewById(R.id.songoptions);
            ll=(LinearLayout)itemView.findViewById(R.id.songlay);
            ll.setOnClickListener(this);
            options.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final View nv=v;
            if(v.getId() == options.getId())
            {
                PopupMenu popupMenu = new PopupMenu(context, options);
                if(newIntent.equals("playlist"))
                {
                    popupMenu.inflate(R.menu.options_menu_playlist);

                }else if(newIntent.equals("nowplaying"))
                {
                    popupMenu.inflate(R.menu.options_menu_nowplaying);
                }
                else {
                    popupMenu.inflate(R.menu.options_menu);
                }
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addtoplay:
                                CustomDialogClass cdd=new CustomDialogClass(getAdapterPosition());
                                cdd.show();
                                break;
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (newIntent.equals("playlist")) {
                                            SharedPreferences playlist= v.getContext().getSharedPreferences(PlaylistView.name, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor edi=playlist.edit();
                                            edi.clear();
                                            edi.commit();
                                            allSongs.remove(getAdapterPosition());
                                            String allsngs = gson.toJson(allSongs);
                                            edi.putString("playlist", allsngs);
                                            edi.commit();
                                            notifyItemRemoved(getAdapterPosition());
                                            Toast.makeText(v.getContext(),"Deleted from Playlist!!",Toast.LENGTH_SHORT).show();
                                        } else {
                                            File f = new File(allSongs.get(getAdapterPosition()).getSongData());
                                            Boolean deleted = f.delete();
                                            if (deleted) {
                                                Uri uri = MediaStore.Audio.Media.getContentUriForPath(allSongs.get(getAdapterPosition()).getSongData());
                                                int roweffected = nv.getContext().getContentResolver().delete(uri,
                                                        MediaStore.MediaColumns.DATA + "=\"" + allSongs.get(getAdapterPosition()).getSongData() + "\"",
                                                        null);
                                                if (roweffected > 0) {
                                                    allSongs.remove(getAdapterPosition());
                                                    notifyItemRemoved(getAdapterPosition());
                                                    Toast.makeText(nv.getContext(), "Song Deleted!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            dialog.dismiss();
                                        }
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
                                File file = new File(allSongs.get(getAdapterPosition()).getSongData());
                                Uri uri = Uri.fromFile(file);
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("audio/mp3");
                                share.putExtra(Intent.EXTRA_STREAM, uri);
                                v.getContext().startActivity(Intent.createChooser(share, "Share Audio File"));
                                break;
                            case R.id.playnow:
                                if(newIntent.equals("nowplaying"))
                                {
                                    NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allSongs,context);
                                    np.methodRun();
                                            Intent i = new Intent(context,playeractivity.class);
                                            i.putExtra("data", allSongs.get(getAdapterPosition()).getSongData());
                                            i.putExtra("albumid",allSongs.get(getAdapterPosition()).getSongAlbumid());
                                            i.putExtra("position",getAdapterPosition());
                                            i.putExtra("intent",newIntent);
                                            context.startActivity(i);
                                    ((Activity)context).finish();
                                }else {
                                    playNewSong(getAdapterPosition());
                                }
                                break;
                            case R.id.playnext:
                                if(MediaPlayerService.musicBound) {
                                    SharedPreferences preferences = v.getContext().getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
                                    Gson gson = new Gson();
                                    String json = preferences.getString("nowplaylist", null);
                                    Type type = new TypeToken<ArrayList<SongObject>>() {
                                    }.getType();
                                    ArrayList<SongObject> arrayList = gson.fromJson(json, type);
                                    arrayList.add(MediaPlayerService.getPosition() + 1, allSongs.get(getAdapterPosition()));
                                    NowPlaylistAsyncTask np = new NowPlaylistAsyncTask(arrayList, context);
                                    np.methodRun();
                                }
                                else {
                                    NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allSongs,context);
                                    np.methodRun();
                                    android.os.Handler handler = new android.os.Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(context,playeractivity.class);
                                            i.putExtra("data", allSongs.get(getAdapterPosition()).getSongData());
                                            i.putExtra("albumid",allSongs.get(getAdapterPosition()).getSongAlbumid());
                                            i.putExtra("position",getAdapterPosition());
                                            i.putExtra("intent",newIntent);
                                            context.startActivity(i);
                                        }
                                    },100);
                                }
                                break;
                            case R.id.setring:
                                setRingtone(allSongs.get(getAdapterPosition()).getSongData());
                                break;
                            case R.id.editinfo:
                                    SetTrackInfo info=new SetTrackInfo(allSongs.get(getAdapterPosition()).getSongData(),allSongs.get(getAdapterPosition()).getSongAlbumid());
                                    info.show();
                                    notifyItemChanged(getAdapterPosition());
                                break;
                        }
                        return true;
                    }
                });
            }
            else if(v.getId()==ll.getId())
            {
                NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allSongs,context);
                np.methodRun();
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context,playeractivity.class);
                        i.putExtra("data", allSongs.get(getAdapterPosition()).getSongData());
                        i.putExtra("albumid",allSongs.get(getAdapterPosition()).getSongAlbumid());
                        i.putExtra("position",getAdapterPosition());
                        i.putExtra("intent",newIntent);
                        context.startActivity(i);
                    }
                },100);
            }
        }
    }

    protected class CustomDialogClass extends Dialog {
        ArrayList<PlaylistObject> Objectlist;
        Button createplay;
        RecyclerView lst;
        TextView empty;
        int posi;

        public CustomDialogClass(int pos) {
            super(context);
            this.posi=pos;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.playlist_dialog);
            final SharedPreferences preferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE);

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
                final PlayListAdapter adapter = new PlayListAdapter(context, Objectlist,"other");
                lst.setAdapter(adapter);
                lst.setLayoutManager(new LinearLayoutManager(context));
                lst.setHasFixedSize(true);
                lst.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SharedPreferences addplay= view.getContext().getSharedPreferences(Objectlist.get(position).getPlaylistTitle(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor addplayedit=addplay.edit();
                        String json = addplay.getString("playlist", null);
                        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
                        List<SongObject> newplay = gson.fromJson(json, type);
                        newplay.add(new SongObject(allSongs.get(posi).getSongTitle(),allSongs.get(posi).getSongAuthor(),allSongs.get(posi).getSongDuration(),allSongs.get(posi).getSongData(),allSongs.get(posi).getSongAlbumid()));
                        addplayedit.clear();
                        addplayedit.commit();
                        String newplayjson = gson.toJson(newplay);
                        addplayedit.putString("playlist", newplayjson);
                        addplayedit.commit();
                        Toast.makeText(view.getContext(),"Song Added!!",Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        cancel();
                    }
                }));
            }

            createplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreatePlaylistDialog cdd=new CreatePlaylistDialog(posi);
                    cdd.show();
                    cancel();
                }

            });
        }
    }

    protected class CreatePlaylistDialog extends Dialog {
        int position;
        public CreatePlaylistDialog(int pos) {
            super(context);
            this.position=pos;
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
                            SharedPreferences pref = context.getSharedPreferences("playlists", Context.MODE_PRIVATE);
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

                    SharedPreferences playlist=context.getSharedPreferences(name, Context.MODE_PRIVATE);
                    SharedPreferences.Editor playedit=playlist.edit();
                    final List<SongObject> newplaylist = new ArrayList<SongObject>();
                    newplaylist.add(new SongObject(allSongs.get(position).getSongTitle(),allSongs.get(position).getSongAuthor(),allSongs.get(position).getSongDuration(),allSongs.get(position).getSongData(),allSongs.get(position).getSongAlbumid()));
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

    public void playNewSong(int pos)
    {
        if(!MediaPlayerService.musicBound){
            Intent i = new Intent(context, MediaPlayerService.class);
            Bundle b=new Bundle();
            b.putString("data",allSongs.get(pos).getSongData());
            b.putInt("pos",pos);
            i.putExtras(b);
            context.startService(i);
        }
        else
        {
            Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
            Bundle b=new Bundle();
            b.putString("data",allSongs.get(pos).getSongData());
            b.putInt("pos",pos);
            broadcastIntent.putExtras(b);
            context.sendBroadcast(broadcastIntent);
        }

        NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allSongs,context);
        np.methodRun();
        Toast.makeText(context,"Song Played!!",Toast.LENGTH_SHORT).show();
    }

    public void setRingtone(String data){
        final File source = new File(data);
        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ringtones/"+source.getName();
        File destination = new File(destinationPath);
        try
        {
            FileChannel inChannel = new FileInputStream(source).getChannel();
            FileChannel outChannel = new FileOutputStream(destination).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null)
                    outChannel.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
            Boolean retVal = Settings.System.canWrite(context);
            if(!retVal){
                new AlertDialog.Builder(context)
                        .setTitle("Permission")
                        .setMessage("Permission needed to set Ringtone?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            context.startActivity(intent);
                                                        } else {
                                                            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.WRITE_SETTINGS},2);
                                                        }
                                String path = Environment.getExternalStorageDirectory().toString()
                                        + "/Ringtones";
                                File k = new File(path, source.getName());
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
                                values.put(MediaStore.MediaColumns.TITLE, k.getName());
                                values.put(MediaStore.MediaColumns.SIZE, k.length());
                                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                                values.put(MediaStore.Audio.Media.ARTIST, "ringtone");
                                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                                values.put(MediaStore.Audio.Media.IS_MUSIC, false);
                                Uri songuri = MediaStore.Audio.Media.getContentUriForPath(k.toString());
                                context.getContentResolver().delete(
                                        songuri,
                                        MediaStore.MediaColumns.DATA + "=\"" + k.getAbsolutePath()
                                                + "\"", null);
                                Uri newUri = context.getContentResolver().insert(songuri, values);
                                try {
                                    RingtoneManager.setActualDefaultRingtoneUri(context,
                                            RingtoneManager.TYPE_RINGTONE, newUri);
                                } catch (Throwable t) {
                                    Log.d(TAG, "catch exception");
                                    System.out.println("ringtone set exception " + t.getMessage());
                                }

                                Toast.makeText(context,"Ringtone Set!!",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else {
                String path = Environment.getExternalStorageDirectory().toString()
                        + "/Ringtones";
                File k = new File(path, source.getName());
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, k.getName());
                values.put(MediaStore.MediaColumns.SIZE, k.length());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                values.put(MediaStore.Audio.Media.ARTIST, "ringtone");
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);
                Uri songuri = MediaStore.Audio.Media.getContentUriForPath(k.toString());
                context.getContentResolver().delete(
                        songuri,
                        MediaStore.MediaColumns.DATA + "=\"" + k.getAbsolutePath()
                                + "\"", null);
                Uri newUri = context.getContentResolver().insert(songuri, values);
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(context,
                            RingtoneManager.TYPE_RINGTONE, newUri);
                } catch (Throwable t) {
                    Log.d(TAG, "catch exception");
                    System.out.println("ringtone set exception " + t.getMessage());
                }
                Toast.makeText(context,"Ringtone Set!!",Toast.LENGTH_SHORT).show();

            }
    }

    public class SetTrackInfo extends Dialog implements AsyncResponse
    {
        String songpath;
        String artist=null,album=null,title=null,genre=null,picturepath=null,albumid=null;
        AudioFile f;
        Tag tag;
        ImageView imgv;
        boolean check=false;
        AsyncResponse async= this;
        AlbumArtAsyncTask asynctask;

        public SetTrackInfo(String data,String albid) {
            super(context);
            this.songpath=data;
            register_recieveimage();
            this.albumid=albid;
        }

        @Override
        protected void onStop() {
            super.onStop();
            context.unregisterReceiver(recieveimage);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.editsonginfo_layout);

            imgv=(ImageView)findViewById(R.id.editinfoalbumart);
            final EditText edartist=(EditText)findViewById(R.id.editinfoartist);
            final EditText edalbum=(EditText)findViewById(R.id.editinfoalbum);
            final EditText edtitle=(EditText)findViewById(R.id.editinfotitle);
            final EditText edgenre=(EditText)findViewById(R.id.editinfogenre);
            Button savebtn=(Button) findViewById(R.id.editinfosavebtn);
            Button cancelbtn=(Button) findViewById(R.id.editinfocancelbtn);
            DisplayMetrics disp=context.getResources().getDisplayMetrics();
            int height = disp.heightPixels;
            int layoutheight = (height * 40) / 100;
            imgv.getLayoutParams().height = layoutheight;


            TagOptionSingleton.getInstance().setAndroid(true);
            File mp3File = new File(songpath);
            try {
                f = AudioFileIO.read(mp3File);
                tag = f.getTagOrCreateAndSetDefault();
                    artist = tag.getFirst(FieldKey.ARTIST);
                    title = tag.getFirst(FieldKey.TITLE);
                    album = tag.getFirst(FieldKey.ALBUM);
                    genre = tag.getFirst(FieldKey.GENRE);
                    edartist.setText(artist);
                    edalbum.setText(album);
                    edtitle.setText(title);
                    edgenre.setText(genre);
                asynctask=new AlbumArtAsyncTask(context,albumid);
                asynctask.delegate=async;
                asynctask.execute();

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

            savebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        try {
                            tag.setField(FieldKey.ARTIST, edartist.getText().toString());
                            tag.setField(FieldKey.ALBUM, edalbum.getText().toString());
                            tag.setField(FieldKey.TITLE, edtitle.getText().toString());
                            tag.setField(FieldKey.GENRE,edgenre.getText().toString());
                            if(check) {
                                Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumid));
                                context.getContentResolver().delete(uri,null, null);
                                ContentValues values = new ContentValues();
                                values.put("album_id", albumid);
                                values.put("_data", picturepath);
                                Uri newuri = context.getContentResolver()
                                        .insert(Uri.parse("content://media/external/audio/albumart"),
                                                values);
                                if(newuri!=null) {
                                    context.getContentResolver().notifyChange(uri, null);
                                }
                            }
                        } catch (FieldDataInvalidException e) {
                            e.printStackTrace();
                        }
                    try {
                        f.commit();
                        Toast.makeText(context, "Song Data Edited", Toast.LENGTH_SHORT).show();
                        cancel();
                    } catch (CannotWriteException e) {
                        e.printStackTrace();
                    }
                }
            });
            cancelbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });

            imgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,"Choose a Image",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                }
            });
        }

        BroadcastReceiver recieveimage=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b=intent.getExtras();
                String data=b.getString("data");
                Uri uri= Uri.parse(data);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    imgv.setImageBitmap(bitmap);
                    check=true;
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    String picturePath = cursor.getString(columnIndex);
                    picturepath=picturePath;
                    cursor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        private void register_recieveimage() {
            IntentFilter filter = new IntentFilter("recieveimagefilter");
            context.registerReceiver(recieveimage, filter);
        }

        @Override
        public void processFinish(Bitmap output) {

            if(output!=null)
            {
                imgv.setImageBitmap(output);
            }
            else
            {
                imgv.setImageResource(R.drawable.placeholder);
            }

        }
    }
}
