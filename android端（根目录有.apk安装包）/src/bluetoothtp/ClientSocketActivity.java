/***********�������ܣ���ʾ���������豸**************/
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.example.bigfiletp.FileBrowserActivity;
import com.example.bigfiletp.UploadActivity;
import com.example.bigfiletp.UploadThread.UploadProgressListener;
import com.example.bigfiletp.wifi������.Constant;
import com.example.bigfiletp.wifi������.WifiApAdmin;
import com.example.testtab.R;



import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ClientSocketActivity  extends Activity
{
	private Button begin;
	private BluetoothDevice buttonusdevice=null;
	private static final int PROGRESS_DIALOG2=1;
	private ProgressDialog progressDialog;
	private FileServer fileserver=null;//�����ļ�������
	private static final String TAG = ClientSocketActivity.class.getSimpleName();
	private static final int REQUEST_DISCOVERY = 0x1;;////////
	private Handler _handler = new Handler();
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();//����������,�������������в���;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
		WindowManager.LayoutParams.FLAG_BLUR_BEHIND);///////////////////////////////////////?????
		setContentView(R.layout.client_socket);
		initView();
		if (!_bluetooth.isEnabled()) {
			finish();
			return;
		}
		Intent intent = new Intent(this, DiscoveryActivity.class);//��ʾ�������豸��activity
		/* ��ת�������������豸�б���������ѡ�� */
		startActivityForResult(intent, REQUEST_DISCOVERY);//�ܷ���activity��Ϣ�ĵ��÷���,����ѡ����豸BluetoothDevice
	}
	/* ѡ���˷�����֮��������� */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			return;
		}
		if (resultCode != RESULT_OK) {
			return;
		}
		final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//�õ�ѡ���������豸�ķ�����Ϣ,����Ϣ��װ��BluetoothDevice������
		buttonusdevice=device;
		/*************���ӷ�����socket���ļ����͹���********************/
		/*fileserver=new FileServer(ClientSocketActivity.this,device);
		
		new Thread() {//���ӷ������߳����ֹ����
			public void run() {
				// ���� 
					try {
						fileserver.start(ClientSocketActivity.this);
						System.out.println("���ӳɹ�");
					} catch (Exception e) {
						System.out.println("�����쳣3");
						e.printStackTrace();
					}
			};
		}.start();*/
		/*************���ӷ�����socket���ļ����͹���********************/
		
		
		/**********�ͻ�����ʾ���ȶԻ����test*************/
		/*SystemClock.sleep(5000);//���߳��ӳ�1S
		int Client=0;
		Client=fileserver.getFileLeng();
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
		});*/
		/*********�ͻ�����ʾ���ȶԻ����test*****************
		
		/**���activityʲô���ֶ�û������ѡ��Ҫ���ӵ��豸�󷵻ص�activity��
		 *��������������ʾ�����ļ��Ľ��ȣ��ͽ������հ�ť�����������ֻ������
		 *����Ľ��棬�ֱ��ǽ����ļ��ͷ����ļ�
		 * ******************************************************************************/
	}
	
/******��ʼ���հ�ť********/
private void initView(){
	begin=(Button)findViewById(R.id.begin);//�ϴ���ť
	begin.setOnClickListener(new Button.OnClickListener()//����wifi�ȵ�
	{

		@Override
		public void onClick(View v)
		{
			fileserver=new FileServer(ClientSocketActivity.this,buttonusdevice);
			new Thread() {//���ӷ������߳����ֹ����
				public void run() {
					// ���� 
						try {
							fileserver.start(ClientSocketActivity.this);
							System.out.println("���ӳɹ�");
						} catch (Exception e) {
							System.out.println("�����쳣3");
							e.printStackTrace();
						}
				};
			}.start();
			//�ͻ�����ʾ���ȶԻ���
			//SystemClock.sleep(1500);//���߳��ӳ�1S
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
			//�ͻ�����ʾ���ȶԻ���
		}
	});
}
	
	/*���ȶԻ���*/
	protected Dialog onCreateDialog(int id){
		switch(id){		
/*******************************�ͻ��˽�����************************************************/			
		case PROGRESS_DIALOG2:
			progressDialog=new ProgressDialog(ClientSocketActivity.this);//���ȿ���ʾ��λ��
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//����Dialog�ķ��
			progressDialog.setButton("��ͣ", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileserver.quit();//ģ��ϵ�,�Ͽ��ͻ���(���ط�)��socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("���ڽ����ļ�");
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
				Toast.makeText(ClientSocketActivity.this, "�ļ��������", 1).show();
			}
		}
	};
	
}

