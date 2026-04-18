package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.constants.InvoiceConstants;

import java.util.ArrayList;
import java.util.List;

public class SuggestionHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SuggestionsDB";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    public static final String TABLE_OTHER_SUGGESTIONS = "other_suggestions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRIPTION = "description";

    // Create table query
    private static final String CREATE_TABLE_SUGGESTIONS =
            "CREATE TABLE " + TABLE_OTHER_SUGGESTIONS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_DESCRIPTION + " TEXT UNIQUE" +
                    ")";

    public SuggestionHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SUGGESTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTHER_SUGGESTIONS);
        onCreate(db);
    }

    // Insert a suggestion (avoid duplicates due to UNIQUE constraint)
    public long insertSuggestion(String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, description);

        // Insert will fail silently for duplicates due to UNIQUE constraint
        long id = db.insertWithOnConflict(TABLE_OTHER_SUGGESTIONS,
                null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    // Get all suggestions from database
    public List<String> getAllSuggestions() {
        List<String> suggestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_OTHER_SUGGESTIONS,
                    new String[]{COLUMN_ID, COLUMN_DESCRIPTION},
                    null, null, null, null,
                    COLUMN_DESCRIPTION + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);

                // Check if column exists in cursor
                if (descriptionIndex == -1) {
                    // If column doesn't exist, try alternative approach
                    return getSuggestionsAlternative(db);
                }

                do {
                    String description = cursor.getString(descriptionIndex);
                    if (description != null) {
                        suggestions.add(description);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return suggestions;
    }

    // Alternative method to get suggestions using rawQuery
    private List<String> getSuggestionsAlternative(SQLiteDatabase db) {
        List<String> suggestions = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT " + COLUMN_DESCRIPTION + " FROM " +
                    TABLE_OTHER_SUGGESTIONS + " ORDER BY " + COLUMN_DESCRIPTION + " ASC", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Get by column index (0 for first column)
                    String description = cursor.getString(0);
                    if (description != null) {
                        suggestions.add(description);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return suggestions;
    }

    // Check if table is empty
    public boolean isSuggestionsTableEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isEmpty = true;

        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_OTHER_SUGGESTIONS, null);
            if (cursor != null && cursor.moveToFirst()) {
                isEmpty = (cursor.getInt(0) == 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
        return isEmpty;
    }

    // Insert default suggestions if table is empty
    public void insertDefaultSuggestionsIfNeeded() {
        if (isSuggestionsTableEmpty()) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                for (String suggestion : InvoiceConstants.SUGGESTIONS) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_DESCRIPTION, suggestion);
                    db.insertWithOnConflict(TABLE_OTHER_SUGGESTIONS,
                            null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    // Debug method to check table structure
    public void debugTableStructure() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + TABLE_OTHER_SUGGESTIONS + ")", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String columnName = cursor.getString(cursor.getColumnIndex("name"));
                    String columnType = cursor.getString(cursor.getColumnIndex("type"));
                    android.util.Log.d("DB_DEBUG", "Column: " + columnName + " Type: " + columnType);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
        }
    }
}