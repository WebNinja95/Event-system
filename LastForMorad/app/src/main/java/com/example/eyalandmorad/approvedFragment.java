package com.example.eyalandmorad;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import androidx.fragment.app.Fragment;





import java.util.List;

public class approvedFragment extends Fragment {
    private ListView eventListView;
    private List<Event> eventList;
    private EventAdapter eventAdapter;
    private String userName;
    private FireBase fb;

    public approvedFragment(String userName) {
        this.userName = userName;
        fb = new FireBase();
    }

    public static approvedFragment newInstance(String userName) {
        approvedFragment fragment = new approvedFragment(userName);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approved, container, false);
        eventListView = view.findViewById(R.id.approvedEventsListView);
        Context context = getContext();
        EventDataBase db = EventDataBase.getInstance(getContext());
        db.openDB();
        eventList = db.myApprovedEvents(userName);
        db.closeDB();

        //set all the approved event by call the function in the database class that filter the event and return the approved events
        /*eventAdapter = new EventAdapter(context, eventList,db, userName);
        eventAdapter.notifyDataSetChanged();

        eventListView.setAdapter(eventAdapter);*/

        // Check if the eventList is empty
        if (!eventList.isEmpty()) {
            eventAdapter = new EventAdapter(context, eventList, db, userName);
            eventListView.setAdapter(eventAdapter);
        }

        return view;
    }

    //this function reRender the view after event edited or added
    public void reRender(boolean isConnectedToInternet) {
        Context context = getContext();
        if (isConnectedToInternet){
            eventList = fb.getApprovedEventsFromFB(userName, eventAdapter);
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
