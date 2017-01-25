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

public class DiscoveryActivity  extends ListActivity//////////�̳�ListActivity
{
	private Handler _handler = new Handler();
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* �����洢�������������豸 */
	private List<BluetoothDevice> _devices = new ArrayList<BluetoothDevice>();
	/* �Ƿ�������� */
	private volatile boolean _discoveryFinished;
	private Runnable _discoveryWorkder = new Runnable() {//�������пɼ��豸
		public void run() 
		{
			/* ��ʼ���� */
			//startDiscovery()������������߷�����Ӧ�Ĺ㲥
			_bluetooth.startDiscovery();//startDiscovery()������һ���첽��������������������豸��������������ʱ��Ϊ12�룬ע��!�����пɼ��豸
			for (;;) 
			{
				if (_discoveryFinished) //û�з����豸����ѭ����_discoveryFinished����Ӧû�����豸�Ĺ㲥����������Ϊture
				{
					break;
				}
				try 
				{
					Thread.sleep(100);//sleep�������ִ���߳�һ��
				} 
				catch (InterruptedException e){}
			}
		}
	};
	/**
	 * ������
	 * �����������豸���ʱ����
	 */
	private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {//�㲥������,�����ҵ������豸   �㲥
		public void onReceive(Context context, Intent intent) {
			/* ��intent��ȡ������������� */
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  //*��׽���ֵ����豸
			String DesighName = device.getName();
			System.out.println("name:"+DesighName);
			if(DesighName.equals("�������������")){    //����豸����"�������������"�ͷ�������豸
				System.out.println("�ҵ�DesighName");
				Intent result = new Intent();
				result.putExtra(BluetoothDevice.EXTRA_DEVICE, device);  //��������豸,��������
				DiscoveryActivity.this.setResult(RESULT_OK, result);
				_bluetooth.cancelDiscovery();     /***�ر������豸�Ĺ���;���رյĻ�,Ҫ�ȴ�һ��ʱ�䣬����������ſ��Կ�ʼ�����ļ�**/
				finish();                        
			}
			/* �������ӵ��б��� */
			_devices.add(device);
			/* ��ʾ��Ѱ�����豸�б� */
			showDevices();
		}
	};
	
	/****����"���������㲥"�Ĺ㲥������*****/
	private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {//�㲥������,û���ҵ��豸���ս�����������㲥
                                                                            //�����ɷ����豸����Ҳ���������㲥
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			/* ж��ע��Ľ����� */
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
		/* �������������û�д򿪣����� */
		if (!_bluetooth.isEnabled())
		{

			finish();
			return;
		}
		/* ע������� */
		IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//ע��㲥��������ϢBluetoothAdapter.ACTION_DISCOVERY_FINISHED
		registerReceiver(_discoveryReceiver, discoveryFilter);
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//ע��㲥��������ϢBluetoothDevice.ACTION_FOUND
		registerReceiver(_foundReceiver, foundFilter);
		/* ��ʾһ���Ի���,�������������豸 */
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

	/* ��ʾ�б� */
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
			list.add(s);//��ӵ�list��һ��item��
		}

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		_handler.post(new Runnable() {
			public void run()
			{

				setListAdapter(adapter);//����listView��
			}
		});
	}
	protected void onListItemClick(ListView l, View v, int position, long id)
	{//�������������ĳ���豸����������BluetoothDevice(�൱��id��)��Ϣ������һ������socket������ѡ����豸

		Intent result = new Intent();
		result.putExtra(BluetoothDevice.EXTRA_DEVICE, _devices.get(position));/////�����б�ĵ�һ������
		setResult(RESULT_OK, result);
		finish();
	}
}

/**����ǿͻ���ʵ��ֱ�����ӷ������豸�����ļ������activity
 * ���ӵ��������豸������finsh��,�ص���ʾ�����ļ��ķ���acivity
 * �����滹����ʾ�����豸�Ĺ���,�����ﲻ��Ҫ�õ���,û��ɾ����������������Ϊ�������豸������ʾ�Ĳ���
 * **********************************/


