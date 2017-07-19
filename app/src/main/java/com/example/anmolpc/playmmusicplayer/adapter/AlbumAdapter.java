package com.example.anmolpc.playmmusicplayer.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.data.StreamAssetPathFetcher;
import com.example.anmolpc.playmmusicplayer.AlbumArtAsyncTask;
import com.example.anmolpc.playmmusicplayer.AlbumSongsList;
import com.example.anmolpc.playmmusicplayer.AsyncResponse;
import com.example.anmolpc.playmmusicplayer.MainPlayerActivity;
import com.example.anmolpc.playmmusicplayer.MediaPlayerService;
import com.example.anmolpc.playmmusicplayer.NowPlaylistAsyncTask;
import com.example.anmolpc.playmmusicplayer.PlaylistView;
import com.example.anmolpc.playmmusicplayer.PreloadData;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.RecyclerItemClickListener;
import com.example.anmolpc.playmmusicplayer.SplashActivity;
import com.example.anmolpc.playmmusicplayer.fragments.AlbumObject;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistObject;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.example.anmolpc.playmmusicplayer.playeractivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import static com.example.anmolpc.playmmusicplayer.playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO;

/**
 * Created by Anmol Pc on 4/15/2017.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{
    private Context context;
    private List<AlbumObject> albums=null;
    private LayoutInflater mInflater;
    Bitmap bmap;
    Gson gson=new Gson();


    public AlbumAdapter(Context context, List<AlbumObject> alBums)
    {
        this.context = context;
        this.albums = alBums;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_grid_layout, parent, false);
        AlbumAdapter.ViewHolder viewHolder = new AlbumAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
        AlbumObject allalbums = albums.get(position);
        holder.artistname.setText(allalbums.getAlbumArtist());
        holder.artstalbum.setText(allalbums.getAlbumName());
        holder.numofsongs.setText(allalbums.getNumberOfSongs()+" Songs");
        DisplayMetrics disp=context.getResources().getDisplayMetrics();
        int width = disp.widthPixels;
        int height=disp.heightPixels;
        holder.iv.getLayoutParams().width=width/2;
        holder.iv.getLayoutParams().height=(height*25)/100;
        Bitmap bmp= PreloadData.getBitmapFromMemCache(allalbums.getAlbumCover());
        if(bmp!=null) {
            holder.iv.setImageBitmap(bmp);
        }
        else {
            holder.iv.setImageResource(R.drawable.placeholder);
        }

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iv;
        TextView artstalbum,artistname,numofsongs;
        RelativeLayout options;
        public ViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.albumview);
            artistname=(TextView) itemView.findViewById(R.id.artistalbum);
            artstalbum=(TextView) itemView.findViewById(R.id.albumname);
            numofsongs=(TextView) itemView.findViewById(R.id.numberofsngs);
            options=(RelativeLayout) itemView.findViewById(R.id.albumlayoptions);
            itemView.setOnClickListener(this);
            options.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==options.getId())
            {
                PopupMenu popupMenu = new PopupMenu(context, options);
                popupMenu.inflate(R.menu.options_menu_albums);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.addtoplay:
                                CustomDialogClass cdd=new CustomDialogClass(albums.get(getAdapterPosition()).getAlbumCover());
                                cdd.show();
                                break;

                            case R.id.playnext:
                                String selection = "is_music != 0";
                                final String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
                                final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
                                Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                selection = selection + " and album_id = " + albums.get(getAdapterPosition()).getAlbumCover();
                                final Cursor cursor = context.getContentResolver().query(urinew, projection, selection, null, sortOrder);

                                if(MediaPlayerService.musicBound) {
                                    SharedPreferences preferences = context.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
                                    Gson gson = new Gson();
                                    String json = preferences.getString("nowplaylist", null);
                                    Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
                                    ArrayList<SongObject> arrayList = gson.fromJson(json, type);
                                    int i=1;
                                    while(cursor.moveToNext()) {
                                        long finaltimetxt=Long.parseLong(cursor.getString(3));
                                        long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                                        long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                        toMinutes(finaltimetxt));
                                        arrayList.add(MediaPlayerService.getPosition() + i, new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albums.get(getAdapterPosition()).getAlbumCover()));
                                        i++;
                                    }
                                    NowPlaylistAsyncTask np = new NowPlaylistAsyncTask(arrayList, context);
                                    np.methodRun();
                                }
                                else {
                                    final List<SongObject> arrayList=new ArrayList<SongObject>();
                                    while(cursor.moveToNext()) {
                                        long finaltimetxt=Long.parseLong(cursor.getString(3));
                                        long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                                        long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                        toMinutes(finaltimetxt));
                                        arrayList.add(new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albums.get(getAdapterPosition()).getAlbumCover()));
                                    }
                                    NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(arrayList,context);
                                    np.methodRun();
                                    android.os.Handler handler = new android.os.Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(context,playeractivity.class);
                                            i.putExtra("data", arrayList.get(0).getSongData());
                                            i.putExtra("albumid",arrayList.get(0).getSongAlbumid());
                                            i.putExtra("position",0);
                                            i.putExtra("intent","other");
                                            context.startActivity(i);
                                        }
                                    },100);
                                }
                                break;

                            case R.id.playnow:
                                playAllSongsNow(getAdapterPosition());
                                break;

                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selection = "is_music != 0";
                                        final String[] projection = {MediaStore.Audio.Media.DATA};
                                        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                        selection = selection + " and album_id = " + albums.get(getAdapterPosition()).getAlbumCover();
                                        final Cursor cursor = context.getContentResolver().query(urinew,projection,selection,null,null);
                                        int roweffected = 0;
                                        while(cursor.moveToNext()) {
                                            File f = new File(cursor.getString(0));
                                            Boolean deleted = f.delete();
                                            if (deleted) {
                                                Uri uri = MediaStore.Audio.Media.getContentUriForPath(cursor.getString(0));
                                                roweffected = context.getContentResolver().delete(uri,
                                                        MediaStore.MediaColumns.DATA + "=\"" + cursor.getString(0) + "\"",
                                                        null);
                                            }
                                        }
                                        if (roweffected > 0) {
                                            albums.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                            Toast.makeText(context, "Album Deleted!!", Toast.LENGTH_SHORT).show();
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
                                String shareselection = "is_music != 0";
                                String[] shareprojection = {MediaStore.Audio.Media.DATA};
                                Uri shareurinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                shareselection = shareselection + " and album_id = " + albums.get(getAdapterPosition()).getAlbumCover();
                                Cursor sharecursor = context.getContentResolver().query(shareurinew,shareprojection,shareselection,null,null);
                                ArrayList<Uri> files = new ArrayList<Uri>();
                                while(sharecursor.moveToNext()){
                                        File file = new File(sharecursor.getString(0));
                                        Uri uri = Uri.fromFile(file);
                                        files.add(uri);
                                }
                                Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                share.setType("*/*");
                                share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                                context.startActivity(Intent.createChooser(share, "Share Audio File"));
                                break;
                            case R.id.editinfo:
                                SetAlbumInfo sai=new SetAlbumInfo(albums.get(getAdapterPosition()).getAlbumCover(),getAdapterPosition());
                                sai.show();
                                break;
                        }
                        return true;
                    }
                });

            }
            else{
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                AlbumSongsList albumSongsList=new AlbumSongsList();
                if(albumSongsList != null) {
                    if (AlbumSongsList.active = true) {
                        albumSongsList.finish();
                    }
                }
                Intent i=new Intent(context, AlbumSongsList.class);
                i.putExtra("albumid",albums.get(getAdapterPosition()).getAlbumCover());
                context.startActivity(i);
                    }
                },100);
            }
        }
    }

    public class CustomDialogClass extends Dialog {
        ArrayList<PlaylistObject> Objectlist;
        Button createplay;
        RecyclerView lst;
        TextView empty;
        String albumID;
        String selection = "is_music != 0";
        final String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        public CustomDialogClass(String albid) {
            super(context);
            this.albumID=albid;
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
                        selection = selection + " and album_id = " + albumID;
                        final Cursor cursor = context.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                        while(cursor.moveToNext()) {
                            long finaltimetxt=Long.parseLong(cursor.getString(3));
                            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes(finaltimetxt));
                            newplay.add(new SongObject(cursor.getString(0), cursor.getString(1),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albumID));
                        }
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
                    CreatePlaylistDialog cdd=new CreatePlaylistDialog(albumID);
                    cdd.show();
                    cancel();
                }

            });
        }
    }

    protected class CreatePlaylistDialog extends Dialog {
        String albID;
        String selection = "is_music != 0";
        final String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        public CreatePlaylistDialog(String albid) {
            super(context);
            this.albID=albid;
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
                    selection = selection + " and album_id = " + albID;
                    final Cursor cursor = context.getContentResolver().query(urinew, projection, selection, null, sortOrder);
                    while(cursor.moveToNext()) {
                        long finaltimetxt=Long.parseLong(cursor.getString(3));
                        long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
                        long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes(finaltimetxt));
                        newplaylist.add(new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albID));
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

    public void playAllSongsNow(int pos)
    {
        String selection = "is_music != 0";
        final String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        Uri urinew = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        selection = selection + " and album_id = " + albums.get(pos).getAlbumCover();
        final Cursor cursor = context.getContentResolver().query(urinew, projection, selection, null, sortOrder);
        List<SongObject> allsongs=new ArrayList<>();
        while(cursor.moveToNext()) {
            long finaltimetxt=Long.parseLong(cursor.getString(3));
            long min= TimeUnit.MILLISECONDS.toMinutes(finaltimetxt);
            long sec=TimeUnit.MILLISECONDS.toSeconds(finaltimetxt) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                            toMinutes(finaltimetxt));
            allsongs.add(new SongObject(cursor.getString(0).toString(),cursor.getString(1).toString(),String.valueOf(min)+":"+String.valueOf(sec),cursor.getString(2),albums.get(pos).getAlbumCover()));
        }
        if(!MediaPlayerService.musicBound){
            Intent i = new Intent(context, MediaPlayerService.class);
            Bundle b=new Bundle();
            b.putString("data",allsongs.get(0).getSongData());
            b.putInt("pos",0);
            i.putExtras(b);
            context.startService(i);
        }
        else
        {
            Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
            Bundle b=new Bundle();
            b.putString("data",allsongs.get(0).getSongData());
            b.putInt("pos",0);
            broadcastIntent.putExtras(b);
            context.sendBroadcast(broadcastIntent);
        }

        NowPlaylistAsyncTask np=new NowPlaylistAsyncTask(allsongs,context);
        np.methodRun();
        Toast.makeText(context,"Album Played!!",Toast.LENGTH_SHORT).show();
    }

    public class SetAlbumInfo extends Dialog
    {
        String artist=null,album=null,year=null,genre=null,picturepath=null,albumid=null;
        AudioFile f;
        Tag tag;
        ImageView imgv;
        boolean check=false;
        int pos;
        Bitmap bitmap;

        public SetAlbumInfo(String albid,int position) {
            super(context);
            register_recieveimage();
            this.albumid=albid;
            this.pos=position;
        }

        @Override
        protected void onStop() {
            super.onStop();
            context.unregisterReceiver(recieveimage);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.editalbuminfo_layout);

            Toast.makeText(context,"Click on the image to choose a new one",Toast.LENGTH_SHORT).show();

            imgv=(ImageView)findViewById(R.id.editalbuminfoalbumart);
            final EditText edartist=(EditText)findViewById(R.id.editalbuminfoartist);
            final EditText edalbum=(EditText)findViewById(R.id.editalbuminfoalbum);
            final EditText edyear=(EditText)findViewById(R.id.editalbuminfoyear);
            final EditText edgenre=(EditText)findViewById(R.id.editalbuminfogenre);
            Button savebtn=(Button) findViewById(R.id.editalbuminfosavebtn);
            Button cancelbtn=(Button) findViewById(R.id.editalbuminfocancelbtn);
            DisplayMetrics disp=context.getResources().getDisplayMetrics();
            int height = disp.heightPixels;
            int layoutheight = (height * 40) / 100;
            imgv.getLayoutParams().height = layoutheight;

            Bitmap bmp= PreloadData.getBitmapFromMemCache(albumid);
            if(bmp!=null) {
                imgv.setImageBitmap(bmp);
            }
            else {
                imgv.setImageResource(R.drawable.placeholder);
            }

            String[] projection = {MediaStore.Audio.Albums.ALBUM,MediaStore.Audio.Albums.ARTIST};
            Uri urinew = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Albums._ID+" = " + albumid;
            Cursor cursor = context.getContentResolver().query(urinew, projection, selection, null, null);
            cursor.moveToFirst();
                edartist.setText(cursor.getString(1));
                edalbum.setText(cursor.getString(0));


            String selection1 = "is_music != 0";
            String[] projection1 = {MediaStore.Audio.Media.DATA};
            Uri urinew1 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            selection1 = selection1 + " and album_id = " + albumid;
            Cursor cursor1 = context.getContentResolver().query(urinew1, projection1, selection1, null, null);
            cursor1.moveToFirst();
            TagOptionSingleton.getInstance().setAndroid(true);
            File mp3File = new File(cursor1.getString(0));
            try {
                f = AudioFileIO.read(mp3File);
                tag = f.getTagOrCreateAndSetDefault();
                genre = tag.getFirst(FieldKey.GENRE);
                year = tag.getFirst(FieldKey.YEAR);
                edgenre.setText(genre);
                edyear.setText(year);
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

            final List<String> files=new ArrayList<>();
            cursor1.move(-1);
            while (cursor1.moveToNext())
            {
                files.add(cursor1.getString(0));
            }
            final String songlist[]=new String[files.size()];
            savebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(check) {
                        final String uriString = "content://media/external/audio/albumart";
                        Uri uri = ContentUris.withAppendedId(Uri.parse(uriString), Long.parseLong(albumid));
                        context.getContentResolver().delete(uri,null, null);
                        ContentValues values = new ContentValues();
                        values.put("album_id", albumid);
                        values.put("_data", picturepath);
                        Uri newuri = context.getContentResolver()
                                .insert(Uri.parse(uriString),
                                        values);
                        if(newuri!=null) {
                            context.getContentResolver().notifyChange(uri, null);
                            PreloadData.imageCache.remove(albumid);
                            PreloadData.imageCache.put(albumid,bitmap);
                        }
                    }
                    ContentValues values=new ContentValues();
                    values.put(MediaStore.Audio.Albums.ALBUM,edalbum.getText().toString());
                    values.put(MediaStore.Audio.Albums.ARTIST,edartist.getText().toString());
                    context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,MediaStore.Audio.Albums.ALBUM_ID+" = "+albumid,null);
                    for(int i=0;i<files.size();i++){
                        songlist[i]=files.get(i);
                        TagOptionSingleton.getInstance().setAndroid(true);
                        File mp3File = new File(files.get(i));
                        try {
                            f = AudioFileIO.read(mp3File);
                            tag = f.getTagOrCreateAndSetDefault();
                            tag.setField(FieldKey.ARTIST, edartist.getText().toString());
                            tag.setField(FieldKey.ALBUM, edalbum.getText().toString());
                            tag.setField(FieldKey.YEAR, edyear.getText().toString());
                            tag.setField(FieldKey.GENRE, edgenre.getText().toString());
                            f.commit();
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
                        } catch (CannotWriteException e) {
                            e.printStackTrace();
                        }
                    }
                    cancel();
                    Toast.makeText(context, "Album Data Edited", Toast.LENGTH_SHORT).show();
                    albums.set(pos,new AlbumObject(albumid,edartist.getText().toString(),edalbum.getText().toString(),albums.get(pos).getNumberOfSongs()));
                    notifyItemChanged(pos,new AlbumObject(albumid,edartist.getText().toString(),edalbum.getText().toString(),albums.get(pos).getNumberOfSongs()));
                    MediaScannerConnection.scanFile(
                            context,
                            songlist,
                            null,
                            new MediaScannerConnection.MediaScannerConnectionClient()
                            {
                                public void onMediaScannerConnected()
                                {
                                }
                                public void onScanCompleted(String path, Uri uri)
                                {
                                }
                            });
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
                    ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
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
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
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
            IntentFilter filter = new IntentFilter("recieveimagealbumfilter");
            context.registerReceiver(recieveimage, filter);
        }
    }
}
