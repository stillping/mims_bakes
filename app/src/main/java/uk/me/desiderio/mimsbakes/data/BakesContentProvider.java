package uk.me.desiderio.mimsbakes.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import uk.me.desiderio.mimsbakes.data.BakesContract.IngredientEntry;
import uk.me.desiderio.mimsbakes.data.BakesContract.RecipeEntry;
import uk.me.desiderio.mimsbakes.data.BakesContract.StepEntry;

/**
 * Content provider to access data from the recipes.db
 */
// TODO: 24/12/2017 add robelectric test which are in a stash
public class BakesContentProvider extends ContentProvider {

    private static final String TAG = BakesContentProvider.class.getSimpleName();

    private BakesDBHelper dbHelper;

    static final int RECIPES = 100;
    static final int RECIPES_WITH_ID = 101;
    static final int INGREDIENTS = 200;
    static final int INGREDIENTS_WITH_ID = 201;
    static final int STEPS = 300;
    static final int STEPS_WITH_ID = 301;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    @NonNull
    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // recipe directory
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_RECIPES, RECIPES);
        // single recipe
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_RECIPES + "/#", RECIPES_WITH_ID);
        // ingredient directory
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_INGREDIENTS, INGREDIENTS);
        // single ingredient
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_INGREDIENTS + "/#", INGREDIENTS_WITH_ID);
        // step directory
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_STEPS, STEPS);
        // single step
        uriMatcher.addURI(BakesContract.CONTENT_AUTHORITY, BakesContract.PATH_STEPS + "/#", STEPS_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BakesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String
            selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int match = uriMatcher.match(uri);

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String tableName;

        switch (match) {
            case RECIPES:
                tableName = RecipeEntry.TABLE_NAME;
                break;
            case INGREDIENTS:
                tableName = IngredientEntry.TABLE_NAME;
                break;
            case STEPS:
                tableName = StepEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("BakesContentProvider doesn't support " +
                    "this operariong. Unknown URI : " + uri);
        }

        Cursor cursor = database.query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        Log.d(TAG, "Returning " + cursor.getCount() + " data items from table " + tableName );

        if (getContentResolver() != null) {
            cursor.setNotificationUri(getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = uriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case RECIPES:
                returnUri = doInsert(RecipeEntry.TABLE_NAME, uri, contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }



        if(getContentResolver() != null) {
            getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    /** helper method that carries out db inserts with the setting provided as its parameters */
    private Uri doInsert(String tableName, Uri contentUri, ContentValues contentValues) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        Uri returnUri;

        long id = database.insert(tableName, null, contentValues);
        Log.d(TAG, "Data item inserted with id: " + id);
        if(id != -1) {
            returnUri = ContentUris.withAppendedId(contentUri, id);
        } else {
            throw new SQLException("Failed to insert raw in uri : " + contentUri);
        }
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int match = uriMatcher.match(uri);

        switch (match) {
            case RECIPES:
                return executedBulkInsertAt(RecipeEntry.TABLE_NAME, uri, values);
            case INGREDIENTS:
                return executedBulkInsertAt(IngredientEntry.TABLE_NAME, uri, values);
            case STEPS:
                return executedBulkInsertAt(StepEntry.TABLE_NAME, uri, values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    public ContentResolver getContentResolver() {
        if(getContext() != null) {
            return getContext().getContentResolver();
        }
        return null;
    }

    /** helper method that carries out bulk insert with settings provided as its parameters */
    private int executedBulkInsertAt(@NonNull String tableName, @NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Bulk inserting : " + values.length + " data items");

        int rowInserted = 0;

        database.beginTransaction();
        try {
            for (ContentValues value : values) {
                long id = database.insert(tableName, null, value);
                if (id != -1) {
                    rowInserted++;
                }
            }
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }

        if (rowInserted > 0) {
            Log.d(TAG, "Bulk insertion completed  : " + values.length + " data items");
            if (getContentResolver() != null) {
                getContentResolver().notifyChange(uri, null);
            }
        }
        return rowInserted;
    }


    // not implemented actions

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
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
