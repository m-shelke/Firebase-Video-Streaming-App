package com.example.firebasevideostreamingapp.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.firebasevideostreamingapp.Activities.ChangePasswordActivity;
import com.example.firebasevideostreamingapp.Activities.DeleteAccountActivity;
import com.example.firebasevideostreamingapp.Activities.EmailLoginActivity;
import com.example.firebasevideostreamingapp.Activities.ProfileEditActivity;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.databinding.FragmentAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    //Firebase Database reference

    DatabaseReference reference;
    //obj of Video DataModel class

    private FragmentAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private ProgressDialog progressDialog;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init Context for this Fragment class
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //init/setup ProgressDialog to while Account Verification
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        //get instance of the firebase auth for Auth related task
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        // User is signed in
        if (user != null) {

            //creating "UserName" reference in Firebase Database
            reference = FirebaseDatabase.getInstance().getReference().child("VideoUsers");

            loadMyInfo();
        } else {
            // No user is signed in
        }






//        reference.child(userId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        String profileImageUrl = "" + snapshot.child("image").getValue();
//                        Log.e("profileImage", profileImageUrl);
//
//                        String name = "" + snapshot.child("name").getValue();
//                        binding.userNameTV.setText(name);
//
//                        Glide.with(mContext).load(profileImageUrl).placeholder(R.drawable.baseline_more_horiz_24).into(binding.userProfileIv);
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        //saving data that user enter to Firebase Realtime Database
//        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String name = binding.nameEt.getText().toString();
//
//                //if nameEt is empty
//                if (name.isEmpty()){
//                    //showing Toast Message
//                    Toast.makeText(mContext, "Name is Empty", Toast.LENGTH_SHORT).show();
//                }else {
//                    //if nameEt is not empty and nameEt value to DataModel class
//                    video.setUserName(name);
//                    //and after to Firebase Database
//                    reference.child(userId).setValue(video);
//                }
//            }
//        });

        binding.userNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.userEmailTv.setVisibility(View.VISIBLE);
            }
        });

        binding.userEmailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.userEmailTv.setVisibility(View.GONE);
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
                requireActivity().finishAffinity();
            }
        });


        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });


        binding.verifyAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });


        //Handle changepasswordBtn click, start ChangePasswordActivity
        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
            }
        });

     //  handling referredBtn button event
        binding.refferedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,"https://github.com/m-shelke/Firebase-Video-Streaming-App/releases/tag/Streamy-1.0.1");
                startActivity(Intent.createChooser(intent,"Choose Sharing Option: "));
            }
        });

        //Handle deleteAccountBtn click, Start DeleteAccountActivity
        binding.deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
                //getActivity().finish();
            }
        });
    }


    //Retrieving Name of User from Firebase Database
//    @Override
//    public void onStart() {
//
//        loadMyInfo();
//
//        super.onStart();
//    }

    private void loadMyInfo() {

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

            //    Profile profile = snapshot.getValue(Profile.class);

                  String profileImg=""+snapshot.child("imageUrl").getValue();

                //getting "userName" value form Firebase Database
                  String userName = (String) snapshot.child("name").getValue();
                  String userEmail = (String) snapshot.child("email").getValue();

                //and set it to binding.userName and userEmail
                binding.userNameTV.setText(userName);
                binding.userEmailTv.setText(userEmail);

                try {

                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);


                    Glide.with(mContext)
                            .load(profileImg)
                            .apply(requestOptions)
                            .placeholder(R.drawable.baseline_more_horiz_24)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Load failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(binding.userProfileIv);


                } catch (Exception e) {
                    Toast.makeText(mContext, "Image Fetching Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Data Fetching Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


//       FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//
//
//
//        //User type is Email, have to check if verified or not
//        boolean isVerified= firebaseUser.isEmailVerified();
//
//
//        if (isVerified){
//            //verified, hide the Verify Account Option
////            binding.verifyAccountBtn.setVisibility(View.GONE);
////            binding.verificationIv.setVisibility(View.VISIBLE);
//
//            binding.demo.setText("Verified");
//        }else {
//            //not verified, show the Verify Account Option
////            binding.verifyAccountBtn.setVisibility(View.VISIBLE);
////            binding.verificationIv.setVisibility(View.GONE);
//
//            binding.demo.setText("Not Verified");
//        }

        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                binding.verificationIv.setVisibility(View.VISIBLE);
                binding.verifyAccountBtn.setVisibility(View.GONE);
            } else {
                binding.verificationIv.setVisibility(View.GONE);
                binding.verifyAccountBtn.setVisibility(View.VISIBLE);
            }
        });

    }


    private void verifyAccount() {
        Log.d(TAG, "verifyAccount: ");
        //show progressDialog
        progressDialog.setMessage("Sending Account Verification Instruction To Your Email ");
        progressDialog.show();
        //send account/email verification instruction to the registered email
        Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //instruction send, check email, sometimes it goes to the spam folder so if not into inbox please check the spam folder
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Account Verification Mail Send To Your Email ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send instruction
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Account Verification Mail Failed; " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}