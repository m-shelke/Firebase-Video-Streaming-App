package com.example.firebasevideostreamingapp;

import static io.github.glailton.expandabletextview.ExpandableTextViewKt.EXPAND_TYPE_LAYOUT;

import android.app.Application;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Collections;

import io.github.glailton.expandabletextview.ExpandableTextView;

//VideoViewHolder class for getting the References of Layout File
public class VideoViewHolder extends RecyclerView.ViewHolder {

    //Layout Variable
    SimpleExoPlayer simpleExoPlayer;

    //PlayerView instance
    PlayerView playerView;

    //XML ImageButton, TextView and LinearLayout variable declaration
    public ImageButton likeButton,commentImg,downloadBtn;
    TextView like_txt,commentCountTv;
    public LinearLayout commentBoxclick;

    //likeCount variable for integer value count
    int likeCount,commentCount;

    //Database instance
    DatabaseReference reference,postReference;

    //Matching super constructor
    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);

        //finding XML view
        downloadBtn = itemView.findViewById(R.id.item_download);

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
    public void setSimpleExoPlayer(Application application,String name, String description, String videouri){

        //Finding XML Layout ID's
        TextView textView = itemView.findViewById(R.id.item_name);
        ExpandableTextView expandableTextView = itemView.findViewById(R.id.item_ExpandableTv);
        playerView = itemView.findViewById(R.id.item_exoPlayer);
        commentImg = itemView.findViewById(R.id.item_comment);
        commentCountTv = itemView.findViewById(R.id.commentCountTv);

        //setting name of Video to TextView
        textView.setText(name);

        //Expandable TextView coding part
       expandableTextView
                .setAnimationDuration(500)
                .setReadMoreText("View More")
                .setReadLessText("View Less")
                .setCollapsedLines(3)
                .setIsExpanded(false)
                .setIsUnderlined(true)
                .setExpandType(EXPAND_TYPE_LAYOUT)
                .setEllipsizedTextColor(ContextCompat.getColor(application, R.color.blue));

       //and setting Description to Expandable TextView
       expandableTextView.setText(description);



//        //Building ExoPlayer
//        exoPlayer = new ExoPlayer.Builder(application).build();
//        //setting Player to ExoPlayer PlayView
//        playerView.setPlayer(exoPlayer);
//
//        //parsing Uri
//        Uri uri = Uri.parse(videouri);
//        //creating obj of MediaItem class and providing resource id i.e uri
//        MediaItem mediaItem = MediaItem.fromUri(uri);
//
//        //setting media item to Exoplayer
//        exoPlayer.setMediaItem(mediaItem);
//        //preparing ExoPlayer now
//        exoPlayer.prepare();
//        //by default, when video is ready to play, we don't play it automatically or not play when video ready play
//        exoPlayer.setPlayWhenReady(false);

        //Building SimpleExoPlayer
        simpleExoPlayer = new SimpleExoPlayer.Builder(application).build();

        //setting Player to ExoPlayer PlayView
        playerView.setPlayer(simpleExoPlayer);

        //creating obj of MediaItem class and providing resource id i.e uri
        MediaItem mediaItem = MediaItem.fromUri(videouri);

        //setting media item to Exoplayer
        simpleExoPlayer.addMediaItems(Collections.singletonList(mediaItem));

        //preparing SimpleExoPlayer
        simpleExoPlayer.prepare();

        //by default, when video is ready to play, we don't play it automatically or not play when video ready play
        simpleExoPlayer.setPlayWhenReady(false);





        //This code is deprecate now, because we used updated dependencies
        /*try {
            //provides estimates of the currently available Bandwidth
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(application).build();

          TrackSelector trackSelector = new DefaultTrackSelector((Context) application, (ExoTrackSelection.Factory) bandwidthMeter);

            // SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);

            Uri video = Uri.parse(videouri);
            DefaultHttpDataSource dataSourceFactory = new DefaultHttpDataSource("video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

    }



    //creating setLikeButtonStatus() for checking video like status
    public void setLikeButtonStatus(final String postKey) {

        //finding View id and references
        likeButton = itemView.findViewById(R.id.like_button);
        like_txt = itemView.findViewById(R.id.like_txt);

        //getting instance of Firebase Database and creating "Likes" path Json in Database
        reference = FirebaseDatabase.getInstance().getReference("Likes");

        //Firebase Database Reference path of "Comments"
        postReference = FirebaseDatabase.getInstance().getReference().child("Video").child(postKey).child("Comments");

        //getting instance of FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //getting UID of Current user
        String userId = user.getUid();

        //adding Value EventListener reference path of Database
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    //getting Children Count
                    commentCount = (int) snapshot.getChildrenCount();
                    //and set it's to
                    commentCountTv.setText(Integer.toString(commentCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

    //creating this Function to set comments to RecyclerView
    public void  setComments(Application application,  String comment, String date, String time, String userName){

        //finding View id and references
        TextView item_comment_dateTime = itemView.findViewById(R.id.item_comment_dateTime);
        TextView item_comment_lorem = itemView.findViewById(R.id.item_comment_lorem);
        commentBoxclick = itemView.findViewById(R.id.commentBoxClick);

        //setting Date and Time text to item_comment_dateTime
        item_comment_dateTime.setText("Date: "+date+" Time: "+time);

        //setting Author name text to item_comment_lorem
        item_comment_lorem.setText("Author: "+userName+"-"+"\n  "+comment);
    }

    //calling interface Clicklistener and init obj
    private VideoViewHolder.Clicklistener mClickListener;


    //creating interface Clicklistener for both onClick and onLongClick event handler
    public interface Clicklistener{

        //abstract method for onItemClick and onItemLongClick
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    //creating another setOnClickListener() method and passing VideoViewHolder.Clicklistener clickListener obj
    public void setOnClickListener(VideoViewHolder.Clicklistener clickListener){

        //assigning clickListener to mClickListener
        mClickListener = clickListener;
    }

}
