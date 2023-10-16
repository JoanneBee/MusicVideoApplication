package com.example.beediotest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder> {
    private int selectedPosition;
    Context context;
    List<MusicListAdapter> arrayListSongs;
    private RecyclerItemClickListener listener;





    public SongsAdapter(Context context, List<MusicListAdapter> arrayListSongs, RecyclerItemClickListener listener){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.video_row, parent,false);
        return new SongsAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int position) {

        MusicListAdapter uploadSong = arrayListSongs.get(position);
        holder.titleTxt.setText(uploadSong.getSongTitle());
        holder.durationTxt.setText(uploadSong.getSongDuration());
        //bind
        holder.bind(uploadSong,listener);

    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }


    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder{

        TextView titleTxt, durationTxt;
        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.music_title);
            durationTxt = itemView.findViewById(R.id.textVideo_duration);
        }

        public void bind(MusicListAdapter uploadSong, RecyclerItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(uploadSong,getAdapterPosition());
                }
            });
        }
    }
    public List<MusicListAdapter> getArrayListSongs() {
        return arrayListSongs;
    }

    public interface RecyclerItemClickListener {
        void onClickListener(MusicListAdapter uploadSongs, int position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
}