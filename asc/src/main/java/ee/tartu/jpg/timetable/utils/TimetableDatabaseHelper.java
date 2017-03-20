package ee.tartu.jpg.timetable.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.Classroom;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.Form;
import ee.tartu.jpg.timetable.data.LessonPeriod;
import ee.tartu.jpg.timetable.data.Subject;
import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;

/**
 * Provides helper methods to put and receive timetables data from local database.
 */
public class TimetableDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "timetables.db";

    private static final String TABLE_META = "tt_meta";
    private static final String TABLE_DAYS = "tt_days";
    private static final String TABLE_PERIODS = "tt_periods";
    private static final String TABLE_FORMS = "tt_forms";
    private static final String TABLE_SUBJECTS = "tt_subjects";
    private static final String TABLE_CLASSROOMS = "tt_classrooms";
    private static final String TABLE_TEACHERS = "tt_teachers";
    private static final String TABLE_SCHEDULES = "tt_schedules";

    private static final String COL_TIMETABLE_ID = "TIMETABLE_ID";
    private static final String COL_DAY_ID = "DAY_ID";
    private static final String COL_PERIOD_ID = "PERIOD_ID";
    private static final String COL_FORM_ID = "FORM_ID";
    private static final String COL_SUBJECT_ID = "SUBJECT_ID";
    private static final String COL_CLASSROOM_ID = "CLASSROOM_ID";
    private static final String COL_TEACHER_ID = "TEACHER_ID";

    private static final String COL_NAME = "NAME";
    private static final String COL_SHORT_NAME = "SHORT_NAME";
    private static final String COL_MODIFIED_MILLIS = "MODIFIED_MILLIS";
    private static final String COL_START_TIME = "START_TIME";
    private static final String COL_END_TIME = "END_TIME";
    private static final String COL_COLOUR = "COLOUR";

    public TimetableDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_META + "(" + COL_TIMETABLE_ID + " TEXT PRIMARY KEY," + COL_MODIFIED_MILLIS + " INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_DAYS + "(" + COL_DAY_ID + " INTEGER," + COL_NAME + " TEXT," + COL_SHORT_NAME + " TEXT," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_DAY_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_PERIODS + "(" + COL_PERIOD_ID + " INTEGER," + COL_START_TIME + " TEXT," + COL_END_TIME + " TEXT," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_PERIOD_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_FORMS + "(" + COL_FORM_ID + " INTEGER," + COL_NAME + " TEXT," + COL_SHORT_NAME + " TEXT," + COL_TEACHER_ID + " INTEGER," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_FORM_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_SUBJECTS + "(" + COL_SUBJECT_ID + " INTEGER," + COL_NAME + " TEXT," + COL_SHORT_NAME + " TEXT," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_SUBJECT_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_CLASSROOMS + "(" + COL_CLASSROOM_ID + " INTEGER," + COL_NAME + " TEXT," + COL_SHORT_NAME + " TEXT," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_CLASSROOM_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_TEACHERS + "(" + COL_TEACHER_ID + " INTEGER," + COL_NAME + " TEXT," + COL_SHORT_NAME + " TEXT," + COL_COLOUR + " INTEGER," + COL_TIMETABLE_ID + " INTEGER, PRIMARY KEY (" + COL_TEACHER_ID + ", " + COL_TIMETABLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_SCHEDULES + "(" + COL_DAY_ID + " INTEGER," + COL_PERIOD_ID + " INTEGER," + COL_CLASSROOM_ID + " INTEGER," + COL_TEACHER_ID + " INTEGER," + COL_SUBJECT_ID + " INTEGER," + COL_FORM_ID + " TEXT," + COL_TIMETABLE_ID + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_META);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERIODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULES);
        onCreate(db);
    }

    public boolean insertTimetableMeta(String timetable_id, long modified) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TIMETABLE_ID, timetable_id);
        contentValues.put(COL_MODIFIED_MILLIS, modified);
        long result = db.insertWithOnConflict(TABLE_META, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean removeTimetable(String timetable_id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_META, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_DAYS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_PERIODS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_FORMS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_SUBJECTS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_CLASSROOMS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_TEACHERS, COL_TIMETABLE_ID + "=?", new String[]{timetable_id})
                + db.delete(TABLE_SCHEDULES, COL_TIMETABLE_ID + "=?", new String[]{timetable_id});
        return result > 0;
    }

    public boolean insertTimetableDay(int id, String name, String shortname, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DAY_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_SHORT_NAME, shortname);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_DAYS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertTimetablePeriod(int id, String startTime, String endTime, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PERIOD_ID, id);
        contentValues.put(COL_START_TIME, startTime);
        contentValues.put(COL_END_TIME, endTime);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_PERIODS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertTimetableForm(int id, String name, String shortname, int teacherid, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FORM_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_SHORT_NAME, shortname);
        contentValues.put(COL_TEACHER_ID, teacherid);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_FORMS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertTimetableSubject(int id, String name, String shortname, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SUBJECT_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_SHORT_NAME, shortname);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_SUBJECTS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertTimetableClassroom(int id, String name, String shortname, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CLASSROOM_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_SHORT_NAME, shortname);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_CLASSROOMS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean insertTimetableTeacher(int id, String name, String shortname, int colour, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TEACHER_ID, id);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_SHORT_NAME, shortname);
        contentValues.put(COL_COLOUR, colour);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_TEACHERS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean clearSchedules(String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.delete(TABLE_SCHEDULES, COL_TIMETABLE_ID + "=?", new String[]{timetableID});
        return count != 0;
    }

    public boolean insertTimetableSchedule(int dayid, int periodid, int classroomid, int teacherid, int subjectid, String formids, String timetableID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DAY_ID, dayid);
        contentValues.put(COL_PERIOD_ID, periodid);
        contentValues.put(COL_CLASSROOM_ID, classroomid);
        contentValues.put(COL_TEACHER_ID, teacherid);
        contentValues.put(COL_SUBJECT_ID, subjectid);
        contentValues.put(COL_FORM_ID, formids);
        contentValues.put(COL_TIMETABLE_ID, timetableID);
        long result = db.insertWithOnConflict(TABLE_SCHEDULES, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public List<Day> requestDays(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<Day> days = new ArrayList<>();
        if (c.moveToFirst())
            do {
                days.add(new Day(c, timetable));
            } while (c.moveToNext());
        c.close();
        return days;
    }

    public List<LessonPeriod> requestPeriods(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PERIODS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<LessonPeriod> periods = new ArrayList<>();
        if (c.moveToFirst())
            do {
                periods.add(new LessonPeriod(c, timetable));
            } while (c.moveToNext());
        c.close();
        return periods;
    }

    public List<Form> requestForms(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FORMS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<Form> forms = new ArrayList<>();
        if (c.moveToFirst())
            do {
                forms.add(new Form(c, timetable));
            } while (c.moveToNext());
        c.close();
        return forms;
    }

    public List<Subject> requestSubjects(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SUBJECTS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<Subject> subjects = new ArrayList<>();
        if (c.moveToFirst())
            do {
                subjects.add(new Subject(c, timetable));
            } while (c.moveToNext());
        c.close();
        return subjects;
    }

    public List<Classroom> requestClassrooms(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_CLASSROOMS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<Classroom> classrooms = new ArrayList<>();
        if (c.moveToFirst())
            do {
                classrooms.add(new Classroom(c, timetable));
            } while (c.moveToNext());
        c.close();
        return classrooms;
    }

    public List<Teacher> requestTeachers(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TEACHERS + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<Teacher> teachers = new ArrayList<>();
        if (c.moveToFirst())
            do {
                teachers.add(new Teacher(c, timetable));
            } while (c.moveToNext());
        c.close();
        return teachers;
    }

    public List<TimeTableSchedule> requestSchedules(Timetable timetable) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULES + " WHERE " + COL_TIMETABLE_ID + "=?", new String[]{timetable.getId()});
        List<TimeTableSchedule> schedules = new ArrayList<>();
        if (c.moveToFirst())
            do {
                schedules.add(new TimeTableSchedule(c, timetable));
            } while (c.moveToNext());
        c.close();
        return schedules;
    }

    public List<Timetable> requestMetas() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_META, null);
        List<Timetable> timetable = new ArrayList<>();
        if (c.moveToFirst())
            do {
                timetable.add(new Timetable(c));
            } while (c.moveToNext());
        c.close();
        return timetable;
    }
}
