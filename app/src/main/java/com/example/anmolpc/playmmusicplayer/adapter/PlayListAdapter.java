package com.example.anmolpc.playmmusicplayer.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anmolpc.playmmusicplayer.MainPlayerActivity;
import com.example.anmolpc.playmmusicplayer.MediaPlayerService;
import com.example.anmolpc.playmmusicplayer.NowPlaylistAsyncTask;
import com.example.anmolpc.playmmusicplayer.PlaylistView;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.fragments.PlaylistObject;
import com.example.anmolpc.playmmusicplayer.fragments.SongObject;
import com.example.anmolpc.playmmusicplayer.playeractivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.anmolpc.playmmusicplayer.playeractivity.MEDIAPLAYER_PLAY_NEW_AUDIO;

/**
 * Created by Anmol Pc on 5/1/2017.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.myViewHolder> {
    Context mContext;
    List<PlaylistObject> lst;
    String check;
    Gson gson = new Gson();
    NowPlaylistAsyncTask np;

    public PlayListAdapter(Context context,List<PlaylistObject> objectList,String check)
    {
        this.mContext=context;
        this.lst=objectList;
        this.check=check;
    }
    @Override
    public PlayListAdapter.myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        return new myViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayListAdapter.myViewHolder holder, int position) {
        if(check.equals("other"))
        {
            holder.options.setVisibility(View.GONE);
        }
        else if(check.equals("playlist"))
        {
            holder.options.setVisibility(View.VISIBLE);
        }
        SharedPreferences preferences= MainPlayerActivity.context.getSharedPreferences(lst.get(position).getPlaylistTitle(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=preferences.edit();
        Gson gson = new Gson();
        String json = preferences.getString("playlist", null);
        int songs=0;
        Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
        ArrayList<SongObject> Objectlist = gson.fromJson(json, type);
        holder.playname.setText(lst.get(position).getPlaylistTitle());
        if(preferences.contains("playlist"))
        {
            for (SongObject sob:Objectlist) {
                File f=new File(sob.getSongData());
                if(!f.exists())
                {
                    Objectlist.remove(sob);
                    edit.clear();
                    edit.commit();
                    String jsonsong = gson.toJson(Objectlist);
                    edit.putString("playlist", jsonsong);
                    edit.commit();
                }
            }
            songs=Objectlist.size();
        }
        holder.nosongs.setText(songs+" Songs");
    }

    @Override
    public int getItemCount() {
        return lst.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView playname,nosongs,options;
        RelativeLayout seloptions;
        public myViewHolder(View itemView) {
            super(itemView);
            playname=(TextView) itemView.findViewById(R.id.playname);
            nosongs=(TextView) itemView.findViewById(R.id.playnoofsongs);
            options=(TextView) itemView.findViewById(R.id.playlistoptions);
            seloptions=(RelativeLayout) itemView.findViewById(R.id.selectplayoptions);
            itemView.setOnClickListener(this);
            seloptions.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==itemView.getId())
            {
                Intent i = new Intent(mContext, PlaylistView.class);
                i.putExtra("list", lst.get(getAdapterPosition()).getPlaylistTitle());
                mContext.startActivity(i);
            }
            else if(v.getId()==seloptions.getId())
            {
                final String name=lst.get(getAdapterPosition()).getPlaylistTitle();
                final SharedPreferences preferences= mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
                String json = preferences.getString("playlist", null);
                Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
                final List<SongObject> Objectlist = gson.fromJson(json, type);
                PopupMenu popupMenu = new PopupMenu(mContext, options);
                popupMenu.inflate(R.menu.options_menu_playlists);
                if(name.equals("Recently Added") || name.equals("Most Played"))
                {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.playnow:

                                if(!MediaPlayerService.musicBound){
                                    Intent i = new Intent(mContext, MediaPlayerService.class);
                                    Bundle b=new Bundle();
                                    b.putString("data",Objectlist.get(0).getSongData());
                                    b.putInt("pos",0);
                                    i.putExtras(b);
                                    mContext.startService(i);
                                }
                                else
                                {
                                    Intent broadcastIntent = new Intent(MEDIAPLAYER_PLAY_NEW_AUDIO);
                                    Bundle b=new Bundle();
                                    b.putString("data",Objectlist.get(0).getSongData());
                                    b.putInt("pos",0);
                                    broadcastIntent.putExtras(b);
                                    mContext.sendBroadcast(broadcastIntent);
                                }

                               np=new NowPlaylistAsyncTask(Objectlist,mContext);
                                np.methodRun();
                                Toast.makeText(mContext,"Artist Played!!",Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.playnext:
                                if(MediaPlayerService.musicBound) {
                                    SharedPreferences preferences = mContext.getSharedPreferences("nowplaylist", Context.MODE_PRIVATE);
                                    Gson gson = new Gson();
                                    String json = preferences.getString("nowplaylist", null);
                                    Type type = new TypeToken<ArrayList<SongObject>>() {}.getType();
                                    ArrayList<SongObject> arrayList = gson.fromJson(json, type);
                                    int i=1;
                                    for (SongObject s:Objectlist) {
                                        arrayList.add(MediaPlayerService.getPosition() + i,s);
                                        i++;
                                    }
                                    np = new NowPlaylistAsyncTask(arrayList, mContext);
                                    np.methodRun();
                                }
                                else {
                                    np =new NowPlaylistAsyncTask(Objectlist,mContext);
                                    np.methodRun();
                                    android.os.Handler handler = new android.os.Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(mContext,playeractivity.class);
                                            i.putExtra("data", Objectlist.get(0).getSongData());
                                            i.putExtra("albumid",Objectlist.get(0).getSongAlbumid());
                                            i.putExtra("position",0);
                                            i.putExtra("intent","other");
                                            mContext.startActivity(i);
                                        }
                                    },100);
                                }
                                break;
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences.Editor edit=preferences.edit();
                                        edit.clear();
                                        edit.apply();
                                        SharedPreferences playlists=mContext.getSharedPreferences("playlists", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor=playlists.edit();
                                        if(playlists.contains("list"))
                                        {
                                            Gson gson = new Gson();
                                            String json = playlists.getString("list", null);
                                            Type type = new TypeToken<ArrayList<PlaylistObject>>() {}.getType();
                                            final ArrayList<PlaylistObject> Objectlist = gson.fromJson(json, type);
                                            if (Objectlist != null) {
                                                Objectlist.remove(getAdapterPosition());
                                            }
                                            editor.clear();
                                            editor.apply();
                                            String jsonb = gson.toJson(Objectlist);
                                            editor.putString("list", jsonb);
                                            editor.commit();
                                        }
                                        Toast.makeText(mContext,name.toUpperCase()+" Playlist Deleted!!",Toast.LENGTH_SHORT).show();
                                        lst.remove(getAdapterPosition());
                                        notifyItemRemoved(getAdapterPosition());
                                        File file = new File(mContext.getFilesDir().getParent() + "/shared_prefs/"+name+".xml");
                                        if(file.exists())
                                        {
                                            file.delete();
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
                                ArrayList<Uri> files = new ArrayList<>();
                                for(SongObject s:Objectlist)
                                {
                                    File file = new File(s.getSongData());
                                    Uri uri = Uri.fromFile(file);
                                    files.add(uri);
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
            }
        }
    }
}
