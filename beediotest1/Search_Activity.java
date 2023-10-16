package com.example.beediotest1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Search_Activity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    AppCompatEditText mEditText;
    TextView mNoSearchResultsFoundText;
    ImageView mClearQueryImageView;
    ImageView mVoiceSearchImageView;

    List<MusicListAdapter> msongList;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    final int SPEECH_REQUEST_CODE=1;
    SongsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mRecyclerView = findViewById(R.id.search_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Search_Activity.this));
        mRecyclerView.setHasFixedSize(true);
        mEditText = findViewById(R.id.search_edit_text);
        mNoSearchResultsFoundText = findViewById(R.id.no_search_results_found_text);
        mClearQueryImageView = findViewById(R.id.clear_search_query);
        mVoiceSearchImageView = findViewById(R.id.voice_search_query);

        ImageView homeBtn = findViewById(R.id.homeBtn);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Assign data to variable

        msongList= new ArrayList<>();
        adapter = new SongsAdapter(Search_Activity.this, msongList, new SongsAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(MusicListAdapter uploadSongs, int position) {
                Toast.makeText(Search_Activity.this, "Playing "+uploadSongs.getSongTitle(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Search_Activity.this,Video.class);
                i.putExtra("video_title",uploadSongs.getSongTitle());
                i.putExtra("video_url",uploadSongs.getSongLink());
                i.putExtra("currentPosition",position);
                i.putExtra("size",adapter.getItemCount());
                startActivity(i);
            }
        });
        mRecyclerView.setAdapter(adapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");

        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msongList.clear();
                for(DataSnapshot dss:snapshot.getChildren()){
                    MusicListAdapter uploadSong = dss.getValue(MusicListAdapter.class);
                    uploadSong.setmKey(dss.getKey());
                    msongList.add(uploadSong);
                    Video.getArrayList().add(uploadSong);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Search_Activity.this, "Song not found.", Toast.LENGTH_SHORT).show();
            }
        });



        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                String query = text.toString().toLowerCase();
                filterWithQuery(query);
                toggleImageView(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        //voice option

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }



        mVoiceSearchImageView.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"speak to text");
            try{
                startActivityForResult(intent, SPEECH_REQUEST_CODE);
            }catch (ActivityNotFoundException e){
                Toast.makeText(this,"No voice permission permitted",Toast.LENGTH_LONG).show();
            }
        });


        mClearQueryImageView.setOnClickListener(v ->
                mEditText.setText("")
        );
    }

    @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                ArrayList<String> results = Objects.requireNonNull(data).getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spokenText = results.get(0);
                // Do something with spokenText
                mEditText.setText(spokenText);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

    private void attachAdapter(List<MusicListAdapter> list) {
        adapter = new SongsAdapter(Search_Activity.this, list, new SongsAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(MusicListAdapter uploadSongs, int position) {
                Toast.makeText(Search_Activity.this, "Playing "+uploadSongs.getSongTitle(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Search_Activity.this,Video.class);
                i.putExtra("video_title",uploadSongs.getSongTitle());
                i.putExtra("video_url",uploadSongs.getSongLink());
                i.putExtra("currentPosition",position);


                startActivity(i);
            }
        });
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void filterWithQuery(String query) {
        if (!query.isEmpty()) {
            List<MusicListAdapter> filteredList = onFilterChanged(query);
            attachAdapter(filteredList);
            toggleRecyclerView(filteredList);
        } else {
            attachAdapter(msongList);
        }
    }

    private List<MusicListAdapter> onFilterChanged(String filterQuery) {
        List<MusicListAdapter> filteredList = new ArrayList<>();
        for (MusicListAdapter currentSong : msongList) {
            if (currentSong.getSongTitle().toLowerCase(Locale.getDefault()).contains(filterQuery)) {
                filteredList.add(currentSong);
            }
        }
        return filteredList;
    }


    private void toggleRecyclerView(List<MusicListAdapter> sportsList) {
        if (sportsList.isEmpty()) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mNoSearchResultsFoundText.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoSearchResultsFoundText.setVisibility(View.INVISIBLE);
        }
    }

    private void toggleImageView(String query) {
        if (!query.isEmpty()) {
            mClearQueryImageView.setVisibility(View.VISIBLE);
            mVoiceSearchImageView.setVisibility(View.INVISIBLE);
        } else {
            mClearQueryImageView.setVisibility(View.INVISIBLE);
            mVoiceSearchImageView.setVisibility(View.VISIBLE);
        }
    }
}