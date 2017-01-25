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
import com.example.bigfiletp.wifi������.Constant;
import com.example.bigfiletp.wifi������.WifiAdmin;
import com.example.bigfiletp.wifi������.WifiApAdmin;
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
import ��������.FileServer;

public class UploadActivity extends Activity implements OnClickListener{
	/*�ǵü�Ȩ��*/
	private File uploadFile2;
	private static final String TAG="UploadThread";
	private Button OpenWifihot,ConnectWifihot;
	private TextView info,info2;
	private static final int PROGRESS_DIALOG=0;	
	private static final int PROGRESS_DIALOG2=1;
	private ProgressDialog progressDialog;
	private UploadThread uploadThread=null;//�ϴ��߳�;
	private String uploadFilePath=null;
	private String fileName;
	private FileServer fileserver=null;//��������
	/*����wifi��*/
	WifiAdmin mWifiAdmin;
	WifiApAdmin wifiAp;
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		initView();
	}
	
	
	/*******************��������**************************/
	/* �����ͻ���---���ļ� */
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	public void onOpenClientSocketButtonClicked(View view) {
		_bluetooth.enable();//��������,�Ǻ�ʱ��������ʵ���ڷ����߳�����ɵ�
		try {
			TimeUnit.SECONDS.sleep(1);      //Ϊ��������������,�����߳��ӳ�1����ִ��,��Ȼ�Ļ��������ִ����������û����
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent enabler = new Intent(this, ClientSocketActivity.class);
		startActivity(enabler);
	}
	/* ���������---���ļ� */
	public void onOpenServerSocketButtonClicked(View view) {
		_bluetooth.enable();//��������
		/*********�����ɷ��ֹ���***********/
		Intent enabler2 = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivityForResult(enabler2, 0);//��REQUEST_DISCOVERABLE�ĳ�0�Ͳ������ѯ���Ƿ����ɼ��Ի�����
		_bluetooth.setName("�������������");
		/**********�����ɷ��ֹ���**********/
		try {
			TimeUnit.SECONDS.sleep(1);      //Ϊ��������������,�����߳��ӳ�1����ִ��,��Ȼ�Ļ��������ִ����������û����
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Intent enabler = new Intent(this, ServerSocketActivity.class);
		startActivity(enabler);
	}

	/*******************��������**************************/
	
	/*******�����ļ���pc**********/
	public void senttopcButtonClicked(View view) {
		
		/*WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo info = wifiManage.getDhcpInfo();
		String deviceself = intToIp(info.serverAddress);
		System.out.println("������ַ��"+deviceself);

		
		WifiInfo wifiinfo = wifiManage.getConnectionInfo();
		String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
		System.out.println("�ȵ�ĵ�ַ��"+wifihotdeviceip);

		Toast.makeText(UploadActivity.this, deviceself, 1).show();
		//����DHCP��ȡ����IP��ַ
		showDialog(PROGRESS_DIALOG);
		progressDialog.setMax((int)uploadFile2.length());//���ó����ļ������̶ȣ�
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
	
	
	/*******�����ļ���pc**********/
	
	
	
	
	
	
	
	
	private void initView(){
		OpenWifihot=(Button)findViewById(R.id.openwifihot);//��wifi�ȵ㰴ť
		ConnectWifihot=(Button)findViewById(R.id.connectwifihot);//����wifi�ȵ㰴ť
		info=(TextView)findViewById(R.id.info);//������ʾ���Ӳ���wifi�ȵ�
		info2=(TextView)findViewById(R.id.info2);//������ʾ���Ӳ���wifi�ȵ�
		
		
		/******wifi********/
		context=this;
		ConnectWifihot.setOnClickListener(new Button.OnClickListener()//����wifi�ȵ�
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
				//��wifi��
				mWifiAdmin.openWifi();
				// ����WIFI�ȵ�����WPA��ʽ����-----ʲô��WPA��ʽ������
				//wpa�����������뱣����
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD,
						WifiAdmin.TYPE_WPA));
				/****************************************************/
				Toast.makeText(UploadActivity.this, "���Ե��������ӷ������ȵ�", 1).show();
				SystemClock.sleep(5000);//�ȴ�������wifi�ȵ�
				WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         	    WifiInfo wifiinfo = wifiManage.getConnectionInfo();
         	    String WifiHotName=wifiinfo.getSSID();
         	    System.out.println("�ȵ���:"+WifiHotName);
         	    String a= new String("\"HotSpotRobin\"");
         	    if(WifiHotName.equals(a)){
         		  System.out.println("���ӵ���Ŀ���ȵ�"+WifiHotName);
/************************************��Ŀ���ȵ㿪ʼ�����ļ�����*********************************************/
     			DhcpInfo info = wifiManage.getDhcpInfo();
     			String deviceself = intToIp(info.serverAddress);//��Ҫandroidϵͳ>=4.2.2;
     			System.out.println("������ַ��"+deviceself);
     			//
     			WifiInfo wifiinfo1 = wifiManage.getConnectionInfo();
     			String wifihotdeviceip = intToIp(wifiinfo1.getIpAddress());
     			System.out.println("�ȵ�ĵ�ַ��"+wifihotdeviceip);
     			/***����DHCP��ȡ����IP��ַ****/
     			
     			Toast.makeText(UploadActivity.this, "�ȵ���Ϣ:"+wifiinfo1.getSSID(), 1).show();
     			
     			fileserver=new FileServer(UploadActivity.this,/*"192.168.43.1"*/deviceself,8750);
     			MyThread my=new MyThread();
     			my.start();
     			/**********�ͻ�����ʾ���ȶԻ����test*************/
     			//SystemClock.sleep(1000);//���߳��ӳ�1S
     			int Client=0;
     			while(Client==0){
     			Client=fileserver.getFileLeng();
     			}
     			showDialog(PROGRESS_DIALOG2);
     			System.out.println("���ý���������"+Client);//////////////////////////////
     			progressDialog.setMax(Client);         ////���ó����ļ������̶ȣ�
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
         		  Toast.makeText(UploadActivity.this, "û��������Ŀ���ȵ����ֶ�����", 1).show();
         		  info.setText("�޷�������Ŀ��wifi:HotSpotRobin������ֶ�����");
         		  info2.setText("�ȵ�����Ϊ:123456789");
         	   }
         	   /*****************************************************/
			}
		});
		OpenWifihot.setOnClickListener(new Button.OnClickListener()//����wifi�ȵ�
		{

			@Override
			public void onClick(View v)
			{
				//��ȡwifi����
				wifiAp = new WifiApAdmin(context);
				//��constant���HOST_SPOT_SSID�������ȵ�ID�ţ���HOST_SPOT_PASS_WORD�����룩�����startWifiAp()
				wifiAp.startWifiAp(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD);
				/*************�����ļ������****************/
				Intent intent=new Intent();
				intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//������ʼĿ¼�Ͳ��ҵ��ļ����ͣ�
				intent.setClass(UploadActivity.this, FileBrowserActivity.class);//�ǵ�Ҫע��
				startActivityForResult(intent,1);
			}
		});
		/******wifi******/
	}
	//ѡ�����ļ������
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			fileName = data.getStringExtra("FileName");//�ļ���
			final String path = data.getStringExtra("FilePath");//�����ļ��ĸ�Ŀ¼
			int last=path.lastIndexOf("/");
			uploadFilePath=path.substring(0,last+1);
			SystemClock.sleep(2500);
			/******ѡ�����ļ����ϴ��ļ�*****/
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				if(uploadFilePath==null){
					Toast.makeText(UploadActivity.this, "��ûѡ���ϴ��ļ�", 1).show();
				}
				else{//
				File uploadFile=new File(new File(uploadFilePath),fileName);/////////////////
				uploadFile2=uploadFile;
				if(uploadFile.exists()){
					showDialog(PROGRESS_DIALOG);
					progressDialog.setMax((int)uploadFile.length());//���ó����ļ������̶ȣ�
					
					
					/***����DHCP��ȡ����IP��ַ****/
					WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					DhcpInfo info = wifiManage.getDhcpInfo();
					String deviceself = intToIp(info.serverAddress);
					System.out.println("������ַ��"+deviceself);

					
					WifiInfo wifiinfo = wifiManage.getConnectionInfo();
					String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
					System.out.println("�ȵ�ĵ�ַ��"+wifihotdeviceip);

					Toast.makeText(UploadActivity.this, deviceself, 1).show();
					/***����DHCP��ȡ����IP��ַ****/
					
					//String testip1="192.168.43.138";
					//String testip2="192.168.43.1";
					
					//uploadThread = new UploadThread(UploadActivity.this,uploadFile,/*ConstantValues.HOST*/deviceself,/*ConstantValues.PORT*/8750);
					System.out.println("Ҫ�ϴ����ļ�����"+uploadFile.getName());
					
					if(uploadThread==null){
						System.out.println("�ϴ��߳�Ϊ��");
					}else{
						System.out.println("�ϴ��̲߳�Ϊ��");
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
					Toast.makeText(UploadActivity.this, "�ļ�������", 1).show();
				}
				}
			}
			else{
				Toast.makeText(UploadActivity.this, "SDcard������", 1).show();
			}
			/******ѡ�����ļ����ϴ��ļ�*****/
		}
	}
	//���ȶԻ���
	protected Dialog onCreateDialog(int id){
		switch(id){
		case PROGRESS_DIALOG:
			progressDialog=new ProgressDialog(UploadActivity.this);//���ȿ���ʾ��λ��
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//����Dialog�ķ��
			progressDialog.setButton("��ͣ", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					uploadThread.closeLink();//ģ��ϵ�,�Ͽ�������(�ϴ���)��socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("�����ϴ�");
			progressDialog.setMax(100);
			return progressDialog;
			
/*******************************�ͻ��˽�����************************************************/			
		case PROGRESS_DIALOG2:
			progressDialog=new ProgressDialog(UploadActivity.this);//���ȿ���ʾ��λ��
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//����Dialog�ķ��
			progressDialog.setButton("��ͣ", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileserver.quit();//ģ��ϵ�,�Ͽ��ͻ���(���ط�)��socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("��������");
			progressDialog.setMax(100);
			return progressDialog;
/*******************************�ͻ��˽�����************************************************/
		
		default:
			return null;
		}
	}
	
	//����ui��Handler
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			int length=msg.getData().getInt("size");//��ȡ�ϴ��ĳ��ȣ�
			progressDialog.setProgress(length);
			if(progressDialog.getProgress()==progressDialog.getMax()){//�ϴ����
				progressDialog.dismiss();
				Toast.makeText(UploadActivity.this, "�ļ��������", 1).show();
			}
		}
	};
	@Override
	public void onClick(View v){//�ϴ��ļ���ť
		Resources r=getResources();
		switch(v.getId()){
		/*case R.id.recievefile: //�����ļ�		
			break;*/
			
		}
	}
	
	/** ����ȡ��intתΪ������ip��ַ*/
	private String intToIp(int i)
	{
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}
	class MyThread extends Thread{
		public void run(){
			try {
				fileserver.start(UploadActivity.this);
				System.out.println("���ӳɹ�");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("�����쳣3");
				e.printStackTrace();
			}
		}
	}
	
	
}
