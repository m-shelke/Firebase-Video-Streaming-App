package com.example.firebasevideostreamingapp.NewUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.firebasevideostreamingapp.ProfileActivity;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.RegisterEmailActivity;
import com.example.firebasevideostreamingapp.Video;
import com.example.firebasevideostreamingapp.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private Context mContext;

    //Firebase Database reference
    DatabaseReference reference;
    //obj of Video DataModel class
    Video video;

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init Context for this Fragment class
        mContext=context;
        super.onAttach(context);
    }

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentAccountBinding.inflate(LayoutInflater.from(mContext),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get instance of the firebase auth for Auth related task
        firebaseAuth=FirebaseAuth.getInstance();


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
                    Toast.makeText(mContext, "Name is Empty", Toast.LENGTH_SHORT).show();
                }else {
                    //if nameEt is not empty and nameEt value to DataModel class
                    video.setUserName(name);
                    //and after to Firebase Database
                    reference.child(userId).setValue(video);
                }
            }
        });


        //handle logoutBtn click, logout user and start MainActivity
        binding.logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //logout user
                firebaseAuth.signOut();

                //start MainActivity
                startActivity(new Intent(mContext, EmailLoginActivity.class));
                getActivity().finishAffinity();
            }
        });
    }


    //Retrieving Name of User from Firebase Database
    @Override
    public void onStart() {

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
                    Toast.makeText(mContext, "No userName Found ", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        super.onStart();
    }
}