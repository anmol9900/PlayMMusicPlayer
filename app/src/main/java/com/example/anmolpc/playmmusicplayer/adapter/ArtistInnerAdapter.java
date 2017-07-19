package com.example.anmolpc.playmmusicplayer.adapter;


import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.anmolpc.playmmusicplayer.AlbumSongsList;
import com.example.anmolpc.playmmusicplayer.PreloadData;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.SplashActivity;
import com.example.anmolpc.playmmusicplayer.fragments.AlbumObject;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.anmolpc.playmmusicplayer.playeractivity.sArtworkUri;

public class ArtistInnerAdapter extends RecyclerView.Adapter<ArtistInnerAdapter.ViewHolder> {

    private Context mcontext;
    private List<AlbumObject> albums;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    AlbumObject allalbums;
    View newv;

    // data is passed into the constructor
    public ArtistInnerAdapter(Context context, List<AlbumObject> objectList) {
        this.mInflater = LayoutInflater.from(context);
        this.albums=objectList;
        this.mcontext=context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_grid_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        allalbums = albums.get(position);

        holder.artistname.setText(allalbums.getAlbumArtist());
        holder.artstalbum.setText(allalbums.getAlbumName());
        holder.numofsongs.setText(allalbums.getNumberOfSongs()+" Songs");
        DisplayMetrics disp=mcontext.getResources().getDisplayMetrics();
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

    // total number of cells
    @Override
    public int getItemCount() {
        return albums.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iv;
        TextView artstalbum,artistname,numofsongs;

        public ViewHolder(View itemView) {
            super(itemView);

            iv = (ImageView) itemView.findViewById(R.id.albumview);
            artistname=(TextView) itemView.findViewById(R.id.artistalbum);
            artstalbum=(TextView) itemView.findViewById(R.id.albumname);
            numofsongs=(TextView) itemView.findViewById(R.id.numberofsngs);
                   itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    Intent i=new Intent(mcontext, AlbumSongsList.class);
                                    i.putExtra("albumid",albums.get(getAdapterPosition()).getAlbumCover());
                                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    mcontext.startActivity(i);
                                }
                            }, 100);
                        }
                    });
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return albums.get(id).getAlbumCover();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
