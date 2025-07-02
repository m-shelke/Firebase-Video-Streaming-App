package com.example.firebasevideostreamingapp.NewUI;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.databinding.ActivityForgotEmailPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotEmailPasswordActivity extends AppCompatActivity {

    //ViewBing
    private ActivityForgotEmailPasswordBinding binding;
    //TAG for logs in logcat
    private static final String TAG="FORGOT_PASS_TAG";
    //Firebase Auth for auth related task
    private FirebaseAuth firebaseAuth;
    //ProgressDialog to show while sending password recovery instruction
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating XML Layout file with java code and logic
        binding = ActivityForgotEmailPasswordBinding.inflate(getLayoutInflater());

        //Attaching Root of XML Layout file
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //ProgressDialog to show while sending password recovery instruction
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //get instance of FirebaseAuth to Auth related task
        firebaseAuth=FirebaseAuth.getInstance();

        //Handle submitBtn click, validate data to start password recovery
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });


    }

    private String email="";

    private void validateData() {
        Log.d(TAG, "validateData: ");

        //input data
        email=binding.emailEt.getText().toString().trim();

        Log.d(TAG, "validateData: Email: "+email);

        //Validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email patterns, show error in emailEt
            binding.emailEt.setError("Invalid Email Patterns..");
            binding.emailEt.requestFocus();
        }else {
            //Email patterns is valid, send password recovery instruction
            sendPasswordRecoveryInstructions();
        }

    }

    private void sendPasswordRecoveryInstructions() {

        Log.d(TAG, "sendPasswordRecoveryInstructions: ");

        //show progressDialog
        progressDialog.setMessage("Sending Password Recovery Instruction To "+email);
        progressDialog.show();


        //send password recovery instruction, pass the input email as parameter
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //instruction sent, check email. Sometimes it goes in the spam folder, so if not in inbox check the your spam folder
                        progressDialog.dismiss();
                        Toast.makeText(ForgotEmailPasswordActivity.this, "Password Reset Email Sent To: "+email, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to sent instruction
                        Log.e(TAG, "onFailure: ",e);
                        progressDialog.dismiss();

                        Toast.makeText(ForgotEmailPasswordActivity.this, "Failed To Sent Password Reset Email: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}