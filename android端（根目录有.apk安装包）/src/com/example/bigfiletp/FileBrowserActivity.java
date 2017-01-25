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
		
		Toast.makeText(getApplicationContext(), "ѡ��Ҫ���͵��ļ�", Toast.LENGTH_SHORT).show();
		ListView fileView = (ListView)findViewById(R.id.fileView);
		listItem = new ArrayList<HashMap<String, Object>>();
		//ImageView image = (ImageView)findViewById(R.id.image);
		//image.setImageResource();
		
		//Environment.getExternalStorageDirectory().getPath()��ȡSD����·����
		//��������û�����ж�SD���Ƿ���ڣ�
		this.FilesListView(Environment.getExternalStorageDirectory().getPath());
		adapter = new SimpleAdapter(
				getApplicationContext(),//��ʾ���ࣻ
				listItem,
				R.layout.item_view,//list�Ĳ���activity
				new String[] {"image", "name", "path", "type", "parent"},//map�еļ�ֵ����5����
				//��map�еļ�ֵ��Ӧ�����ݸ���list����activity�������ҲӦ��5����
				new int[]{R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent}
		);
		fileView.setAdapter(adapter);
		//ListView������;
		fileView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView isDirectory = (TextView)view.findViewById(R.id.file_type); 
				TextView path = (TextView)view.findViewById(R.id.file_path);
				TextView name = (TextView)view.findViewById(R.id.file_name);
				//�㵽ListView�����ȡ�����view��ʾ���ļ�·��
				if (Boolean.parseBoolean(isDirectory.getText().toString())){
					//��ʾ·���µ��ļ�;
					FilesListView(path.getText().toString());
					adapter.notifyDataSetChanged();
				}else{
					//���ѡ�����һ���ļ�����Intentװ���ļ�����·�������ڷ��ظ����ࣻ
					Intent intent = new Intent();
					intent.putExtra("FileName", name.getText().toString());
					intent.putExtra("FilePath", path.getText().toString());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}
	//FilesListView�Ĺ��캯����
	private void FilesListView(String selectedPath){
		File selectedFile = new File(selectedPath);
		//selectedFile.canRead()�鿴�ļ��Ƿ��ܶ����ܶ��ͷ���1�������򷵻�0��
		if (selectedFile.canRead()){
			//listFiles()listFiles()�����ܻ�ȡ��ǰĿ¼�µ������ļ��������Ǳ������ļ�����File[]�У�
			File[] file = selectedFile.listFiles();
			listItem.clear();
			for (int i = 0; i < file.length; i++){
				HashMap<String, Object> map = new HashMap<String, Object>();
				//�ж�Ŀ�����ļ������ļ������ò�ͬͼ�ꣻ
				map.put("image", file[i].isDirectory()?R.drawable.folder:R.drawable.file);
				map.put("name", file[i].getName());
				map.put("path", file[i].getPath());
				map.put("type", file[i].isDirectory());
				map.put("parent", file[i].getParent());
				
				listItem.add(map);
			}
			//�ж����޸�Ŀ¼�����ӷ�����һ��Ŀ¼�˵�
			if (selectedFile.getParent() != null){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", "������һ��Ŀ¼");
				map.put("path", selectedFile.getParent());
				map.put("type", true);
				map.put("parent", selectedFile.getParent());
				listItem.add(0, map);
			}
		}else{
			Toast.makeText(getApplicationContext(), "��Ŀ¼���ܶ�ȡ", Toast.LENGTH_SHORT).show();
		}
	}

}
