package com.example.eyalandmorad;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditEvent extends AppCompatActivity {
    
    private Spinner spinnerEventType;
    private EditText editTextDescription;
    private EditText editTextLocation;
    private Spinner spinnerArea;
    private Spinner spinnerRiskLevel;
    private Button submitChangesButton;
    private Bitmap cameraImage;
    private Button cameraBtn;
    private Event passedEvent;
    private static final int REQUEST_IMAGE_CAPTURE=11;

    private ImageView imageViewFromCamera;
    EventDataBase db = EventDataBase.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        String eventId = getIntent().getStringExtra("event");
        db.openDB();
        passedEvent = db.getEventById(eventId);
        db.closeDB();

        // Initialize view values by the event that selected to edit
        spinnerEventType = findViewById(R.id.spinnerEventType);
        int selectedEventTypeIndex = Arrays.asList(EventType.values()).indexOf(passedEvent.getEventType());
        spinnerEventType.setSelection(selectedEventTypeIndex);

        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDescription.setText(passedEvent.getDescription());

        editTextLocation = findViewById(R.id.editTextLocation);
        editTextLocation.setText(passedEvent.getLocation());

        spinnerArea = findViewById(R.id.spinnerArea);
        int selectedAreaIndex = Arrays.asList(Area.values()).indexOf(passedEvent.getArea());
        spinnerArea.setSelection(selectedAreaIndex);

        spinnerRiskLevel = findViewById(R.id.spinnerRiskLevel);
        int selectedRiskLevelIndex = Arrays.asList(RiskLevel.values()).indexOf(passedEvent.getRiskLevel());
        spinnerRiskLevel.setSelection(selectedRiskLevelIndex);

        submitChangesButton = findViewById(R.id.submitChangesButton);
        cameraBtn = findViewById(R.id.cameraBtn);
        imageViewFromCamera = findViewById(R.id.photo_view);
        //set only if its not null
        cameraImage = null;



        // Set click listener for the Add photo button
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // Set click listener for the submit changes button
        submitChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditEvent.this);
                builder.setTitle("Submit changes?");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retrieve the values entered in the form fields
                        String eventType = spinnerEventType.getSelectedItem().toString();
                        String description = editTextDescription.getText().toString();
                        String location = editTextLocation.getText().toString();
                        String area = spinnerArea.getSelectedItem().toString();
                        String riskLevel = spinnerRiskLevel.getSelectedItem().toString();
                        if(!check_if_fill_all_fields(eventType, description, location, area, riskLevel)){
                            return;
                        }
                        Date currentDate = new Date();
                        String user = getIntent().getStringExtra("username");
                        //create object of event to send him to eventmangment class
                        Event event = new Event(eventId, EventType.valueOf(eventType), description, location, Area.valueOf(area), RiskLevel.valueOf(riskLevel), currentDate, user, cameraImage);
                        updateEventInFireBase(event, eventId);
                        // Return to the EventManagementActivity
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    //this function is to show the image when we cauptre
    //The onActivityResult() method is called when the camera application finishes and returns the result. It receives three parameters: requestCode, resultCode, and data
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewFromCamera.setImageBitmap(imageBitmap);
            this.cameraImage = imageBitmap;
        }
    }
    //validation to the form
    public boolean check_if_fill_all_fields(String eventType, String description, String location, String area, String riskLevel){
        if (eventType.equals("Event Type")){
            Toast.makeText(EditEvent.this, "Please fill Event Type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.equals("")){
            Toast.makeText(EditEvent.this, "Please fill Description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.equals("")){
            Toast.makeText(EditEvent.this, "Please fill location", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (area.equals("Area")){
            Toast.makeText(EditEvent.this, "Please fill Area", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (riskLevel.equals("Risk Level")){
            Toast.makeText(EditEvent.this, "Please fill Risk Level", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void updateEventInFireBase(Event event, String eventId){
//i edit all the attribue of the event in the fb
        Map<String, Object> fieldUpdates = new HashMap<>();
        fieldUpdates.put("eventType", event.getEventType());
        fieldUpdates.put("description", event.getDescription());
        fieldUpdates.put("location", event.getLocation());
        fieldUpdates.put("area", event.getArea());
        fieldUpdates.put("riskLevel", event.getRiskLevel());
        fieldUpdates.put("user", event.getUser());
        //check if the user take a picture we take his picture and upload it to firestorage
        if ((passedEvent.getImageUrl() == null ||passedEvent.getImageUrl() == "") && event.getImage() != null) {
            Date currentDate = new Date();
            String imageurl = String.valueOf(currentDate.getYear()) + String.valueOf(currentDate.getDay()) + String.valueOf(currentDate.getMonth()) + String.valueOf(currentDate.getSeconds() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            event.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            final String imageName = imageurl;
            StorageReference imageRef = storageRef.child(imageName);
            UploadTask uploadTask = imageRef.putBytes(data);
            event.setImage(null);
            passedEvent.setImageUrl(imageurl);
        }
        fieldUpdates.put("imageUrl", passedEvent.getImageUrl());
        if(event.getImage()!=null){
            event.setImage(null);
        }

        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        firebaseDB.collection("Events")
                .document(eventId)
                .update(fieldUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        db.openDB();
                        event.setId(eventId);
                        db.updateEvent(event);
                        db.closeDB();
                    }
                });
    }

}