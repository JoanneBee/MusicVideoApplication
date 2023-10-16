package com.example.beediotest1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MusicListAdapter{

    public String songTitle, songDuration, songLink, mKey;
    public MusicListAdapter(){}
    public MusicListAdapter(String songTitle, String songDuration, String songLink){
        if (songTitle.trim().equals("")){
            songTitle = "No title";
        }
        this.songTitle = songTitle;
        this.songDuration = songDuration;
        this.songLink = songLink;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getSongLink() {
        return songLink;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}



