package com.example.eyalandmorad;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {
    private ListView eventListView;
    private List<Event> eventList;
    private EventAdapter eventAdapter;
    private String userName;
    private FireBase fb;

    public ReportFragment(String userName) {
        this.userName = userName;
        fb = new FireBase();
    }

    public static ReportFragment newInstance(String userName) {
        // Create a new instance of the ReportFragment with the specified userName
        ReportFragment fragment = new ReportFragment(userName);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment_report layout for the fragment's view
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        // Get a reference to the eventListView from the inflated view
        eventListView = view.findViewById(R.id.myEventsListView);
        Context context = getContext();
        // Create an instance of the EventDataBase
        EventDataBase db = EventDataBase.getInstance(getContext());
        db.openDB();
        eventList = db.getMyEvents(userName);
        db.closeDB();
        // Create an instance of the EventAdapter with the context, eventList, database, and userName
        eventAdapter = new EventAdapter(context, eventList,db, userName);


        eventListView.setAdapter(eventAdapter);

        return view;
    }

    //this function reRender the view after event edited or added
    public void reRender(boolean isConnectedToInternet) {
        Context context = getContext();
        if (isConnectedToInternet){
            eventList = fb.getMyEventsFromFB(userName, eventAdapter);
        } else{
            EventDataBase db = EventDataBase.getInstance(context);
            // Open the database
            db.openDB();
            // Retrieve all my events from the database
            eventList = db.getMyEvents(userName);
            // Close the database
            db.closeDB();
        }

        // Clear the eventAdapter and add all events from the eventList
        eventAdapter.clear();
        eventAdapter.addAll(eventList);

        // Notify the eventAdapter that the data set has changed
        eventAdapter.notifyDataSetChanged();
        eventAdapter.reRenderAdapter();
    }

}
