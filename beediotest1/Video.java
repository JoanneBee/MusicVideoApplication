package com.example.beediotest1;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Rational;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.beediotest1.Services.OnClearFromRecentService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Video extends AppCompatActivity{
    MediaController mediaController;
    static int index;
    static String videoUrl;
    static String videoTitle;
    SongsAdapter adapter;
    static List<MusicListAdapter> arrayList = new ArrayList<>();
    RelativeLayout relativeLayout;
    VideoView videoView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference songRef = database.getReference("songs");
    MediaController.MediaPlayerControl player;
    ImageView homeBtn;
    ImageView fullScreenBtn;
    ImageView LockBtn;
    ImageButton PipButton;
    Boolean isFullScreen =false;
    Boolean isLock = false;
    NotificationManager notificationManager;
    PictureInPictureParams.Builder pictureInPictureParams;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);
        getIncomingIntent();
        //button to back home
        homeBtn = findViewById(R.id.homeBtn);
        
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        fullScreenBtn=findViewById(R.id.fullScreenBtn);
        LockBtn = findViewById(R.id.lockBtn);

        fullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable myDrawable = getResources().getDrawable(R.drawable.ic_fullscreen_exit);
                Drawable myDrawable1 = getResources().getDrawable(R.drawable.ic_fullscreen);

                if(!isFullScreen){
                    fullScreenBtn.setImageDrawable(myDrawable);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
                else{
                    fullScreenBtn.setImageDrawable(myDrawable1);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                isFullScreen = !isFullScreen;
            }
        });

        LockBtn.setOnClickListener(new View.OnClickListener() {

            Drawable myDrawableLock = getResources().getDrawable(R.drawable.ic_lock);
            Drawable myDrawableLockOpen = getResources().getDrawable(R.drawable.ic_lock_open);
            @Override
            public void onClick(View v) {
                if(!isLock){
                    LockBtn.setImageDrawable(myDrawableLock);
                    homeBtn.setVisibility(View.INVISIBLE);
                }
                else{
                    LockBtn.setImageDrawable(myDrawableLockOpen);
                    homeBtn.setVisibility(View.VISIBLE);
                }
                isLock = !isLock;
                lockScreen(isLock);
            }
        });

        PipButton = findViewById(R.id.PipButton);
        relativeLayout = findViewById(R.id.relativeLayoutVideo);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pictureInPictureParams = new PictureInPictureParams.Builder();
        }
        PipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPipButton();
            }
        });
    }

    private void getPipButton() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            /*Rational aspectRation = new Rational(videoView.getWidth(),videoView.getHeight());*/
            Rational aspectRation = new Rational(350,250);
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(!isInPictureInPictureMode()){
                getPipButton();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isInPictureInPictureMode) {
                PipButton.setVisibility(View.INVISIBLE);
                homeBtn.setVisibility(View.INVISIBLE);
                videoView.setY(90);
                LockBtn.setVisibility(View.INVISIBLE);

            } else {
                PipButton.setVisibility(View.VISIBLE);
                homeBtn.setVisibility(View.VISIBLE);
                videoView.setY(180);
                LockBtn.setVisibility(View.VISIBLE);
            }
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
    }


    // Lock screen to avoid user trigger anything
    private void lockScreen(Boolean isLock) {
        if(isLock){
            videoView.setMediaController(null);
        }
        else{
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
        }

    }



    private void getIncomingIntent(){
        if(getIntent().hasExtra("video_title") && getIntent().hasExtra("video_url")&&getIntent().hasExtra("currentPosition")){
            String videoTitle = getIntent().getStringExtra("video_title");
            String videoUrl = getIntent().getStringExtra("video_url");
            index = getIntent().getIntExtra("currentPosition",0);
            Toast.makeText(this,"Current Index: "+index,Toast.LENGTH_SHORT).show();
            setVideo(videoTitle,videoUrl,index);
        }else{
            Toast.makeText(this,"No intent song",Toast.LENGTH_SHORT).show();
        }
    }

    private void setVideo(String videoTitle, String videoUrl,int curPos){
        mediaController = new MediaController(this);
        TextView musictilte= findViewById(R.id.music_title);
        musictilte.setText(videoTitle);
        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath(videoUrl);
        lockScreen(isLock);
        videoView.requestFocus();
        videoView.start();
        mediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               playNext();
            }
        }, v -> {
              playPrevious();
        });

    }

    public void playNext(){
        long size = getIntent().getIntExtra("size",0);
        Toast.makeText(this,"Size: "+String.valueOf(size), Toast.LENGTH_SHORT).show();

        if (index>=0 && index <size-1){
            if(arrayList!=null && arrayList.size() > 0){
                MusicListAdapter uploadsongs=arrayList.get(index+1);
                videoUrl = uploadsongs.getSongLink();
                videoTitle = uploadsongs.getSongTitle();
                setVideo(videoTitle,videoUrl,index);
                Toast.makeText(this,"Index: "+index,Toast.LENGTH_SHORT).show();
                index++;
            }
        }
    }

    public void playPrevious(){
        long size = getIntent().getIntExtra("size",0);
        if (index>0 && index <=size-1){
            if(arrayList!=null && arrayList.size() > 0){
                MusicListAdapter uploadsongs=arrayList.get(index-1);
                videoUrl = uploadsongs.getSongLink();
                videoTitle = uploadsongs.getSongTitle();
                setVideo(videoTitle,videoUrl,index);
                index--;
            }
        }else{
            Toast.makeText(this,"This is the last song",Toast.LENGTH_SHORT).show();
        }

    }


    public static List<MusicListAdapter> getArrayList() {
        return arrayList;
    }

    public static void setArrayList(List<MusicListAdapter> arrayList) {
        Video.arrayList = arrayList;
    }

    

}
