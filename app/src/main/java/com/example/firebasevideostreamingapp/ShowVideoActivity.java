package com.example.firebasevideostreamingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.firebasevideostreamingapp.databinding.ActivityShowVideoBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ShowVideoActivity extends AppCompatActivity {

    //enabling ViewBinding
    ActivityShowVideoBinding binding;
    //declare DatabaseReference
    DatabaseReference reference,lkeReference;
    //declare FirebaseDatabase
    FirebaseDatabase firebaseDatabase;
    //checking is like or not to video
    boolean likeChecker = false;

    //name String for getting and setting Recycler Item and url String for passing click item url to FullScreenActivity
    String name,url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //getting XML Layout
        binding = ActivityShowVideoBinding.inflate(getLayoutInflater());
        //setting XML root to MainActivityBinding
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init and set ActionBar Title Text
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        //init FirebaseDatabase
        firebaseDatabase = FirebaseDatabase.getInstance();
        //getting reference of the Video path
        reference = firebaseDatabase.getReference("Video");
        lkeReference = firebaseDatabase.getReference("Likes");

    }

    //creating method for performing Firebase Search Operation on Firebase Database
    private void firebaseSearch(String searchTxt){

        //getting search query in lower case
        String query = searchTxt.toLowerCase();

        //getting Firebase Query to Search Operation
        Query firebaseQuery = reference.orderByChild("search").startAt(query).endAt(query + "\uf8ff");

        //FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Video> options =
                new FirebaseRecyclerOptions.Builder<Video>()
                        .setQuery(firebaseQuery, Video.class)
                        .build();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Video,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Video model) {
                //calling setSimpleExoPlayer() method of ViewHolder class and passing req. argument
                holder.setSimpleExoPlayer(getApplication(), model.getName(), model.getVideouri());

                //Overriding onItemClick and onItemLongClick abstract method
                holder.setOnClickListener(new ViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //getting name item clicked
                        name = getItem(position).getName();
                        //getting uri item clicked
                        url = getItem(position).getVideouri();

                        //calling Intent class and providing root Destination and Target Destination
                        Intent intent = new Intent(ShowVideoActivity.this, FullScreenActivity.class);
                        //sending name to next Activity
                        intent.putExtra("name",name);
                        //sending url to next Activity
                        intent.putExtra("url",url);

                        //finally start Activity
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //getting name item, that want to Delete
                        name = getItem(position).getName();
                        //creating method for Deleting RecyclerView item
                        showDeleteDialog(name);
                    }
                });
            }

            //onCreateViewHolder
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating R.layout.item_video
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video,parent,false);
                //returning ViewHolder(view); with view
                return new ViewHolder(view);
            }
        };

        //start Listening firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening();
        //finally setting firebaseRecyclerAdapter to XML RecyclerView
        binding.recyclerview.setAdapter(firebaseRecyclerAdapter);
    }


    //override onStart method
    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Video> options =
                new FirebaseRecyclerOptions.Builder<Video>()
                        .setQuery(reference, Video.class)
                        .build();

        //FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Video,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Video model) {

                //calling setSimpleExoPlayer() method of ViewHolder class and passing req. argument
                holder.setSimpleExoPlayer(getApplication(), model.getName(), model.getVideouri());

                //init FirebaseUser and FirebaseAuth
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //getting current Firebase user id (Generating via Email_Authentication)
                String currentUserId = user.getUid();
                //getting the post video key
                 final String postKey = getRef(position).getKey();

                //Overriding onItemClick and onItemLongClick abstract method
                holder.setOnClickListener(new ViewHolder.Clicklistener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //getting name item clicked
                        name = getItem(position).getName();
                        //getting uri item clicked
                        url = getItem(position).getVideouri();

                        //calling Intent class and providing root Destination and Target Destination
                        Intent intent = new Intent(ShowVideoActivity.this, FullScreenActivity.class);
                        //sending name to next Activity
                        intent.putExtra("name",name);
                        //sending url to next Activity
                        intent.putExtra("url",url);

                        //finally start Activity
                        startActivity(intent);

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //getting name item, that want to Delete
                        name = getItem(position).getName();
                        //creating method for Deleting RecyclerView item
                        showDeleteDialog(name);
                    }
                });



                //calling  holder.setLikeButtonStatus(postKey); method from ViewHolder class
                holder.setLikeButtonStatus(postKey);

                //setting onClicked event on commentImg ImageButton
                holder.commentImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //starting CommentActivity and passing video postKey to CommentActivity
                        Intent intent = new Intent(ShowVideoActivity.this, CommentActivity.class);
                        intent.putExtra("postKey",postKey);
                        startActivity(intent);
                    }
                });

                //setting onClick on like Button clicked / trigger
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //now  likeChecker =  true; by default it's false
                        likeChecker =  true;

                        //setting Value EventListener lkeReference path of JSON file
                        lkeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //if user already like the Video
                                if (likeChecker == true){
                                    if (snapshot.child(postKey).hasChild(currentUserId)){
                                        lkeReference.child(postKey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    }else {
                                        //if it's first time, that user like the Video
                                        lkeReference.child(postKey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //Showing Toast message, when "Error while Like Video
                                Toast.makeText(ShowVideoActivity.this, "Error while Like Video: "+error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }

            //onCreateViewHolder
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflating R.layout.item_video
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video,parent,false);
                //returning ViewHolder(view); with view
                return new ViewHolder(view);
            }
        };

        //start Listening firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening();
        //finally setting firebaseRecyclerAdapter to XML RecyclerView
        binding.recyclerview.setAdapter(firebaseRecyclerAdapter);
    }

    //creating method for showing Delete dialog
    private void showDeleteDialog(String name) {
        //AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowVideoActivity.this);
        //AlertDialog Title
        builder.setTitle("Delete");
        //AlertDialog Message
        builder.setMessage("Sure about Delete");

        //setting PositiveButton
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Firebase Database Query
                Query query = reference.orderByChild("name").equalTo(name);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //DataSnapshot
                        for (DataSnapshot ds : snapshot.getChildren()){
                            //getting reference and removing entire path reference value
                            ds.getRef().removeValue();
                        }
                        //if the Remove Value get success, show Toast Message
                        Toast.makeText(ShowVideoActivity.this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //and if Remove Value not get success, Show error Toast Message
                        Toast.makeText(ShowVideoActivity.this, "Video Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        //setting NegativeButton
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog box here
                dialog.dismiss();
            }
        });

        //finally showing AlertDialog Builder
        builder.create();

        //or this is way of Creating Alert Builder
        AlertDialog alertDialog = builder.create();
        //show alertDialog here
        alertDialog.show();
    }


    //overriding onCreateOptionsMenu() method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflating menu directory with menu
        getMenuInflater().inflate(R.menu.search_menu,menu);

        //finding menu's item
        MenuItem menuItem = menu.findItem(R.id.search_item);

        //init SearchView
        SearchView searchView = (SearchView) menuItem.getActionView();

        //setting On Query TextListener to SearchView
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //calling firebaseSearch() nd search String to it
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //calling firebaseSearch() and search String to it
                firebaseSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //get id of the menu item clicked
        int itemId = item.getItemId();

        //if the item id is equal to person_item, then
        if (itemId == R.id.person_item){
            //start ProfileActivity.class
            Intent intent = new Intent(ShowVideoActivity.this, ProfileActivity.class);
            //and start activity
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}