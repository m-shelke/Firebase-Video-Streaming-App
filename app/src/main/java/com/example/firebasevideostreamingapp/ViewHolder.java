package com.example.firebasevideostreamingapp;

import android.app.Application;
import android.content.Context;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//ViewHolder class for getting the References of Layout File
public class ViewHolder extends RecyclerView.ViewHolder {

    //Layout Variable
    ExoPlayer exoPlayer;
    PlayerView playerView;
    ImageView likeButton;
    TextView like_txt;

    //likeCount variable for integer value count
    int likeCount;
    //Database Reference
    DatabaseReference reference;


    //Matching super constructor
    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        //setting OnClickListener on itemView
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting item clicked position
                mClickListener.onItemClick(v,getAdapterPosition());
            }
        });

        //setting OnLongClickListener on itemView
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //getting item clicked position
                mClickListener.onItemLongClick(v,getAdapterPosition());
                return false;
            }
        });
    }

    //Method for setting ExoPlayer
    public void setSimpleExoPlayer(Application application,String name, String videouri){

        //Finding XML Layout ID's
        TextView textView = itemView.findViewById(R.id.item_name);
        playerView = itemView.findViewById(R.id.item_exoPlayer);

        //setting name of Video to TextView
        textView.setText(name);

        //Building ExoPlayer
        exoPlayer = new ExoPlayer.Builder(application).build();
        //setting Player to ExoPlayer PlayView
        playerView.setPlayer(exoPlayer);

        //parsing Uri
        Uri uri = Uri.parse(videouri);
        //creating obj of MediaItem class and providing resource id i.e uri
        MediaItem mediaItem = MediaItem.fromUri(uri);

        //setting media item to Exoplayer
        exoPlayer.setMediaItem(mediaItem);
        //preparing ExoPlayer now
        exoPlayer.prepare();
        //by default, when video is ready to play, we don't play it automatically or not play when video ready play
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

    //creating setLikeButtonStatus() for checking video like status
    public void setLikeButtonStatus(final String postKey) {
        //finding View id and references
        likeButton = itemView.findViewById(R.id.like_button);
        like_txt = itemView.findViewById(R.id.like_txt);

        //getting instance of Firebase Database and creating "Likes" path Json in Database
        reference = FirebaseDatabase.getInstance().getReference("Likes");
        //getting instance of FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //getting UID of Current user
        String userId = user.getUid();

        //adding Value EventListener reference path of Database
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //inflicting Drawable Resources and Like ImageView on Database get changes
                if (snapshot.child(postKey).hasChild(userId)){
                    //getting Children Count
                    likeCount = (int)snapshot.child(postKey).getChildrenCount();
                    //setting resource to likeButton ImageView
                    likeButton.setImageResource(R.drawable.baseline_like_24);
                    //setting count text to TextView
                    like_txt.setText(Integer.toString(likeCount));
                }else {
                    //getting Children Count
                    likeCount = (int)snapshot.child(postKey).getChildrenCount();
                    //setting resource to likeButton ImageView
                    likeButton.setImageResource(R.drawable.baseline_unlike_24);
                    //setting count text to TextView
                    like_txt.setText(Integer.toString(likeCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //calling interface Clicklistener and init obj
    private ViewHolder.Clicklistener mClickListener;


    //creating interface Clicklistener for both onClick and onLongClick event handler
    public interface Clicklistener{
        //abstract method for onItemClick and onItemLongClick
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    //creating another setOnClickListener() method and passing ViewHolder.Clicklistener clickListener obj
    public void setOnClickListener(ViewHolder.Clicklistener clickListener){
        //assigning clickListener to mClickListener
        mClickListener = clickListener;
    }

}
