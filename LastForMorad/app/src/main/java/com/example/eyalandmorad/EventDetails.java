package com.example.eyalandmorad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EventDetails extends AppCompatActivity {
    private FireBase fb;
    private String eventId;
    private String username;
    private ImageView eventImage;
    private TextView textViewEventDetails;
    private TextView noCommentsTextView;
    private EventDataBase db;
    private Context context;
    private List<Comment> commentsList = new ArrayList<>(); // List of comments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fb = new FireBase();
        eventId = getIntent().getStringExtra("eventID");
        username = getIntent().getStringExtra("user");
        setContentView(R.layout.activity_event_deatils);
        db = EventDataBase.getInstance(this);

        eventImage = findViewById(R.id.eventImage);
        textViewEventDetails = findViewById(R.id.textViewEventDetails);
        noCommentsTextView = findViewById(R.id.noCommentsTextView);
        context = this;
        reRender();
    }

    public void reRender(){
        fb.getEventById(eventId,
                new OnSuccessListener<Event>() {
                    @Override
                    public void onSuccess(Event event) {
                        // The event was successfully retrieved, and you can use the event object here
                        // For example, you can update your UI with the event data
                        if(event.getImageUrl() != null) {
                            downloadImage(event.getImageUrl(),eventImage);
                        } else {
                            eventImage.setImageResource(R.drawable.no_photo_taken);
                        }
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to retrieve the event or the event with the given ID doesn't exist
                        // Handle the error here
                        Toast.makeText(EventDetails.this, "Error retrieving the event", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        db.openDB();
        Event event = db.getEventById(eventId);
        String eventDetails = event.toString();
        textViewEventDetails.setText(eventDetails);


        commentsList = db.getAllComments(eventId);
        if (commentsList.size() == 0){
            noCommentsTextView.setVisibility(View.VISIBLE);
        }
        else{
            noCommentsTextView.setVisibility(View.GONE);
            RecyclerView commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            CommentAdapter commentAdapter = new CommentAdapter(commentsList, db, this, username);
            commentsRecyclerView.setAdapter(commentAdapter);
        }
        db.closeDB();
    }
//    functio to take the image from firestorage and desplay it
    public void downloadImage(String imageUrl, ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference storageReference = storageRef.child(imageUrl);
        storageReference.getDownloadUrl().addOnCompleteListener(
                new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            Glide.with(context)
                                    .load(downloadUrl)
                                    .into(imageView);
                        } else {
                            System.out.println("Getting download url was not successful." +
                                    task.getException());
                        }
                    }
                });
    }


}
