package ee.tartu.jpg.stuudium.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataMap;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.LoginData;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;

/**
 * Provides helper methods to put and receive Stuudium data from local database.
 */
public class StuudiumDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "StuudiumDatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stuudium.db";

    private static final String TABLE_LOGIN_DATA = "s_login_data";
    private static final String TABLE_USER = "s_user";
    private static final String TABLE_PEOPLE = "s_people";
    private static final String TABLE_EVENTS = "s_events";
    private static final String TABLE_JOURNALS = "s_journals";
    private static final String TABLE_ASSIGNMENTS = "s_assignments";

    private static final String COL_ID = "ID";
    private static final String COL_SUBJECT = "SUBJECT";
    private static final String COL_PERSON_ID = "USERID";
    private static final String COL_JSON = "JSON_CONTENT";
    private static final String COL_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String COL_EXPIRES_IN = "EXPIRES_IN";
    private static final String COL_REQUEST_TIME = "REQUEST_TIME";

    public StuudiumDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LOGIN_DATA + "(" + COL_ID + " INTEGER PRIMARY KEY CHECK (" + COL_ID + " = 0), " + COL_ACCESS_TOKEN + " TEXT, " + COL_EXPIRES_IN + " INTEGER, " + COL_REQUEST_TIME + " INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_USER + "(" + COL_ID + " INTEGER PRIMARY KEY CHECK (" + COL_ID + " = 0), " + COL_JSON + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_PEOPLE + "(" + COL_ID + " TEXT PRIMARY KEY, " + COL_JSON + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + "(" + COL_ID + " TEXT, " + COL_PERSON_ID + " TEXT, " + COL_JSON + " TEXT, PRIMARY KEY (" + COL_ID + ", " + COL_PERSON_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_JOURNALS + "(" + COL_SUBJECT + " TEXT, " + COL_PERSON_ID + " TEXT, " + COL_JSON + " TEXT, PRIMARY KEY (" + COL_SUBJECT + ", " + COL_PERSON_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_ASSIGNMENTS + "(" + COL_ID + " TEXT, " + COL_PERSON_ID + " TEXT, " + COL_JSON + " TEXT, PRIMARY KEY (" + COL_ID + ", " + COL_PERSON_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        onCreate(db);
    }

    public boolean setLoginData(String accessToken, long expiresIn, long requestTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, 0);
        contentValues.put(COL_ACCESS_TOKEN, accessToken);
        contentValues.put(COL_EXPIRES_IN, expiresIn);
        contentValues.put(COL_REQUEST_TIME, requestTime);
        long result = db.insertWithOnConflict(TABLE_LOGIN_DATA, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean removeLoginData() {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_LOGIN_DATA, COL_ID + "=0", null);
        return result > 0;
    }

    public boolean setUser(String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, 0);
        contentValues.put(COL_JSON, json);
        long result = db.insertWithOnConflict(TABLE_USER, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean removeUser() {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_USER, COL_ID + "=0", null);
        return result > 0;
    }

    public boolean insertStuudiumEvent(String id, String personid, String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_PERSON_ID, personid);
        contentValues.put(COL_JSON, json);
        long result = db.insertWithOnConflict(TABLE_EVENTS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertStuudiumJournal(String subject, String personid, String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SUBJECT, subject);
        contentValues.put(COL_PERSON_ID, personid);
        contentValues.put(COL_JSON, json);
        long result = db.insertWithOnConflict(TABLE_JOURNALS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertStuudiumAssignment(String id, String personid, String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_PERSON_ID, personid);
        contentValues.put(COL_JSON, json);
        long result = db.insertWithOnConflict(TABLE_ASSIGNMENTS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertStuudiumPerson(String id, String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, id);
        contentValues.put(COL_JSON, json);
        long result = db.insertWithOnConflict(TABLE_PEOPLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean removeAllData() {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_USER, null, null)
                + db.delete(TABLE_LOGIN_DATA, null, null)
                + db.delete(TABLE_EVENTS, null, null)
                + db.delete(TABLE_ASSIGNMENTS, null, null)
                + db.delete(TABLE_JOURNALS, null, null)
                + db.delete(TABLE_PEOPLE, null, null);
        return result > 0;
    }

    public boolean removeStuudiumPerson(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, COL_PERSON_ID + "=?", new String[]{id})
                + db.delete(TABLE_ASSIGNMENTS, COL_PERSON_ID + "=?", new String[]{id})
                + db.delete(TABLE_JOURNALS, COL_PERSON_ID + "=?", new String[]{id})
                + db.delete(TABLE_PEOPLE, COL_ID + "=?", new String[]{id});
        return result > 0;
    }

    public boolean removeStuudiumEvent(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, COL_ID + "=?", new String[]{id});
        return result > 0;
    }

    public boolean removeStuudiumAssignment(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_ASSIGNMENTS, COL_ID + "=?", new String[]{id});
        return result > 0;
    }

    public boolean removeStuudiumJournal(String subject) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_JOURNALS, COL_SUBJECT + "=?", new String[]{subject});
        return result > 0;
    }

    public LoginData requestLoginData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_LOGIN_DATA, null);
        LoginData loginData = null;
        if (c.moveToFirst()) {
            String access_token = c.getString(1);
            long expires_in = c.getLong(2);
            long request_time = c.getLong(3);
            loginData = new LoginData(access_token, expires_in, request_time);
        }
        c.close();
        return loginData;
    }

    public User requestUser() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        User user = null;
        if (c.moveToFirst()) try {
            JSONObject json = new JSONObject(c.getString(1));
            user = new User(json);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }
        c.close();
        return user;
    }

    public DataMap<String, Person> requestPeople() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PEOPLE, null);
        DataMap<String, Person> events = new DataMap<>();
        if (c.moveToFirst()) do try {
            String id = c.getString(0);
            JSONObject json = new JSONObject(c.getString(1));
            events.put(id, new Person(json));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } while (c.moveToNext());
        c.close();
        return events;
    }

    public DataSet<Event> requestEvents(String personId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE " + COL_PERSON_ID + "=?", new String[]{personId});
        DataSet<Event> events = new DataSet<>();
        if (c.moveToFirst()) do try {
            JSONObject json = new JSONObject(c.getString(2));
            events.add(new Event(json));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } while (c.moveToNext());
        c.close();
        return events;
    }

    public DataSet<Assignment> requestAssignments(String personId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE " + COL_PERSON_ID + "=?", new String[]{personId});
        DataSet<Assignment> events = new DataSet<>();
        if (c.moveToFirst()) do try {
            JSONObject json = new JSONObject(c.getString(2));
            events.add(new Assignment(personId, json));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } while (c.moveToNext());
        c.close();
        return events;
    }

    public DataSet<Journal> requestJournals(String personId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_JOURNALS + " WHERE " + COL_PERSON_ID + "=?", new String[]{personId});
        DataSet<Journal> events = new DataSet<>();
        if (c.moveToFirst()) do try {
            JSONObject json = new JSONObject(c.getString(2));
            events.add(new Journal(json));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } while (c.moveToNext());
        c.close();
        return events;
    }
}
