package com.example.firebasevideostreamingapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firebasevideostreamingapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    //inti Binding
    ActivityMainBinding binding;

    //Unique code for Activity Result
    private static final int PICK_VIDEO = 100;
    //Uri for storing Video to Firebase
    private Uri videoUri;
    //MediaController for Video controls
    MediaController mediaController;

    // Firebase Storage Reference
    StorageReference storageReference;
    //Firebase Realtime Database reference
    DatabaseReference databaseReference;
    //Data Model class
    Video video;
    //for upload operation
    UploadTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //getting XML Layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setting XML root to MainActivityBinding
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init Data Model class
        video = new Video();
        //init Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("Video");
        //init Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Video");

        //init MediaController class
        mediaController = new MediaController(MainActivity.this);
        //setting mediaController to VideoView
        binding.videoViewMain.setMediaController(mediaController);
        //starting VideoView
        binding.videoViewMain.start();



    }

    //onActivityResult for bringing the Result form user pick
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if requestCode, resultCode, data and data.getData() every thing according to condition, then execute this block of code
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null){

            //saving Video Data to videoUri
            videoUri = data.getData();

            //setting video uri to videoView
            binding.videoViewMain.setVideoURI(videoUri);
        }
    }

    //onClick method on chooseVideo Button
    public void chooseVideo(View view) {
        //Intent class for picking the Video form Storage
        Intent intent = new Intent();
        //setting intent type as a Video
        intent.setType("video/*");
        //Allow user to select particular kind of Data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //calling startActivityForResult method to return Result of Activity
        startActivityForResult(intent,PICK_VIDEO);
    }

    //onClick method on showVideo Button
    public void showVideo(View view) {

    }

    //uploading video, when Button get clicked
    public void uploadVideo(View view) {
        //Getting video name from EditText
        String videoName = binding.videoNameEt.getText().toString();
        //getting video name edit text in search variable
        String search = binding.videoNameEt.getText().toString();

        //if both vidoeUri and videoName is not null, then
        if (videoUri != null && !TextUtils.isEmpty(videoName)){
            //Visible progressBar, while uploading
            binding.progressBar.setVisibility(View.VISIBLE);

            //storing video in StorageReference "Video" reference but reference name is current Time in Milli second and specify extension as videoUri
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExtension(videoUri));
            //and finaly putting file in Storage
            uploadTask = reference.putFile(videoUri);

            //Retrieving uri of Uploaded or saved video
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    //if task is not Successful, then throw Exception
                    if (!task.isSuccessful()){
                        //throw Exception
                        throw task.getException();
                    }

                    //returning Download Url of uploaded video
                   return reference.getDownloadUrl();
                }
            })
                    //addOnCompleteListener to listen for event trigger
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            //if task is successful
                            if (task.isSuccessful()){
                                //getting downLoadUrl
                                Uri downLoadUrl = task.getResult();

                                //Hiding ProgressBar
                                binding.progressBar.setVisibility(View.INVISIBLE);

                                //showing Toast message, when video uploaded successfully
                                Toast.makeText(MainActivity.this, "Video Saved", Toast.LENGTH_SHORT).show();

                                //giving videoName, search and downLoadUrl to Data Model class i.e Video
                                video.setName(videoName);
                                video.setSearch(search.toLowerCase());
                                video.setVideouri(downLoadUrl.toString());

                                //Generating random key
                                String i = databaseReference.push().getKey();
                                //storing all Data Model data to Firebase Realtime Database
                                databaseReference.child(i).setValue(video);
                            }else {
                                Toast.makeText(MainActivity.this, "Video Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else {
            Toast.makeText(this, "Name Is Missing..!!", Toast.LENGTH_SHORT).show();
        }
    }

    //Method for getting and may be setting Extension for Video
    private String getExtension(Uri uri){
        //In Android, the ContentResolver is a class that provides access to content providers.
        ContentResolver contentResolver = getContentResolver();
        //In Android, MimeTypeMap is a class in the Android SDK that helps you map file extensions to MIME types
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        //return the register MIME for given resource
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


}