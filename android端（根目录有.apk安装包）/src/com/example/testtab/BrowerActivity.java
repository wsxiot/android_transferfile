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
	private ListView listView = null;// 声明ListView
	private List<ItemBean> List = new ArrayList<ItemBean>();// 数据源
	private final static String ROOT = "/mnt/sdcard";// 根目录
	private String curlocate = ROOT;// 当前目录
	private FileAdapter adapter;// 适配器
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
		listView = (ListView) findViewById(R.id.fileList);// 绑定ListView
		button = (Button) findViewById(R.id.butt);
		FilesListView(ROOT);// 获取数据源
		adapter = new FileAdapter(this, List);// 新建一个adapter
		listView.setAdapter(adapter);// 设定adapter
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
		menu.setHeaderTitle("文件操作");
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
			Toast.makeText(this, "" + "复制完成", Toast.LENGTH_SHORT).show();
			break;
		case R.id.shanchu:
			getListFile();
			if (ListFile.size() != 0) {
				int flag = 0;
				AlertDialog.Builder buil = new AlertDialog.Builder(BrowerActivity.this).setTitle("系统提示");// 设置对话框标题
				buil.setMessage("是否删除文件");// 设置显示的内容
				buil.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 添加确定按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {// 确定按钮的响应事件
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
						Toast.makeText(BrowerActivity.this, "" + "删除完成", Toast.LENGTH_SHORT).show();
					}
				});
				buil.setNegativeButton("返回", new DialogInterface.OnClickListener() {// 添加返回按钮
					@Override
					public void onClick(DialogInterface dialog, int which) {// 响应事件
						dialog.cancel();
					}
				});
				buil.create().show();
			} else {
				Toast.makeText(this, "" + "请选择文件", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.youchuan:
			getListFile();
			if(ListFile.size()==1&&!new File(ListFile.get(0)).isDirectory()){
				Message message = handler.obtainMessage();
				handler.sendMessage(message);
				nameout=new File(ListFile.get(0)).getAbsolutePath();
				Toast.makeText(this, "" + "传输完成 文件名:"+new File(ListFile.get(0)).getName(), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "" + "请选择仅一个文件(非文件夹)", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.youshou:

			AlertDialog.Builder bu = new Builder(BrowerActivity.this);
			bu.setTitle("请输入接收的文件名字");
			final EditText editTet = new EditText(this);
			bu.setView(editTet);
			bu.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					namein = editTet.getText().toString();
					File file = new File(ini +namein);
						Message message = handler.obtainMessage();
						handler1.sendMessage(message);
						
						Toast.makeText(BrowerActivity.this, "" + "接收完成", Toast.LENGTH_SHORT).show();
				}
			});
			bu.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			bu.setCancelable(false);
			bu.create().show();
			
			
			break;
		case R.id.xinjian:
			AlertDialog.Builder builder = new Builder(BrowerActivity.this);
			builder.setTitle("请输入新建的文件夹名字");
			final EditText editText = new EditText(this);
			builder.setView(editText);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String Thing = editText.getText().toString();
					File file = new File(curlocate + File.separator + Thing);
					if (file.exists()) {
						Toast.makeText(BrowerActivity.this, "" + "文件或文件夹名字已存在，新建失败", Toast.LENGTH_SHORT).show();
					} else {
						file.mkdirs();
						FilesListView(curlocate);
						adapter.notifyDataSetChanged();
						Toast.makeText(BrowerActivity.this, "" + "新建成功", Toast.LENGTH_SHORT).show();
					}
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
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
			Toast.makeText(BrowerActivity.this, "" + "粘贴成功，如果有重名情况加上了'~'标记", Toast.LENGTH_SHORT).show();

			break;
		case R.id.chongmingming:
			getListFile();
			if (ListFile.size() == 1) {
				AlertDialog.Builder builde = new Builder(BrowerActivity.this);
				builde.setTitle("请输入新建的文件夹名字");
				final EditText editTex = new EditText(this);
				builde.setView(editTex);
				builde.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String Thing = editTex.getText().toString();
						File file = new File(curlocate + File.separator + Thing);
						if (file.exists()) {
							Toast.makeText(BrowerActivity.this, "" + "文件或文件夹名字已存在，重命名失败", Toast.LENGTH_SHORT).show();
						} else {
							String tmp = ListFile.get(0);
							File oleFile = new File(tmp);
							oleFile.renameTo(file); // 执行重命名
							FilesListView(curlocate);
							adapter.notifyDataSetChanged();
							Toast.makeText(BrowerActivity.this, "" + "重命名成功", Toast.LENGTH_SHORT).show();
						}
					}
				});
				builde.setNeutralButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builde.setCancelable(false);
				builde.create().show();
			} else {
				Toast.makeText(this, "" + "请选择仅一个文件或文件夹", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.xiangxi:
			getListFile();
			if (ListFile.size() == 1) {
				AlertDialog.Builder build = new AlertDialog.Builder(BrowerActivity.this);
				build.setTitle("详细信息");
				File file = new File(ListFile.get(0));
				build.setMessage("位置:" + ListFile.get(0) + "\n大小:" + file.length() / 1024 + "M" + "\n时间"
						+ new Date(file.lastModified()).toLocaleString() + "\n可读:" + file.canRead() + "\n可写:"
						+ file.canWrite() + "\n隐藏:" + file.isHidden());
				build.setCancelable(false);
				build.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				build.create().show();
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this, "" + "请选择仅一个文件或文件夹", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.guanyu:
			Toast.makeText(this, "" + "山科大的胡信屹和武守晓制作", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onContextItemSelected(item);
	}
	private Handler handler = new Handler(){//线程直接刷新UI会导致程序卡死  所以用Handler
		public void handleMessage(Message msg){
			new Thread(){
				public void run(){
				netout();
				}
				}.start();	
		}
	};
	
	private Handler handler1 = new Handler(){//线程直接刷新UI会导致程序卡死  所以用Handler
		public void handleMessage(Message msg){
			new Thread(){
				public void run(){
				netin();
				}
				}.start();	
		}
	};
	
	
	public void netout(){//传出
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
	public void netin(){//传进
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
	 * 删除文件夹及其子文件
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
	 * 打开方式
	 */
	/** 通过文件后缀名判断是否为给定类型的文件 */
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
		 * Intent负责对应用中一次操作的动作、动作涉及数据、附加数据进行描述， Android则根据此Intent的描述，负责找到对应的组件，将
		 * Intent传递给调用的组件，并完成组件的调用
		 * 因此，Intent在这里起着一个媒体中介的作用，专门提供组件互相调用的相关信息，实现调用者与被调用者之间的解耦
		 */
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file2 = new File(file.getAbsolutePath());
		String fileName = file2.getName();
		// 根据不同文件类型，打开文件 MIME类型
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
		File selectedFile = new File(selectedPath);// 构造File对象
		if (selectedFile.canRead()) {// 查看文件是否能读，能读就返回1，不能则返回0 如果可读
			File[] file = selectedFile.listFiles();// 列举该路径下的所有文件或文件夹
			List.clear();// 从表中移除所有元素
			for (int i = 0; i < file.length; i++) {
				List.add(new ItemBean(file[i].isDirectory() ? R.drawable.folder : R.drawable.file, file[i].getName(),
						file[i].getPath()));
			}
		} else {// 如果此路径不可读
			Toast.makeText(getApplicationContext(), "该目录不能读取", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 粘贴文件或文件夹
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
						// 返回实际读取的字节数，如果读到文件末尾则返回-1
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
		TextView path = (TextView) view.findViewById(R.id.file_path);// 文件路径
		TextView name = (TextView) view.findViewById(R.id.file_name);// 文件名
		File isDirectory = new File(path.getText().toString());
		if (isDirectory.isDirectory()) {// 如果是文件夹
			curlocate = path.getText().toString();
			FilesListView(curlocate);// 获取数据源
			adapter.notifyDataSetChanged();
		} else {
			openFile(isDirectory);
		}
	}

	/**
	 * 返回键是返回上一级目录
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
