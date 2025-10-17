package com.example.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "FrequentTransactions.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Receivers";

    // Columns
    private static final String COL_RECEIVER = "Receiver";
    private static final String COL_GROUP = "GroupName";
    private static final String COL_COLOUR = "Colour";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_RECEIVER + " TEXT PRIMARY KEY, " +
                COL_GROUP + " TEXT, " +
                COL_COLOUR + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 1️⃣ Add a row
    public boolean addRow(String receiver, String groupName, String colour) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RECEIVER, receiver);
        values.put(COL_GROUP, groupName);
        values.put(COL_COLOUR, colour);

        long result = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // 2️⃣ Remove a row by receiver
    public boolean removeRow(String receiver) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COL_RECEIVER + "=?", new String[]{receiver});
        return rows > 0;
    }

    // 3️⃣ Read all rows
    public Cursor readAllRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // 4️⃣ Read all rows with the same group
    public Cursor readRowsByGroup(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_GROUP + "=?", new String[]{groupName});
    }

    // 5️⃣ Change colour of all rows having the same group
    public boolean changeColourByGroup(String groupName, String newColour) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COLOUR, newColour);
        int rows = db.update(TABLE_NAME, values, COL_GROUP + "=?", new String[]{groupName});
        return rows > 0;
    }

    // 6️⃣ Delete all rows having the same group
    public boolean deleteRowsByGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COL_GROUP + "=?", new String[]{groupName});
        return rows > 0;
    }
}
