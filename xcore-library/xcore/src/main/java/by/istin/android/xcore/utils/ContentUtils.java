package by.istin.android.xcore.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.provider.ModelContract;

public class ContentUtils {

    public static ContentValues getEntity(Context context, Class<?> entityClass, Long id) {
        Cursor entityCursor = null;
        ContentValues values = null;
        try {
            entityCursor = context.getContentResolver().query(ModelContract.getUri(entityClass, id), null, null, null, null);
            if (!CursorUtils.isEmpty(entityCursor) && entityCursor.moveToFirst()) {
                values = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(entityCursor, values);
            }
        } finally {
            CursorUtils.close(entityCursor);
        }
        return values;
    }

    public static void putEntity(Context context, Class<?> entityClass, ContentValues entity) {
        context.getContentResolver().insert(ModelContract.getUri(entityClass), entity);
    }

    public static void putEntities(Context context, Class<?> entityClass, List<ContentValues> entities) {
        if (entities == null) return;
        putEntities(context, entityClass, entities.toArray(new ContentValues[entities.size()]));
    }

    public static void putEntities(Context context, Class<?> entityClass, ContentValues... entity) {
        context.getContentResolver().bulkInsert(ModelContract.getUri(entityClass), entity);
    }

    public static void removeEntity(Context context, Class<?> entityClass, long id) {
        String where = BaseColumns._ID + "=?";
        removeEntities(context, entityClass, where, String.valueOf(id));
    }

    public static void removeEntities(Context context, Class<?> entityClass, String where, String ... selectionArgs) {
        context.getContentResolver().delete(ModelContract.getUri(entityClass), where, selectionArgs);
    }

    public static ContentValues getEntity(Context context, Class<?> entityClass, String selection, String ... selectionArgs) {
        List<ContentValues> entities = getEntities(context, entityClass, selection, selectionArgs);
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        return entities.get(0);
    }

    public static List<ContentValues> getEntities(Context context, Class<?> entityClass, String selection, String ... selectionArgs) {
        Cursor entityCursor = null;
        List<ContentValues> result = null;
        try {
            entityCursor = context.getContentResolver().query(ModelContract.getUri(entityClass), null, selection, selectionArgs, null);
            if (!CursorUtils.isEmpty(entityCursor) && entityCursor.moveToFirst()) {
                result = new ArrayList<ContentValues>();
                CursorUtils.convertToContentValuesAndClose(entityCursor, result);
            }
        } finally {
            CursorUtils.close(entityCursor);
        }
        return result;
    }
}
