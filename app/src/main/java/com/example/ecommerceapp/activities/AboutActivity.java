package com.example.ecommerceapp.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecommerceapp.R;

public class AboutActivity extends AppCompatActivity {


    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {

            mediaPlayer = MediaPlayer.create(this, R.raw.welcome_sound);

            if (mediaPlayer != null) {
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        stopAndReleasePlayer();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About Us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopAndReleasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndReleasePlayer();
    }


    private void stopAndReleasePlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}