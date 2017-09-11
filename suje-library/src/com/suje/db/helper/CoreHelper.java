package com.suje.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoreHelper extends SQLiteOpenHelper {
	
    public static final String CREATE_JS_FILE_UP = "CREATE TABLE IF NOT EXISTS z_js_file_up ("
            + "id VARCHAR primary key, "
            + "url VARCHAR, "
            + "name VARCHAR, "
            + "totalSize BIGINT, "
            + "size BIGINT, "
            + "dateTime VARCHAR, "
			+ "h5 VARCHAR, "
            + "status integer, "
            + "partId integer, "
            + "serverFileId VARCHAR, "
            + "uploadUrl VARCHAR, "
            + "ownerId VARCHAR, "
            + "refreshUrl VARCHAR)";
    public static final String CREATE_JS_FILE_DOWN = "CREATE TABLE IF NOT EXISTS z_js_file_down ("
            + "id VARCHAR primary key, "
            + "url VARCHAR, "
            + "name VARCHAR, "
            + "totalSize BIGINT, "
            + "size BIGINT, "
            + "dateTime VARCHAR, "
			+ "h5 VARCHAR, "
            + "status integer)";

	public CoreHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_JS_FILE_UP);
        db.execSQL(CREATE_JS_FILE_DOWN);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
	
	

}
