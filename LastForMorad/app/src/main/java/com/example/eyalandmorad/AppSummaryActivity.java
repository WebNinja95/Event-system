package com.example.eyalandmorad;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AppSummaryActivity extends AppCompatActivity  {

    private TextView topUsersTextView;
    private TextView TopUserPoint;
    private EventAdapter eventAdapter;

    private TextView NumofEventperRisk;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_summary);
        topUsersTextView = findViewById(R.id.topUsersTextView);
        TopUserPoint = findViewById(R.id.TopPointuser);
        NumofEventperRisk = findViewById(R.id.NumofEventperRisk);
        ListView commentsListView = findViewById(R.id.commentsListView);
        EventDataBase db = EventDataBase.getInstance(this);
        Intent intent = getIntent();
        String receivedUsername = intent.getStringExtra("USERNAME_KEY");
        //take all the comments that in the database
        List<Comment> commentsList = db.getAllComments();
        List<Comment> commentList2 = new ArrayList<>();
        //put in new array comment all the comment exept the comments that the user do
        for(Comment c:commentsList){
            if(!(c.getCreatedUser().equals(receivedUsername)))
                commentList2.add(c);
        }
        //set in array adptar to show in list view
        CommentsAdapterList commentsAdapter = new CommentsAdapterList(this, commentList2);
        commentsListView.setAdapter(commentsAdapter);

        //Events per RiskLevel
        db.openDB();
        db.openDB();
        List<Event> events = db.getAllEvents();
        int Low = 0;
        int MID = 0;
        int High = 0;
        //loop over the events and count all the risk level option
        for (Event e:events){
            if(e.getRiskLevel().equals(RiskLevel.LOW))
                Low++;
            if(e.getRiskLevel().equals(RiskLevel.MEDIUM))
                MID++;
            if(e.getRiskLevel().equals(RiskLevel.HIGH))
                High++;
        }
        NumofEventperRisk.setText("events by risk:"+"\n" + "low events:" + Low + "\n" + "Mid:" + MID + "\n" + "High:" + High);


        //Top 10 users repotrts
        HashMap<String,Integer>TopTen = new HashMap<>();

        for (Event e: events){
            if(TopTen.containsKey(e.getUser())){
                TopTen.put(e.getUser(),TopTen.get(e.getUser())+1);
            }
            else{
                TopTen.put(e.getUser(),1);
            }
        }
        //sort all the users with there vaule of report with function
        LinkedHashMap<String, Integer> sortedMap1 = sortByValue(TopTen);
        String top = "";
        int i=0;
        for (Map.Entry<String, Integer> entry : sortedMap1.entrySet()) {
            if(i==10)
                break;
            String key = entry.getKey();
            int value = entry.getValue();
            top+= "user:" + key +" "+ "reports:" + String.valueOf(value) + "\n";
            i++;
        }
        topUsersTextView.setText("Top 10 users reports:" + "\n"+ top);

        //The user with the heighst points
        HashMap<String,Integer>userspoint = new HashMap<>();


        for (Event e: events){
            if(!(userspoint.containsKey(e.getUser())))
                userspoint.put(e.getUser(),TotalPoint(e.getUser()));
        }
        //take the first from the hashmap because its the most point because i sorted it
        LinkedHashMap<String, Integer> sortedMap = sortByValue(userspoint);
        Map.Entry<String, Integer> firstEntry = sortedMap.entrySet().iterator().next();
        db.closeDB();



        TopUserPoint.setText("The user with the most points" + "\n" + "The user:" + firstEntry.getKey() + " " +" Total Points:" + String.valueOf(firstEntry.getValue()));
    }
    //function that loop over the events and call to function in the db that return the numbers of the approbal and rejected
    public int TotalPoint(String username){
        EventDataBase eventDataBase;
        eventDataBase = EventDataBase.getInstance(this);
        eventDataBase.openDB();
        List<Event>array = eventDataBase.getMyEvents(username);
        int totalPoint= 0;

        for(Event e:array){
            int aprroved = eventDataBase.getApprovalCountByEventId(e.getId(),"approval");
            int reported = eventDataBase.getApprovalCountByEventId(e.getId(),"rejected");
            int sum = aprroved+ reported;
            if((int) ((aprroved / (double) sum) * 100)>=70){
                totalPoint += 10;
            }
        }
        totalPoint += (eventDataBase.getReportsCountByUserId(username)*3);
        eventDataBase.closeDB();
        return totalPoint;

    }
    public static LinkedHashMap<String, Integer> sortByValue(HashMap<String, Integer> hashMap) {
        // Convert hashmap to a list of entries
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(hashMap.entrySet());

        // Sort the list using a custom comparator based on values
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                // Sorting in descending order (change the order of entry2 and entry1 for ascending)
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        // Create a new LinkedHashMap to maintain the order of sorted entries
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        // Put the sorted entries into the LinkedHashMap
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }



    }



