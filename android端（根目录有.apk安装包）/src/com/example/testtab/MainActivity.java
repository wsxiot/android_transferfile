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
		//获取对象  
        tabHost = getTabHost();  
          
         
        tabHost.addTab(tabHost.newTabSpec("center")  
                .setIndicator("服务空间")  
                .setContent(new Intent(this, MiddleActivity.class))); 
        
        tabHost.addTab(tabHost.newTabSpec("brower")  
                .setIndicator("文件浏览器")  
                .setContent(new Intent(this, BrowerActivity.class)));  
        
        /*tabHost.addTab(tabHost.newTabSpec("self")  
                .setIndicator("设置")  
                .setContent(new Intent(this, LastActivity.class))); */
        
          
        
        //指定的当前的tab  
        //通过索引指定  索引从0开始  
        //即一开始要显示的是哪一页
        tabHost.setCurrentTab(0); //从零开始  
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
