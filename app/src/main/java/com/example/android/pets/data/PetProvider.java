package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PetProvider extends ContentProvider {

    private PetDbHelper mDbHelper;

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // if thre is no Uri, return early
        if (uri == null) {
            throw new IllegalArgumentException("Cannot query, no URI passed!");
        }
        // Open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // match the Uri and get the result code
        int match = sUriMatcher.match(uri);

        Cursor cursor;

        // According to the result code, Perform the type of query on the pets table
        switch (match){
            case PETS:
                cursor = db.query(
                        PetContract.PetEntry.TABLE_NAME,   // The table to query
                        projection,            // The columns to return
                        selection,                  // The columns for the WHERE clause
                        selectionArgs,                  // The values for the WHERE clause
                        null,                  // Don't group the rows
                        null,                  // Don't filter by row groups
                        sortOrder);                   // The sort order
                        break;
            case PET_ID:
                if(selection.isEmpty()) {
                    //Since the Uri was sent as pointing to one of the rows the selection must be
                    // that row, even if it is empty, So
                    // (1) here the "_id" column name is selected, to have the SQL command: "WHERE _id="
                    // (
                    selection = PetContract.PetEntry._ID + "=?";
                }
                if(selectionArgs.length == 0){
                    // (2) here the row number is set as argument, to finish the SQL command: "WHERE _id=<#>"
                    selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                }
                // make the query and get the cursor back
                cursor = db.query(
                        PetContract.PetEntry.TABLE_NAME,   // The table to query
                        projection,     // The columns to return "SELECT <column1>, <column2>"
                        selection,      // The columns for the WHERE clause
                        selectionArgs,  // The values for the WHERE clause
                        null,   // Don't group the rows
                        null,    // Don't filter by row groups
                        sortOrder);     // The sort order: "ORDER BY="
                        break;
                default:
                    throw new IllegalArgumentException("Cannot query, unknown URI: " + uri);
        }
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues(contentValues);

        // Insert a new row for pet in the database, returning the ID of that new row.
        long newRowId = db.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
