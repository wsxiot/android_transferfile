package bluetoothtp;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UploadLogService {
public SQLiteDatabase db=null;
private final static String DATABASE_NAME="db5.db";
private final static String TABLE_NAME="table01";
private final static String _ID="_id";
private final static String FILEPATH="filepath";
private final static String FILEID="fileid";
/*�������ݱ���ֶ�*/
private final static String CREATE_TABLE="create table table01(_id integer primary key autoincrement,"+"filepath text not null,fileid text not null);";
private Context mCtx=null;
/*1.�������ݿ�ʹ�����*/
public UploadLogService(Context ctx){
	this.mCtx=ctx;
	db=mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
	try{
		db.execSQL(CREATE_TABLE);
	}catch(Exception e){
	}
}
/*2.��������|String,String|*/
public long save(String fileId,File file){
String filepath=file.getPath();
ContentValues args=new ContentValues();
args.put(FILEPATH, filepath);
args.put(FILEID, fileId);
return db.insert(TABLE_NAME, null, args);
}
/*3.�������ݱ�õ��ļ�id*/
public String getBindId(File file)throws SQLException {
	String filepath=file.getPath();
	String FID=null;
	Cursor mCursor=db.query(TABLE_NAME, new String[]{_ID,FILEPATH,FILEID,},FILEPATH+"='"+filepath+"'",null,null,null,null,null);	
	if(mCursor!=null){
		mCursor.moveToFirst();
		if(mCursor.moveToFirst()){
		FID=mCursor.getString(2);
		}
	}
	return FID;
}
/*4.ɾ������ָ��������*/
public boolean delete(File file){
	String filepath=file.getPath();
	return db.delete(TABLE_NAME,"filepath='"+filepath+"'", null)>0;
}
/*5.�ر����ݿ�*/
public void close(){
db.close();	
}




} 

























