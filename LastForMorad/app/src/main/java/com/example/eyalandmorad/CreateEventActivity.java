package com.example.eyalandmorad;



import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity {

    private Spinner spinnerEventType;
    private EditText editTextDescription;
    private EditText editTextLocation;
    private Spinner spinnerArea;
    private Spinner spinnerRiskLevel;
    private Button submitButton;
    private Bitmap cameraImage;
    private Button cameraBtn;
    private static final int REQUEST_IMAGE_CAPTURE=11;

    private ImageView imageViewFromCamera;
    private EventDataBase db = EventDataBase.getInstance(this);
    private FireBase fb;

    private FirebaseFirestore firestoreDB;
    private CollectionReference eventsCollection;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Event event;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        fb = new FireBase();
        firestoreDB=FirebaseFirestore.getInstance();
        eventsCollection = firestoreDB.collection("Events");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Initialize views
        spinnerEventType = findViewById(R.id.spinnerEventType);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerRiskLevel = findViewById(R.id.spinnerRiskLevel);
        submitButton = findViewById(R.id.submitButton);
        cameraBtn = findViewById(R.id.cameraBtn);
        imageViewFromCamera = findViewById(R.id.photo_view);
        cameraImage = null;

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                String imageurl = String.valueOf(currentDate.getYear()) + String.valueOf(currentDate.getDay()) + String.valueOf(currentDate.getMonth()) + String.valueOf(currentDate.getSeconds() + ".jpg");
                String user = getIntent().getStringExtra("username");
                //i sent 2 events  because the firebase canot take bitmap and i want to store the bitmap in the database sql
                //so i save the image url in the firebase and the bitmap in the sql
                //here i add the 2 object to firebase that add it to sql
                Event event = new Event("1", EventType.valueOf(eventType), description, location, Area.valueOf(area), RiskLevel.valueOf(riskLevel), currentDate, user, cameraImage);
                Event event2 = new Event("1", EventType.valueOf(eventType), description, location, Area.valueOf(area), RiskLevel.valueOf(riskLevel), currentDate, user, imageurl);
                if(isConnectedToInternet()){
                    fb.addEventToFireBase(event, db,event2);
                } else {
                    Toast.makeText(CreateEventActivity.this, "Sorry, You can't add events without an internet connection", Toast.LENGTH_SHORT).show();
                }

                // Return to the EventManagementActivity
                Intent intent = new Intent();

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Set click listener for the submit button
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
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
    //validation for the form create events
    public boolean check_if_fill_all_fields(String eventType, String description, String location, String area, String riskLevel){
        if (eventType.equals("Event Type")){
            Toast.makeText(CreateEventActivity.this, "Please fill Event Type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.equals("")){
            Toast.makeText(CreateEventActivity.this, "Please fill Description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.equals("")){
            Toast.makeText(CreateEventActivity.this, "Please fill location", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (area.equals("Area")){
            Toast.makeText(CreateEventActivity.this, "Please fill Area", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (riskLevel.equals("Risk Level")){
            Toast.makeText(CreateEventActivity.this, "Please fill Risk Level", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //set a actionbar to this page like morad ask in the pdf homework
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuitem_add) {
            Toast.makeText(this, "You are on this page already " , Toast.LENGTH_SHORT).show();

        } else if (itemId == R.id.menuitem_sort) {
            Toast.makeText(this, "you have to go back to events Page " , Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.menuitem_filter) {
            Toast.makeText(this, "you have to go back to events Page " , Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.menuItem_my_reported_events) {
            Toast.makeText(this, "you have to go back to events Page " , Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (itemId == R.id.menuitem_approved) {
            Toast.makeText(this, "you have to go back to events Page " , Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (itemId == R.id.menuitem_ownsummary) {
            Toast.makeText(this, "you have to go back to events Page " , Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) CreateEventActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}

