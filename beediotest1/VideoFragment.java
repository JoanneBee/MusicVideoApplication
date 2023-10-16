package com.example.beediotest1;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beediotest1.Services.OnClearFromRecentService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VideoFragment extends Fragment {


    RecyclerView videoList;
    ProgressBar progressBar;
    List<MusicListAdapter> mUpload;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    SongsAdapter adapter;
    NotificationManager notificationManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video, container, false);
        videoList = v.findViewById(R.id.recycleView);
        videoList.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressBar = v.findViewById(R.id.progressBar);
        mUpload = new ArrayList<>();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        adapter = new SongsAdapter(getActivity(), mUpload, new SongsAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(MusicListAdapter uploadSongs, int position) {
                Toast.makeText(getActivity(), "Playing "+uploadSongs.getSongTitle(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(),Video.class);
                i.putExtra("video_title",uploadSongs.getSongTitle());
                i.putExtra("video_url",uploadSongs.getSongLink());
                i.putExtra("currentPosition",position);
                i.putExtra("size",adapter.getItemCount());
                startActivity(i);

                createNotification();
                CreateNotification.createNotification(getContext(),mUpload.get(position), position,adapter.getItemCount()-1);

            }
        });

        videoList.setAdapter(adapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");


        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUpload.clear();
                for(DataSnapshot dss:snapshot.getChildren()){
                    MusicListAdapter uploadSong = dss.getValue(MusicListAdapter.class);
                    uploadSong.setmKey(dss.getKey());
                    mUpload.add(uploadSong);
                    Video.getArrayList().add(uploadSong);

                }
                //desc order
                //Collections.reverse(mUpload);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Song not found.", Toast.LENGTH_SHORT).show();
            }
        });


    return v;
    }

    private void createNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Beedio", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);

    }

}