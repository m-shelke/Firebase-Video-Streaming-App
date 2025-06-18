package com.example.firebasevideostreamingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firebasevideostreamingapp.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    //ViewBinding Class
    ActivityProfileBinding binding;

    //Firebase Database reference
    DatabaseReference reference;
    //obj of Video DataModel class
    Video video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //inflating Layout with java class
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        //getting root if XML file
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init FirebaseAuth and FirebaseUser
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //value shout not be null, for that it's required
        assert user != null;
        //getting current UID in Variable
        String userId = user.getUid();

        //instance of Video DataModel class
        video = new Video();

        //creating "UserName" reference in Firebase Database
        reference = FirebaseDatabase.getInstance().getReference().child("UserName");

        //saving data that user enter to Firebase Realtime Database
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEt.getText().toString();

                //if nameEt is empty
                if (name.isEmpty()){
                    //showing Toast Message
                    Toast.makeText(ProfileActivity.this, "Name is Empty", Toast.LENGTH_SHORT).show();
                }else {
                    //if nameEt is not empty and nameEt value to DataModel class
                    video.setUserName(name);
                    //and after to Firebase Database
                    reference.child(userId).setValue(video);
                }
            }
        });

    }

    //Retrieving Name of User from Firebase Database
    @Override
    protected void onStart() {

        //init FirebaseAuth and FirebaseUser
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //value shout not be null, for that it's required
        assert user != null;
        //getting current UID in Variable
        String userId = user.getUid();

        //Adding ValueEventListener on "UserName" path reference to listen
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //if current data exit at this location
                if (snapshot.exists()){
                    //getting "userName" value form Firebase Database
                    String userName = snapshot.child("userName").getValue().toString();
                    //and set it to binding.userName
                    binding.userName.setText(userName);
                }else {
                    // and if "userName" path reference is empty, then showing Toast message
                    Toast.makeText(ProfileActivity.this, "No userName Found ", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        super.onStart();
    }
}