package com.example.eyalandmorad;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class SummaryFragment extends Fragment {

    private EventDataBase db;
    private String userName;

    private ArrayAdapter<String> commentAdapter;

    private ListView commentsListView;
    private TextView reportedTextView;
    private TextView approvedTextView;
    private TextView rejectedTextView;
    private TextView pointsFromReported;
    private TextView pointsFromEvents;
    private TextView title;

    public SummaryFragment(String userName) {
        this.userName = userName;
    }

    public static SummaryFragment newInstance(String userName) {
        SummaryFragment fragment = new SummaryFragment(userName);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.summary_fragment, container, false);
        // Find the required views in the layout
        reportedTextView = view.findViewById(R.id.reportEvents);
        pointsFromEvents = view.findViewById(R.id.pointsFromEvents);
        approvedTextView = view.findViewById(R.id.approvedEvents);
        rejectedTextView = view.findViewById(R.id.rejectedEvents);
        pointsFromReported = view.findViewById(R.id.pointsFromReported);
        title = view.findViewById(R.id.summaryTitle);
        commentsListView = view.findViewById(R.id.commentsListView);
        List<Comment> commentsList = new ArrayList<>();
        List<String> commentsList2 = new ArrayList<>();
        Context context = getContext();
        db = EventDataBase.getInstance(context);
        db.openDB();
        commentsList = db.getMyComments(userName);
        db.closeDB();
        for (Comment p:commentsList) {
            commentsList2.add(p.getCommentText());

        }
        commentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, commentsList2);
        commentsListView.setAdapter(commentAdapter);
        // Set the title text using the provided username
        title.setText(getResources().getString(R.string.summary_title, userName));

        reRender();
        return view;
    }


    //this function reRender the view after event edited or added
    public void reRender() {
        Context context = getContext();
        EventDataBase db = EventDataBase.getInstance(context);
        // Open the database
        db.openDB();

        int reportsPointsSum  = db.getMyReportsCount(userName)*3;

        // Set the reported events count text
        reportedTextView.setText(getResources().getString(R.string.reported_events_number) +" " + db.getMyEvents(userName).size());
        // Set the approved events count text
        approvedTextView.setText(getResources().getString(R.string.approved_events_number) + " " + db.countReportedEventsByUser(userName, "approval"));
        // Set the rejected events count text
        rejectedTextView.setText(getResources().getString(R.string.rejected_events_number) + " " + db.countReportedEventsByUser(userName, "rejected"));
        // Set the rejected events count text
        pointsFromReported.setText(getResources().getString(R.string.points_from_reports) + " " + PointsByreport());
        pointsFromEvents.setText("number of events that aprroved:" + " " +Pointbyusers() );

        // Close the database
        db.closeDB();

    }

    public int Pointbyusers(){
        Context context = getContext();
        db = EventDataBase.getInstance(context);
        db.openDB();
        List<Event>array = db.getMyEvents(userName);
        int totalPoint= 0;

        for(Event e:array){
            int aprroved = db.getApprovalCountByEventId(e.getId(),"approval");
            int reported = db.getApprovalCountByEventId(e.getId(),"rejected");
            int sum = aprroved+ reported;
            if((int) ((aprroved / (double) sum) * 100)>=70){
                totalPoint += 10;
                }
            }
        db.closeDB();
        return totalPoint;

    }
    public int PointsByreport(){
        Context context = getContext();
        db = EventDataBase.getInstance(context);
        db.openDB();
        int count = (db.getReportsCountByUserId(userName)*3);
        return count;
    }




}
