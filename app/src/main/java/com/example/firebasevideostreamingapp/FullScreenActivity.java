package com.example.firebasevideostreamingapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firebasevideostreamingapp.databinding.ActivityFullScreenBinding;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class FullScreenActivity extends AppCompatActivity {

    //inti Binding
    ActivityFullScreenBinding binding;

    //Layout Variable
    private ExoPlayer exoPlayer;
    //name String for getting and setting Recycler Item and url String for getting url form previous activity
    private String name, url;

    private boolean playWhenReady = false;
    private int currentWindow = 0;
    private long playBackPosition = 0;

    boolean fullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //getting XML Layout
        binding = ActivityFullScreenBinding.inflate(getLayoutInflater());
        //setting XML root to ActivityFullScreenBinding
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //for setting statusBarColor of Activity
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
//        }




//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("Full Screen");
//
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        name = intent.getExtras().getString("name");
        url = intent.getExtras().getString("url");

        binding.name.setText(name);


        ImageView fullScreenButton = binding.playerView.findViewById(R.id.exo_fullscreen_icon);

        initializePlayer();


        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fullScreen) {


                    fullScreenButton.setImageDrawable(ContextCompat.getDrawable(FullScreenActivity.this, R.drawable.baseline_fullscreen_24));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                    binding.playerView.setLayoutParams(params);

                    binding.name.setVisibility(View.VISIBLE);
                    fullScreen = false;
                } else {


                    fullScreenButton.setImageDrawable(ContextCompat.getDrawable(FullScreenActivity.this, R.drawable.baseline_fullscreen_exit_24));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    binding.playerView.setLayoutParams(params);

                    binding.name.setVisibility(View.GONE);
                    fullScreen = true;
                }
            }
        });

        binding.playerView.findViewById(R.id.exo_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exoPlayer.stop();
            }
        });

    }

    private MediaSource mediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "Video");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void initializePlayer() {

        //Building ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        //setting Player to ExoPlayer PlayView
        binding.playerView.setPlayer(exoPlayer);

        //parsing Uri
        Uri uri = Uri.parse(url);
        //creating obj of MediaItem class and providing resource id i.e uri
        MediaItem mediaItem = MediaItem.fromUri(uri);

        //setting media item to Exoplayer
        exoPlayer.setMediaItem(mediaItem);

        //by default, when video is ready to play, we don't play it automatically or not play when video ready play
        exoPlayer.setPlayWhenReady(playWhenReady);

        exoPlayer.seekTo(currentWindow,playBackPosition);

        //preparing ExoPlayer now
        exoPlayer.prepare(mediaSource(uri),false,false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT >= 26){
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Util.SDK_INT >= 26 && exoPlayer  == null){
//            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Util.SDK_INT >= 26){
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT >= 26){
            releasePlayer();
        }
    }

    private void releasePlayer() {

        if (exoPlayer != null){
            playWhenReady = exoPlayer.getPlayWhenReady();
            playBackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            exoPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();

        final Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }
}