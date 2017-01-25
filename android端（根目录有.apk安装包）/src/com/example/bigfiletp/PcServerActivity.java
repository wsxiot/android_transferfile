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
		//�����ļ������
		Intent intent=new Intent();
		intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//������ʼĿ¼�Ͳ��ҵ��ļ����ͣ�
		intent.setClass(PcServerActivity.this, FileBrowserActivity.class);//�ǵ�Ҫע��
		startActivityForResult(intent,1);
	}
	//���ȶԻ���
		protected Dialog onCreateDialog(int id){
			switch(id){
			case PROGRESS_DIALOG:
				progressDialog=new ProgressDialog(PcServerActivity.this);//���ȿ���ʾ��λ��
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//����Dialog�ķ��
				progressDialog.setButton("��ͣ", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						filetopc.closeLink();//ģ��ϵ�,�Ͽ�������(�ϴ���)��socket
						dialog.dismiss();
						
					}
				});
				progressDialog.setMessage("�����ϴ�");
				progressDialog.setMax(100);
				return progressDialog;
			
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
					Toast.makeText(PcServerActivity.this, "�ļ��������", 1).show();
				}
			}
		};
	/** ����ȡ��intתΪ������ip��ַ*/
	private String intToIp(int i)
	{
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
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
				//SystemClock.sleep(2500);
				File uploadFile=new File(new File(uploadFilePath),fileName);
				
				
				WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				DhcpInfo info = wifiManage.getDhcpInfo();
				String deviceself = intToIp(info.serverAddress);
				System.out.println("������ַ��"+deviceself);

				
				WifiInfo wifiinfo = wifiManage.getConnectionInfo();
				String wifihotdeviceip = intToIp(wifiinfo.getIpAddress());
				System.out.println("�ȵ�ĵ�ַ��"+wifihotdeviceip);

				Toast.makeText(PcServerActivity.this, deviceself, 1).show();
				//����DHCP��ȡ����IP��ַ
				showDialog(PROGRESS_DIALOG);
				progressDialog.setMax((int)uploadFile.length());//���ó����ļ������̶ȣ�
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
