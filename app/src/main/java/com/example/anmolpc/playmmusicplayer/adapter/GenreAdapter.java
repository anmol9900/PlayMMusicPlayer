package com.example.anmolpc.playmmusicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.anmolpc.playmmusicplayer.GenreView;
import com.example.anmolpc.playmmusicplayer.R;
import com.example.anmolpc.playmmusicplayer.fragments.GenreObject;

import java.util.List;

/**
 * Created by anmol9900 on 6/6/2017.
 */

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {
    Context mContext;
    List<GenreObject> genres;
    public GenreAdapter(Context context, List<GenreObject> inclist)
    {
        this.mContext=context;
        this.genres=inclist;
    }

    @Override
    public GenreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.genre_fragment, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GenreAdapter.ViewHolder holder, int position) {
            holder.genrename.setText(genres.get(position).getGenrename());
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView genrename;
        RelativeLayout genreoptions;
        public ViewHolder(View itemView) {
            super(itemView);
            genrename=(TextView) itemView.findViewById(R.id.genrename);
            genreoptions=(RelativeLayout) itemView.findViewById(R.id.genreoptions);
            itemView.setOnClickListener(this);
            genreoptions.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(genreoptions.getId()==v.getId())
            {

            }
            else
            {
                Intent i = new Intent(mContext, GenreView.class);
                i.putExtra("id",genres.get(getAdapterPosition()).getGenreid());
                i.putExtra("name",genres.get(getAdapterPosition()).getGenrename());
                mContext.startActivity(i);
            }
        }
    }
}