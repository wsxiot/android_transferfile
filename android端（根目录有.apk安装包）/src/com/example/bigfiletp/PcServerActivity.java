package com.example.bigfiletp;

import java.io.File;

import com.example.bigfiletp.UploadThread.UploadProgressListener;
import com.example.testtab.R;

import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class PcServerActivity extends Activity {
	private static final int PROGRESS_DIALOG=0;	
	private ProgressDialog progressDialog;
	private String uploadFilePath=null;
	private String fileName;
	private Filetopc filetopc=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pc_server);
	}
	public void senttopcButtonClicked(View view) {
		//进入文件浏览器
		Intent intent=new Intent();
		intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//设置起始目录和查找的文件类型；
		intent.setClass(PcServerActivity.this, FileBrowserActivity.class);//记得要注册
		startActivityForResult(intent,1);
	}
	//进度对话框
		protected Dialog onCreateDialog(int id){
			switch(id){
			case PROGRESS_DIALOG:
				progressDialog=new ProgressDialog(PcServerActivity.this);//进度框显示的位置
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置Dialog的风格；
				progressDialog.setButton("暂停", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						filetopc.closeLink();//模拟断点,断开服务器(上传方)的socket
						dialog.dismiss();
						
					}
				});
				progressDialog.setMessage("正在上传");
				progressDialog.setMax(100);
				return progressDialog;
			
			default:
				return null;
			}
		}
		
		//更新ui的Handler
		private Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				int length=msg.getData().getInt("size");//获取上传的长度；
				progressDialog.setProgress(length);
				if(progressDialog.getProgress()==progressDialog.getMax()){//上传完成
					progressDialog.dismiss();
					Toast.makeText(PcServerActivity.this, "文件传输完成", 1).show();
				}
			}
		};
	/** 将获取的int转为真正的ip地址*/
	private String intToIp(int i)
	{
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}
	
	//选择发送文件后调用
		@Override
		protected void onActivityResult(int requestCode,int resultCode,Intent data){
			super.onActivityResult(requestCode, resultCode, data);
			if(resultCode==RESULT_OK){
				fileName = data.getStringExtra("FileName");//文件名
				final String path = data.getStringExtra("FilePath");//发送文件的父目录
				int last=path.lastIndexOf("/");
				uploadFilePath=path.substring(0,last+1);
				//SystemClock.sleep(2500);
				File uploadFile=new File(new File(uploadFilePath),fileName);
				
				
				WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				DhcpInfo info = wifiManage.getDhcpInfo();
				String deviceself = intToIp(info.serverAddress);
				System.out.println("本机地址："+deviceself);

				
				WifiInfo wifiinfo = wifiManage.getConnectionInfo();
				String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
				System.out.println("热点的地址："+wifihotdeviceip);

				Toast.makeText(PcServerActivity.this, deviceself, 1).show();
				//利用DHCP获取本机IP地址
				showDialog(PROGRESS_DIALOG);
				progressDialog.setMax((int)uploadFile.length());//设置长传文件的最大刻度；
				filetopc=new Filetopc(PcServerActivity.this,uploadFile,deviceself,8750);
				filetopc.setListener(new UploadProgressListener(){
					@Override
					public void onUploadSize(int size){
						Message msg = new Message();
						msg.getData().putInt("size", size);
						handler.sendMessage(msg);
					}
				});
				filetopc.start();

			}
		}
	

}
