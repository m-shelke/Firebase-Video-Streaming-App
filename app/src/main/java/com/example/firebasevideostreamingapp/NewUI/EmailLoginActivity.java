package com.example.firebasevideostreamingapp.NewUI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.RegisterEmailActivity;
import com.example.firebasevideostreamingapp.databinding.ActivityEmailLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    //Enabling ViewBinding
    ActivityEmailLoginBinding binding;

    private static final String TAG="LOGIN_TAG";
    private ProgressDialog progressDialog;

    //Firebase Auth instance
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //inflating ActivityEmailLoginBinding Layout file with Java code
        binding = ActivityEmailLoginBinding.inflate(getLayoutInflater());

        //Attaching XML Layout root
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.emailLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Glide.with(this)
//                .asGif()
//                .load(R.drawable.login)
//                .into(binding.loginGif);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }


        //Handled Skip button click, and go back.
        binding.SkipCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling  onBackPressed(); to go back
               getOnBackPressedDispatcher().onBackPressed();
            }
        });


        //get instance of firebase auth for auth related task
        firebaseAuth=FirebaseAuth.getInstance();

        //initiate setup ProgressDialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...!!");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle noAccountTv click, open RegisterEmailActivity to register user with email and password
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmailLoginActivity.this, RegisterEmailActivity.class));
            }
        });

        //Handle forgotPasswordTv click, open ForgotPasswordActivity to send Password recovery instruction to registered email
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmailLoginActivity.this, ForgotEmailPasswordActivity.class));
            }
        });

        //handle loginBtn click, start login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateData();

            }
        });

    }

    private String email,password;

    private void validateData() {

        //input data
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString();

        Log.d(TAG,"ValidateData: email: "+email);
        Log.d(TAG,"ValidateData: Password: "+password);

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            //email pattern is Invalid, show error
            binding.emailEt.setError("Invalid Email..");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {

            //password is not entered, show error
            binding.passwordEt.setError("Enter Password...");
            binding.passwordEt.requestFocus();
        }else {
            //email pattern is valid and password is entered
            loginUser();
        }
    }

    private void loginUser() {

        //show progress..
        progressDialog.setMessage("Login In");
        progressDialog.show();

        //start user login
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //user login success
                        Log.d(TAG,"onSuccess: Logged In...");
                        progressDialog.dismiss();

                        //start MainActivity
                        startActivity(new Intent(EmailLoginActivity.this, HomeActivity.class));
                        finishAffinity();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //user login failed
                        Log.e(TAG,"onFailure: ",e);
                        Toast.makeText(EmailLoginActivity.this, "Login Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }


}