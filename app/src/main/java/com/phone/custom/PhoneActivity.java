package com.phone.custom;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.widge.PhoneLayout;
import com.phone.widge.PileLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phone on 2017/6/19.
 */

public class PhoneActivity extends Activity {

	private List<String> names;
	private PhoneActivity.MyAdapter adapter;
	private int imgs[] = { R.mipmap.item_00, R.mipmap.item_01, R.mipmap.item_02, R.mipmap.item_03, R.mipmap.item_03,
			R.mipmap.item_04, R.mipmap.item_05, R.mipmap.item_06, R.mipmap.item_07, R.mipmap.item_08, R.mipmap.item_09,
			R.mipmap.item_10, R.mipmap.item_11 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone);
		PhoneLayout phoneLayout = (PhoneLayout) findViewById(R.id.phoneLayout);
		adapter = new PhoneActivity.MyAdapter();
		phoneLayout.setAdapter(adapter);
		phoneLayout.setOnItemClickListener(new PileLayout.OnItemClickListener() {
			@Override
			public void onItemclick(int position) {
				Toast.makeText(PhoneActivity.this, "onItemClick:" + position, Toast.LENGTH_SHORT).show();
			}
		});
		phoneLayout.setOnItemLongClickListener(new PileLayout.OnItemLongClickListener() {
			@Override
			public void onItemLongClick(int position) {
				Toast.makeText(PhoneActivity.this, "onItemLongClick:" + position, Toast.LENGTH_SHORT).show();
			}
		});
	}

	class MyAdapter extends BaseAdapter {

		public MyAdapter() {
			names = generateNames(imgs.length);
		}

		@Override
		public int getCount() {
			return names.size();
		}

		@Override
		public Object getItem(int i) {
			return names.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			if (convertView == null) {
				convertView = LayoutInflater.from(PhoneActivity.this).inflate(R.layout.item, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			textView.setText(names.get(position));
			ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
			imageView.setImageResource(imgs[position]);
			return convertView;
		}
	}

	private List<String> generateNames(int count) {
		List<String> datas = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			datas.add("应用" + i);
		}
		return datas;
	}
}
