/***********搜索功能，显示搜索出的设备**************/
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
import com.example.bigfiletp.wifi功能类.Constant;
import com.example.bigfiletp.wifi功能类.WifiApAdmin;
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
	private FileServer fileserver=null;//接收文件处理类
	private static final String TAG = ClientSocketActivity.class.getSimpleName();
	private static final int REQUEST_DISCOVERY = 0x1;;////////
	private Handler _handler = new Handler();
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();//蓝牙配适器,用来对蓝牙进行操作;
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
		Intent intent = new Intent(this, DiscoveryActivity.class);//显示可连接设备的activity
		/* 跳转到搜索的蓝牙设备列表区，进行选择 */
		startActivityForResult(intent, REQUEST_DISCOVERY);//能返回activity消息的调用方法,返回选择的设备BluetoothDevice
	}
	/* 选择了服务器之后进行连接 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			return;
		}
		if (resultCode != RESULT_OK) {
			return;
		}
		final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//得到选择欲连接设备的返回信息,该信息包装在BluetoothDevice对象中
		buttonusdevice=device;
		/*************连接服务器socket和文件发送功能********************/
		/*fileserver=new FileServer(ClientSocketActivity.this,device);
		
		new Thread() {//连接放在新线程里，防止阻塞
			public void run() {
				// 连接 
					try {
						fileserver.start(ClientSocketActivity.this);
						System.out.println("连接成功");
					} catch (Exception e) {
						System.out.println("连接异常3");
						e.printStackTrace();
					}
			};
		}.start();*/
		/*************连接服务器socket和文件发送功能********************/
		
		
		/**********客户端显示进度对话框的test*************/
		/*SystemClock.sleep(5000);//主线程延迟1S
		int Client=0;
		Client=fileserver.getFileLeng();
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
		});*/
		/*********客户端显示进度对话框的test*****************
		
		/**这个activity什么布局都没有它是选择要连接的设备后返回的activity；
		 *可以在这上面显示下载文件的进度，和结束接收按钮，返回最初的只有两个
		 *组件的界面，分别是接收文件和发送文件
		 * ******************************************************************************/
	}
	
/******开始接收按钮********/
private void initView(){
	begin=(Button)findViewById(R.id.begin);//上传按钮
	begin.setOnClickListener(new Button.OnClickListener()//创建wifi热点
	{

		@Override
		public void onClick(View v)
		{
			fileserver=new FileServer(ClientSocketActivity.this,buttonusdevice);
			new Thread() {//连接放在新线程里，防止阻塞
				public void run() {
					// 连接 
						try {
							fileserver.start(ClientSocketActivity.this);
							System.out.println("连接成功");
						} catch (Exception e) {
							System.out.println("连接异常3");
							e.printStackTrace();
						}
				};
			}.start();
			//客户端显示进度对话框
			//SystemClock.sleep(1500);//主线程延迟1S
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
			//客户端显示进度对话框
		}
	});
}
	
	/*进度对话框*/
	protected Dialog onCreateDialog(int id){
		switch(id){		
/*******************************客户端进度条************************************************/			
		case PROGRESS_DIALOG2:
			progressDialog=new ProgressDialog(ClientSocketActivity.this);//进度框显示的位置
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置Dialog的风格；
			progressDialog.setButton("暂停", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileserver.quit();//模拟断点,断开客户端(下载方)的socket
					dialog.dismiss();
					
				}
			});
			progressDialog.setMessage("正在接收文件");
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
				Toast.makeText(ClientSocketActivity.this, "文件传输完成", 1).show();
			}
		}
	};
	
}

