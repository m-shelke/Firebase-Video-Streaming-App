package com.example.firebasevideostreamingapp.NewUI;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.firebasevideostreamingapp.R;
import com.example.firebasevideostreamingapp.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_EDIT_TAG";
    private ActivityProfileEditBinding binding;
    //FirebaseAuth for Auth related task
    private FirebaseAuth firebaseAuth;

    //ProgressDialog:to show while Profile Update
    private ProgressDialog progressDialog;
    private Uri imageUri;

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image capture or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as asinged in PickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Capture " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .into(binding.updateProfileImg);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //canceled
                        Toast.makeText(ProfileEditActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    Log.d(TAG, "onActivityResult: " + result.toString());

                    //Let's check if permission granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //Camera or Storage or both permission granted, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g Cameta, Storage");
                        pickImageCamera();
                    } else {
                        //Camera or Storage or both permission denied, can not camera to capture image
                        Log.d(TAG, "onActivityResult: All or Either one is denied");
                        Toast.makeText(ProfileEditActivity.this, "Both Camera and Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check, if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get Uri of image picked
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked from Gallery " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .into(binding.updateProfileImg);

                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }

                    } else {
                        //Canceled
                        Toast.makeText(ProfileEditActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher<String> requestsStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    //Let's check if permission is granted or not
                    if (isGranted) {
                        //  Storage Permission granted, we can now launch Gallery to pick Image
                        pickImageGallery();
                    } else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(ProfileEditActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //get instance of the Firebase fo auth related task
        firebaseAuth = FirebaseAuth.getInstance();

        //progressDialog: to show while profile update
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        //Handle profileImagePickFab click, show image pick popup menu
        binding.updateProfileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });


    }

    String name;
//    String email;

    private void validateData() {

        name=binding.updateNameEd.getText().toString().trim();
//         email=binding.emailEt.getText().toString().trim();

        //Validate data
        if (imageUri==null){
            //no image to upload to storage, just update Database
            updateProfileDb();

            Toast.makeText(this, "imageUri is null", Toast.LENGTH_SHORT).show();
        }else {
            //image need to upload storage, first upload image then update Database
            uploadProfileImageStorage();
        }

    }

    private void updateProfileDb() {


        //show progressDialog
        progressDialog.setMessage("Updating user info");
        progressDialog.show();

        //setup data in hashMap to update to Firebase Database
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("name",name);
//        hashMap.put("dob",""+email);

//        if (imageUrl !=null) {
//            //update profileImageUrl in the Database only if uploaded image url is not null
//            hashMap.put("imageUrl", imageUrl);
//        }


            //DatabaseReference of user to update info
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("VideoUsers");
            reference.child(firebaseAuth.getUid())
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            //update Successfully
                            Log.d(TAG, "onSuccess: Info Updated");
                            progressDialog.dismiss();

                            Toast.makeText(ProfileEditActivity.this, "Only Profile Name Updated", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //update failed
                            Log.e(TAG, "onFailure: ",e);
                            progressDialog.dismiss();

                            Toast.makeText(ProfileEditActivity.this, "Profile Update Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void uploadProfileImageStorage() {

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
//                            updateProfileDb(uploadedImageUrl);

                            //change progress dialog message
                            progressDialog.setMessage("Saving user Info..");

                            //getting uid of the Registered user to save in Realtime database
                            String registerUserId = firebaseAuth.getUid();

                            //setup data to save in firebase realtime db. most of the data will be empty and will set in edit profile
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("name",name);
                            hashMap.put("imageUrl",uploadedImageUrl);

                            //set data to firebase db
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VideoUsers");
                            reference.child(registerUserId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            //update Successfully
                                            Log.d(TAG, "onSuccess: Info Updated");
                                            progressDialog.dismiss();

                                            Toast.makeText(ProfileEditActivity.this, "Profile Name-Image Updated", Toast.LENGTH_SHORT).show();

                                            onBackPressed();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //update failed
                                            Log.e(TAG, "onFailure: ",e);
                                            progressDialog.dismiss();

                                            Toast.makeText(ProfileEditActivity.this, "Profile Update Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(ProfileEditActivity.this, "Failed to Update Profile Image: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void imagePickDialog() {

        //init popup menu param#1  is context and Param#2 is the UI View (profileImagePickFab) to above or below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.updateProfileImagePickFab);
        //add menu items to our popup menu Param#1 is GroupId,Param#2 is ItemId,Param#3 is OrderID,Param#4 Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        //show popup menu
        popupMenu.show();

        //Handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //get id of the menu item clicked
                int itemId = menuItem.getItemId();

                if (itemId == 1) {
                    //Camera is clicked we need to check if we have permission of Camera,Storage before launching Camera to capture Image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. We only need Camera Permission
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        //Device version is below TIRAMISU. We need Camera and Storage Permission
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Check if Storage permission is granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
    }

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");

        //setup Content Values,MediaStore to capture high quality image using Camera Intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Intent to launch Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void loadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VideoUsers");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get User Info, spelling should be as in Firebase realtime database
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String imageUrl = "" + snapshot.child("imageUrl").getValue();

                binding.updateNameEd.setText(name);


                try {

                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

                    Glide.with(ProfileEditActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.baseline_more_horiz_24)
                            .apply(requestOptions)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Load failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(binding.updateProfileImg);

                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileEditActivity.this, "Fetching Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}