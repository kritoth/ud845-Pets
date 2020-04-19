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
import android.util.Log;

public class PetProvider extends ContentProvider {
    private static final String TAG = PetProvider.class.getSimpleName();

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
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // match the Uri and get the result code
        int match = sUriMatcher.match(uri);
        // create the Uri to be returned
        Uri insertedRowUri;

        switch (match) {
            case PETS:
                // Insert a new row for pet in the database, returning the ID of that new row.
                long newRowId = db.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
                if(newRowId == -1){
                    Log.e(TAG, "Failed to insert row for the Uri: " + uri);
                    return null;
                }
                insertedRowUri = ContentUris.withAppendedId(uri, newRowId);
                break;

            default:
                throw new IllegalArgumentException("Cannot insert with the URI: " + uri);
        }
        return insertedRowUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        // If there is no data to be updated return early
        if( contentValues.size() == 0) return 0;

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // match the Uri and get the result code
        int match = sUriMatcher.match(uri);
        // create the Uri to be returned
        int numberOfRowsUpdated;

        switch (match) {
            case PETS:
                // Insert a new row for pet in the database, returning the ID of that new row.
                numberOfRowsUpdated = db.update(
                        PetContract.PetEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                if(numberOfRowsUpdated == -1){
                    Log.e(TAG, "Failed to insert row for the Uri: " + uri);
                    return numberOfRowsUpdated;
                }
                break;

            case PET_ID:
                if(selection.isEmpty()) {
                    //Since the Uri was sent as pointing to one of the rows the selection must be
                    // that row, even if it is empty, So
                    // (1) here the "_id" column name is selected, to have the SQL command: "WHERE _id="
                    selection = PetContract.PetEntry._ID + "=?";
                }
                if(selectionArgs.length == 0){
                    // (2) here the row number is set as argument, to finish the SQL command: "WHERE _id=<#>"
                    selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                }
                // make the query and get the cursor back
                numberOfRowsUpdated = db.update(
                        PetContract.PetEntry.TABLE_NAME,   // The table to query
                        contentValues,   // Values to update
                        selection,       // The columns for the WHERE clause
                        selectionArgs);  // The values for the WHERE clause
                break;

            default:
                throw new IllegalArgumentException("Cannot update with the URI: " + uri);
        }
        return numberOfRowsUpdated;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                return database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
