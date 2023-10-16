package com.example.beediotest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class uploadActvity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 1;
    AppCompatEditText editTextTitle;
    TextView TextViewImage;
    ProgressBar ProgressBar;
    Uri musicUri;
    StorageReference mStorageRef;
    StorageTask mUploadTask;
    DatabaseReference referenceSongs;
    ImageView homeBtn;
    boolean isKitKat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        }



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_actvity);
        getSupportActionBar().setTitle("Upload Music");

        editTextTitle = findViewById(R.id.songTitle);
        TextViewImage = findViewById(R.id.SongFileTextView);
        ProgressBar = findViewById(R.id.progressBar);
        referenceSongs = FirebaseDatabase.getInstance().getReference().child("songs");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("songs");
        PackageManager packageManager=getPackageManager();
        homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void openMusicFile(View v){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT){
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i,101);
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101&&resultCode==RESULT_OK&&data.getData()!=null){
            musicUri = data.getData();
            String fileName= getFileName(musicUri);


            TextViewImage.setText(fileName);

        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try {
                if(cursor!=null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }

        if(result==null){
            result=uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut!=-1){
                result= result.substring(cut+1);
            }
        }
        return result;
    }

    public void uploadMusicToFirebase(View v){
        if (TextViewImage.getText().toString().equals("No file selected")){
            Toast.makeText(getApplicationContext(),"Please select a song",Toast.LENGTH_LONG).show();
        }
        else{
            if(mUploadTask!=null && mUploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Uploading Song...",Toast.LENGTH_LONG).show();
            }
            else {

                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if(musicUri!=null) {
            String durationTxt;
            Toast.makeText(this, "Uploading a music file...", Toast.LENGTH_LONG).show();

            ProgressBar.setVisibility(View.VISIBLE);

            StorageReference storageReference=mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(musicUri));
//get duration

            /*String musicStringUrl = musicUri.toString();
            MediaPlayer mp = MediaPlayer.create(this, Uri.parse(musicStringUrl)); // Downloads is the folder and vid is video file.
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int duration = mp.getDuration();
                    mp.release();
                }
            });*/

            int durationInMillis = findSongDuration(musicUri);

            if(durationInMillis==0){
                durationTxt="N/A";
            }
            durationTxt=getDurationFromMillis(durationInMillis);


            final String finalDurationTxt = durationTxt;
            mUploadTask = storageReference.putFile(musicUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //getDownload Uri
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            MusicListAdapter musicListAdapter = new MusicListAdapter(editTextTitle.getText().toString(),
                                    finalDurationTxt,uri.toString());

                            String uploadId = referenceSongs.push().getKey();
                            referenceSongs.child(uploadId).setValue(musicListAdapter);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress =(100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    ProgressBar.setProgress((int)progress);
                    //if progress bar reach maximum value
                    int progressBarMaxVal= ProgressBar.getMax();
                    if (progress>=progressBarMaxVal){
                        //set everything to NULL and make toast
                        TextViewImage.setText("");
                        ProgressBar.setProgress(0);
                        Toast.makeText(uploadActvity.this,"Song Uploaded",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"No file upload",Toast.LENGTH_LONG).show();
        }
    }


    private String getDurationFromMillis(int durationInMillis) {

        String myTime= String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationInMillis),
                TimeUnit.MILLISECONDS.toSeconds(durationInMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInMillis))
        );

        return myTime;

    }

    private int findSongDuration(Uri musicUri) {
        int timeInMillissec = 0;
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this,musicUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillissec = Integer.parseInt(time);

            retriever.release();


            return timeInMillissec;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri musicUri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(musicUri));
    }

}