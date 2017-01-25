package com.example.testtab;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	public TabHost tabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		//��ȡ����  
        tabHost = getTabHost();  
          
         
        tabHost.addTab(tabHost.newTabSpec("center")  
                .setIndicator("����ռ�")  
                .setContent(new Intent(this, MiddleActivity.class))); 
        
        tabHost.addTab(tabHost.newTabSpec("brower")  
                .setIndicator("�ļ������")  
                .setContent(new Intent(this, BrowerActivity.class)));  
        
        /*tabHost.addTab(tabHost.newTabSpec("self")  
                .setIndicator("����")  
                .setContent(new Intent(this, LastActivity.class))); */
        
          
        
        //ָ���ĵ�ǰ��tab  
        //ͨ������ָ��  ������0��ʼ  
        //��һ��ʼҪ��ʾ������һҳ
        tabHost.setCurrentTab(0); //���㿪ʼ  
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
