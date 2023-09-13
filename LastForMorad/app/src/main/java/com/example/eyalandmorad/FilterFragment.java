package com.example.eyalandmorad;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment {

    private Spinner spinnerEventType;
    private Spinner spinnerArea;
    private Spinner spinnerRiskLevel;
    private Button buttonFilter;
    private ListView filteredEventListView;

    private List<Event> eventList;
    private List<Event> filteredEventList;
    private EventAdapter eventAdapter;
    private String userName;
    private FireBase fb;
    public FilterFragment(String userName) {
        this.userName = userName;
        fb = new FireBase();
    }

    public static FilterFragment newInstance(List<Event> eventList, EventAdapter eventAdapter, String userName) {
        FilterFragment fragment = new FilterFragment(userName);
        fragment.eventList = eventList;
        fragment.eventAdapter = eventAdapter;
        fragment.filteredEventList = new ArrayList<>(eventList);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_event, container, false);

        spinnerEventType = view.findViewById(R.id.EventType);
        spinnerArea = view.findViewById(R.id.Area);
        spinnerRiskLevel = view.findViewById(R.id.RiskLevel);
        buttonFilter = view.findViewById(R.id.buttonFilter);


        filteredEventListView = view.findViewById(R.id.filteredEventListView);
        Context context = getContext();
        EventDataBase db = EventDataBase.getInstance(getContext());
        db.openDB();
        eventList =   db.getAllEvents();
        db.closeDB();
        eventAdapter = new EventAdapter(context, eventList,db, userName);
        filteredEventListView.setAdapter(eventAdapter);

        // Set up the spinner adapters
        ArrayAdapter<EventType> eventTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, EventType.values());
        spinnerEventType.setAdapter(eventTypeAdapter);

        ArrayAdapter<Area> areaAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Area.values());
        spinnerArea.setAdapter(areaAdapter);

        ArrayAdapter<RiskLevel> riskLevelAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, RiskLevel.values());
        spinnerRiskLevel.setAdapter(riskLevelAdapter);

        // Set up the filter button click listener
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEvents();
            }
        });

        return view;
    }

    private void filterEvents() {
        Context context = getContext();
        EventDataBase db = EventDataBase.getInstance(getContext());

        filteredEventList.clear();

        // Get the selected values from the spinners
        String selectedEventType = spinnerEventType.getSelectedItem().toString();
        String selectedArea =  spinnerArea.getSelectedItem().toString();
        String selectedRiskLevel = spinnerRiskLevel.getSelectedItem().toString();

        // Filter the events based on the selected values
        for(Event event : eventList){
            if((event.getEventType()==EventType.valueOf(selectedEventType) || selectedEventType == "Event_Type") &&
                    (event.getArea() == Area.valueOf(selectedArea) || selectedArea == "Area") &&
                    (event.getRiskLevel() == RiskLevel.valueOf(selectedRiskLevel) || selectedRiskLevel == "Risk_Level")){
                filteredEventList.add(event);
            }
        }
        eventAdapter.clear();
        eventAdapter.addAll(filteredEventList);

        // Set the filtered events in the filteredEventAdapter
        //eventAdapter.addAll(filteredEventList);

        // Notify the adapter of the data change
        eventAdapter.notifyDataSetChanged();
    }

    //this function reRender the view after event edited or added
    public void reRender(boolean isConnectedToInternet) {
        Context context = getContext();
        EventDataBase db = EventDataBase.getInstance(context);
        if (isConnectedToInternet){
            eventList = fb.getEventsFromFireBase(eventAdapter, db);
        } else{
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

        // Filter events based on any applied filters
        filterEvents();

        // Notify the eventAdapter that the data set has changed
        eventAdapter.notifyDataSetChanged();
        eventAdapter.reRenderAdapter();
    }
}

