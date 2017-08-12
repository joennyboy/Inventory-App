/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.product;

import com.example.android.product.CommodityContract.CommodityEntry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.android.product.CommodityContract.CommodityEntry.COLUMN_COMMODITY_PRICE;
import static com.example.android.product.CommodityContract.CommodityEntry.COLUMN_COMMODITY_QUANTITY;

/**
 * {@link ContentProvider} for Commodity app.
 */
public class CommodityProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CommodityProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int COMMODITYS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int COMMODITY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(CommodityContract.CONTENT_AUTHORITY, CommodityContract.PATH_COMMODITYS, COMMODITYS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(CommodityContract.CONTENT_AUTHORITY, CommodityContract.PATH_COMMODITYS + "/#", COMMODITY_ID);
    }

    /**
     * Database helper object
     */
    private CommodityDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new CommodityDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITYS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(CommodityEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case COMMODITY_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = CommodityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CommodityEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITYS:
                return insertCommodity(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCommodity(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(CommodityEntry.COLUMN_COMMODITY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("A name is required for the Commodity");
        }

        // Check that the gender is valid
        Integer price = values.getAsInteger(COLUMN_COMMODITY_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Commodity requires valid commodity");
        }
// Check that the quantity is valid
        Integer quantity = values.getAsInteger(COLUMN_COMMODITY_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Commodity requires valid quantity");
        }
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer sold = values.getAsInteger(CommodityEntry.COLUMN_COMMODITY_SOLD);
        if (sold != null && sold < 0) {
            throw new IllegalArgumentException("valid number of commodity sold");
        }
        {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentCommodityUri will already identify the correct row in the database that
            // we want to modify.

        }


        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(CommodityEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITYS:
                return updateCommodity(uri, contentValues, selection, selectionArgs);
            case COMMODITY_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = CommodityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCommodity(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateCommodity(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(CommodityEntry.COLUMN_COMMODITY_NAME)) {
            String name = values.getAsString(CommodityEntry.COLUMN_COMMODITY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Commodity requires a name");
            }
        }
        // If the {@link CommodityEntry#COLUMN_COMMODITY_QUANTITY} key is present,
        // check that the gender value is valid.
        if (values.containsKey(CommodityEntry.COLUMN_COMMODITY_PRICE)) {
            Integer price = values.getAsInteger(COLUMN_COMMODITY_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Commodity requires a Cost");

            }
        }

        // If the {@link CommodityEntry#COLUMN_COMMODITY_QUANTITY} key is present,
        // check that the gender value is valid.
        if (values.containsKey(COLUMN_COMMODITY_QUANTITY)) {
            Integer quantity = values.getAsInteger(COLUMN_COMMODITY_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Commodity requires valid sales number");
            }
        }
        // If the {@link CommodityEntry#COLUMN_COMMODITY_SOLD} key is present,
        // check that the sold value is valid.
        if (values.containsKey(CommodityEntry.COLUMN_COMMODITY_SOLD)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer sold = values.getAsInteger(CommodityEntry.COLUMN_COMMODITY_SOLD);
            if (sold != null && sold < 0) {
                throw new IllegalArgumentException("Commodity requires valid sales number");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(CommodityEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITYS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CommodityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMODITY_ID:
                // Delete a single row given by the ID in the URI
                selection = CommodityEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(CommodityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITYS:
                return CommodityEntry.CONTENT_LIST_TYPE;
            case COMMODITY_ID:
                return CommodityEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
