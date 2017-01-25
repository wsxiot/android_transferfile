package com.example.testtab;

import java.util.concurrent.TimeUnit;

import com.example.bigfiletp.UploadActivity;

import bluetoothtp.ClientSocketActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MiddleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_middle);
	}
	
	public void ButtonClicked1(View view) {
		Intent enabler = new Intent(this, UploadActivity.class);
		startActivity(enabler);
	}
	
	public void ButtonClicked2(View view) {
		Intent enabler = new Intent(this, BrowerActivity.class);
		startActivity(enabler);
	}
	
	
}
