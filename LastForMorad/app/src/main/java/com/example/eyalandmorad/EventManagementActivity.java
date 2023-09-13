package com.example.eyalandmorad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//aviv salem 315765594
    //segev grotas 318827615
public class EventManagementActivity extends AppCompatActivity {
    //all the attribue of the eventmangment class
    private List<Event> eventList;
    private boolean isAscendingOrder = false;
    private EventAdapter eventAdapter;
    private EventDataBase eventDataBase;
    private ListView eventListView;
    private String username;
    private FirebaseStorage firebaseStorage;

    private static final int CREATE_EVENT_REQUEST_CODE = 1;
    private static final int EDIT_EVENT_REQUEST_CODE = 1;

    private FireBase fb;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_management);
        username = getIntent().getStringExtra("username");
        eventDataBase = EventDataBase.getInstance(this);
        fb = new FireBase();
//        fb.deleteCommentsWithNonExistingEvents();

        // Use the username as needed
        Toast.makeText(this, "Logged in as: " + username, Toast.LENGTH_SHORT).show();

        eventList = new ArrayList<>();
        // Initialize the database helper
        eventDataBase = EventDataBase.getInstance(this);

        //List<Event> eventList = getEventsFromFireBase();
        eventAdapter = new EventAdapter(this, eventList, eventDataBase, username);
        eventListView = findViewById(R.id.eventListView);
        eventListView.setAdapter(eventAdapter);

        eventList = fb.getEventsFromFireBase(eventAdapter, eventDataBase);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    //set the actions for the option in the menu for each option
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        //move to other activty to create event to show in the listview
        if (itemId == R.id.menuitem_add) {
            if (!isConnectedToInternet()) {
                Toast.makeText(EventManagementActivity.this, "Sorry, You can't add events without an internet connection", Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(EventManagementActivity.this, CreateEventActivity.class);
            intent.putExtra("username", username);
            startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        } else if (itemId == R.id.menuitem_sort) {
            //call to sortevents function to sort event by date when u click in the menu on the sort date
            sortEvents();
            return true;
        } else if (itemId == R.id.menuitem_filter) {
            //show the fragment filter page when we click in the menu on filter events
            showFilterOptions();
            return true;
        } else if (itemId == R.id.menuItem_my_reported_events) {
            //show the fragment reported page when we click in the menu on filter events
            showReportFragment();
            return true;
        } else if (itemId == R.id.menuitem_approved) {
            showApprovedFragment();
            return true;
        } else if (itemId == R.id.menuitem_ownsummary) {
            showSummaryFragment();
            return true;
        } else if (itemId == R.id.menuitem_Logout) {
            logout();
            return true;
        } else if (itemId == R.id.menuitem_eventlist) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

            //check if now the user in fragment and if yes close it
            if (currentFragment != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(currentFragment);
                fragmentTransaction.commit();
                // Initialize the database helper
                eventDataBase = EventDataBase.getInstance(this);
                eventDataBase.openDB();

                // Initialize the event list and adapter
                eventList = eventDataBase.getAllEvents();
                eventAdapter = new EventAdapter(this, eventList, eventDataBase, username);

                // Initialize the ListView
                eventListView = findViewById(R.id.eventListView);
                eventListView.setAdapter(eventAdapter);
                eventDataBase.closeDB();

            }
            return true;
        } else if (itemId == R.id.menuitem_appsummary) {
            Intent intent = new Intent(this, AppSummaryActivity.class);
            intent.putExtra("USERNAME_KEY", username);
            // Start the AppSummaryActivity when the menu item is clicked
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//sort function to sort the events by data , when the user click its sort by the new to the oldest and reverse if he click again

    private void sortEvents() {
        // Create and show the FilterFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

        // Check if there is a currently displayed fragment
        if (currentFragment != null) {
            // Remove the current fragment
            fragmentManager.beginTransaction().remove(currentFragment).commit();
        }

        // Toggle the sorting order
        isAscendingOrder = !isAscendingOrder;
        //isAscendingOrder = false;
//        eventDataBase.openDB();
//        eventList = eventDataBase.getAllEvents();
//        eventDataBase.closeDB();
        eventList = fb.getEventsFromFireBase(eventAdapter,eventDataBase);
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                // Compare the dates based on the sorting order
                if (isAscendingOrder) {
                    Toast.makeText(EventManagementActivity.this, "sorted from new to old", Toast.LENGTH_SHORT).show();
                    return event2.getDate().compareTo(event1.getDate());
                } else {
                    Toast.makeText(EventManagementActivity.this, "sorted from old to new", Toast.LENGTH_SHORT).show();
                    return event1.getDate().compareTo(event2.getDate());
                }
            }
        });
        eventAdapter.clear();
        eventAdapter.addAll(eventList);
        eventAdapter.notifyDataSetChanged();
        eventAdapter.reRenderAdapter();
    }

//when we go back from other activity and we load the database for refersh the events

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_EVENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the events from the database
            loadEventsFromDatabase();
            Snackbar.make(findViewById(android.R.id.content), "Event modified successfully", Snackbar.LENGTH_LONG).show();
        }
        else if (requestCode == EDIT_EVENT_REQUEST_CODE && resultCode == RESULT_OK){
            loadEventsFromDatabase();
            Snackbar.make(findViewById(android.R.id.content), "Event modified successfully", Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(findViewById(android.R.id.content), "Opps, Something wrong", Snackbar.LENGTH_LONG).show();
        }
    }
    //load the events from the database to add them to adapter and show them in the events mengmantscreen

    //reload the events from database after activity results and rerenders fregments
    private void loadEventsFromDatabase() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);
        if (currentFragment instanceof FilterFragment) {
            FilterFragment filterFragment = (FilterFragment) currentFragment;
            filterFragment.reRender(isConnectedToInternet());
        } else if (currentFragment instanceof ReportFragment) {
            ReportFragment myReportedFragment = (ReportFragment) currentFragment;
            myReportedFragment.reRender(isConnectedToInternet());
        } else if (currentFragment instanceof approvedFragment){
            approvedFragment approvedEvents = (approvedFragment) currentFragment;
            approvedEvents.reRender(isConnectedToInternet());
        } else if (currentFragment instanceof SummaryFragment){
            SummaryFragment eventsSummary = (SummaryFragment) currentFragment;
            eventsSummary.reRender();
        }
        else {
            if(isConnectedToInternet()){
                fb.getEventsFromFireBase(eventAdapter, eventDataBase);
//                fb.deleteCommentsWithNonExistingEvents();
            } else{
                eventDataBase.openDB();
                eventList = eventDataBase.getAllEvents();
                eventDataBase.close();
            }
        }

        //List<Event> eventList = getEventsFromFireBase();
        eventAdapter.clear();
        eventListView.setAdapter(eventAdapter);

        eventAdapter.notifyDataSetChanged();
        eventAdapter.reRenderAdapter();
    }
    //get a call to show the fragment of the filter fragment to show the fragment that filter the events by event type,risk level,and area
    private void showFilterOptions() {
        // Create and show the FilterFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

        // Check if there is a currently displayed fragment
        if (currentFragment != null) {
            // Remove the current fragment
            fragmentManager.beginTransaction().remove(currentFragment).commit();
        }

        // Create and show the FilterFragment
        eventAdapter.clear();
        eventDataBase.closeDB();
        eventList = eventDataBase.getAllEvents();
        eventDataBase.closeDB();
        FilterFragment filterFragment = FilterFragment.newInstance(eventList, eventAdapter, username);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, filterFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    //get a call to show the fragment of the repotrtfragment to show the fragment that show all the report events of the user
    private void showReportFragment() {

        // Create and show the FilterFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

        // Check if there is a currently displayed fragment
        if (currentFragment != null) {
            // Remove the current fragment
            fragmentManager.beginTransaction().remove(currentFragment).commit();
        }

        // Create an instance of the ReportFragment
        eventAdapter.clear();
        ReportFragment reportFragment = ReportFragment.newInstance(username);

        // Get the FragmentManager and start a transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the content of the fragmentContainer with the ReportFragment
        transaction.replace(android.R.id.content, reportFragment);

        // Add the transaction to the back stack (optional)
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    //get a call to show the fragment of the approvedfragment to show the fragment that show all the approved events of the user
    private void showApprovedFragment() {

        // Create and show the FilterFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

        // Check if there is a currently displayed fragment
        if (currentFragment != null) {
            // Remove the current fragment
            fragmentManager.beginTransaction().remove(currentFragment).commit();
        }

        // Create an instance of the ReportFragment
        eventAdapter.clear();
        approvedFragment ApprovedFragment = approvedFragment.newInstance(username);

        // Get the FragmentManager and start a transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the content of the fragmentContainer with the ReportFragment
        transaction.replace(android.R.id.content, ApprovedFragment);

        // Add the transaction to the back stack (optional)
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    //get a call to show the fragment of the summaryfragment to show the fragment that show all the summary  of the user
    private void showSummaryFragment() {

        // Create and show the FilterFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(android.R.id.content);

        // Check if there is a currently displayed fragment
        if (currentFragment != null) {
            // Remove the current fragment
            fragmentManager.beginTransaction().remove(currentFragment).commit();
        }

        // Create an instance of the ReportFragment
        eventAdapter.clear();
        SummaryFragment summaryFragment = SummaryFragment.newInstance(username);

        // Get the FragmentManager and start a transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the content of the fragmentContainer with the ReportFragment
        transaction.replace(android.R.id.content, summaryFragment);

        // Add the transaction to the back stack (optional)
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    // Method to check internet connection
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) EventManagementActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

        private void logout() {
            // Sign out the user from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Redirect the user back to the login screen
            Intent intent = new Intent(EventManagementActivity.this, LoginPage.class);
            startActivity(intent);
            finish(); // Finish the EventManagementActivity to prevent the user from going back to it
        }




}
