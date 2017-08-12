package com.example.android.product;
// reference from Pet app lecture for training purposes
// refernce from stackoverflow. codes

/**
 * Created by Etumusei on 8/2/2017.
 */

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.product.CommodityContract.CommodityEntry;

import java.io.IOException;

import static com.example.android.product.R.id.image_view;
import static com.example.android.product.R.id.name;
import static com.example.android.product.R.string.error_missing;

/**
 * Allows user to create a new pet or edit an existing one.
 **/
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_COMMODITY_LOADER = 0;
    Button dell;
    /**
     * Content URI for the current inventory, if it exists
     */
    private Uri mCurrentCommodityUri;
    /**
     * Content URI for the image
     */
    private Uri mImageUri;
    private String mLukeEmail;
    private String mLukeCommodity;
    /**
     * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
     */
    private boolean mCommodityHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCommodityHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCommodityHasChanged = true;
            return false;
        }
    };
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSoldEditText;
    private ImageView mCommodityImageView;
    private Button upQty;
    private Button downQty;
    private Button order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_commodity_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_commodity_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_commodity_quantity);
        mSoldEditText = (EditText) findViewById(R.id.edit_commodity_sales);
        mCommodityImageView = (ImageView) findViewById(R.id.image_view);
        upQty = (Button) findViewById(R.id.up_quantity);
        downQty = (Button) findViewById(R.id.down_quantity);
        order = (Button) findViewById(R.id.commodity_order);
        dell = (Button) findViewById(R.id.commodity_delete);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new commodity or editing an existing one.
        final Intent intent = getIntent();
        mCurrentCommodityUri = intent.getData();

        // Hide Image
        mCommodityImageView.setVisibility(View.VISIBLE);

        // If the intent DOES NOT contain a commodity content URI, then we know that we are
        // creating a new commodity.
        if (mCurrentCommodityUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_commodity));

            order.setVisibility(View.GONE);
            upQty.setVisibility(View.GONE);
            downQty.setVisibility(View.GONE);
            dell.setVisibility(View.GONE);


            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a commodity that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_commodity));
            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_COMMODITY_LOADER, null, this);
        }


        upQty.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // ToDo: increase quantity from the EditText
                if (TextUtils.isEmpty(mQuantityEditText.getText().toString()))
                    mQuantityEditText.setText(String.valueOf(0));
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                Toast.makeText(EditorActivity.this, "" + quantity, Toast.LENGTH_SHORT).show();
                if (quantity >= 0) {
                    quantity++;
                    mQuantityEditText.setText(String.valueOf(quantity));
                } else {
                    Toast.makeText(EditorActivity.this, " Increase the Quantity by 1", Toast.LENGTH_SHORT).show();
                }
            }
        });


        downQty.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // ToDo: decrease quantity from the EditText
                if (TextUtils.isEmpty(mQuantityEditText.getText().toString()))
                    mQuantityEditText.setText(String.valueOf(0));
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                if (quantity > 0) {
                    quantity--;
                    mQuantityEditText.setText(String.valueOf(quantity));
                } else {

                    Toast.makeText(EditorActivity.this, " Decrease the Quantity by 1", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Button to delete data
        Button dell = (Button) findViewById(R.id.commodity_delete);
        dell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });


        //button to click on photo to get images from gallery and others
        ImageButton buttonLoadImage = (ImageButton) findViewById(R.id.add_image);
        buttonLoadImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.form_pick_photos)), EXISTING_COMMODITY_LOADER);
            }
        });

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supplier();
            }
        });
    }

    public void checkWriteToExternalPerms() {
        if (Build.VERSION.SDK_INT >= 20) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {
            }
        } else {
        }


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSoldEditText.setOnTouchListener(mTouchListener);
        mCommodityImageView.setOnTouchListener(mTouchListener);

    }

    /**
     * Get user input from editor and save commodity into database.
     */
    private void saveCommodity() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String soldString = mSoldEditText.getText().toString().trim();

        // Check if this is supposed to be a new commodity
        // and check if all the fields in the editor are blank
        if (mCurrentCommodityUri == null && mImageUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(price) &&
                TextUtils.isEmpty(soldString) && TextUtils.isEmpty(quantity)) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, error_missing, Toast.LENGTH_LONG).show();
            // No change has been made so we can return
            // if name field is empty
            return;
        } else if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, error_missing, Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, error_missing, Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(quantity)) {
            Toast.makeText(this, error_missing, Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(soldString)) {
            Toast.makeText(this, error_missing, Toast.LENGTH_LONG).show();
            return;

        } else if (mImageUri == null) {
            Toast.makeText(this, R.string.image_required, Toast.LENGTH_LONG).show();
            return;
        } else {
            // saves comodity here because it passed all the validation checks

        }


        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CommodityEntry.COLUMN_COMMODITY_NAME, nameString);
        values.put(CommodityEntry.COLUMN_COMMODITY_PRICE, price);
        values.put(CommodityEntry.COLUMN_COMMODITY_QUANTITY, quantity);
        values.put(CommodityEntry.COLUMN_COMMODITY_IMAGE, mImageUri.toString());

        // If the sold is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int sold = 0;
        if (!TextUtils.isEmpty(soldString)) {

        }
        values.put(CommodityEntry.COLUMN_COMMODITY_SOLD, sold);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentCommodityUri == null) {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(CommodityEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_commodity_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_commodity_successful), Toast.LENGTH_SHORT).show();
            }
        } else {

            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentCommodityUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_commodity_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_commodity_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentCommodityUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                showUnsavedChangesDialog();
                return true;
            // Exit activity

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCommodityHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the Commodity hasn't changed, continue with handling back button press

        if (!mCommodityHasChanged) {
            super.onBackPressed();

        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                CommodityEntry._ID,
                CommodityEntry.COLUMN_COMMODITY_NAME,
                CommodityEntry.COLUMN_COMMODITY_PRICE,
                CommodityEntry.COLUMN_COMMODITY_QUANTITY,
                CommodityEntry.COLUMN_COMMODITY_IMAGE,
                CommodityEntry.COLUMN_COMMODITY_SOLD};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCommodityUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_QUANTITY);
            int soldColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_SOLD);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int sold = cursor.getInt(soldColumnIndex);
            mLukeEmail = "Orders@" + "supplierinfo" + ".com";
            mLukeCommodity = name;


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mSoldEditText.setText(Integer.toString(sold));

            // ToDo: this how to set Image from Uri
            int imageColumnIndex = cursor.getColumnIndex(CommodityEntry.COLUMN_COMMODITY_IMAGE);
            mImageUri = Uri.parse(cursor.getString(imageColumnIndex));
            Bitmap bitmap = null;
            try {
                bitmap = Media.getBitmap(getContentResolver(), mImageUri);
            } catch (IOException e) {
                e.printStackTrace();

            }
            mCommodityImageView.setImageBitmap(bitmap);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EXISTING_COMMODITY_LOADER) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    mImageUri = data.getData();
                    Bitmap bitmap = Media.getBitmap(getContentResolver(), mImageUri);
                    mCommodityImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveCommodity();
                finish();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the commodity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteCommodity();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the commodit in the database.
     */
    private void deleteCommodity() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentCommodityUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCommodityUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_commodity_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_commodity_successful),
                        Toast.LENGTH_SHORT).show();

            }
        }

        //Close the activity
        finish();
    }

    private void supplier() {
        String[] TO = {mLukeEmail};
        Intent mIntent = new Intent(Intent.ACTION_SEND);
        mIntent.setData(Uri.parse("sendto"));
        mIntent.setType("text/html");
        mIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "Order" + mLukeCommodity);
        mIntent.putExtra(Intent.EXTRA_TEXT, "Send Orders" + mLukeCommodity);
        try {
            startActivity(mIntent.createChooser(mIntent, "Forward Order email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }


}