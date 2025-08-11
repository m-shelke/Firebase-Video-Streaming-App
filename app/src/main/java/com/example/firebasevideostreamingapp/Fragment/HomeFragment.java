package com.example.firebasevideostreamingapp.Fragment;

import static androidx.core.content.PermissionChecker.checkCallingOrSelfPermission;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.firebasevideostreamingapp.Activities.CommentActivity;
import com.example.firebasevideostreamingapp.Activities.EmailLoginActivity;
import com.example.firebasevideostreamingapp.Activities.FullScreenActivity;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.Model.Video;
import com.example.firebasevideostreamingapp.ViewHolder.VideoViewHolder;
import com.example.firebasevideostreamingapp.databinding.FragmentHomeBinding;
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

public class HomeFragment extends Fragment {

    //Storage Runtime permission
    private static final int STORAGE_PER = 100;

    //declare DatabaseReference
    DatabaseReference reference, lkeReference,viewsReference;

    //declare FirebaseDatabase
    FirebaseDatabase firebaseDatabase;

    //checking is like or not to video
    boolean likeChecker = false;

    //name String for getting and setting Recycler Item and url String for passing click item url to FullScreenActivity
    String name, url, downloadUrl;

    //View Binding
    private FragmentHomeBinding binding;

    //Context for this fragment class
    private Context mContext;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);

        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(mContext));

        //init FirebaseDatabase
        firebaseDatabase = FirebaseDatabase.getInstance();

        //getting reference of the Video and Likes path of Firebase Database
        reference = firebaseDatabase.getReference("Video");
        lkeReference = firebaseDatabase.getReference("Likes");


        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                firebaseSearch(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseSearch(s.toString());
            }
        });


        //init FirebaseUser and FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(mContext, EmailLoginActivity.class));
        }

        if (user != null) {

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
                    holder.setSimpleExoPlayer((Application) mContext.getApplicationContext(), model.getName(), model.getDescription(), model.getVideouri());

                    holder.setVideoSizeTv(model.getVideouri());

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
                            Intent intent = new Intent(mContext, FullScreenActivity.class);

                            //sending name to next Activity
                            intent.putExtra("name", name);

                            //sending url to next Activity
                            intent.putExtra("url", url);

                            //sending postKey of video to FullScreenActivity
                            intent.putExtra("postKey",postKey);

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

                            int sdkInt = Build.VERSION.SDK_INT;

                            if (sdkInt < Build.VERSION_CODES.Q) {
                                //for Android 9 and below (API < 29)

                                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PER);
                                } else {

                                    //getting downloadUrl form DataModel class
                                    downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                                    //calling  startDownloading(downloadUrl); method
                                    startDownloading(downloadUrl);

                                    String sizeOfVideo = String.valueOf(downloadUrl.length());
                                    Log.e("VIDEOSIZE", "onClick: " + sizeOfVideo);

                                    holder.videoSizeTv.setText(sizeOfVideo);
                                }
                            } else if (sdkInt == Build.VERSION_CODES.Q) {
                                //For Android 10 (API 29)
                                //requestLegacyExternalStorage should be set in AndroidManifest.file

                                //getting downloadUrl form DataModel class
                                downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                                //calling  startDownloading(downloadUrl); method
                                startDownloading(downloadUrl);

                                String sizeOfVideo = String.valueOf(downloadUrl.length());
                                Log.e("VIDEOSIZE", "onClick: " + sizeOfVideo);

                                holder.videoSizeTv.setText(sizeOfVideo);

                            } else {
                                if (Environment.isExternalStorageManager()) {
                                    //getting downloadUrl form DataModel class
                                    downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                                    //calling  startDownloading(downloadUrl); method
                                    startDownloading(downloadUrl);

                                } else {
                                    //ask usr to allow MANAGE_EXTERNAL permission manually
                                    try {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                        intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                                        startActivity(intent);
                                    } catch (Exception e) {

                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                        startActivity(intent);
                                    }
                                }
                            }


                            //if Build.VERSION.SDK_INT is greater and equal to  Build.VERSION_CODES.M
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                //if PackageManager.PERMISSION_DENIED
                                if (checkCallingOrSelfPermission(mContext.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
                                    String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

                                    //RequestPermissions
                                    requestPermissions(new String[]{permission}, STORAGE_PER);
                                } else {
                                    //getting downloadUrl form DataModel class
                                    downloadUrl = getItem(holder.getAdapterPosition()).getVideouri();

                                    //calling  startDownloading(downloadUrl); method
                                    startDownloading(downloadUrl);

                                    String sizeOfVideo = String.valueOf(downloadUrl.length());
                                    Log.e("VIDEOSIZE", "onClick: " + sizeOfVideo);

                                    holder.videoSizeTv.setText(sizeOfVideo);
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
                            Intent intent = new Intent(mContext, CommentActivity.class);
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
                                    Toast.makeText(mContext, "Error while Like Video: " + error.toString(), Toast.LENGTH_SHORT).show();
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


            //finally setting firebaseRecyclerAdapter to XML RecyclerView
            binding.recyclerview.setAdapter(firebaseRecyclerAdapter);

            //start Listening firebaseRecyclerAdapter
            firebaseRecyclerAdapter.startListening();
        } else {
            binding.goneCardView.setVisibility(View.VISIBLE);
            binding.searchCv.setVisibility(View.INVISIBLE);
        }
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
                holder.setSimpleExoPlayer((Application) mContext.getApplicationContext(), model.getName(), model.getDescription(), model.getVideouri());

                //Overriding onItemClick and onItemLongClick abstract method
                holder.setOnClickListener(new VideoViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //getting name item clicked
                        name = getItem(position).getName();
                        //getting uri item clicked
                        url = getItem(position).getVideouri();

                        //calling Intent class and providing root Destination and Target Destination
                        Intent intent = new Intent(mContext, FullScreenActivity.class);
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
        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //Enqueue a new download. The download will start automatically once the download manager is ready to execute it and connectivity is available
        manager.enqueue(request);
    }


    //creating method for showing Delete dialog
    private void showDeleteDialog(String name) {

        //AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

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
                        Toast.makeText(mContext, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //and if Remove Value not get success, Show error Toast Message
                        Toast.makeText(mContext, "Video Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    //checking Request Permissions Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PER) {

            //if grantResults.length > 0 and grantResults[0] == PackageManager.PERMISSION_GRANTED is also 0
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        startDownloading(downloadUrl); // All files access granted

                        //start downloading Video, we call this  startDownloading(downloadUrl); method
                        //  startDownloading(downloadUrl);
                    } else {
                        //and if the Permission is denied, then showing Toast message
                        Toast.makeText(mContext, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}





