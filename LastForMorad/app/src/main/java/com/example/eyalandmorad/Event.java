package com.example.eyalandmorad;

import android.graphics.Bitmap;


import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event implements Serializable {

    private String id;

    private EventType eventType;

    private Bitmap image;
    private String imageUrl;
    private String description;
    private String location;
    private Area area;
    private RiskLevel riskLevel;
    private Date date;
    private String user;
    //constructor to create object of event class

    public Event(String id, EventType eventType, String description,
                 String location, Area area, RiskLevel riskLevel,
                 Date date, String user,Bitmap image) {
        this.id = "";
        this.eventType = eventType;
        this.image = image;
        this.description = description;
        this.location = location;
        this.area = area;
        this.riskLevel = riskLevel;
        this.user = user;
        this.date = date;
    }
    public Event(String id, EventType eventType, String description,
                 String location, Area area, RiskLevel riskLevel,
                 Date date, String user,String imageUrl) {
        this.id = "";
        this.eventType = eventType;
        this.imageUrl = imageUrl;
        this.description = description;
        this.location = location;
        this.area = area;
        this.riskLevel = riskLevel;
        this.user = user;
        this.date = date;
    }

    public Event() {}

    //all getters and setters of the event class
    public String getImageUrl(){
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getUser(){
        return this.user;
    }
    public Date getDate(){
        return this.date;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
    public Area getArea() {
        return area;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setArea(Area area){
        this.area = area;
    }

    public void setRiskLevel(RiskLevel riskLevel){
        this.riskLevel = riskLevel;
    }
    //function that convert image to byte to save the image in database
    public byte[] convertImageToByte(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if(this.image == null){return  null;}
        this.image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(date); // Format the date as desired
        return "Event Details:\n" +
                "Event Type: " + eventType + "\n" +
                "Description: " + description + "\n" +
                "Location: " + location + "\n" +
                "Area: " + area + "\n" +
                "Risk Level: " + riskLevel + "\n" +
                "Username: " + user + "\n" +
                "Date: " + formattedDate; // Include the formatted date
    }

}