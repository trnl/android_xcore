package by.istin.android.xcore.test.bo;

import android.provider.BaseColumns;
import by.istin.android.xcore.annotations.dbLong;
import by.istin.android.xcore.annotations.dbString;
import com.google.gson.annotations.SerializedName;

public class SubEntity implements BaseColumns {

	@dbLong
    @SerializedName(value = "id")
	public static String ID = _ID;

	@dbLong
	public static String TEST_ENTITY_ID = "testentity_id";

	@dbString
	public static String STRING_VALUE = "STRING_VALUE";
	
}