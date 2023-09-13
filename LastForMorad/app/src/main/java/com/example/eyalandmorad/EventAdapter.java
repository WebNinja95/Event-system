package com.example.eyalandmorad;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAdapter extends ArrayAdapter<Event> {

    private static class ViewHolder {
        TextView textViewEventDetails;
        Button buttonRemove;
        Button buttonEdit;
        Button buttonApproval;
        Button buttonComment;
        Button buttonRejected;
        Button eventDetailsBtn;
        ImageView eventImage;

    }
    EventDataBase db;
    private Context context;
    private String userName;
    private static final int EDIT_EVENT_REQUEST_CODE = 1;
    private FireBase fb;

    public EventAdapter(Context context, List<Event> events, EventDataBase db, String userName) {

        super(context, 0, events);
        this.context = context;
        this.db = db;
        this.userName = userName;
        this.fb = new FireBase();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
            viewHolder.textViewEventDetails = convertView.findViewById(R.id.textViewEventDetails);
            viewHolder.buttonRemove = convertView.findViewById(R.id.buttonRemove);
            viewHolder.buttonEdit = convertView.findViewById(R.id.buttonEdit);
            viewHolder.buttonApproval = convertView.findViewById(R.id.buttonApproval);
            viewHolder.buttonRejected = convertView.findViewById(R.id.buttonRejected);
            viewHolder.buttonComment = convertView.findViewById(R.id.buttonComment);
            viewHolder.eventImage = convertView.findViewById(R.id.eventImage);
            viewHolder.eventDetailsBtn = convertView.findViewById(R.id.eventDetailsBtn);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // If the current logged in user did not create the event,
        // he can't edit or remove it so that buttons will be GONE,
        // and Hide approve and reject buttons for events that the user created
        if ( userName.equals(event.getUser())) {
            viewHolder.buttonRemove.setVisibility(View.VISIBLE);
            viewHolder.buttonEdit.setVisibility(View.VISIBLE);
            viewHolder.buttonApproval.setVisibility(View.GONE);
            viewHolder.buttonComment.setVisibility(View.GONE);
            viewHolder.buttonRejected.setVisibility(View.GONE);
        } else {
            viewHolder.buttonRemove.setVisibility(View.GONE);
            viewHolder.buttonEdit.setVisibility(View.GONE);
            viewHolder.buttonApproval.setVisibility(View.VISIBLE);
            viewHolder.buttonComment.setVisibility(View.VISIBLE);
            viewHolder.buttonRejected.setVisibility(View.VISIBLE);

            colorReportedEvents(viewHolder, event);
        }


        // Set the event details in the text view
        String eventDetails = event.toString();
        viewHolder.textViewEventDetails.setText(eventDetails);
        if(event.getImageUrl() != null && event.getImageUrl() !="") {
            downloadImage(event.getImageUrl(),viewHolder.eventImage);
//            set the image with function from the firestorage
        } else {
            viewHolder.eventImage.setImageResource(R.drawable.no_photo_taken);
        }

        // Set click listener for details button
        viewHolder.eventDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventDetails.class);
                intent.putExtra("eventID", event.getId());
                intent.putExtra("user", userName);
                context.startActivity(intent);
            }
        });

        // Set click listener for add comment button
        viewHolder.buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_comment_dialog dialogFragment = new add_comment_dialog();
                View rootView = ((AppCompatActivity) context).findViewById(android.R.id.content);
                dialogFragment.setParameters(userName, event.getId(), db, rootView);
                dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "add_comment_dialog");
                return;
            }
        });


        // Set click listener for remove button
        viewHolder.buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if user don't have internet connection, he can't remove events.
                if (!isConnectedToInternet()){
                    Toast.makeText(getContext(), "Sorry, You can't remove events without an internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Create an alert dialog to confirm event deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Deleting event \"" + event.getDescription() + "\"");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fb.removeEventFromFireBase(event.getId(), db);
                        // Remove the event from the list
                        remove(event);
                        Toast.makeText(getContext(), "Event removed", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                    // Do nothing if the user cancels the deletion
                });
                // Create and display the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                notifyDataSetChanged();
            }
        });

        // Set click listener for edit button
        viewHolder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if user don't have internet connection, he can't edit events.
                if (!isConnectedToInternet()){
                    Toast.makeText(getContext(), "Sorry, You can't edit events without an internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, EditEvent.class);
                intent.putExtra("event", event.getId());
                intent.putExtra("username", event.getUser());
                ((Activity) context).startActivityForResult(intent, EDIT_EVENT_REQUEST_CODE);
                notifyDataSetChanged();
            }
        });

        // Set click listeners for approval and report buttons
        viewHolder.buttonApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testt", "start " + event.getId());
                //if user don't have internet connection, he can't approve or reject events.
                if (!isConnectedToInternet()){
                    Toast.makeText(getContext(), "Sorry, You can't report events without an internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.openDB();
                // Check if the event is already rejected by the current user
                if(db.isReportedEventExists(event.getId(), userName, "rejected")){
                    Toast.makeText(getContext(), "Please remove the rejection first", Toast.LENGTH_SHORT).show();
                }
                // Check if the event is not already approved by the current user
                else if (!db.isReportedEventExists(event.getId(), userName, "approval")){
                    addReportedEventsToFirebase(event.getId(), userName, "approval", viewHolder);
                    Toast.makeText(getContext(), "Event approved", Toast.LENGTH_SHORT).show();
                }
                else{
                    // If the event is already approved by the current user, remove the approval
                    removeReportedEventsToFirebase(event.getId(), userName, viewHolder, "approval");
                    Toast.makeText(getContext(), "Event has unapproved", Toast.LENGTH_LONG).show();
                }
                db.closeDB();
                notifyDataSetChanged();
            }
        });

        viewHolder.buttonRejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if user don't have internet connection, he can't approve or reject events.
                if (!isConnectedToInternet()){
                    Toast.makeText(getContext(), "Sorry, You can't report events without internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the event is already approved by the current user
                db.openDB();
                if(db.isReportedEventExists(event.getId(), userName, "approval")){
                    Toast.makeText(getContext(), "Please remove the approval first", Toast.LENGTH_SHORT).show();
                }
                else if (!db.isReportedEventExists(event.getId(), userName, "rejected")){
                    // Check if the event is not already rejected by the current user
                    addReportedEventsToFirebase(event.getId(), userName, "rejected", viewHolder);
                    Toast.makeText(getContext(), "Event rejected", Toast.LENGTH_SHORT).show();
                }
                else{
                    // If the event is already rejected by the current user, remove the rejection
                    removeReportedEventsToFirebase(event.getId(), userName, viewHolder, "rejected");
                    Toast.makeText(getContext(), "Event has unrejected", Toast.LENGTH_SHORT).show();
                }
                db.closeDB();
                notifyDataSetChanged();
            }
        });

        return convertView;

    }

    public void reRenderAdapter() {
        for (int i = 0; i < getCount(); i++) {
            Event event = getItem(i);
            ViewHolder viewHolder = (ViewHolder) getView(i, null, null).getTag();
            colorReportedEvents(viewHolder, event);
        }

        notifyDataSetChanged();
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    // Method to check internet connection
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    public void addReportedEventsToFirebase(String eventID,String userName,String reportType, ViewHolder viewHolder){
        Map<String, Object> reportedEvent = new HashMap< String, Object >();
        reportedEvent.put("eventID", eventID);
        reportedEvent.put("userName", userName);
        reportedEvent.put("reportType", reportType);
        reportedEvent.put("reportId", "");

        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("Reports")
                .add(reportedEvent)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String reportIdFromFB = documentReference.getId();
                        db.openDB();
                        db.addReportedEvents(reportIdFromFB, eventID, userName, reportType);
                        db.closeDB();

                        int colorBtn = 0;
                        if (reportType == "approval"){
                            colorBtn = ContextCompat.getColor(context, R.color.approval_green);
                            viewHolder.buttonApproval.setBackgroundColor(colorBtn);
                            viewHolder.buttonApproval.invalidate();
                        }
                        else{
                            colorBtn = ContextCompat.getColor(context, R.color.rejected_red);
                            viewHolder.buttonRejected.setBackgroundColor(colorBtn);
                            viewHolder.buttonRejected.invalidate();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void removeReportedEventsToFirebase(String eventID, String userName, ViewHolder viewHolder, String reportType){
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        firebaseDB.collection("Reports")
                .whereEqualTo("eventID", eventID)
                .whereEqualTo("userName", userName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                documentSnapshot.getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                db.openDB();
                                                db.removeReportedEventByUser(eventID, userName);
                                                db.closeDB();
                                                int colorBtn = ContextCompat.getColor(context, R.color.black);
                                                if (reportType == "approval") {
                                                    viewHolder.buttonApproval.setBackgroundColor(colorBtn);
                                                    viewHolder.buttonApproval.invalidate();
                                                }
                                                else{
                                                    viewHolder.buttonRejected.setBackgroundColor(colorBtn);
                                                    viewHolder.buttonRejected.invalidate();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public void colorReportedEvents(ViewHolder viewHolder, Event event){
        int colorBtn = ContextCompat.getColor(context, R.color.black);

        viewHolder.buttonApproval.setBackgroundColor(colorBtn);
        viewHolder.buttonRejected.setBackgroundColor(colorBtn);

        db.openDB();
        // Check if the event has been reported with "approval"
        if (db.isReportedEventExists(event.getId(), userName, "approval")) {
            // Set the background color of the "Approval" button to green
            int approval_green = ContextCompat.getColor(context, R.color.approval_green);
            viewHolder.buttonApproval.setBackgroundColor(approval_green);
        }

        // Check if the event has been reported with "rejected"
        if (db.isReportedEventExists(event.getId(), userName, "rejected")) {
            // Set the background color of the "Rejected" button to red
            int rejected_red = ContextCompat.getColor(context, R.color.rejected_red);
            viewHolder.buttonRejected.setBackgroundColor(rejected_red);
        }

        // Call invalidate() on the buttons
        viewHolder.buttonApproval.invalidate();
        viewHolder.buttonRejected.invalidate();
        db.closeDB();
    }
    //take the image from the firestorage with imageurl and gilde leabry
    // and desplay it here
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
