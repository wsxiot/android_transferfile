package bluetoothtp;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLServe {
public SQLiteDatabase db=null;
private static String DATABASE_NAME;
private static String TABLE_NAME;
private final static String _ID="_id";
private final static String FILEPATH="filepath";
private final static String FILEID="fileid";
/*创建数据表的字段*/
private static String CREATE_TABLE;
private Context mCtx=null;
/*1.创建数据库和创建表*/
public SQLServe(Context ctx,String databasename,String tablename){
	this.mCtx=ctx;
	DATABASE_NAME=databasename;
	TABLE_NAME=tablename;
	CREATE_TABLE="create table "+TABLE_NAME+"(_id integer primary key autoincrement,"+"filepath text not null,fileid text not null);";
	db=mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
	try{
		db.execSQL(CREATE_TABLE);
	}catch(Exception e){
	}
}
/*2.储存数据|String,String|*/
public long save(String fileId,File file){
String filepath=file.getPath();
ContentValues args=new ContentValues();
args.put(FILEPATH, filepath);
args.put(FILEID, fileId);
return db.insert(TABLE_NAME, null, args);
}
//服务器的save
public long saveserve(long fileId,File file){
String filepath=file.getPath();
ContentValues args=new ContentValues();
args.put(FILEPATH, filepath);
args.put(FILEID, fileId);
return db.insert(TABLE_NAME, null, args);
}
/*3.查找数据表得到文件id*/
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
//得到文件路径
public String GetPath(long id)throws SQLException {
	String FID=null;
	Cursor mCursor=db.query(TABLE_NAME, new String[]{_ID,FILEPATH,FILEID,},FILEID+"='"+id+"'",null,null,null,null,null);	
	if(mCursor!=null){
		if(mCursor.moveToFirst()){
		FID=mCursor.getString(1);
		}
	}
	return FID;
}
/*4.删除表中指定的数据*/
public boolean delete(File file){
	String filepath=file.getPath();
	return db.delete(TABLE_NAME,"filepath='"+filepath+"'", null)>0;
}
//服务器的delete
public boolean delete(long id){
	return db.delete(TABLE_NAME,"fileid='"+id+"'", null)>0;
}
/*5.关闭数据库*/
public void close(){
db.close();	
}




} 

























