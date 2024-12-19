package com.example.imusic;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_MEDIA_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ArrayList<File> mySongs = fetchSongs(Environment.getExternalStorageDirectory());

                        // Create an ArrayList to store the song paths (Strings)
                        ArrayList<String> songPaths = new ArrayList<>();
                        for (File song : mySongs) {
                            songPaths.add(song.getAbsolutePath());  // Store the absolute path of each file
                        }

                        String[] items = new String[mySongs.size()];
                        for (int i = 0; i < mySongs.size(); i++) {
                            items[i] = mySongs.get(i).getName().replace(".mp3", "");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, items);
                        listView.setAdapter(adapter);

                        // Send song paths and song name to the PlaySong activity
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(MainActivity.this, PlaySong.class);
                                intent.putStringArrayListExtra("songPaths", songPaths);  // Pass song paths (strings)
                                intent.putExtra("currentSong", mySongs.get(position).getName());  // Current song name
                                intent.putExtra("position", position);  // Current song position
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Permission Denied! Cannot fetch songs.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> fetchSongs(File rootDirectory) {
        ArrayList<File> songList = new ArrayList<>();
        File[] files = rootDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !file.isHidden()) {
                    songList.addAll(fetchSongs(file));
                } else if (file.getName().endsWith(".mp3") && !file.getName().startsWith(".")) {
                    songList.add(file);
                }
            }
        }
        return songList;
    }
}
