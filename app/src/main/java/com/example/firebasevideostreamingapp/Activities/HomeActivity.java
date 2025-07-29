package com.example.firebasevideostreamingapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.firebasevideostreamingapp.Fragment.AccountFragment;
import com.example.firebasevideostreamingapp.Fragment.HomeFragment;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    //ViewBinding class
    ActivityHomeBinding binding;

    Float translationYaxis = 100f;
    boolean isMenuOpen = false;
    private static final String TAG = "HOME_ACTIVITY";
    OvershootInterpolator interpolator = new OvershootInterpolator();

    //instance of FirebaseAuth
    private FirebaseAuth firebaseAuth;

    //Unique code for Activity Result
    private static final int PICK_VIDEO = 101;

    //Uri for storing Video to Firebase
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //activity_main.xml = ActivityMainBinding
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        //Attaching Layout root
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //get instance of the firebase auth for firebase auth related task
        firebaseAuth=FirebaseAuth.getInstance();

        //if the current User in equal to null, then start Register User Activity
        if (firebaseAuth.getCurrentUser()==null){
            //user is not logged in, move to LoginActivity
            startLoginOption();
        }

        //by default (when app is open) show HomeFragment
        showHomeFragment();

        showMenu();

//        binding.sellFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                showMenu();
//              //  chooseVideo(v);
//
//            }
//        });

        binding.bottomNv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //get id of the menu item
                int itemId = item.getItemId();

                //if itemId is equal to  R.id.menu_home
                if(itemId== R.id.menu_home){
                    //home item clicked,show HomeFragment
                    showHomeFragment();
                    return true;

                }
                //if itemId is equal to R.id.menu_account
                else if (itemId == R.id.menu_account) {

                    //account item clicked, show AccountFragment
                    if (firebaseAuth.getCurrentUser()==null){
                        Toast.makeText(HomeActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        //and startLoginOption(); called this method
                        startLoginOption();
                        return false;
                    }else {
                        showAccountFragment();
                        return true;
                    }
                }else {
                    //else return false
                    return false;
                }
            }
        });


    }


    //onActivityResult for bringing the Result form user pick
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //if requestCode, resultCode, data and data.getData() every thing according to condition, then execute this block of code
//        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null){
//
//            try {
//                //saving Video Data to videoUri
//                videoUri = data.getData();
//                //setting video uri to videoView
//              //  binding.videoViewMain.setVideoURI(videoUri);
//
//                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
//                intent.setData(videoUri);
//                startActivity(intent);
//
//            }catch (Exception e){
//                Toast.makeText(this, "No File Selected: "+e.toString(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    //onClick method on chooseVideo Button
//    public void chooseVideo(View view) {
//        //Intent class for picking the Video form Storage
//        Intent intent = new Intent();
//        //setting intent type as a Video
//        intent.setType("video/*");
//        //Allow user to select particular kind of Data and return it
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        //calling startActivityForResult method to return Result of Activity
//        startActivityForResult(intent,PICK_VIDEO);
//    }


    private void showHomeFragment() {
        //change toolbar textview text/title to Home
        //  binding.toolbarTitleTv.setText("Home");

        //show fragment
        HomeFragment homeFragment=new HomeFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),homeFragment,"HomeFragment");
        fragmentTransaction.commit();
    }

    private void showAccountFragment(){
        //change toolbar textview text/title to Account
        //  binding.toolbarTitleTv.setText("Account");

        //show fragment
        AccountFragment accountsFragment=new AccountFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),accountsFragment,"AccountFragment");
        fragmentTransaction.commit();
    }

    //Method for if user is Register or not, if not, then jumped to RegisterEmailActivity
    private void startLoginOption(){
        //HomeActivity to RegisterEmailActivity
        startActivity(new Intent(HomeActivity.this, EmailLoginActivity.class));
    }


    private void showMenu() {

        binding.cameraFab.setAlpha(0f);
        binding.fileFab.setAlpha(0f);

        binding.cameraFab.setTranslationY(translationYaxis);
        binding.fileFab.setTranslationY(translationYaxis);

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMenuOpen){
                    closeMenu();
                }else {
                    openMenu();
                }
            }
        });


        binding.cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromCamera();
                Toast.makeText(HomeActivity.this, "Camera Clicked", Toast.LENGTH_SHORT).show();
                closeMenu();
            }
        });

        binding.fileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromGallery();
                Toast.makeText(HomeActivity.this, "File Storage Clicked", Toast.LENGTH_SHORT).show();
                closeMenu();
            }
        });

    }

    private void openMenu() {

        isMenuOpen = !isMenuOpen;
        binding.sellFab.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
        binding.fileFab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.cameraFab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void closeMenu() {

        isMenuOpen = !isMenuOpen;
        binding.sellFab.setImageResource(R.drawable.round_add_24);
        binding.cameraFab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fileFab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();

    }




    public void getFromCamera(){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            //Device version is TIRAMISU or above. We only need Camera Permission
            requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA});
        }else {
            //Device version is below TIRAMISU. We need Camera and Storage Permission
            requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }


    private ActivityResultLauncher<String[]> requestCameraPermission= registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    Log.d(TAG, "onActivityResult: "+result.toString());

                    //Let's check if permission granted or not
                    boolean areAllGranted=true;
                    for (Boolean isGranted: result.values()){
                        areAllGranted =areAllGranted && isGranted;
                    }

                    if (areAllGranted){
                        //Camera or Storage or both permission granted, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g Camera, Storage");
                        getVideoFromCamera();
                    }else {
                        //Camera or Storage or both permission denied, can not camera to capture image
                        Log.d(TAG, "onActivityResult: All or Either one is denied");
                        Toast.makeText(HomeActivity.this, "Both Camera and Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    private void getVideoFromCamera(){
        Log.d(TAG, "pickImageCamera: ");

        //setup Content Values,MediaStore to capture high quality image using Camera Intent
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION");

        videoUri=getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,contentValues);

        //Intent to launch Camera
        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,videoUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image capture or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as asinged in PickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Capture " + videoUri);


                            try {
                                //saving Video Data to videoUri
                               // videoUri = data.getData();
                                //setting video uri to videoView
                                //  binding.videoViewMain.setVideoURI(videoUri);

                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                intent.setData(videoUri);
                                startActivity(intent);

                            }catch (Exception e){
                                Toast.makeText(HomeActivity.this, "No File Selected: "+e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        Log.e(TAG, String.valueOf(videoUri));
                    }
                }
            }
    );


    public void getFromGallery(){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            getVideoFromGallery();
        }else {
            requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


    private ActivityResultLauncher<String> requestsStoragePermission=registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: "+isGranted);

                    //Let's check if permission is granted or not
                    if (isGranted){
                        //  Storage Permission granted, we can now launch Gallery to pick Image
                        getVideoFromGallery();
                    }else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(HomeActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private void getVideoFromGallery(){
        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent=new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("video/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check, if image is picked or not
                    if (result.getResultCode()==Activity.RESULT_OK){
                        //get data
                        Intent data=result.getData();
                        //get Uri of image picked
                        videoUri=data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked from Gallery "+videoUri);

                        try {
                            //saving Video Data to videoUri
                            // videoUri = data.getData();
                            //setting video uri to videoView
                            //  binding.videoViewMain.setVideoURI(videoUri);

                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                            intent.setData(videoUri);
                            startActivity(intent);

                        }catch (Exception e){
                            Toast.makeText(HomeActivity.this, "No File Selected: "+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        Log.e(TAG, "onActivityResult: "+videoUri );


                    }else {
                        //Canceled
                        Toast.makeText(HomeActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
}