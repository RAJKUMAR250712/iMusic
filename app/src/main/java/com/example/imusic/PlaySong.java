package com.example.imusic;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    private TextView textView;
    private ImageView play, previous, next;
    private SeekBar seekBar;
    private ArrayList<File> songs;
    private MediaPlayer mediaPlayer;
    private String textContent;
    private int position;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

        // Receive the song paths
        Intent intent = getIntent();
        ArrayList<String> songPaths = intent.getStringArrayListExtra("songPaths");
        textContent = intent.getStringExtra("currentSong");
        position = intent.getIntExtra("position", 0);

        // Convert paths to File objects
        songs = new ArrayList<>();
        for (String path : songPaths) {
            songs.add(new File(path));
        }

        textView.setText(textContent);
        textView.setSelected(true);

        // Start playing the song
        playSong();

        // SeekBar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pause updates while dragging
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                handler.post(updateSeekBar);
            }
        });

        // Play/Pause button
        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                play.setImageResource(R.drawable.play);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.pause);
                mediaPlayer.start();
                handler.post(updateSeekBar); // Restart updates
            }
        });

        // Previous button
        previous.setOnClickListener(v -> playPreviousSong());

        // Next button
        next.setOnClickListener(v -> playNextSong());
    }

    private void playSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();

        // Update SeekBar max duration
        seekBar.setMax(mediaPlayer.getDuration());

        // Start SeekBar updates
        handler.post(updateSeekBar);

        // Change play button to "Pause" icon
        play.setImageResource(R.drawable.pause);

        // Release MediaPlayer when song completes
        mediaPlayer.setOnCompletionListener(mp -> playNextSong());
    }

    private void playPreviousSong() {
        if (position > 0) {
            position--;
        } else {
            position = songs.size() - 1;
        }
        textContent = songs.get(position).getName();
        textView.setText(textContent);
        playSong();
    }

    private void playNextSong() {
        if (position < songs.size() - 1) {
            position++;
        } else {
            position = 0;
        }
        textContent = songs.get(position).getName();
        textView.setText(textContent);
        playSong();
    }

    // Runnable to update the SeekBar progress
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000); // Update every second
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
        super.onDestroy();
    }
}
