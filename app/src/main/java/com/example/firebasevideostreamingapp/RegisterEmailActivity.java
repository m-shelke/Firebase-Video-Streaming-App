package com.example.firebasevideostreamingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebasevideostreamingapp.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_TAG";
    //ProgressDialog to show while sign-up
    ProgressDialog progressDialog;
    String email, password, cPassword;
    private ActivityRegisterEmailBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_register_email.xml=ActivityRegisterEmailBinding
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get instance of firebase auth for Auth related task
        firebaseAuth = FirebaseAuth.getInstance();

        //initiated/set ProgressDialog while sign-up
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle toolbarBackBtn, go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //handle haveAccountTv click, go back to LoginEmailActivity
        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle registerBtn click, start user registration
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private void validateData() {

        //Input data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();

        Log.d(TAG, "ValidateData: email: " + email);
        Log.d(TAG, "ValidateDAta: Password: " + password);
        Log.d(TAG, "ValidateData: Confirm Password: " + cPassword);

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            //email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email Pattern..");
            binding.emailEt.requestFocus();

        } else if (password.isEmpty()) {

            //password is not enter, show error
            binding.passwordEt.setError("Enter Paasword..");
            binding.passwordEt.requestFocus();

        } else if (!password.equals(cPassword)) {

            //password and confirm password is not match, show error
            binding.cPasswordEt.setError("Password Doesn't Match..");
            binding.cPasswordEt.requestFocus();

        } else {

            //all data is valid, start sign-up
            registerUser();
        }
    }

    private void registerUser() {

        //show progress
        progressDialog.setMessage("Creating Account..");
        progressDialog.show();

        //start user sign-up
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //user registration success, We also need to save user info to firebase database
                        Log.d(TAG, "onSuccess: Register Success..");
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //user registration failed
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(RegisterEmailActivity.this, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateUserInfo() {

        //change progress dialog message
        progressDialog.setMessage("Saving user Info..");

        //get current timestamp e.g show user registration date/time

        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        //getting uid of the Registered user to save in Realtime database
        String registerUserId = firebaseAuth.getUid();

        //setup data to save in firebase realtime db. most of the data will be empthy and will set in edit profile
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserId);

        //set data to firebase db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VideoUsers");
        reference.child(registerUserId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //firebase db save success
                        Log.d(TAG, "onSuccess: Info saved...");
                        progressDialog.dismiss();


                        startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //firebase db save failed
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        progressDialog.dismiss();

                        Toast.makeText(RegisterEmailActivity.this, "Failed to Save Info due to " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

}