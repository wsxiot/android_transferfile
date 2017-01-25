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
	private ProgressDialog progressDialog;//进度条对话框
	private static final int PROGRESS_DIALOG=0;//进度框标识	
	private String uploadFilePath=null;
	private String fileName;
	private UploadThread uploadThread=null;//上传文件功能线程
	
	/* 一些常量，代表服务器的名称 */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	private static final String TAG = ServerSocketActivity.class.getSimpleName();
	private Handler _handler = new Handler();
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* 蓝牙服务器 */
	private BluetoothServerSocket _serverSocket;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.server_socket);
		if (!_bluetooth.isEnabled()) {//检查蓝牙是否打开,没有打开自动结束服务器,不是自动打开蓝牙,这不是我需要的;
			finish();
			return;
		}
		/* 开始监听 */
		//_serverWorker.start();
	}

	public void onButtonClicked(View view) {//按钮用于关闭服务器的socket
		if(uploadThread!=null){
			uploadThread.closeLink();
			uploadThread.closeLink2();
		}
	}
	
	
	/****************************************************************/
	//进度对话框
	protected Dialog onCreateDialog(int id){
		switch(id){
		case PROGRESS_DIALOG:
			progressDialog=new ProgressDialog(ServerSocketActivity.this);//进度框显示的位置
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置Dialog的风格；
			progressDialog.setButton("暂停", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					uploadThread.closeLink();//模拟断点
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
				Toast.makeText(ServerSocketActivity.this, "文件传输完成", 1).show();
			}
		}
	};
	/****************进入选择文件按钮**************/
	public void onButtonCelectSendFile(View view){
		Intent intent=new Intent();
		intent.setDataAndType(Uri.fromFile(new File("/sdcard")), "*/*");//设置起始目录和查找的文件类型；
		intent.setClass(ServerSocketActivity.this, FileBrowserActivity.class);//记得要注册*********************************************
		startActivityForResult(intent,1);
	}
	
	/*****************************退出文件选择activit后调用********************************/
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			fileName = data.getStringExtra("FileName");//文件名
			final String path = data.getStringExtra("FilePath");//发送文件的父目录
			int last=path.lastIndexOf("/");
			uploadFilePath=path.substring(0,last+1);
			System.out.println("这次选择的文件是"+fileName);///////////////////////////////////////////////////
		}
	}
	/*********文件发送按钮**********/
	public void onButtonSendFile(View view){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			
			
			/*********开启可发现功能***********/
			Intent enabler2 = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivityForResult(enabler2, 0);//把REQUEST_DISCOVERABLE改成0就不会出现询问是否开启可见对话框了
			/*********开启可发现功能***********/
			
			
			if(uploadFilePath==null){
				Toast.makeText(ServerSocketActivity.this, "还没选择上传文件", 1).show();
			}
			else{
			File uploadFile=new File(new File(uploadFilePath),fileName);
			if(uploadFile.exists()){
				showDialog(PROGRESS_DIALOG);
				progressDialog.setMax((int)uploadFile.length());//设置长传文件的最大刻度；
				
				if(uploadThread==null){
					System.out.println("上传线程为空");
				}else{
					System.out.println("上传线程不为空");
					uploadThread.closeLink2();
				}
				
				uploadThread = new UploadThread(ServerSocketActivity.this,uploadFile/*要上传的文件*//*deviceself ip地址,8750*/);
				uploadThread.setListener(new bluetoothtp.UploadThread.UploadProgressListener(){//进度对话框
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
				Toast.makeText(ServerSocketActivity.this, "文件不存在", 1).show();
			}
			}
		}
		else{
			Toast.makeText(ServerSocketActivity.this, "SDcard不存在", 1).show();
		}
	}
	
}

/*****这是点击"服务器发送文件"按钮后调用的activity，选择发送文件，发送文件
 * ************************************************************/







