package com.suje.db.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.suje.db.JsFile;
import com.suje.db.JsFileDown;
import com.suje.db.JsFileUp;


public class SujeDbHelper {
	
	public static final String DATABASE_NAME = "isuje.db";
    public static final int DATABASE_VERSION = 2;//
	private static SujeDbHelper mDbHelper;
	private static CoreHelper mCoreHelper;
	
	public static synchronized SujeDbHelper getInstance(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new SujeDbHelper(context);
        }
        return mDbHelper;
    }

    public SujeDbHelper(Context context) {
    	mCoreHelper = new CoreHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void create(JsFile jsFile){
    	SQLiteDatabase db = mCoreHelper.getReadableDatabase();
    	ContentValues values = new ContentValues();
    	
    	values.put("id", jsFile.getId());
    	values.put("url", jsFile.getUrl());
    	values.put("name", jsFile.getName());
    	values.put("totalSize", jsFile.getTotalSize());
    	values.put("size", jsFile.getSize());
    	values.put("dateTime", jsFile.getDateTime());
    	values.put("h5", jsFile.getH5());
    	values.put("status", jsFile.getStatus());
    	values.put("thirdExterpriseId", jsFile.getThirdExterpriseId());
    	if(jsFile instanceof JsFileUp){
    		JsFileUp jsFileUp = (JsFileUp)jsFile;
    		values.put("partId", jsFileUp.getPartId());
        	values.put("serverFileId", jsFileUp.getServerFileId());
        	values.put("uploadUrl", jsFileUp.getUploadUrl());
        	values.put("ownerId", jsFileUp.getOwnerId());
        	values.put("refreshUrl", jsFileUp.getRefreshUrl());
        	db.insert("z_js_file_up", null, values);
    	}else if(jsFile instanceof JsFileDown){
        	db.insert("z_js_file_down", null, values);
    	}
    }
    
    public void update(JsFile jsFile){
    	SQLiteDatabase db = mCoreHelper.getReadableDatabase();
    	ContentValues values = new ContentValues();
    	
    	values.put("id", jsFile.getId());
    	values.put("url", jsFile.getUrl());
    	values.put("name", jsFile.getName());
    	values.put("totalSize", jsFile.getTotalSize());
    	values.put("size", jsFile.getSize());
    	values.put("dateTime", jsFile.getDateTime());
		values.put("h5", jsFile.getH5());
    	values.put("status", jsFile.getStatus());
    	values.put("thirdExterpriseId", jsFile.getThirdExterpriseId());
    	if(jsFile instanceof JsFileUp){
    		JsFileUp jsFileUp = (JsFileUp)jsFile;
    		values.put("partId", jsFileUp.getPartId());
        	values.put("serverFileId", jsFileUp.getServerFileId());
        	values.put("uploadUrl", jsFileUp.getUploadUrl());
        	values.put("ownerId", jsFileUp.getOwnerId());
        	values.put("refreshUrl", jsFileUp.getRefreshUrl());
        	db.update("z_js_file_up", values, "id=?", new String[]{jsFile.getId()});
    	}else if(jsFile instanceof JsFileDown){
        	db.update("z_js_file_down", values, "id=?", new String[]{jsFile.getId()});
    	}
    }
    
    public void delete(JsFile jsFile){
    	SQLiteDatabase db = mCoreHelper.getReadableDatabase();
    	if(jsFile instanceof JsFileUp){
        	db.delete("z_js_file_up", "id=?", new String[]{jsFile.getId()});
    	}else if(jsFile instanceof JsFileDown){
        	db.delete("z_js_file_down", "id=?", new String[]{jsFile.getId()});
    	}
    }
    
    @SuppressWarnings("unchecked")
	public <T> T load(Class<T> clazz, String id){
    	SQLiteDatabase db = mCoreHelper.getReadableDatabase();
    	Cursor cursor = null;
    	if(clazz == JsFileUp.class){
    		cursor = db.rawQuery("select " + getFieldsUp() + " from z_js_file_up where id=? ", new String[]{id});
    		JsFileUp jsFileUp = new JsFileUp();
    		while(cursor.moveToNext()){
    			jsFileUp.setId(cursor.getString(0));
    			jsFileUp.setUrl(cursor.getString(1));
    			jsFileUp.setName(cursor.getString(2));
    			jsFileUp.setTotalSize(cursor.getLong(3));
    			jsFileUp.setSize(cursor.getLong(4));
    			jsFileUp.setDateTime(cursor.getString(5));
    			jsFileUp.setH5(cursor.getString(6));
    			jsFileUp.setStatus(cursor.getInt(7));
    			jsFileUp.setThirdExterpriseId(cursor.getString(8));
    			jsFileUp.setPartId(cursor.getInt(9));
    			jsFileUp.setServerFileId(cursor.getString(10));
    			jsFileUp.setUploadUrl(cursor.getString(11));
    			jsFileUp.setOwnerId(cursor.getString(12));
    			jsFileUp.setRefreshUrl(cursor.getString(13));
        	}
    		cursor.close();
    		return (T)jsFileUp;
    	}else if(clazz == JsFileDown.class){
    		cursor = db.rawQuery("select " + getFieldsDown() + " from z_js_file_down where id=? ", new String[]{id});
    		JsFileDown jsFileDown = new JsFileDown();
    		while(cursor.moveToNext()){
    			jsFileDown.setId(cursor.getString(0));
    			jsFileDown.setUrl(cursor.getString(1));
    			jsFileDown.setName(cursor.getString(2));
    			jsFileDown.setTotalSize(cursor.getLong(3));
    			jsFileDown.setSize(cursor.getLong(4));
    			jsFileDown.setDateTime(cursor.getString(5));
				jsFileDown.setH5(cursor.getString(6));
    			jsFileDown.setStatus(cursor.getInt(7));
    			jsFileDown.setThirdExterpriseId(cursor.getString(8));
        	}
    		cursor.close();
    		return (T)jsFileDown;
    	}
    	return null;
    }
    
	@SuppressWarnings("unchecked")
	public <T> List<T> list(Class<T> clazz, String thirdExterpriseId, String pageNum, String pageSize){
    	int page = 0;//第几页
    	int rows = 0;//每页多少条
    	try{
    		page = Integer.parseInt(pageNum);//第几页
        	rows = Integer.parseInt(pageSize);//每页多少条
    	}catch(Exception e){
    		page = 1;
        	rows = 10;
    	}
    	int index = (page - 1) * rows;
    	SQLiteDatabase db = mCoreHelper.getReadableDatabase();
    	Cursor cursor = null;
    	List<T> result = new ArrayList<T>();
    	if(clazz == JsFileUp.class){
    		cursor = db.rawQuery("select " + getFieldsUp() + " from z_js_file_up where thirdExterpriseId=? order by id desc limit ?, ? ", new String[]{thirdExterpriseId, index + "", rows + ""});
    		while(cursor.moveToNext()){
    			JsFileUp jsFileUp = new JsFileUp();
				jsFileUp.setId(cursor.getString(0));
				jsFileUp.setUrl(cursor.getString(1));
				jsFileUp.setName(cursor.getString(2));
				jsFileUp.setTotalSize(cursor.getLong(3));
				jsFileUp.setSize(cursor.getLong(4));
				jsFileUp.setDateTime(cursor.getString(5));
				jsFileUp.setH5(cursor.getString(6));
				jsFileUp.setStatus(cursor.getInt(7));
				jsFileUp.setThirdExterpriseId(cursor.getString(8));
    			jsFileUp.setPartId(cursor.getInt(9));
    			jsFileUp.setServerFileId(cursor.getString(10));
    			jsFileUp.setUploadUrl(cursor.getString(11));
    			jsFileUp.setOwnerId(cursor.getString(12));
    			jsFileUp.setRefreshUrl(cursor.getString(13));
    			result.add((T)jsFileUp);
        	}
    		cursor.close();
    	}else if(clazz == JsFileDown.class){
    		cursor = db.rawQuery("select " + getFieldsDown() + " from z_js_file_down where thirdExterpriseId=? order by id desc limit ?, ? ", new String[]{thirdExterpriseId, index + "", rows + ""});
    		while(cursor.moveToNext()){
    			JsFileDown jsFileDown = new JsFileDown();
    			jsFileDown.setId(cursor.getString(0));
    			jsFileDown.setUrl(cursor.getString(1));
    			jsFileDown.setName(cursor.getString(2));
    			jsFileDown.setTotalSize(cursor.getLong(3));
    			jsFileDown.setSize(cursor.getLong(4));
    			jsFileDown.setDateTime(cursor.getString(5));
				jsFileDown.setH5(cursor.getString(6));
    			jsFileDown.setStatus(cursor.getInt(7));
    			jsFileDown.setThirdExterpriseId(cursor.getString(8));
    			result.add((T)jsFileDown);
        	}
    		cursor.close();
    	}
    	return result;
    }
    
    private String getFieldsUp(){
    	String sql = " id, url, name, totalSize, size, dateTime, h5, status, thirdExterpriseId, partId, serverFileId, uploadUrl, ownerId, refreshUrl ";
    	return sql;
    }
    
    private String getFieldsDown(){
    	String sql = " id, url, name, totalSize, size, dateTime, h5, status, thirdExterpriseId ";
    	return sql;
    }

}
