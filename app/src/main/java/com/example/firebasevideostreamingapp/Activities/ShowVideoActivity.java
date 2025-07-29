package com.example.firebasevideostreamingapp.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.firebasevideostreamingapp.Model.Video;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.ViewHolder.VideoViewHolder;
import com.example.firebasevideostreamingapp.databinding.ActivityShowVideoBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ShowVideoActivity extends AppCompatActivity {

    //Storage Runtime permission
    private static final int STORAGE_PER = 100;
    //enabling ViewBinding
    ActivityShowVideoBinding binding;
    //declare DatabaseReference
    DatabaseReference reference, lkeReference;
    //declare FirebaseDatabase
    FirebaseDatabase firebaseDatabase;
    //checking is like or not to video
    boolean likeChecker = false;
    //name String for getting and setting Recycler Item and url String for passing click item url to FullScreenActivity
    String name, url, downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //getting XML Layout
        binding = ActivityShowVideoBinding.inflate(getLayoutInflater());
        //setting XML root to MainActivityBinding
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init and set ActionBar Title Text
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        //init FirebaseDatabase
        firebaseDatabase = FirebaseDatabase.getInstance();
        //getting reference of the Video path
        reference = firebaseDatabase.getReference("Video");
        lkeReference = firebaseDatabase.getReference("Likes");

    }

    //creating method for performing Firebase Search Operation on Firebase Database
    private void firebaseSearch(String searchTxt) {

        //getting search query in lower case
        String query = searchTxt.toLowerCase();

        //getting Firebase Query to Search Operation
        Query firebaseQuery = reference.orderByChild("search").startAt(query).endAt(query + "\uf8ff");

        //FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Video> options =
                new FirebaseRecyclerOptions.Builder<Video>()
                        .setQuery(firebaseQuery, Video.class)
                        .build();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Video, VideoViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, VideoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull Video model) {
                //calling setSimpleExoPlayer() method of VideoViewHolder class and passing req. argument
                holder.setSimpleExoPlayer(getApplication(), model.getName(), model.getDescription(), model.getVideouri());

                //Overriding onItemClick and onItemLongClick abstract method
                holder.setOnClickListener(new VideoViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //getting name item clicked
                        name = getItem(position).getName();
                        //getting uri item clicked
                        url = getItem(position).getVideouri();

                        //calling Intent class and providing root Destination and Target Destination
                        Intent intent = new Intent(ShowVideoActivity.this, FullScreenActivity.class);
                        //sending name to next Activity
                        intent.putExtra("name", name);
                        //sending url to next Activity
                        intent.putExtra("url", url);

                        //finally start Activity
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //getting name item, that want to Delete
                        name = getItem(position).getName();
                        //creating method for Deleting RecyclerView item
                        showDeleteDialog(name);
                    }
                });
            }

            //onCreateViewHolder
            @NonNull
            @Override
            public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating R.layout.item_video
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
                //returning VideoViewHolder(view); with view
                return new VideoViewHolder(view);
            }
        };

        //start Listening firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening();
        //finally setting firebaseRecyclerAdapter to XML RecyclerView
        binding.recyclerview.setAdapter(firebaseRecyclerAdapter);
    }


    //override onStart method
    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Video> options =
                new FirebaseRecyclerOptions.Builder<Video>()
                        .setQuery(reference, Video.class)
                        .build();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Video, VideoViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, VideoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull Video model) {

                //calling setSimpleExoPlayer() method of VideoViewHolder class and passing req. argument
                holder.setSimpleExoPlayer(getApplication(), model.getName(), model.getDescription(), model.getVideouri());

                //init FirebaseUser and FirebaseAuth
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //getting current Firebase user id (Generating via Email_Authentication)
                String currentUserId = user.getUid();
                //getting the post video key
                final String postKey = getRef(position).getKey();

                //Overriding onItemClick and onItemLongClick abstract method
                holder.setOnClickListener(new VideoViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //getting name item clicked
                        name = getItem(position).getName();

                        //getting uri item clicked
                        url = getItem(position).getVideouri();

                        //calling Intent class and providing root Destination and Target Destination
                        Intent intent = new Intent(ShowVideoActivity.this, FullScreenActivity.class);
                        //sending name to next Activity
                        intent.putExtra("name", name);
                        //sending url to next Activity
                        intent.putExtra("url", url);

                        //finally start Activity
                        startActivity(intent);

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //getting name item, that want to Delete
                        name = getItem(position).getName();

                        //creating method for Deleting RecyclerView item
                        showDeleteDialog(name);
                    }
                });


                //setting ClickListener on download button
                holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //if Build.VERSION.SDK_INT is greater and equal to  Build.VERSION_CODES.M
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            //if PackageManager.PERMISSION_DENIED
                            if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

                                //RequestPermissions
                                requestPermissions(new String[]{permission}, STORAGE_PER);
                            } else {
                                //getting downloadUrl form DataModel class
                                downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                                //calling  startDownloading(downloadUrl); method
                                startDownloading(downloadUrl);
                            }
                        } else {
                            //getting downloadUrl form DataModel class
                            downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                            //calling  startDownloading(downloadUrl); method
                            startDownloading(downloadUrl);
                        }
                    }
                });

                //calling  holder.setLikeButtonStatus(postKey); method from VideoViewHolder class
                holder.setLikeButtonStatus(postKey);

                //setting onClicked event on commentImg ImageButton
                holder.commentImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //starting CommentActivity and passing video postKey to CommentActivity
                        Intent intent = new Intent(ShowVideoActivity.this, CommentActivity.class);
                        intent.putExtra("postKey", postKey);
                        startActivity(intent);
                    }
                });

                //setting onClick on like Button clicked / trigger
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //now  likeChecker =  true; by default it's false
                        likeChecker = true;

                        //setting Value EventListener lkeReference path of JSON file
                        lkeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //if user already like the Video
                                if (likeChecker == true) {
                                    if (snapshot.child(postKey).hasChild(currentUserId)) {
                                        lkeReference.child(postKey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    } else {
                                        //if it's first time, that user like the Video
                                        lkeReference.child(postKey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //Showing Toast message, when "Error while Like Video
                                Toast.makeText(ShowVideoActivity.this, "Error while Like Video: " + error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }

            //onCreateViewHolder
            @NonNull
            @Override
            public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating R.layout.item_video
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
                //returning VideoViewHolder(view); with view
                return new VideoViewHolder(view);
            }
        };

        //start Listening firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening();
        //finally setting firebaseRecyclerAdapter to XML RecyclerView
        binding.recyclerview.setAdapter(firebaseRecyclerAdapter);
    }

    //creating method for Downloading video from Firebase cloud storage
    private void startDownloading(String downloadUrl) {

        //instance of  DownloadManager.Request and providing request address as well
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //Allowing Video download over on Mobile Data and WiFi
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //setting Title of Download notification
        request.setTitle("Download");
        //setting Description of Download notification
        request.setDescription("Video File Downloading...");

        //allowing Scanning By MediaScanner
        request.allowScanningByMediaScanner();
        //after successfully downing is completed, notify user on Device Screen
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //path, where video is saved and on what name is saved, i.e System.currentTimeMillis
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis());

        //instance of DownloadManager, and getting System Service for DOWNLOAD_SERVICE
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //Enqueue a new download. The download will start automatically once the download manager is ready to execute it and connectivity is available
        manager.enqueue(request);
    }

    //checking Request Permissions Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PER) {//if grantResults.length > 0 and grantResults[0] == PackageManager.PERMISSION_GRANTED is also 0
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //start downloading Video, we call this  startDownloading(downloadUrl); method
                startDownloading(downloadUrl);
            } else {
                //and if the Permission is denied, then showing Toast message
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //creating method for showing Delete dialog
    private void showDeleteDialog(String name) {
        //AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowVideoActivity.this);
        //AlertDialog Title
        builder.setTitle("Delete");
        //AlertDialog Message
        builder.setMessage("Sure about Delete");

        //setting PositiveButton
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Firebase Database Query
                Query query = reference.orderByChild("name").equalTo(name);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //DataSnapshot
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //getting reference and removing entire path reference value
                            ds.getRef().removeValue();
                        }
                        //if the Remove Value get success, show Toast Message
                        Toast.makeText(ShowVideoActivity.this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //and if Remove Value not get success, Show error Toast Message
                        Toast.makeText(ShowVideoActivity.this, "Video Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        //setting NegativeButton
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog box here
                dialog.dismiss();
            }
        });

        //finally showing AlertDialog Builder
        builder.create();

        //or this is way of Creating Alert Builder
        AlertDialog alertDialog = builder.create();
        //show alertDialog here
        alertDialog.show();
    }
}