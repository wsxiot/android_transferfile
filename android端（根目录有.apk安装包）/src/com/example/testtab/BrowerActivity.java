package com.example.testtab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BrowerActivity extends Activity implements OnItemClickListener {
	private ListView listView = null;// ����ListView
	private List<ItemBean> List = new ArrayList<ItemBean>();// ����Դ
	private final static String ROOT = "/mnt/sdcard";// ��Ŀ¼
	private String curlocate = ROOT;// ��ǰĿ¼
	private FileAdapter adapter;// ������
	private Button button;
	private List<String> tempFile = new ArrayList<String>();
	private List<String> ListFile = new ArrayList<String>();
	private final String ip="115.159.191.218";
	private final int port=8888;
	private final String ini="/mnt/sdcard/transmit/";
	private String namein=null;
	private String nameout=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brower);
		listView = (ListView) findViewById(R.id.fileList);// ��ListView
		button = (Button) findViewById(R.id.butt);
		FilesListView(ROOT);// ��ȡ����Դ
		adapter = new FileAdapter(this, List);// �½�һ��adapter
		listView.setAdapter(adapter);// �趨adapter
		listView.setOnItemClickListener(this);
		this.registerForContextMenu(button);
		File file=new File(ini);
		if(!file.exists()){
			file.mkdirs();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("�ļ�����");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.shuaxin:
			FilesListView(curlocate);
			adapter.notifyDataSetChanged();
			break;
		case R.id.fuzhi:
			getListFile();
			tempFile.clear();
			tempFile = ListFile;
			for (String tmp : tempFile) {
				System.out.println(tmp);
			}
			adapter.notifyDataSetChanged();
			Toast.makeText(this, "" + "�������", Toast.LENGTH_SHORT).show();
			break;
		case R.id.shanchu:
			getListFile();
			if (ListFile.size() != 0) {
				int flag = 0;
				AlertDialog.Builder buil = new AlertDialog.Builder(BrowerActivity.this).setTitle("ϵͳ��ʾ");// ���öԻ������
				buil.setMessage("�Ƿ�ɾ���ļ�");// ������ʾ������
				buil.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {// ���ȷ����ť
					@Override
					public void onClick(DialogInterface dialog, int which) {// ȷ����ť����Ӧ�¼�
						for (String tmp : ListFile) {
							File file = new File(tmp);
							if (file.exists()) {
								if (file.isDirectory()) {
									deleteFolder(file);
								} else {
									file.delete();
								}
							}
						}
						FilesListView(curlocate);
						adapter.notifyDataSetChanged();
						Toast.makeText(BrowerActivity.this, "" + "ɾ�����", Toast.LENGTH_SHORT).show();
					}
				});
				buil.setNegativeButton("����", new DialogInterface.OnClickListener() {// ��ӷ��ذ�ť
					@Override
					public void onClick(DialogInterface dialog, int which) {// ��Ӧ�¼�
						dialog.cancel();
					}
				});
				buil.create().show();
			} else {
				Toast.makeText(this, "" + "��ѡ���ļ�", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.youchuan:
			getListFile();
			if(ListFile.size()==1&&!new File(ListFile.get(0)).isDirectory()){
				Message message = handler.obtainMessage();
				handler.sendMessage(message);
				nameout=new File(ListFile.get(0)).getAbsolutePath();
				Toast.makeText(this, "" + "������� �ļ���:"+new File(ListFile.get(0)).getName(), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "" + "��ѡ���һ���ļ�(���ļ���)", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.youshou:

			AlertDialog.Builder bu = new Builder(BrowerActivity.this);
			bu.setTitle("��������յ��ļ�����");
			final EditText editTet = new EditText(this);
			bu.setView(editTet);
			bu.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					namein = editTet.getText().toString();
					File file = new File(ini +namein);
						Message message = handler.obtainMessage();
						handler1.sendMessage(message);
						
						Toast.makeText(BrowerActivity.this, "" + "�������", Toast.LENGTH_SHORT).show();
				}
			});
			bu.setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			bu.setCancelable(false);
			bu.create().show();
			
			
			break;
		case R.id.xinjian:
			AlertDialog.Builder builder = new Builder(BrowerActivity.this);
			builder.setTitle("�������½����ļ�������");
			final EditText editText = new EditText(this);
			builder.setView(editText);
			builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String Thing = editText.getText().toString();
					File file = new File(curlocate + File.separator + Thing);
					if (file.exists()) {
						Toast.makeText(BrowerActivity.this, "" + "�ļ����ļ��������Ѵ��ڣ��½�ʧ��", Toast.LENGTH_SHORT).show();
					} else {
						file.mkdirs();
						FilesListView(curlocate);
						adapter.notifyDataSetChanged();
						Toast.makeText(BrowerActivity.this, "" + "�½��ɹ�", Toast.LENGTH_SHORT).show();
					}
				}
			});
			builder.setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setCancelable(false);
			builder.create().show();

			break;
		case R.id.zhantie:
			for (String tmp : tempFile) {
				File fil = new File(tmp);
				if (fil.exists()) {
					File file = new File(curlocate + File.separator + fil.getName());
					while (file.exists()) {
						file = new File(file.getAbsoluteFile().toString() + "~");
					}
					copy(fil, file);
				}
			}
			FilesListView(curlocate);
			adapter.notifyDataSetChanged();
			Toast.makeText(BrowerActivity.this, "" + "ճ���ɹ���������������������'~'���", Toast.LENGTH_SHORT).show();

			break;
		case R.id.chongmingming:
			getListFile();
			if (ListFile.size() == 1) {
				AlertDialog.Builder builde = new Builder(BrowerActivity.this);
				builde.setTitle("�������½����ļ�������");
				final EditText editTex = new EditText(this);
				builde.setView(editTex);
				builde.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String Thing = editTex.getText().toString();
						File file = new File(curlocate + File.separator + Thing);
						if (file.exists()) {
							Toast.makeText(BrowerActivity.this, "" + "�ļ����ļ��������Ѵ��ڣ�������ʧ��", Toast.LENGTH_SHORT).show();
						} else {
							String tmp = ListFile.get(0);
							File oleFile = new File(tmp);
							oleFile.renameTo(file); // ִ��������
							FilesListView(curlocate);
							adapter.notifyDataSetChanged();
							Toast.makeText(BrowerActivity.this, "" + "�������ɹ�", Toast.LENGTH_SHORT).show();
						}
					}
				});
				builde.setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builde.setCancelable(false);
				builde.create().show();
			} else {
				Toast.makeText(this, "" + "��ѡ���һ���ļ����ļ���", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.xiangxi:
			getListFile();
			if (ListFile.size() == 1) {
				AlertDialog.Builder build = new AlertDialog.Builder(BrowerActivity.this);
				build.setTitle("��ϸ��Ϣ");
				File file = new File(ListFile.get(0));
				build.setMessage("λ��:" + ListFile.get(0) + "\n��С:" + file.length() / 1024 + "M" + "\nʱ��"
						+ new Date(file.lastModified()).toLocaleString() + "\n�ɶ�:" + file.canRead() + "\n��д:"
						+ file.canWrite() + "\n����:" + file.isHidden());
				build.setCancelable(false);
				build.setPositiveButton("֪����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				build.create().show();
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this, "" + "��ѡ���һ���ļ����ļ���", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.guanyu:
			Toast.makeText(this, "" + "ɽ�ƴ�ĺ����ٺ�����������", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onContextItemSelected(item);
	}
	private Handler handler = new Handler(){//�߳�ֱ��ˢ��UI�ᵼ�³�����  ������Handler
		public void handleMessage(Message msg){
			new Thread(){
				public void run(){
				netout();
				}
				}.start();	
		}
	};
	
	private Handler handler1 = new Handler(){//�߳�ֱ��ˢ��UI�ᵼ�³�����  ������Handler
		public void handleMessage(Message msg){
			new Thread(){
				public void run(){
				netin();
				}
				}.start();	
		}
	};
	
	
	public void netout(){//����
		try {
			Socket s=new Socket(ip,port);
            File file=new File(nameout);
            ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(':'+file.getName());
            RandomAccessFile rafin=new RandomAccessFile(file, "r");
            byte[] buf=new byte[1024];
            int l;
            OutputStream os=s.getOutputStream();
            while((l=rafin.read(buf,0,buf.length))!=-1){
                os.write(buf, 0, l);
                System.out.println("han shu li chuan");
                os.flush();
            }
            rafin.close();
            os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void netin(){//����
		try {
			Socket s=new Socket(ip,port);
			ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(namein);
			File file=new File(ini+namein);
			RandomAccessFile rafout=new RandomAccessFile(file, "rw");
			byte[] buf=new byte[1024];
			int l;
			InputStream is=s.getInputStream();
			while((l=is.read(buf,0,buf.length))!=-1){
			    rafout.write(buf, 0, l);
			}
			rafout.close();
			is.close();
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	

	/**
	 * ɾ���ļ��м������ļ�
	 */
	public boolean deleteFolder(File folder) {
		boolean result = false;
		try {
			String[] children = folder.list();
			if (children == null || children.length <= 0) {
				if (folder.delete()) {
					result = true;
				}
			} else {
				for (int i = 0; i < children.length; i++) {
					String childName = children[i];
					String childPath = folder.getPath() + File.separator + childName;
					File filePath = new File(childPath);
					if (filePath.exists() && filePath.isFile()) {
						if (filePath.delete()) {
							result = true;
						} else {
							result = false;
							break;
						}
					} else if (filePath.exists() && filePath.isDirectory()) {
						if (deleteFolder(filePath)) {
							result = true;
						} else {
							result = false;
							break;
						}
					}
				}
				folder.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * �򿪷�ʽ
	 */
	/** ͨ���ļ���׺���ж��Ƿ�Ϊ�������͵��ļ� */
	private boolean checkEndName(String fileName, String[] fileEnds) {
		for (String endName : fileEnds) {
			if (fileName.endsWith(endName)) {
				return true;
			}
		}
		return false;
	}

	private void openFile(File file) {
		/*
		 * Intent�����Ӧ����һ�β����Ķ����������漰���ݡ��������ݽ��������� Android����ݴ�Intent�������������ҵ���Ӧ���������
		 * Intent���ݸ����õ���������������ĵ���
		 * ��ˣ�Intent����������һ��ý���н�����ã�ר���ṩ���������õ������Ϣ��ʵ�ֵ������뱻������֮��Ľ���
		 */
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file2 = new File(file.getAbsolutePath());
		String fileName = file2.getName();
		// ���ݲ�ͬ�ļ����ͣ����ļ� MIME����
		if (checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingMid))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingMp3))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingWav))) {
			intent.setDataAndType(Uri.fromFile(file2), "audio/*");
		} else if (checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingPicture))) {
			intent.setDataAndType(Uri.fromFile(file2), "image/*");
		} else if (checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingTxt))) {
			intent.setDataAndType(Uri.fromFile(file2), "text/*");
		} else if (checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingVideo))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingWma))) {
			intent.setDataAndType(Uri.fromFile(file2), "video/*");
		} else if (checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingOffice))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingPdf))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingRar))
				|| checkEndName(fileName, this.getResources().getStringArray(R.array.fileEndingZip))) {
			intent.setDataAndType(Uri.fromFile(file2), "application/*");
		}
		this.startActivity(intent);
	}

	public void getListFile() {
		ListFile.clear();
		List = adapter.getmDataList();
		for (ItemBean tmp : List) {
			if (tmp.getType() == 1) {
				ListFile.add(tmp.getItemPath());
			}
		}
	}

	private void FilesListView(String selectedPath) {
		File selectedFile = new File(selectedPath);// ����File����
		if (selectedFile.canRead()) {// �鿴�ļ��Ƿ��ܶ����ܶ��ͷ���1�������򷵻�0 ����ɶ�
			File[] file = selectedFile.listFiles();// �оٸ�·���µ������ļ����ļ���
			List.clear();// �ӱ����Ƴ�����Ԫ��
			for (int i = 0; i < file.length; i++) {
				List.add(new ItemBean(file[i].isDirectory() ? R.drawable.folder : R.drawable.file, file[i].getName(),
						file[i].getPath()));
			}
		} else {// �����·�����ɶ�
			Toast.makeText(getApplicationContext(), "��Ŀ¼���ܶ�ȡ", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ճ���ļ����ļ���
	 */
	public void copy(File src, File target) {
		BufferedInputStream bufferedInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;

		try {
			if (src.isDirectory()) {
				if (!target.exists()) {
					target.mkdirs();
				}
				File[] files = src.listFiles();
				for (File f : files) {
					if (f.isFile()) {
						bufferedInputStream = new BufferedInputStream(new FileInputStream(f));
						bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
								new File(target.getAbsolutePath() + File.separator + f.getName())));
						byte[] buffer = new byte[1024];
						// ����ʵ�ʶ�ȡ���ֽ�������������ļ�ĩβ�򷵻�-1
						int len = 0;
						while ((len = bufferedInputStream.read(buffer)) != -1) {
							bufferedOutputStream.write(buffer, 0, len);
						}
						bufferedOutputStream.flush();
					} else {
						copy(new File(src.getAbsolutePath() + File.separator + f.getName()),
								new File(target.getAbsolutePath() + File.separator + f.getName()));
					}
				}
			} else if (src.isFile()) {
				bufferedInputStream = new BufferedInputStream(new FileInputStream(src));
				bufferedOutputStream = new BufferedOutputStream(
						new FileOutputStream(curlocate + File.separator + src.getName()));
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = bufferedInputStream.read(buffer)) != -1) {
					bufferedOutputStream.write(buffer, 0, len);
				}
				bufferedOutputStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedOutputStream != null) {
					bufferedOutputStream.close();
				}
				if (bufferedInputStream != null) {
					bufferedInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView path = (TextView) view.findViewById(R.id.file_path);// �ļ�·��
		TextView name = (TextView) view.findViewById(R.id.file_name);// �ļ���
		File isDirectory = new File(path.getText().toString());
		if (isDirectory.isDirectory()) {// ������ļ���
			curlocate = path.getText().toString();
			FilesListView(curlocate);// ��ȡ����Դ
			adapter.notifyDataSetChanged();
		} else {
			openFile(isDirectory);
		}
	}

	/**
	 * ���ؼ��Ƿ�����һ��Ŀ¼
	 */
	@Override
	public void onBackPressed() {
		File file = new File(curlocate);
		if (file.getParent() == null || curlocate.equals(ROOT)) {
			super.onBackPressed();
		} else {
			FilesListView(file.getParent());
			adapter.notifyDataSetChanged();
			curlocate = file.getParent().toString();
		}
	}

}
