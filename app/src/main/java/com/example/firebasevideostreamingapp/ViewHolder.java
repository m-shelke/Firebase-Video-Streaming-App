package com.example.firebasevideostreamingapp;

import android.app.Application;
import android.content.Context;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.SilenceMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

//ViewHolder class for getting the References of Layout File
public class ViewHolder extends RecyclerView.ViewHolder {

    //Layout Variable
    ExoPlayer exoPlayer;
    PlayerView playerView;

    //Matching constructor
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    //Method for setting ExoPlayer
    public void setSimpleExoPlayer(Application application,String name, String videouri){

        //Finding XML Layout ID's
        TextView textView = itemView.findViewById(R.id.item_name);
        playerView = itemView.findViewById(R.id.item_exoPlayer);

        //setting name of Video to TextView
        textView.setText(name);

        exoPlayer = new ExoPlayer.Builder(application).build();
        playerView.setPlayer(exoPlayer);

        String videoUrl = videouri;

        Uri uri = Uri.parse(videoUrl);
        MediaItem mediaItem = MediaItem.fromUri(uri);

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);






//        try {
//            //provides estimates of the currently available Bandwidth
//            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(application).build();
//
//          TrackSelector trackSelector = new DefaultTrackSelector((Context) application, (ExoTrackSelection.Factory) bandwidthMeter);
//
//            // SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);
//
//            Uri video = Uri.parse(videouri);
//            DefaultHttpDataSource dataSourceFactory = new DefaultHttpDataSource("video");
//            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

}
