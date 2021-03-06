package by.istin.android.xcore.gson;

import android.content.ContentValues;
import by.istin.android.xcore.annotations.*;
import by.istin.android.xcore.utils.ReflectUtils;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractValuesAdapter<T> implements JsonDeserializer<T> {

    public static final int UNKNOWN_POSITION = -1;


    private Class<?> mContentValuesEntityClazz;

    private List<Field> mEntityKeys;

    private IJsonPrimitiveHandler<T> mJsonPrimitiveHandler;

    public static interface IJsonPrimitiveHandler<T> {

        T convert(JsonPrimitive jsonPrimitive);

    }

    public Class<?> getContentValuesEntityClazz() {
        return mContentValuesEntityClazz;
    }

    public IJsonPrimitiveHandler getJsonPrimitiveHandler() {
        return mJsonPrimitiveHandler;
    }

    public void setJsonPrimitiveHandler(IJsonPrimitiveHandler<T> mJsonPrimitiveHandler) {
        this.mJsonPrimitiveHandler = mJsonPrimitiveHandler;
    }

    public AbstractValuesAdapter(Class<?> contentValuesEntityClazz) {
        this.mContentValuesEntityClazz = contentValuesEntityClazz;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return deserializeContentValues(null, UNKNOWN_POSITION, jsonElement, type, jsonDeserializationContext);
    }

    protected T deserializeContentValues(T parent, int position, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        if (mEntityKeys == null) {
            mEntityKeys = ReflectUtils.getEntityKeys(mContentValuesEntityClazz);
        }
        ContentValues contentValues = new ContentValues();
        if (mEntityKeys == null) {
            return proceed(parent, position, contentValues);
        }
        if (jsonElement.isJsonPrimitive()) {
            if (mJsonPrimitiveHandler == null) {
                return null;
            } else {
                return mJsonPrimitiveHandler.convert((JsonPrimitive) jsonElement);
            }
        }
        JsonObject jsonObject = (JsonObject) jsonElement;
        for (Field field : mEntityKeys) {
            JsonElement jsonValue = null;
            String fieldValue = ReflectUtils.getStaticStringValue(field);
            String serializedName = fieldValue;
            if (field.isAnnotationPresent(SerializedName.class)) {
                SerializedName serializedAnnotation = field.getAnnotation(SerializedName.class);
                if (serializedAnnotation != null) {
                    serializedName = serializedAnnotation.value();
                }
            }
            String separator = null;
            boolean isFirstObjectForJsonArray = false;
            if (field.isAnnotationPresent(JsonSubJSONObject.class)) {
                JsonSubJSONObject jsonSubJSONObject = field.getAnnotation(JsonSubJSONObject.class);
                if (jsonSubJSONObject != null) {
                    separator = jsonSubJSONObject.separator();
                    isFirstObjectForJsonArray = jsonSubJSONObject.isFirstObjectForJsonArray();
                }
            }
            if (separator != null && serializedName.contains(separator)) {
                String[] values = serializedName.split(separator);
                JsonObject tempElement = jsonObject;
                for (int i = 0; i < values.length; i++) {
                    if (i == values.length - 1) {
                        jsonValue = tempElement.get(values[i]);
                    } else {
                        JsonElement element = tempElement.get(values[i]);
                        if (element == null) {
                            break;
                        }
                        if (element.isJsonObject()) {
                            tempElement = (JsonObject) element;
                        } else {
                            if (isFirstObjectForJsonArray && element.isJsonArray()) {
                                tempElement = (JsonObject) ((JsonArray) element).get(0);
                            } else {
                                break;
                            }
                        }
                    }
                }
            } else {
                jsonValue = jsonObject.get(serializedName);
            }
            if (jsonValue == null) {
                continue;
            }
            if (jsonValue.isJsonPrimitive()) {
                putPrimitiveValue(contentValues, field, jsonValue, fieldValue);
            } else if (ReflectUtils.isAnnotationPresent(field, dbEntity.class)) {
                dbEntity entity = field.getAnnotation(dbEntity.class);
                Class<?> clazz = entity.clazz();
                JsonObject subEntityJsonObject = jsonValue.getAsJsonObject();
                proceedSubEntity(type, jsonDeserializationContext, contentValues, field, fieldValue, clazz, subEntityJsonObject);
            } else if (field.isAnnotationPresent(dbEntities.class)) {
                if (jsonValue.isJsonArray()) {
                    JsonArray jsonArray = jsonValue.getAsJsonArray();
                    proceedSubEntities(type, jsonDeserializationContext, contentValues, field, fieldValue, jsonArray);
                } else {
                    dbEntities entity = field.getAnnotation(dbEntities.class);
                    Class<?> clazz = entity.clazz();
                    JsonObject subEntityJsonObject = jsonValue.getAsJsonObject();
                    proceedSubEntity(type, jsonDeserializationContext, contentValues, field, fieldValue, clazz, subEntityJsonObject);
                }
            }
        }
        return proceed(parent, position, contentValues);
    }

    protected void putPrimitiveValue(ContentValues contentValues, Field field, JsonElement jsonValue, String fieldValue) {
        if (ReflectUtils.isAnnotationPresent(field, dbLong.class)) {
            contentValues.put(fieldValue, jsonValue.getAsLong());
        } else if (ReflectUtils.isAnnotationPresent(field, dbString.class)) {
            contentValues.put(fieldValue, jsonValue.getAsString());
        } else if (ReflectUtils.isAnnotationPresent(field, dbBoolean.class)) {
            contentValues.put(fieldValue, jsonValue.getAsBoolean());
        } else if (ReflectUtils.isAnnotationPresent(field, dbByte.class)) {
            contentValues.put(fieldValue, jsonValue.getAsByte());
        } else if (ReflectUtils.isAnnotationPresent(field, dbDouble.class)) {
            contentValues.put(fieldValue, jsonValue.getAsDouble());
        } else if (ReflectUtils.isAnnotationPresent(field, dbInteger.class)) {
            contentValues.put(fieldValue, jsonValue.getAsInt());
        }
    }

    protected abstract void proceedSubEntities(Type type, JsonDeserializationContext jsonDeserializationContext, ContentValues contentValues, Field field, String fieldValue, JsonArray jsonArray);

    protected abstract void proceedSubEntity(Type type, JsonDeserializationContext jsonDeserializationContext, ContentValues contentValues, Field field, String fieldValue, Class<?> clazz, JsonObject subEntityJsonObject);

    protected abstract T proceed(T parent, int position, ContentValues contentValues);

}