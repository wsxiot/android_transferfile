package bluetoothtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.bigfiletp.FileBrowserActivity;
import com.example.testtab.R;


import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class ServerSocketActivity extends ListActivity
{
	private ProgressDialog progressDialog;//�������Ի���
	private static final int PROGRESS_DIALOG=0;//���ȿ��ʶ	
	private String uploadFilePath=null;
	private String fileName;
	private UploadThread uploadThread=null;//�ϴ��ļ������߳�
	
	/* һЩ��������������������� */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	private static final String TAG = ServerSocketActivity.class.getSimpleName();
	private Handler _handler = new Handler();
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* ���������� */
	private BluetoothServerSocket _serverSocket;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.server_socket);
		if (!_bluetooth.isEnabled()) {//��������Ƿ��,û�д��Զ�����������,�����Զ�������,�ⲻ������Ҫ��;
			finish();
			return;
		}
		/* ��ʼ���� */
		//_serverWorker.start();
	}

	public void onButtonClicked(View view) {//��ť���ڹرշ�������socket
		if(uploadThread!=null){
			uploadThread.closeLink();
			uploadThread.closeLink2();
		}
	}
	
	
	/****************************************************************/
	//���ȶԻ���
	protected Dialog onCreateDialog(int id){
		switch(id){
		case PROGRESS_DIALOG:
			progressDialog=new ProgressDialog(ServerSocketActivity.this);//���ȿ���ʾ��λ��
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//����Dialog�ķ��
			progressDialog.setButton("��ͣ", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					uploadThread.closeLink();//ģ��ϵ�
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
				Toast.makeText(ServerSocketActivity.this, "�ļ��������", 1).show();
			}
		}
	};
	/****************����ѡ���ļ���ť**************/
	public void onButtonCelectSendFile(View view){
		Intent intent=new Intent();
		intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//������ʼĿ¼�Ͳ��ҵ��ļ����ͣ�
		intent.setClass(ServerSocketActivity.this, FileBrowserActivity.class);//�ǵ�Ҫע��*********************************************
		startActivityForResult(intent,1);
	}
	
	/*****************************�˳��ļ�ѡ��activit�����********************************/
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			fileName = data.getStringExtra("FileName");//�ļ���
			final String path = data.getStringExtra("FilePath");//�����ļ��ĸ�Ŀ¼
			int last=path.lastIndexOf("/");
			uploadFilePath=path.substring(0,last+1);
			System.out.println("���ѡ����ļ���"+fileName);///////////////////////////////////////////////////
		}
	}
	/*********�ļ����Ͱ�ť**********/
	public void onButtonSendFile(View view){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			
			
			/*********�����ɷ��ֹ���***********/
			Intent enabler2 = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivityForResult(enabler2, 0);//��REQUEST_DISCOVERABLE�ĳ�0�Ͳ������ѯ���Ƿ����ɼ��Ի�����
			/*********�����ɷ��ֹ���***********/
			
			
			if(uploadFilePath==null){
				Toast.makeText(ServerSocketActivity.this, "��ûѡ���ϴ��ļ�", 1).show();
			}
			else{
			File uploadFile=new File(new File(uploadFilePath),fileName);
			if(uploadFile.exists()){
				showDialog(PROGRESS_DIALOG);
				progressDialog.setMax((int)uploadFile.length());//���ó����ļ������̶ȣ�
				
				if(uploadThread==null){
					System.out.println("�ϴ��߳�Ϊ��");
				}else{
					System.out.println("�ϴ��̲߳�Ϊ��");
					uploadThread.closeLink2();
				}
				
				uploadThread = new UploadThread(ServerSocketActivity.this,uploadFile/*Ҫ�ϴ����ļ�*//*deviceself ip��ַ,8750*/);
				uploadThread.setListener(new bluetoothtp.UploadThread.UploadProgressListener(){//���ȶԻ���
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
				Toast.makeText(ServerSocketActivity.this, "�ļ�������", 1).show();
			}
			}
		}
		else{
			Toast.makeText(ServerSocketActivity.this, "SDcard������", 1).show();
		}
	}
	
}

/*****���ǵ��"�����������ļ�"��ť����õ�activity��ѡ�����ļ��������ļ�
 * ************************************************************/







