package com.example.eyalandmorad;



import android.graphics.Bitmap;

import android.view.View;



import androidx.annotation.NonNull;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireBase {
    private FirebaseFirestore firebaseDB;

    public FireBase(){
        firebaseDB = FirebaseFirestore.getInstance();
    }
//    get all the event that belonge to the user name we get parameter
    public List<Event> getMyEventsFromFB(String username, EventAdapter eventAdapter){
        List<Event> eventsList = new ArrayList<>();

        firebaseDB.collection("Events")
                .whereEqualTo("user", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
//                    loop on the events that belonge to the userid
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                create event and put it to list
                                Event event = documentSnapshot.toObject(Event.class);
                                event.setId(documentSnapshot.getId());
                                eventsList.add(event);
                            }
//                            add all to adatpter and sync
                            eventAdapter.addAll(eventsList);
                            eventAdapter.notifyDataSetChanged();
                        }
                    }
                });

        return eventsList;
    }
//    get event by his id
    public void getEventById(String eventId, OnSuccessListener<Event> successListener, OnFailureListener failureListener) {
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        DocumentReference eventRef = firebaseDB.collection("Events").document(eventId);
//        we do the oncomplete and on faiulr because its take time to firebase to syna all this data and see the erors
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        successListener.onSuccess(event);
                    } else {
                        // Document doesn't exist
                        failureListener.onFailure(new Exception("Event not found"));
                    }
                } else {
                    // Error occurred while fetching the document
                    failureListener.onFailure(task.getException());
                }
            }
        });
    }
//    get all the events from the firebase
    public List<Event> getEventsFromFireBase(EventAdapter eventAdapter, EventDataBase db){
        List<Event> eventList = new ArrayList<>();

        firebaseDB.collection("Events").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                Event event = d.toObject(Event.class);
                                event.setId(d.getId());
                                eventList.add(event);
//    if the event dont exsit in db we add it to db
                                db.openDB();
                                if (!db.isEventExists(event.getId())){
                                    db.addEvent(event);
                                }
                                db.closeDB();
                            }
                            eventAdapter.addAll(eventList);
                            eventAdapter.notifyDataSetChanged();
                        }
                    }
                });
        return eventList;
    }
//add event to firebase
    public void addEventToFireBase(Event event, EventDataBase db,Event event2){
//       check if the image is null if not upload to the firestorage
        if(event.getImage()==null)
            event2.setImageUrl(null);
        else{
//            we comprass it to byte and create image url by data and upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            event.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            final String imageName = event2.getImageUrl();
            StorageReference imageRef = storageRef.child(imageName);
            UploadTask uploadTask = imageRef.putBytes(data);
            event.setImage(null);
        }


        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("Events")
                .add(event2)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String eventId = documentReference.getId();
                        event.setId(eventId);
                        event2.setId(eventId);
                        db.openDB();
                        db.addEvent(event);
                        db.closeDB();
                    }
                });
    }

    public List<Event> getApprovedEventsFromFB(String username, EventAdapter eventAdapter){
        List<Event> eventsList = new ArrayList<>();

        firebaseDB.collection("Reports")
                .whereEqualTo("user", username)
                .whereEqualTo("reportAction", "approval")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Event event = documentSnapshot.toObject(Event.class);
                                event.setId(documentSnapshot.getId());
                                eventsList.add(event);
                            }
                            eventAdapter.addAll(eventsList);
                            eventAdapter.notifyDataSetChanged();
                        }
                    }
                });
        return eventsList;
    }

//

    public void removeEventFromFireBase(String eventID, EventDataBase db){
        getEventById(eventID,
                new OnSuccessListener<Event>() {
                    @Override
                    public void onSuccess(Event event) {
                        deleteImageFromCloudStorage(event.getImageUrl());
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to retrieve the event or the event with the given ID doesn't exist
                        // Handle the error here

                    }
                }
        );
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        firebaseDB.collection("Events")
                .document(eventID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        db.openDB();
                        // Remove the event from the database
                        db.removeEvent(eventID);
                        // Remove any reported events associated with this event
                        db.removeReportedEventByEvent(eventID);
                        db.closeDB();
                    }
                });

        removeCommentFromFireBase(eventID,db);
        removeReportedEventsToFirebase2(eventID,db);

    }
    public void deleteImageFromCloudStorage(String imageUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference storageReference = storageRef.child( imageUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("Ok!");
            }
        });
    }



    public void addCommentToFireBase(Comment comment, EventDataBase db, View rootView){
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("Comments")
                .add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String commentId = documentReference.getId();
                        comment.setId(commentId);
                        db.openDB();
                        db.addComment(comment);
                        db.closeDB();

                        Snackbar.make(rootView, "Comment added successfully", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootView, "Opps, Something wrong", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void removeCommentFromFireBase(String commentID, EventDataBase db, View rootView){
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        firebaseDB.collection("Comments")
                .document(commentID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        db.openDB();
                        // Remove the event from the database
                        db.removeComment(commentID);
                        db.closeDB();

                        Snackbar.make(rootView, "Comment removed", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootView, "Opps, Something wrong", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void editComment(String commentId, String commentText, EventDataBase db, View rootView){
            Map<String, Object> fieldUpdates = new HashMap<>();
            fieldUpdates.put("commentText", commentText);

            FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
            firebaseDB.collection("Comments")
                    .document(commentId)
                    .update(fieldUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.openDB();
                            db.updateComment(commentText, commentId);
                            db.closeDB();

                            Snackbar.make(rootView, "Comment edited successfully\nExit and enter again to see it", Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(rootView, "Opps, Something wrong", Snackbar.LENGTH_SHORT).show();
                        }
                    });
    }

    public void syncFireBaseWithLocalDB(EventDataBase db){
        syncEvents(db);
        syncComments(db);
        syncReports(db);
        syncUsers(db);
        return;
    }

    public void syncEvents(EventDataBase db){
        firebaseDB.collection("Events").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                Event event = d.toObject(Event.class);
                                event.setId(d.getId());
                                event.setImage(null);

                                db.openDB();
                                if (!db.isEventExists(event.getId())){
                                    db.addEvent(event);
                                }
                                db.closeDB();
                            }
                        }
                    }
                });
        return;
    }

    public void syncComments(EventDataBase db){
        firebaseDB.collection("Comments").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                Comment comment = d.toObject(Comment.class);
                                comment.setId(d.getId());

                                db.openDB();
                                if (!db.isCommentExists(comment.getCommentId())){
                                    db.addComment(comment);
                                }
                                db.closeDB();
                            }
                        }
                    }
                });
        return;
    }

    public void syncReports(EventDataBase db){
        firebaseDB.collection("Reports").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                String eventID = d.getString("eventID");
                                String username = d.getString("userName");
                                String action = d.getString("reportType");
                                String reportID = d.getId();

                                db.openDB();
                                if (!db.isReportedEventExists(eventID, username, action)){
                                    db.addReportedEvents(reportID, eventID, username, action);
                                }
                                db.closeDB();
                            }
                        }
                    }
                });
        return;
    }

    public void syncUsers(EventDataBase db){
        firebaseDB.collection("User").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                String userEmail = d.getString("Email");
                                long userScoreLong = d.getLong("Score");
                                int userScore = (int) userScoreLong;

                                db.openDB();
                                if (!db.isUserExists(userEmail)){
                                    db.addUser(userEmail, userScore, d.getId());
                                }
                                db.closeDB();
                            }
                        }
                    }
                });
        return;
    }


//    we use this function when we remove event and want to remove his reports
    public void removeReportedEventsToFirebase2(String eventID,EventDataBase db){
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("Reports")
                .whereEqualTo("eventID", eventID)
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
                                                db.removeReportedEventByEvent(eventID);
                                                db.closeDB();

                                            }
                                        });
                            }
                        }
                    }
                });
    }
    //remove the comment from firebase by event id
    public void removeCommentFromFireBase(String eventid, EventDataBase db){
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        firebaseDB.collection("Comments")
                .whereEqualTo("commentEventId",eventid).get()
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
                                                db.removeCommentsByEventId(eventid);
                                                db.closeDB();

                                            }
                                        });
                            }
                        }
                    }
                });
    }





}
