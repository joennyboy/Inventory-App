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


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.product.CommodityContract.CommodityEntry;

import static android.content.ContentValues.TAG;
import static com.example.android.product.R.id.commodity_delete;

/**
 * {@link CommodityCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class CommodityCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link CommodityCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CommodityCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout


        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView tvQuantity = (TextView) view.findViewById(R.id.qquantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.pprice);
        Button salesButton = view.findViewById(R.id.bargain);
        final Button deleteButton = view.findViewById(commodity_delete);


        // Find the columns of commodity attributes that we're interested in
        int commodityId = cursor.getInt(cursor.getColumnIndex(CommodityEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_IMAGE);


        // Read the commodity attributes from the Cursor for the current ccommodity
        int id = cursor.getInt(cursor.getColumnIndex(CommodityEntry._ID));
        final String commodityName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int price = cursor.getInt(priceColumnIndex);

        //ToDo: If you need to display the image use this
        final String mImageUri = cursor.getString(imageColumnIndex);

        String commodityQuantity = String.valueOf(quantity) + "Available";
        String commodityPrice = "Price: £" + String.valueOf(price);

        final Uri mCurrentCommodityUri = ContentUris.withAppendedId(CommodityEntry.CONTENT_URI, commodityId);

        Log.d(TAG, "genero Uri:" + mCurrentCommodityUri + "Commodity name: " + commodityName + "id:" + id);

        nameTextView.setText(commodityName);
        tvQuantity.setText(commodityQuantity);
        tvPrice.setText(commodityPrice);

        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, commodityName + "quantity=" + quantity);
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int qty = quantity;
                    values.put(CommodityEntry.COLUMN_COMMODITY_QUANTITY, --qty);

                    context.getContentResolver().update(mCurrentCommodityUri, values, null, null);
                } else {
                    Toast.makeText(context, " No Quantity available", Toast.LENGTH_SHORT).show();
                }
            }


        });
    }
}


