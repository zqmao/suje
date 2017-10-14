package com.suje.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
            + "thirdExterpriseId VARCHAR, "
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
            + "status integer, "
			+ "thirdExterpriseId VARCHAR )";

	public CoreHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_JS_FILE_UP);
        db.execSQL(CREATE_JS_FILE_DOWN);
        Log.d("Suje", "##SQLiteDatabase##:onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("Suje", "##SQLiteDatabase##:onUpgrade");
		//版本1升级到2，增加了thirdExterpriseId字段
		if(oldVersion == 1 && newVersion == 2){
			String addExterpriseIdUp = "ALTER TABLE z_js_file_up ADD COLUMN thirdExterpriseId VARCHAR";
			String addExterpriseIdDown = "ALTER TABLE z_js_file_down ADD COLUMN thirdExterpriseId VARCHAR";
			db.execSQL(addExterpriseIdUp);
			db.execSQL(addExterpriseIdDown);
		}
	}
	
	

}
