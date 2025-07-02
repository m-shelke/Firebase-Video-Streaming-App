package com.example.firebasevideostreamingapp.NewUI;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.firebasevideostreamingapp.MainActivity;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.RegisterEmailActivity;
import com.example.firebasevideostreamingapp.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    //ViewBinding class
    ActivityHomeBinding binding;

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

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseVideo(v);

            }
        });

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if requestCode, resultCode, data and data.getData() every thing according to condition, then execute this block of code
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null){

            try {
                //saving Video Data to videoUri
                videoUri = data.getData();
                //setting video uri to videoView
              //  binding.videoViewMain.setVideoURI(videoUri);

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setData(videoUri);
                startActivity(intent);

            }catch (Exception e){
                Toast.makeText(this, "No File Selected: "+e.toString(), Toast.LENGTH_SHORT).show();
            }
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

}