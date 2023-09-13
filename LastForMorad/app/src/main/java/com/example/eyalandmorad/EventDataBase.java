package com.example.eyalandmorad;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 1;

    //Start Events Table
    private static final String EVENT_ID = "id";

    private static final String EVENT_TYPE = "type";
    private static final String EVENT_IMAGE = "img";
    private static final String EVENT_DESCRIPTION = "description";
    private static final String EVENT_LOCATION = "location";
    private static final String EVENT_AREA = "area";
    private static final String EVENT_RISK_LEVEL = "riskLevel";
    private static final String EVENT_USER = "user";
    private static final String EVENT_DATE = "date";

    private static final String EVENTS_TABLE = "events";
    private static final String[] EVENTS_COLUMNS = {EVENT_ID, EVENT_TYPE, EVENT_IMAGE, EVENT_DESCRIPTION, EVENT_LOCATION, EVENT_AREA, EVENT_RISK_LEVEL, EVENT_USER, EVENT_DATE};
    //End Events Table

    //Start Reports Table
    private static final String REPORTS_TABLE = "reports";
    private static final String REPORTS_ID = "reportId";
    private static final String REPORTS_EVENT_ID = "id";
    private static final String REPORTS_USER_ID = "user";
    private static final String REPORTS_ACTION = "reportAction";
    private static final String[] APPROVALS_COLUMNS = {REPORTS_ID, REPORTS_EVENT_ID, REPORTS_USER_ID, REPORTS_ACTION};
    //End Reports Table

    //Start Users Table
    private static final String USERS_TABLE = "users";
    private static final String USER_EMAIL = "userEmail";
    private static final String SCORE = "userScore";
    private static final String USER_ID = "id";
    private static final String[] USERS_COLUMNS = {USER_EMAIL, SCORE, USER_ID};
    //End Users Table

    //Start Comments Table
    private static final String COMMENTS_TABLE = "Comments";
    private static final String COMMENT_ID = "commentId";
    private static final String USER_COMMENTED = "createdUser";
    private static final String COMMENT_TEXT = "commentText";
    private static final String COMMENT_EVENT_ID = "commentEventId";
    private static final String[] COMMENTS_COLUMNS = {COMMENT_ID, USER_COMMENTED, COMMENT_TEXT, COMMENT_EVENT_ID};
    //End Comments Table


    private static EventDataBase instance;

    private Context context;
    private static SQLiteDatabase db;

    private EventDataBase(Context ctx) {
        // Call the parent class constructor to initialize the database
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        create_tables(sqLiteDatabase);
    }

    public void create_tables(SQLiteDatabase sqLiteDatabase) {
        //Create Events Table
        String createEventsTable = "CREATE TABLE IF NOT EXISTS " + EVENTS_TABLE + " ("
                + EVENT_ID + " TEXT PRIMARY KEY , "
                + EVENT_TYPE + " TEXT, "
                + EVENT_IMAGE + " BLOB, "
                + EVENT_DESCRIPTION + " TEXT, "
                + EVENT_LOCATION + " TEXT, "
                + EVENT_AREA + " TEXT, "
                + EVENT_RISK_LEVEL + " TEXT, "
                + EVENT_USER + " TEXT, "
                + EVENT_DATE + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(createEventsTable);

        // Create Reports Table
        String createReportsTable = "CREATE TABLE IF NOT EXISTS " + REPORTS_TABLE + "("
                + REPORTS_ID + " TEXT PRIMARY KEY, "
                + REPORTS_EVENT_ID + " TEXT, "
                + REPORTS_USER_ID + " TEXT, "
                + REPORTS_ACTION + " TEXT, "
                + "FOREIGN KEY(" + REPORTS_EVENT_ID + ") REFERENCES " + EVENTS_TABLE + "(" + EVENT_ID + "))";
        sqLiteDatabase.execSQL(createReportsTable);

        // Create Users Table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + "("
                + USER_EMAIL + " TEXT PRIMARY KEY, "
                + SCORE + " INTEGER, "
                + USER_ID + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(createUsersTable);

        // Create Comments Table
        String createCommentsTable = "CREATE TABLE IF NOT EXISTS " + COMMENTS_TABLE + "("
                + COMMENT_ID + " TEXT PRIMARY KEY, "
                + USER_COMMENTED + " TEXT, "
                + COMMENT_TEXT + " TEXT, "
                + COMMENT_EVENT_ID + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(createCommentsTable);

    }

    public void openDB() {
        db = getWritableDatabase();
    }
    // Get a writable database instance

    public void closeDB() {
        if (db != null) {
            // Close the database if it is not null
            db.close();
        }
    }

    public static synchronized EventDataBase getInstance(Context context) {
        if (instance == null) {
            // Create a new instance of EventDataBase if it doesn't exist
            instance = new EventDataBase(context.getApplicationContext());
        }
        return instance;
    }

    //add event to the table
    public boolean addEvent(Event event) {
        create_tables(db);
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, event.getId());
        values.put(EVENT_TYPE, event.getEventType().toString());
        values.put(EVENT_DESCRIPTION, event.getDescription());
        values.put(EVENT_LOCATION, event.getLocation());
        values.put(EVENT_AREA, event.getArea().toString());
        values.put(EVENT_RISK_LEVEL, event.getRiskLevel().toString());
        values.put(EVENT_USER, event.getUser());
        values.put(EVENT_DATE, event.getDate().toString());
        values.put(EVENT_IMAGE, event.convertImageToByte());

        try {
            long db_result = db.insertOrThrow(EVENTS_TABLE, null, values);
            return db_result != -1;
        } catch (SQLiteException e) {
            Log.e("testt", "Error adding event: " + e.getLocalizedMessage());
            return false;
        }
    }

    //remove event from the table
    public boolean removeEvent(String eventId) {
        String selection = EVENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(eventId)};
        int rowsDeleted = db.delete(EVENTS_TABLE, selection, selectionArgs);
        return rowsDeleted > 0;
    }

    //get event from the table
    public Event getEventById(String eventId) {
        String selection = EVENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(eventId)};
        Event event = null;
        Cursor cursor = null;
        try {
            // Execute the query to retrieve the event with the specified ID
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    // Extract the event details from the cursor
                    String eventID = cursor.getString(0);
                    EventType eventType = EventType.valueOf(cursor.getString(1));
                    Bitmap eventImage = null;
                    if (cursor.getBlob(2) != null) {
                        eventImage = BitmapFactory.decodeByteArray(cursor.getBlob(2), 0, cursor.getBlob(2).length);
                    }

                    String eventDescription = cursor.getString(3);
                    String eventLocation = cursor.getString(4);
                    Area eventArea = Area.valueOf(cursor.getString(5));
                    RiskLevel eventRiskLevel = RiskLevel.valueOf(cursor.getString(6));
                    String eventUserName = cursor.getString(7);
                    String eventDateStr = cursor.getString(8);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Date eventDate = dateFormat.parse(eventDateStr);
                    // Create the Event object with the retrieved details
                    event = new Event(eventID, eventType, eventDescription, eventLocation, eventArea, eventRiskLevel, eventDate, eventUserName, "");
                    event.setId(eventID);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return event;
    }

    public boolean updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_TYPE, event.getEventType().toString());
        values.put(EVENT_LOCATION, event.getLocation());
        values.put(EVENT_AREA, event.getArea().toString());
        values.put(EVENT_RISK_LEVEL, event.getRiskLevel().toString());
        values.put(EVENT_DESCRIPTION, event.getDescription());
        values.put(EVENT_DATE, event.getDate().toString());
        values.put(EVENT_IMAGE, event.convertImageToByte());

        String[] selectionArgs = {String.valueOf(event.getId())};
        int rowsAffected = db.update(EVENTS_TABLE, values, EVENT_ID + "=?", selectionArgs);
        db.close();
        return rowsAffected > 0;
    }

    // Check if event exists in local DB
    public boolean isEventExists(String eventId) {
        create_tables(db);
        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(eventId)};
        // Query the database to check if the event exists based on the specified criteria
        Cursor cursor = db.query(EVENTS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Return true if the cursor has at least one row, indicating that the reported event exists
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }


    //return all the events from the table
    public List<Event> getAllEvents() {
        List<Event> eventsList = new ArrayList<Event>();
        Cursor cursor = null;
        try {
            // Execute the query to retrieve all events from the table
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    // Extract the event details from the cursor
                    String eventID = cursor.getString(0);
                    EventType eventType = EventType.valueOf(cursor.getString(1));
                    Bitmap eventImage = null;
                    if (cursor.getBlob(2) != null) {
                        eventImage = BitmapFactory.decodeByteArray(cursor.getBlob(2), 0, cursor.getBlob(2).length);
                    }

                    String eventDescription = cursor.getString(3);
                    String eventLocation = cursor.getString(4);
                    Area eventArea = Area.valueOf(cursor.getString(5));
                    RiskLevel eventRiskLevel = RiskLevel.valueOf(cursor.getString(6));
                    String eventUserName = cursor.getString(7);
                    String eventDateStr = cursor.getString(8);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Date eventDate = dateFormat.parse(eventDateStr);
                    // Create the Event object with the retrieved details
                    Event event = new Event(eventID, eventType, eventDescription, eventLocation, eventArea, eventRiskLevel, eventDate, eventUserName, "");
                    event.setId(eventID);
                    eventsList.add(event);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return eventsList;
    }

    //get all the event that belong to the user that we get from parameter
    public List<Event> getMyEvents(String userName) {
        List<Event> eventsList = new ArrayList<Event>();
        Cursor cursor = null;
        try {
            // Define the selection criteria and arguments for the query
            String selection = "user=?";
            String[] selectionArgs = {userName};
            // Execute the query to retrieve events associated with the specified user
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    // Extract the event details from the cursor
                    String eventID = cursor.getString(0);
                    EventType eventType = EventType.valueOf(cursor.getString(1));
                    Bitmap eventImage = null;
                    if (cursor.getBlob(2) != null) {
                        eventImage = BitmapFactory.decodeByteArray(cursor.getBlob(2), 0, cursor.getBlob(2).length);
                    }

                    String eventDescription = cursor.getString(3);
                    String eventLocation = cursor.getString(4);
                    Area eventArea = Area.valueOf(cursor.getString(5));
                    RiskLevel eventRiskLevel = RiskLevel.valueOf(cursor.getString(6));
                    String eventUserName = cursor.getString(7);
                    String eventDateStr = cursor.getString(8);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Date eventDate = dateFormat.parse(eventDateStr);
                    // Create the Event object with the retrieved details
                    Event event = new Event(eventID, eventType, eventDescription, eventLocation, eventArea, eventRiskLevel, eventDate, eventUserName, eventImage);
                    event.setId(eventID);
                    eventsList.add(event);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return eventsList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // This method is called when the database needs to be upgraded to a new version.
        // Implementation of the upgrade logic should be added here if necessary.
        // The parameters `sqLiteDatabase`, `i`, and `i1` represent the database, old version, and new version respectively.
        // In this case, the method is left empty, indicating no specific upgrade actions are performed.

    }

// add reports to database
    public boolean addReportedEvents(String reportId, String eventId, String userName, String action) {
        try {
            create_tables(db);
            ContentValues values = new ContentValues();
            values.put(REPORTS_ID, reportId);
            values.put(REPORTS_EVENT_ID, eventId);
            values.put(REPORTS_USER_ID, userName);
            values.put(REPORTS_ACTION, action);
            long db_result = db.insert(REPORTS_TABLE, null, values);
            return db_result != -1;
            // Insert the reported event into the database
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }








    public boolean isReportedEventExists(String eventId, String userName, String action) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{REPORTS_TABLE});
        if (cursor != null) {
            if (!(cursor.getCount() > 0)) {
                return false;
            }
            cursor.close();
        }
        String selection = "id = ? AND user = ? AND reportAction = ?";
        String[] selectionArgs = {String.valueOf(eventId), userName, action};
        // Query the database to check if the reported event exists based on the specified criteria
        cursor = db.query(REPORTS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Return true if the cursor has at least one row, indicating that the reported event exists
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    // Remove all the reports of event when the event removed
    public void removeReportedEventByEvent(String eventId) {
        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(eventId)};
        db.delete(REPORTS_TABLE, selection, selectionArgs);
    }

    public void removeReportedEventByUser(String eventId, String username) {
        // Delete the reported event from the database based on the specified event ID and username
        String selection = "id = ? AND user = ?";
        String[] selectionArgs = {String.valueOf(eventId), username};
        db.delete(REPORTS_TABLE, selection, selectionArgs);
    }



    public List<Event> myApprovedEvents(String username) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{REPORTS_TABLE});
        if (cursor != null) {
            if (!(cursor.getCount() > 0)) {
                List<Event> e = new ArrayList<>();
                return e;
            }
            cursor.close();
        }

        List<Event> eventsList = new ArrayList<Event>();
        String selection = "user = ? And reportAction = ?";
        String[] selectionArgs = {username, "approval"};
        // Query the database to retrieve reported events that have been approved by the specified username
        cursor = db.query(REPORTS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            int eventIdColumnIndex = cursor.getColumnIndex("id");
            if (eventIdColumnIndex != -1) {
                do {
                    String eventId = cursor.getString(eventIdColumnIndex);
                    eventsList.add(getEventById(eventId));
                } while (cursor.moveToNext());
            }
        }

        cursor.close();
        return eventsList;
    }

    public int countReportedEventsByUser(String username, String action) {
        String selection = "user = ? AND reportAction = ?";
        String[] selectionArgs = {username, action};
        // Query the database to count the number of reported events for the specified username and action
        Cursor cursor = db.query(REPORTS_TABLE, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //Start Users Table

    public boolean addUser(String userEmail, int score, String id) {
        create_tables(db);
        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, userEmail);
        values.put(SCORE, score);
        values.put(USER_ID, id);

        try {
            long db_result = db.insertOrThrow(USERS_TABLE, null, values);
            return db_result != -1;
        } catch (SQLiteException e) {
            Log.e("testt", "Error adding event: " + e.getLocalizedMessage());
            return false;
        }
    }

//    get the number of the reports that the user create

    public int getMyReportsCount(String username) {
        int count = 0;
        try {
            String selectQuery = "SELECT COUNT(*) FROM Reports WHERE user = ?";
            String[] selectArgs = {username};
            Cursor cursor = db.rawQuery(selectQuery, selectArgs);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Log.e("testt", "Error retrieving reports count: " + e.getLocalizedMessage());
        }
        return count;
    }
//    if the user exsit
    public boolean isUserExists(String userEmail) {
        create_tables(db);
        String selection = "userEmail = ?";
        String[] selectionArgs = {String.valueOf(userEmail)};
        // Query the database to check if the event exists based on the specified criteria
        Cursor cursor = db.query(USERS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Return true if the cursor has at least one row, indicating that the reported event exists
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public String getUserIdByEmail(String userEmail) {
        String userId = "";
        try {
            String selectQuery = "SELECT id FROM " + USERS_TABLE + " WHERE userEmail = ?";
            String[] selectArgs = {userEmail};
            Cursor cursor = db.rawQuery(selectQuery, selectArgs);
            if (cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            }
            cursor.close();
        } catch (SQLiteException e) {
            Log.e("testt", "Error retrieving user id: " + e.getLocalizedMessage());
        }
        return userId;
    }

    //Start Comments Table
    public boolean addComment(Comment comment) {
        create_tables(db);
        ContentValues values = new ContentValues();
        values.put(COMMENT_ID, comment.getCommentId());
        values.put(COMMENT_TEXT, comment.getCommentText());
        values.put(USER_COMMENTED, comment.getCreatedUser());
        values.put(COMMENT_EVENT_ID, comment.getCommentEventId());

        try {
            long db_result = db.insertOrThrow(COMMENTS_TABLE, null, values);
            return db_result != -1;
        } catch (SQLiteException e) {
            Log.e("testt", "Error adding event: " + e.getLocalizedMessage());
            return false;
        }
    }

    // Check if Comment exists in local DB
    public boolean isCommentExists(String commentId) {
        create_tables(db);
        String selection = "commentId = ?";
        String[] selectionArgs = {String.valueOf(commentId)};
        // Query the database to check if the event exists based on the specified criteria
        Cursor cursor = db.query(COMMENTS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Return true if the cursor has at least one row, indicating that the reported event exists
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    //get the all comments by this id event
    public List<Comment> getAllComments(String eventId) {
        List<Comment> commentList = new ArrayList<Comment>();
        Cursor cursor = null;
        try {
            String selection = "commentEventId = ?";
            String[] selectionArgs = {eventId};

            // Execute the query to retrieve all Comments from the table
            cursor = db.query(COMMENTS_TABLE, COMMENTS_COLUMNS, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    // Extract the comment details from the cursor
                    String commentId = cursor.getString(0);
                    String userCommented = cursor.getString(1);
                    String commentText = cursor.getString(2);
                    String commentEventID = cursor.getString(3);

                    Comment comment = new Comment(userCommented, commentText, commentEventID);
                    comment.setId(commentId);
                    commentList.add(comment);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return commentList;
    }

    //remove comment from the table
    public boolean removeComment(String commentID) {
        String selection = COMMENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(commentID)};
        int rowsDeleted = db.delete(COMMENTS_TABLE, selection, selectionArgs);
        return rowsDeleted > 0;
    }
    //remove the comments by event id
    public boolean removeCommentsByEventId(String eventId) {
        String selection = COMMENT_EVENT_ID + " = ?";
        String[] selectionArgs = {eventId};
        int rowsDeleted = db.delete(COMMENTS_TABLE, selection, selectionArgs);
        return rowsDeleted > 0;
    }
    //edit the comment

    public boolean updateComment(String commentText, String commentID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMMENT_TEXT, commentText);

        String[] selectionArgs = {String.valueOf(commentID)};
        int rowsAffected = db.update(COMMENTS_TABLE, values, COMMENT_ID + "=?", selectionArgs);
        db.close();
        return rowsAffected > 0;
    }

    //get all the comments that belong to the user that we get from parameter
    public List<Comment> getMyComments(String userName) {
        List<Comment> comments = new ArrayList<Comment>();
        Cursor cursor = null;
        try {
            // Define the selection criteria and arguments for the query
            String selection = "createdUser=?";
            String[] selectionArgs = {userName};
            // Execute the query to retrieve events associated with the specified user
            cursor = db.query(COMMENTS_TABLE, COMMENTS_COLUMNS, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    // Extract the event details from the cursor
                    String commentsID = cursor.getString(0);
                    String createdUser = cursor.getString(1);
                    String commentText = cursor.getString(2);
                    String commentEventId = cursor.getString(3);

                    Comment comment = new Comment(createdUser, commentText, commentEventId);
                    comment.setId(commentsID);
                    comments.add(comment);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return comments;
    }
    //get the all comments from the database
    public List<Comment> getAllComments() {
        List<Comment> commentsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + COMMENTS_TABLE;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int createdUserIndex = cursor.getColumnIndex(USER_COMMENTED);
                    int commentTextIndex = cursor.getColumnIndex(COMMENT_TEXT);
                    int commentEventIdIndex = cursor.getColumnIndex(COMMENT_EVENT_ID);

                    // Check if the column exists in the cursor before getting the values
                    if (createdUserIndex >= 0 && commentTextIndex >= 0 && commentEventIdIndex >= 0) {
                        String createdUser = cursor.getString(createdUserIndex);
                        String commentText = cursor.getString(commentTextIndex);
                        String commentEventId = cursor.getString(commentEventIdIndex);

                        Comment comment = new Comment(createdUser, commentText, commentEventId);
                        commentsList.add(comment);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        return commentsList;
    }


//get the number of approval events
    public int getApprovalCountByEventId(String eventId, String reportType) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + REPORTS_TABLE + " WHERE id=? AND reportAction=?", new String[]{eventId, reportType});
        int approvalCount = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                approvalCount = cursor.getInt(0);
            }
            cursor.close();
        }

        return approvalCount;
    }
//get the number of reports by user
    public int getReportsCountByUserId(String userId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + REPORTS_TABLE + " WHERE user=?", new String[]{userId});
        int reportsCount = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                reportsCount = cursor.getInt(0);
            }
            cursor.close();
        }

        return reportsCount;
    }



}



