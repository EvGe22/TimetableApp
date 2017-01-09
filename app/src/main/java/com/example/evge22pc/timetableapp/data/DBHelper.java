package com.example.evge22pc.timetableapp.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;


public class DBHelper extends SQLiteOpenHelper {

    /** The name of the database file on the file system */
    private static final String DATABASE_NAME = "Schedule";
    /** The version of the database that this class understands. */
    private static final int DATABASE_VERSION = 2;
    /** Keep track of context so that we can load SQL from string resources */
    private final Context mContext;

    /**
     * Provides self-contained query-specific cursor for Classes.
     * The query and all accessor methods are in the class.
     */
    public static class ClassesCursor extends SQLiteCursor {
        /** The query for this cursor */
        private static final String QUERY =
                "SELECT id, int_id, week_day, week, num, name, class_type, class, teacher, homework "+
                        "FROM classes " +
                        "WHERE week_day = %d AND week %s " +
                        "ORDER BY num";

        public ClassesCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(driver, editTable, query);
        }
        /** Cursor constructor */

        /** Private factory class necessary for rawQueryWithFactory() call */
        private static class Factory implements SQLiteDatabase.CursorFactory{
            @Override
            public Cursor newCursor(SQLiteDatabase db,
                                    SQLiteCursorDriver driver, String editTable,
                                    SQLiteQuery query) {
                return new ClassesCursor(driver, editTable, query);
            }
        }

        /** Accessor functions -- one per database column */
        public int getId(){return getInt(getColumnIndexOrThrow("id"));}
        public int getIntId(){return getInt(getColumnIndex("int_id"));}
        public int getNum(){return getInt(getColumnIndexOrThrow("num"));}
        public String getName(){return getString(getColumnIndexOrThrow("name"));}
        public String getTeacher(){return getString(getColumnIndexOrThrow("teacher"));}
        public int getWeek(){return getInt(getColumnIndexOrThrow("week"));}
        public int getWeekDay(){return getInt(getColumnIndexOrThrow("week_day"));}
        public int getClassType(){return getInt(getColumnIndexOrThrow("class_type"));}
        public String getClassNum(){return getString(getColumnIndexOrThrow("class"));}
        public String getHomework(){return getString(getColumnIndexOrThrow("homework"));}

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getNum()).append(". ").append(getName()).append(
                    (getClassType()==0) ? " практ. " : " лекц. ").append(
                    " ауд. ").append(getClassNum());
            return stringBuilder.toString();
        }
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    /**
     * Execute all of the SQL statements in the String[] array
     * @param db The database on which to execute the statements
     * @param sql An array of SQL statements to execute
     */
    private void execMultipleSQL(SQLiteDatabase db, String[] sql){
        for( String s : sql )
            if (s.trim().length()>0)
                db.execSQL(s);
    }

    /**
     * Creates the table in DB
     * @param db
     */
    private void createTable(SQLiteDatabase db){
        MyLog.v("Creating DB");
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE classes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "int_id INTEGER UNIQUE, " +
                    "week_day INTEGER, " +
                    "week INTEGER, " +
                    "num INTEGER, " +
                    "name TEXT, " +
                    "class_type INTEGER, " +
                    "class TEXT, " +
                    "teacher TEXT, " +
                    "homework TEXT);");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS classes");
        createTable(db);
    }

    /**
     * Adds the class to DB
     * @param obj The JSONObject got from the external DB
     */
    public void addClass(JSONObject obj){
        try{
            String sql = String.format(
                    "INSERT INTO classes (int_id, week_day, week, num, name, class_type, class, teacher, homework) " +
                                 "VALUES (  '%d',     '%d',  '%d','%d','%s',       '%d',  '%s',    '%s',     '%s')",
                    obj.getInt("id"), obj.getInt("week_day"),
                    obj.getInt("week"), obj.getInt("num"),
                    obj.getString("subject_name"), obj.getInt("class_type"),
                    obj.getString("class"), obj.getString("teacher"),
                    obj.getString("homework")
            );
            getWritableDatabase().execSQL(sql);
        } catch (JSONException e) {
            Log.e("Error writing new job", e.toString());
        }
    }

    /**
     * Checks if DB contains a class with such int_id
     * @param int_id The id of the class on the external DB
     */
    public int contains(String int_id){
        String sql = String.format(
                "SELECT id, int_id " +
                        "FROM classes " +
                        "WHERE int_id = %s",
                int_id);
        Cursor cursor = getWritableDatabase().rawQuery(sql,null);  //will this even work? I hope so
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            return cursor.getInt(cursor.getColumnIndex("id"));
        } else {
            return -1;
        }

    }

    /**
     * Updates the class with new data
     * @param id The id of the class on DB
     * @param obj The JSONObject got from the external DB
     */
    public void updateClass(String id, JSONObject obj){
        try {
            String sql = String.format(
                    "UPDATE classes " +
                            "SET name = '%s', " +
                            "num = '%d', " +
                            "week = '%d', " +
                            "week_day = '%d', " +
                            "class_type = '%s', " +
                            "class = '%s', " +
                            "teacher = '%s', " +
                            "homework = '%s' " +
                            "WHERE id = '%s' ",
                    obj.getString("subject_name"),
                    obj.getInt("num"), obj.getInt("week"),
                    obj.getInt("week_day"), obj.getString("class_type"),
                    obj.getString("class"), obj.getString("teacher"),
                    obj.getString("homework"),  id
                    );
            MyLog.v(sql);
            getWritableDatabase().execSQL(sql);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the class from db
     * @param id The id of the class on DB
     */
    public void deleteClass(long id){
        String sql = String.format(
                "DELETE FROM classes " +
                "WHERE id = '%d' ",
                id);
        getWritableDatabase().execSQL(sql);
    }

    /**
     * Returns ClassesCursor according to input data
     * @param week The indicator of the week being even or not even, must be either "<= 1" or ">= 1"
     * @param week_day № of the day of the week
     */
    public ClassesCursor getClasses(int week_day, String week){
        String sql = String.format(ClassesCursor.QUERY, week_day, week);
        ClassesCursor cursor = (ClassesCursor) getReadableDatabase().rawQueryWithFactory(
                new ClassesCursor.Factory(),
                sql,
                null, null
        );
        return cursor;
    }

    public void dropTable(){
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS classes");
        createTable(getWritableDatabase());
    }


}