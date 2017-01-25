package com.example.bigfiletp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.testtab.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowserActivity extends Activity {
	private SimpleAdapter adapter;
	ArrayList<HashMap<String, Object>> listItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_view);
		
		Toast.makeText(getApplicationContext(), "选择要发送的文件", Toast.LENGTH_SHORT).show();
		ListView fileView = (ListView)findViewById(R.id.fileView);
		listItem = new ArrayList<HashMap<String, Object>>();
		//ImageView image = (ImageView)findViewById(R.id.image);
		//image.setImageResource();
		
		//Environment.getExternalStorageDirectory().getPath()获取SD卡的路径；
		//不过并并没有先判断SD卡是否存在；
		this.FilesListView(Environment.getExternalStorageDirectory().getPath());
		adapter = new SimpleAdapter(
				getApplicationContext(),//表示本类；
				listItem,
				R.layout.item_view,//list的布局activity
				new String[] {"image", "name", "path", "type", "parent"},//map中的键值，有5个；
				//用map中的键值对应的内容更新list布局activity的组件，也应是5个；
				new int[]{R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent}
		);
		fileView.setAdapter(adapter);
		//ListView监听器;
		fileView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView isDirectory = (TextView)view.findViewById(R.id.file_type); 
				TextView path = (TextView)view.findViewById(R.id.file_path);
				TextView name = (TextView)view.findViewById(R.id.file_name);
				//点到ListView的项，获取该项的view显示的文件路径
				if (Boolean.parseBoolean(isDirectory.getText().toString())){
					//显示路径下的文件;
					FilesListView(path.getText().toString());
					adapter.notifyDataSetChanged();
				}else{
					//如果选择的是一个文件，用Intent装着文件名和路径，用于返回给主类；
					Intent intent = new Intent();
					intent.putExtra("FileName", name.getText().toString());
					intent.putExtra("FilePath", path.getText().toString());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}
	//FilesListView的构造函数；
	private void FilesListView(String selectedPath){
		File selectedFile = new File(selectedPath);
		//selectedFile.canRead()查看文件是否能读，能读就返回1，不能则返回0；
		if (selectedFile.canRead()){
			//listFiles()listFiles()方法能获取当前目录下的所有文件，把它们保存在文件数组File[]中；
			File[] file = selectedFile.listFiles();
			listItem.clear();
			for (int i = 0; i < file.length; i++){
				HashMap<String, Object> map = new HashMap<String, Object>();
				//判断目标是文件还是文件，配置不同图标；
				map.put("image", file[i].isDirectory()?R.drawable.folder:R.drawable.file);
				map.put("name", file[i].getName());
				map.put("path", file[i].getPath());
				map.put("type", file[i].isDirectory());
				map.put("parent", file[i].getParent());
				
				listItem.add(map);
			}
			//判断有无父目录，增加返回上一级目录菜单
			if (selectedFile.getParent() != null){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", "返回上一级目录");
				map.put("path", selectedFile.getParent());
				map.put("type", true);
				map.put("parent", selectedFile.getParent());
				listItem.add(0, map);
			}
		}else{
			Toast.makeText(getApplicationContext(), "该目录不能读取", Toast.LENGTH_SHORT).show();
		}
	}

}
