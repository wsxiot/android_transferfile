package bluetoothtp;

import java.util.ArrayList;
import java.util.List;

import com.example.testtab.R;



import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DiscoveryActivity  extends ListActivity//////////继承ListActivity
{
	private Handler _handler = new Handler();
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* 用来存储搜索到的蓝牙设备 */
	private List<BluetoothDevice> _devices = new ArrayList<BluetoothDevice>();
	/* 是否完成搜索 */
	private volatile boolean _discoveryFinished;
	private Runnable _discoveryWorkder = new Runnable() {//搜索所有可见设备
		public void run() 
		{
			/* 开始搜索 */
			//startDiscovery()方法会边搜索边发送相应的广播
			_bluetooth.startDiscovery();//startDiscovery()方法是一个异步方法，它会对其他蓝牙设备进行搜索，持续时间为12秒，注意!是所有可见设备
			for (;;) 
			{
				if (_discoveryFinished) //没有发现设备跳出循环，_discoveryFinished在响应没发现设备的广播接收器中设为ture
				{
					break;
				}
				try 
				{
					Thread.sleep(100);//sleep过后会在执行线程一次
				} 
				catch (InterruptedException e){}
			}
		}
	};
	/**
	 * 接收器
	 * 当搜索蓝牙设备完成时调用
	 */
	private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {//广播接收器,接收找到连接设备   广播
		public void onReceive(Context context, Intent intent) {
			/* 从intent中取得搜索结果数据 */
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  //*捕捉发现到的设备
			String DesighName = device.getName();
			System.out.println("name:"+DesighName);
			if(DesighName.equals("蓝牙传输服务器")){    //如果设备名是"蓝牙传输服务器"就返回这个设备
				System.out.println("找到DesighName");
				Intent result = new Intent();
				result.putExtra(BluetoothDevice.EXTRA_DEVICE, device);  //返回这个设备,用于连接
				DiscoveryActivity.this.setResult(RESULT_OK, result);
				_bluetooth.cancelDiscovery();     /***关闭搜索设备的功能;不关闭的话,要等待一段时间，搜索结束后才可以开始传输文件**/
				finish();                        
			}
			/* 将结果添加到列表中 */
			_devices.add(device);
			/* 显示查寻到的设备列表 */
			showDevices();
		}
	};
	
	/****接收"搜索结束广播"的广播接收器*****/
	private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {//广播接收器,没有找到设备接收结束搜索这个广播
                                                                            //搜索可发现设备结束也会调用这个广播
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			/* 卸载注册的接收器 */
			unregisterReceiver(_foundReceiver);
			unregisterReceiver(this);
			_discoveryFinished = true;
		}
	};
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.discovery);
		/* 如果蓝牙适配器没有打开，则结果 */
		if (!_bluetooth.isEnabled())
		{

			finish();
			return;
		}
		/* 注册接收器 */
		IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//注册广播，接收消息BluetoothAdapter.ACTION_DISCOVERY_FINISHED
		registerReceiver(_discoveryReceiver, discoveryFilter);
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播，接收消息BluetoothDevice.ACTION_FOUND
		registerReceiver(_foundReceiver, foundFilter);
		/* 显示一个对话框,正在搜索蓝牙设备 */
		SamplesUtils.indeterminate(DiscoveryActivity.this, _handler, "Scanning...", _discoveryWorkder, new OnDismissListener() {//////
			public void onDismiss(DialogInterface dialog)
			{

				for (; _bluetooth.isDiscovering();)
				{

					_bluetooth.cancelDiscovery();
				}

				_discoveryFinished = true;
			}
		}, true);
	}

	/* 显示列表 */
	protected void showDevices()
	{
		List<String> list = new ArrayList<String>();
		for (int i = 0, size = _devices.size(); i < size; ++i)
		{
			StringBuilder b = new StringBuilder();
			BluetoothDevice d = _devices.get(i);
			b.append(d.getAddress());
			b.append('\n');
			b.append(d.getName());
			String s = b.toString();
			list.add(s);//添加到list的一个item中
		}

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		_handler.post(new Runnable() {
			public void run()
			{

				setListAdapter(adapter);//更新listView表
			}
		});
	}
	protected void onListItemClick(ListView l, View v, int position, long id)
	{//点击搜索出来的某个设备，返回它的BluetoothDevice(相当于id号)信息，创建一个蓝牙socket来连接选择的设备

		Intent result = new Intent();
		result.putExtra(BluetoothDevice.EXTRA_DEVICE, _devices.get(position));/////连接列表的第一个蓝牙
		setResult(RESULT_OK, result);
		finish();
	}
}

/**这个是客户端实现直接连接服务器设备进行文件传输的activity
 * 连接到服务器设备后立即finsh掉,回到显示接收文件的服务acivity
 * 这里面还有显示搜索设备的功能,但这里不需要用到了,没有删除掉，保留下来是为了做多设备下载显示的参照
 * **********************************/


