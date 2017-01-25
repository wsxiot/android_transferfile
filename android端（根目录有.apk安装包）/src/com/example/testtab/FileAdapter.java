package com.example.testtab;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
	private List<ItemBean> mDataList;

	// public FileAdapter(Context context, List<ItemBean> list) {
	// mLayoutInflater = LayoutInflater.from(context);
	// mDataList = list;
	// }
	//
	// @Override
	// public int getCount() {
	// return mDataList.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return mDataList.get(position);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return position;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// ViewHolder viewholder;
	// if(convertView==null){
	// viewholder =new ViewHolder();
	// convertView = mLayoutInflater.inflate(R.layout.item_view, null);
	// viewholder.img=(ImageView) convertView.findViewById(R.id.image);
	// viewholder.name=(TextView) convertView.findViewById(R.id.file_name);
	// viewholder.path=(TextView) convertView.findViewById(R.id.file_path);
	// viewholder.check = (CheckBox)convertView.findViewById(R.id.checkbox);
	// convertView.setTag(viewholder);
	// }else{
	// viewholder = (ViewHolder) convertView.getTag();
	// }
	// ItemBean bean = mDataList.get(position);
	// viewholder.img.setImageResource(bean.itemImage);
	// viewholder.name.setText(bean.itemName);
	// viewholder.path.setText(bean.itemPath);
	//
	// return convertView;
	// }
	// class ViewHolder{
	// public ImageView img;
	// public TextView name;
	// public TextView path;
	// public CheckBox check;
	// }

	public List<ItemBean> getmDataList() {
		return mDataList;
	}

	public FileAdapter(Context context, List<ItemBean> list) {
		mLayoutInflater = LayoutInflater.from(context);
		mDataList = list;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int index = position;
		ViewHolder viewholder;
		if (convertView == null) {
			viewholder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.otheritem_view, null);
			viewholder.img = (ImageView) convertView.findViewById(R.id.image);
			viewholder.name = (TextView) convertView.findViewById(R.id.file_name);
			viewholder.path = (TextView) convertView.findViewById(R.id.file_path);
			viewholder.check = (CheckBox) convertView.findViewById(R.id.checkbox);
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		ItemBean bean = mDataList.get(position);
		viewholder.img.setImageResource(bean.itemImage);
		viewholder.name.setText(bean.itemName);
		viewholder.path.setText(bean.itemPath);
		viewholder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mDataList.get(index).type = 1;
				} else {
					mDataList.get(index).type = 0;
				}

			}

		});
		if (mDataList.get(position).type == 1) {
			viewholder.check.setChecked(true);
		} else {
			viewholder.check.setChecked(false);
		}

		return convertView;
	}

	class ViewHolder {
		public ImageView img;
		public TextView name;
		public TextView path;
		public CheckBox check;
	}


	/*
	 * View view = mLayoutInflater.inflate(R.layout.item_view, null); ImageView
	 * img=(ImageView) view.findViewById(R.id.image); TextView name=(TextView)
	 * view.findViewById(R.id.file_name); TextView
	 * path=(TextView)view.findViewById(R.id.file_path);
	 * 
	 * ItemBean bean = mDataList.get(position);
	 * img.setImageResource(bean.itemImage); name.setText(bean.itemName);
	 * path.setText(bean.itemPath);
	 */

}
