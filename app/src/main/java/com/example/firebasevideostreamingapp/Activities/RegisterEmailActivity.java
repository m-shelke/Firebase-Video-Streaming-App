package com.example.firebasevideostreamingapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegisterEmailActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_TAG";
    //ProgressDialog to show while sign-up
    ProgressDialog progressDialog;
    String name,email, password, cPassword;
    private ActivityRegisterEmailBinding binding;
    private FirebaseAuth firebaseAuth;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_register_email.xml=ActivityRegisterEmailBinding
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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
//        binding.SkipCv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //calling  onBackPressed(); to go back
//               getOnBackPressedDispatcher().onBackPressed();
//            }
//        });

        //get instance of firebase auth for Auth related task
        firebaseAuth = FirebaseAuth.getInstance();

        //if the user is already log-in, then jumped to MainActivity
        if (firebaseAuth.getCurrentUser() != null){
            //if user log-in directly starting MainActivity
            startActivity(new Intent(RegisterEmailActivity.this, HomeActivity.class));
            //and finish this activity here
            finish();
        }

        //initiated/set ProgressDialog while sign-up
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle toolbarBackBtn, go back
//        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });




        //Handle profileImagePickFab click, show image pick popup menu
        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();
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
        name = binding.nameEd.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();


        //Validate data
        if (imageUri==null){
            //no image to upload to storage, just update Database
            Toast.makeText(this, "Image is compulsory", Toast.LENGTH_SHORT).show();
        }else if

        (name.isEmpty()){
            binding.nameEd.setError("Enter Name");
            binding.nameEd.requestFocus();
        } else if

        (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

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
                       uploadProfileImageStorage();
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



    private void uploadProfileImageStorage(){

        //show progress
        progressDialog.setMessage("Uploading User Image..");
        progressDialog.show();

        //setup image and path e.g UserImage/profile_userid
        String filePathAndName="UserImages/"+"profile"+firebaseAuth.getUid();

        //StorageReference to upload image
        StorageReference reference= FirebaseStorage.getInstance().getReference().child(filePathAndName);
        reference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress=(100.0*snapshot.getBytesTransferred()) /snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Process: "+progress);

                        progressDialog.setMessage("Uploading Profile Image.\nProgress: "+(int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image upload successfully, get Url of upload image
                        Log.d(TAG, "onSuccess: Uploaded..!!");

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl=uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){



                                //change progress dialog message
                                progressDialog.setMessage("Saving user Info..");

                                //get current timestamp e.g show user registration date/time

                                String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
                                //getting uid of the Registered user to save in Realtime database
                                String registerUserId = firebaseAuth.getUid();

                                //setup data to save in firebase realtime db. most of the data will be empty and will set in edit profile
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("email", registerUserEmail);
                                hashMap.put("uid", registerUserId);
                                hashMap.put("name",name);
                                hashMap.put("imageUrl",uploadedImageUrl);

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


                                                startActivity(new Intent(RegisterEmailActivity.this, HomeActivity.class));
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to upload image
                        Log.e(TAG, "onFailure: ",e);
                        progressDialog.dismiss();

//                        Utils.toast(ProfileEditActivity.this,"Failed to Upload Profile Image due to "+e.getMessage());
                    }
                });
    }































    private void imagePickDialog(){
        //init popup menu param#1  is context and Param#2 is the UI View (profileImagePickFab) to above or below we need to show popup menu
        PopupMenu popupMenu=new PopupMenu(this,binding.profileImagePickFab);
        //add menu items to our popup menu Param#1 is GroupId,Param#2 is ItemId,Param#3 is OrderID,Param#4 Menu Item Title
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        //show popup menu
        popupMenu.show();

        //Handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //get id of the menu item clicked
                int itemId=menuItem.getItemId();

                if (itemId==1){
                    //Camera is clicked we need to check if we have permission of Camera,Storage before launching Camera to capture Image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission granted or not");

                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                        //Device version is TIRAMISU or above. We only need Camera Permission
                        requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA});
                    }else {
                        //Device version is below TIRAMISU. We need Camera and Storage Permission
                        requestCameraPermission.launch(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId==2) {
                    Log.d(TAG, "onMenuItemClick: Check if Storage permission is granted or not");

                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery();
                    }else {
                        requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
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
                        pickImageGallery();
                    }else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(RegisterEmailActivity.this, "Storage Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );



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
                        pickImageCamera();
                    }else {
                        //Camera or Storage or both permission denied, can not camera to capture image
                        Log.d(TAG, "onActivityResult: All or Either one is denied");
                        Toast.makeText(RegisterEmailActivity.this, "Camera or Storage or both permission denied", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        //setup Content Values,MediaStore to capture high quality image using Camera Intent
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION");

        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        //Intent to launch Camera
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image capture or not
                    if (result.getResultCode()== Activity.RESULT_OK){
                        //Image Captured, we have image in imageUri as asinged in PickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Capture "+imageUri);

                        //set to profileIv
                        binding.profileImage.setImageURI(imageUri);
                    }else {
                        //canceled
                        Toast.makeText(RegisterEmailActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent=new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
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
                        imageUri=data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked from Gallery "+imageUri);

                        //set to profileIv
                        binding.profileImage.setImageURI(imageUri);

                    }else {
                        //Canceled
                        Toast.makeText(RegisterEmailActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

}