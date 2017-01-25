package com.example.bigfiletp;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import bluetoothtp.ClientSocketActivity;
import bluetoothtp.ServerSocketActivity;

import com.example.bigfiletp.UploadThread.UploadProgressListener;
import com.example.bigfiletp.wifi功能类.Constant;
import com.example.bigfiletp.wifi功能类.WifiAdmin;
import com.example.bigfiletp.wifi功能类.WifiApAdmin;
import com.example.testtab.R;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import 服务器类.FileServer;

public class UploadActivity extends Activity implements OnClickListener{
	/*记得加权限*/
	private File uploadFile2;
	private static final String TAG="UploadThread";
	private Button OpenWifihot,ConnectWifihot;
	private TextView info,info2;
	private static final int PROGRESS_DIALOG=0;	
	private static final int PROGRESS_DIALOG2=1;
	private ProgressDialog progressDialog;
	private UploadThread uploadThread=null;//上传线程;
	private String uploadFilePath=null;
	private String fileName;
	private FileServer fileserver=null;//服务器类
	/*关于wifi类*/
	WifiAdmin mWifiAdmin;
	WifiApAdmin wifiAp;
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		initView();
	}
	
	
	/*******************蓝牙服务**************************/
	/* 蓝牙客户端---收文件 */
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	public void onOpenClientSocketButtonClicked(View view) {
		_bluetooth.enable();//开启蓝牙,是耗时的任务，其实是在非主线程中完成的
		try {
			TimeUnit.SECONDS.sleep(1);      //为了先让蓝牙开启,让主线程延迟1秒在执行,不然的话下面的先执行了蓝牙还没开启
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent enabler = new Intent(this, ClientSocketActivity.class);
		startActivity(enabler);
	}
	/* 蓝牙服务端---发文件 */
	public void onOpenServerSocketButtonClicked(View view) {
		_bluetooth.enable();//开启蓝牙
		/*********开启可发现功能***********/
		Intent enabler2 = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivityForResult(enabler2, 0);//把REQUEST_DISCOVERABLE改成0就不会出现询问是否开启可见对话框了
		_bluetooth.setName("蓝牙传输服务器");
		/**********开启可发现功能**********/
		try {
			TimeUnit.SECONDS.sleep(1);      //为了先让蓝牙开启,让主线程延迟1秒在执行,不然的话下面的先执行了蓝牙还没开启
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent enabler = new Intent(this, ServerSocketActivity.class);
		startActivity(enabler);
	}

	/*******************蓝牙服务**************************/
	
	/*******发送文件给pc**********/
	public void senttopcButtonClicked(View view) {
		
		/*WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo info = wifiManage.getDhcpInfo();
		String deviceself = intToIp(info.serverAddress);
		System.out.println("本机地址："+deviceself);

		
		WifiInfo wifiinfo = wifiManage.getConnectionInfo();
		String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
		System.out.println("热点的地址："+wifihotdeviceip);

		Toast.makeText(UploadActivity.this, deviceself, 1).show();
		//利用DHCP获取本机IP地址
		showDialog(PROGRESS_DIALOG);
		progressDialog.setMax((int)uploadFile2.length());//设置长传文件的最大刻度；
		Filetopc filetopc=new Filetopc(UploadActivity.this,uploadFile2,deviceself,8750);
		filetopc.setListener(new UploadProgressListener(){
			@Override
			public void onUploadSize(int size){
				Message msg = new Message();
				msg.getData().putInt("size", size);
				handler.sendMessage(msg);
			}
		});
		filetopc.start();*/
		Intent enabler = new Intent(this, PcServerActivity.class);
		startActivity(enabler);
	}
	
	
	/*******发送文件给pc**********/
	
	
	
	
	
	
	
	
	private void initView(){
		OpenWifihot=(Button)findViewById(R.id.openwifihot);//打开wifi热点按钮
		ConnectWifihot=(Button)findViewById(R.id.connectwifihot);//连接wifi热点按钮
		info=(TextView)findViewById(R.id.info);//用于提示连接不上wifi热点
		info2=(TextView)findViewById(R.id.info2);//用于提示连接不上wifi热点
		
		
		/******wifi********/
		context=this;
		ConnectWifihot.setOnClickListener(new Button.OnClickListener()//连接wifi热点
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				mWifiAdmin = new WifiAdmin(context)
				{

					@Override
					public void myUnregisterReceiver(BroadcastReceiver receiver)
					{
						// TODO Auto-generated method stub
						unregisterReceiver(receiver);
					}

					@Override
					public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter)
					{
						// TODO Auto-generated method stub
						registerReceiver(receiver, filter);
						return null;
					}

					@Override
					public void onNotifyWifiConnected()
					{
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected success!");
						Log.v(TAG, "###############################");
					}

					@Override
					public void onNotifyWifiConnectFailed()
					{
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected failed!");
						Log.v(TAG, "###############################");
					}
				};
				//打开wifi；
				mWifiAdmin.openWifi();
				// 连的WIFI热点是用WPA方式保护-----什么是WPA方式保护；
				//wpa保护就是密码保护；
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD,
						WifiAdmin.TYPE_WPA));
				/****************************************************/
				Toast.makeText(UploadActivity.this, "请稍等正在连接服务器热点", 1).show();
				SystemClock.sleep(5000);//等待连接上wifi热点
				WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         	    WifiInfo wifiinfo = wifiManage.getConnectionInfo();
         	    String WifiHotName=wifiinfo.getSSID();
         	    System.out.println("热点名:"+WifiHotName);
         	    String a= new String("\"HotSpotRobin\"");
         	    if(WifiHotName.equals(a)){
         		  System.out.println("连接的是目的热点"+WifiHotName);
/************************************是目的热点开始进行文件传输*********************************************/
     			DhcpInfo info = wifiManage.getDhcpInfo();
     			String deviceself = intToIp(info.serverAddress);//需要android系统>=4.2.2;
     			System.out.println("本机地址："+deviceself);
     			//
     			WifiInfo wifiinfo1 = wifiManage.getConnectionInfo();
     			String wifihotdeviceip = intToIp(wifiinfo1.getIpAddress());
     			System.out.println("热点的地址："+wifihotdeviceip);
     			/***利用DHCP获取本机IP地址****/
     			
     			Toast.makeText(UploadActivity.this, "热点信息:"+wifiinfo1.getSSID(), 1).show();
     			
     			fileserver=new FileServer(UploadActivity.this,/*"192.168.43.1"*/deviceself,8750);
     			MyThread my=new MyThread();
     			my.start();
     			/**********客户端显示进度对话框的test*************/
     			//SystemClock.sleep(1000);//主线程延迟1S
     			int Client=0;
     			while(Client==0){
     			Client=fileserver.getFileLeng();
     			}
     			showDialog(PROGRESS_DIALOG2);
     			System.out.println("设置进度条长度"+Client);//////////////////////////////
     			progressDialog.setMax(Client);         ////设置长传文件的最大刻度；
     			fileserver.setListener(new UploadProgressListener(){
     				@Override
     				public void onUploadSize(int size){
     					Message msg = new Message();
     					msg.getData().putInt("size", size);
     					handler.sendMessage(msg);
     				}
     			});
         	   }
         	   else{
         		  Toast.makeText(UploadActivity.this, "没有连接上目的热点请手动连接", 1).show();
         		  info.setText("无法连接上目的wifi:HotSpotRobin请进行手动连接");
         		  info2.setText("热点密码为:123456789");
         	   }
         	   /*****************************************************/
			}
		});
		OpenWifihot.setOnClickListener(new Button.OnClickListener()//创建wifi热点
		{

			@Override
			public void onClick(View v)
			{
				//获取wifi服务；
				wifiAp = new WifiApAdmin(context);
				//把constant类的HOST_SPOT_SSID（创建热点ID号）和HOST_SPOT_PASS_WORD（密码）传入给startWifiAp()
				wifiAp.startWifiAp(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD);
				/*************进入文件浏览器****************/
				Intent intent=new Intent();
				intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//设置起始目录和查找的文件类型；
				intent.setClass(UploadActivity.this, FileBrowserActivity.class);//记得要注册
				startActivityForResult(intent,1);
			}
		});
		/******wifi******/
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
			SystemClock.sleep(2500);
			/******选择完文件后上传文件*****/
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				if(uploadFilePath==null){
					Toast.makeText(UploadActivity.this, "还没选择上传文件", 1).show();
				}
				else{//
				File uploadFile=new File(new File(uploadFilePath),fileName);/////////////////
				uploadFile2=uploadFile;
				if(uploadFile.exists()){
					showDialog(PROGRESS_DIALOG);
					progressDialog.setMax((int)uploadFile.length());//设置长传文件的最大刻度；
					
					
					/***利用DHCP获取本机IP地址****/
					WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					DhcpInfo info = wifiManage.getDhcpInfo();
					String deviceself = intToIp(info.serverAddress);
					System.out.println("本机地址："+deviceself);

					
					WifiInfo wifiinfo = wifiManage.getConnectionInfo();
					String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
					System.out.println("热点的地址："+wifihotdeviceip);

					Toast.makeText(UploadActivity.this, deviceself, 1).show();
					/***利用DHCP获取本机IP地址****/
					
					//String testip1="192.168.43.138";
					//String testip2="192.168.43.1";
					
					//uploadThread = new UploadThread(UploadActivity.this,uploadFile,/*ConstantValues.HOST*/deviceself,/*ConstantValues.PORT*/8750);
					System.out.println("要上传的文件名："+uploadFile.getName());
					
					if(uploadThread==null){
						System.out.println("上传线程为空");
					}else{
						System.out.println("上传线程不为空");
						uploadThread.closeLink2();
					}
					
					uploadThread = new UploadThread(UploadActivity.this,uploadFile,8750);
					uploadThread.setListener(new UploadProgressListener(){
						@Override
						public void onUploadSize(int size){
							Message msg = new Message();
							msg.getData().putInt("size", size);
							handler.sendMessage(msg);
						}
					});
					uploadThread.start();
				}
				else{
					Toast.makeText(UploadActivity.this, "文件不存在", 1).show();
				}
				}
			}
			else{
				Toast.makeText(UploadActivity.this, "SDcard不存在", 1).show();
			}
			/******选择完文件后上传文件*****/
		}
	}
	//进度对话框
	protected Dialog onCreateDialog(int id){
		switch(id){
		case PROGRESS_DIALOG:
			progressDialog=new ProgressDialog(UploadActivity.this);//进度框显示的位置
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置Dialog的风格；
			progressDialog.setButton("暂停", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					uploadThread.closeLink();//模拟断点,断开服务器(上传方)的socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("正在上传");
			progressDialog.setMax(100);
			return progressDialog;
			
/*******************************客户端进度条************************************************/			
		case PROGRESS_DIALOG2:
			progressDialog=new ProgressDialog(UploadActivity.this);//进度框显示的位置
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置Dialog的风格；
			progressDialog.setButton("暂停", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileserver.quit();//模拟断点,断开客户端(下载方)的socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("正在下载");
			progressDialog.setMax(100);
			return progressDialog;
/*******************************客户端进度条************************************************/
		
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
				Toast.makeText(UploadActivity.this, "文件传输完成", 1).show();
			}
		}
	};
	@Override
	public void onClick(View v){//上传文件按钮
		Resources r=getResources();
		switch(v.getId()){
		/*case R.id.recievefile: //接收文件		
			break;*/
			
		}
	}
	
	/** 将获取的int转为真正的ip地址*/
	private String intToIp(int i)
	{
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}
	class MyThread extends Thread{
		public void run(){
			try {
				fileserver.start(UploadActivity.this);
				System.out.println("连接成功");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("连接异常3");
				e.printStackTrace();
			}
		}
	}
	
	
}
