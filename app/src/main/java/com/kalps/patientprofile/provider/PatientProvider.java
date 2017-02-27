package com.kalps.patientprofile.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.kalps.patientprofile.utils.Utils;

import java.sql.SQLException;
import java.util.HashMap;

public class PatientProvider extends ContentProvider {


    private static final String PROVIDER_NAME = "com.provider.patientprovider";
    private static final String URL = "content://" + PROVIDER_NAME + "/patients";
    private static final String URL1 = "content://" + PROVIDER_NAME + "/patientsimages";
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final Uri CONTENT_URI1 = Uri.parse(URL1);
    public static final String NAME = "name";
    public static final String MRD_NO = "mrd";
    public static final String SEX = "sex";
    public static final String DOB = "dob";
    public static final String REF_PHYSICIAN = "ref_physician";
    public static final String HOSPITAL = "hospital";
    public static final String DIAGNOSIS = "diagnosis";
    public static final String FOLLOWUP_DATE = "followup_date";
    public static final String EXAMINATION_DATE = "examination_date";
    public static final String COMMENTS = "comments";
    public static final String IMAGES = "image";
    public static final String TIMESTAMP = "timestamp";

    private static HashMap<String, String> PATIENTS_PROJECTION_MAP;

    static final int PATIENTS = 1;
    static final int PATIENTS_ID = 2;
    static final int PATIENTS_IMAGES = 3;
    static final int PATIENTS_IMAGES_ID = 4;
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "patients", PATIENTS);
        uriMatcher.addURI(PROVIDER_NAME, "patients/#", PATIENTS_ID);
        uriMatcher.addURI(PROVIDER_NAME, "patientsimages", PATIENTS_IMAGES);
        uriMatcher.addURI(PROVIDER_NAME, "patientsimages/#", PATIENTS_IMAGES_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "patients";
    static final String PATIENTS_TABLE_NAME = "patients";
    static final String PATIENTS_IMAGES_TABLE_NAME = "patientsimages";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_PATIENTS_TABLE =
            " CREATE TABLE " + PATIENTS_TABLE_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " mrd VARCHAR NOT NULL UNIQUE,  " +
                    " name TEXT NOT NULL, " +
                    " sex TEXT NOT NULL, " +
                    " dob DATE , " +
                    " ref_physician TEXT NOT NULL, " +
                    " hospital TEXT NOT NULL, " +
                    " diagnosis TEXT NOT NULL, " +
                    " followup_date DATE NOT NULL, " +
                    " examination_date DATE NOT NULL, " +
                    " comments TEXT, " +
                    " sample_1 TEXT, " +
                    " sample_2 TEXT, " +
                    " sample_3 TEXT, " +
                    " sample_4 TEXT, " +
                    " sample_5 TEXT, " +
                    " sample_6 TEXT, " +
                    " sample_7 TEXT, " +
                    " sample_8 TEXT, " +
                    " sample_9 TEXT, " +
                    " sample_10 TEXT, " +
                    " sample_int_1 INTEGER, " +
                    " sample_int_2 INTEGER, " +
                    " sample_int_3 INTEGER, " +
                    " sample_date_1 DATE , " +
                    " sample_date_2 DATE , " +
                    " sample_date_3 DATE , " +
                    " sample_date_4 DATE , " +

                    " timestamp VARCHAR);";
    static final String CREATE_PATIENTS_IMAGES_TABLE =
            " CREATE TABLE " + PATIENTS_IMAGES_TABLE_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " mrd VARCHAR , " +
                    " image TEXT NOT NULL);";


    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_PATIENTS_TABLE);
            try {
                db.execSQL(CREATE_PATIENTS_IMAGES_TABLE);
            } catch (android.database.SQLException e) {
                Utils.logVerbose("cannot Crete exception : " + e.getLocalizedMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + PATIENTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PATIENTS_IMAGES_TABLE_NAME);
            onCreate(db);
        }
    }

    public PatientProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case PATIENTS:
                count = db.delete(PATIENTS_TABLE_NAME, selection, selectionArgs);
                break;
            case PATIENTS_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(PATIENTS_TABLE_NAME, MRD_NO + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case PATIENTS_IMAGES:
                count = db.delete(PATIENTS_IMAGES_TABLE_NAME, selection, selectionArgs);
                break;
            case PATIENTS_IMAGES_ID:
                String id1 = uri.getPathSegments().get(1);
                count = db.delete(PATIENTS_IMAGES_TABLE_NAME, MRD_NO + " = " + id1 +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            case PATIENTS:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * Get a particular student
             */
            case PATIENTS_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Utils.logVerbose("insert : " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case PATIENTS:
                Utils.logVerbose("insert in patients");
                long rowID = db.insert(PATIENTS_TABLE_NAME, null, values);
                if (rowID > 0) {
                    Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                } else {
                    try {
                        throw new SQLException("Failed to add a record into " + uri);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            case PATIENTS_IMAGES:
                Utils.logVerbose("insert in patients images");
                long rowID1 = db.insert(PATIENTS_IMAGES_TABLE_NAME, null, values);
                Utils.logVerbose("insert in patients images rowID1 " + rowID1);
                if (rowID1 > 0) {
                    Uri _uri1 = ContentUris.withAppendedId(CONTENT_URI1, rowID1);
                    getContext().getContentResolver().notifyChange(_uri1, null);
                    return _uri1;
                } else {
                    Utils.logVerbose("Failed to add a record into");
                    try {
                        throw new SQLException("Failed to add a record into " + uri);
                    } catch (SQLException e) {
                        Utils.logVerbose("Failed to add a record Exception  " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    return null;
                }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


        switch (uriMatcher.match(uri)) {
            case PATIENTS:
                qb.setTables(PATIENTS_TABLE_NAME);
                qb.setProjectionMap(PATIENTS_PROJECTION_MAP);
                break;
            case PATIENTS_ID:
                qb.setTables(PATIENTS_TABLE_NAME);
                qb.appendWhere(MRD_NO + "=" + uri.getPathSegments().get(1));
                break;
            case PATIENTS_IMAGES:
                qb.setTables(PATIENTS_IMAGES_TABLE_NAME);
                qb.setProjectionMap(PATIENTS_PROJECTION_MAP);
            case PATIENTS_IMAGES_ID:
                qb.setTables(PATIENTS_IMAGES_TABLE_NAME);
                // qb.appendWhere("mrd" + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {

            sortOrder = null;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case PATIENTS:
                count = db.update(PATIENTS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case PATIENTS_IMAGES:
                count = db.update(PATIENTS_IMAGES_TABLE_NAME, values, selection, selectionArgs);
                break;

            case PATIENTS_ID:
                count = db.update(PATIENTS_TABLE_NAME, values, MRD_NO + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case PATIENTS_IMAGES_ID:
                count = db.update(PATIENTS_IMAGES_TABLE_NAME, values, "id" + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
