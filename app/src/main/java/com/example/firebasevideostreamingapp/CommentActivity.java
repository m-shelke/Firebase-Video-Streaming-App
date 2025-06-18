package com.example.firebasevideostreamingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasevideostreamingapp.databinding.ActivityCommentBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    //enabling ViewBinding
    ActivityCommentBinding binding;

    //DataBase Reference
    DatabaseReference reference,postReference;

    //getting postKey in Variable for ShowActivity
    String postKey;

    //instance of Comments DataModel class
    Comments comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating XML file
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        //Attaching XML root
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //ActionBar obj and it's operation
        ActionBar actionBar = getSupportActionBar();
        //if actionBar obj in not null (for handling null pointer error)
        assert actionBar != null;
        //and hiding actionBar for this Activity screen
        actionBar.hide();

        //init Comments DataModel class
        comments = new Comments();

        //getting postKey form ShowActivity
        postKey = getIntent().getExtras().getString("postKey");

        //init FirebaseUser and FirebaseAuth to get current User ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //for getting current UserID
        String userId = user.getUid();

        //getting "UserName" Database Reference
        reference = FirebaseDatabase.getInstance().getReference().child("UserName");

        //creating another Database Reference inside of below path "Comments"
        postReference = FirebaseDatabase.getInstance().getReference().child("Video").child(postKey).child("Comments");

        //RecyclerView can perform several optimizations if it can know in advance that RecyclerView's size is not affected by the adapter contents
        binding.commentRecyclerView.setHasFixedSize(true);
        //LinearLayoutManager which provides similar functionality to RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Used to reverse item traversal and layout order
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        //setting LayoutManager to RecyclerView
        binding.commentRecyclerView.setLayoutManager(linearLayoutManager);

        //onClick() on postBtn View
        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  adding Value EventListener on this path reference.child(userId)
                reference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //if DataSnapshot exists
                        if (snapshot.exists()){
                            //Getting value of this JSON key ""userName")" inside variable
                            String userName = snapshot.child("userName").getValue().toString();

                            //calling  commentFeatures(userName); and passing String userName
                            commentFeatures(userName);
                            //setting empty text to  binding.commentEt
                            binding.commentEt.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }



    //Creating function for Comment Features
    private void commentFeatures(String userName) {

        //getting commentEt text inside variable
        String comment = binding.commentEt.getText().toString();

        //init FirebaseUser and FirebaseAuth to get current User ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //for getting current UserID
        String userId = user.getUid();

        //if commentEt editText is empty
        if (comment.isEmpty()){
            //show Toast message
            Toast.makeText(this, "Comment is Empty", Toast.LENGTH_SHORT).show();
        }else {

            //This code generating error
      /*      Calendar calendarForDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormatForDate = new SimpleDateFormat("dd-MMMM-YYYY");
            final String saveCurrentDate = simpleDateFormatForDate.format(calendarForDate);

            Calendar calendarForTime = Calendar.getInstance();
            SimpleDateFormat simpleDateFormatForTime = new SimpleDateFormat("HH.mm");
            final String saveCurrentTime = simpleDateFormatForTime.format(calendarForTime);*/



            //if android.os.Build.VERSION is greater than or equal to android.os.Build.VERSION_CODES.O
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                // getting the current or Local Date
                LocalDate date = LocalDate.now();

               // formating Date that want to save in DataBase
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");

                LocalTime time = LocalTime.now();
                DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("HH:mm");

               // formating to nextYearDate Date
                String formatedDate = date.format(dateTimeFormatter);
                String getTime = time.format(dateTimeFormatter2);

                //Generating randomKey to stored comment details
                final String randomKey = userId + formatedDate + getTime;

                //HashMap for storing data
                HashMap hashMap = new HashMap();
                //for userId
                hashMap.put("uid", userId);
                //for comment
                hashMap.put("comment", comment);
                //for date
                hashMap.put("date", formatedDate);
                //for time
                   hashMap.put("time",getTime);
                   //for user name
                hashMap.put("userName",userName);

                   //adding Complete Listener on postReference.child(randomKey)
                postReference.child(randomKey).setValue(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                //if task is successful
                                if (task.isSuccessful()) {
                                    //showing submitted Toast message
                                    Toast.makeText(CommentActivity.this, "Comment Submit", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        //if failed
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //showing Error Toast message
                                Toast.makeText(CommentActivity.this, "Comment Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }
    }

    //onStart() method
    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseRecyclerOptions for RecyclerView UI
        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(postReference,Comments.class)
                .build();


        //FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Comments, VideoViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, VideoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull Comments model) {
                holder.setComments(getApplication(), model.getComment(), model.getDate(), model.getTime(), model.getUserName());
            }

            @NonNull
            @Override
            public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments,parent,false);
                return new VideoViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        binding.commentRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}
