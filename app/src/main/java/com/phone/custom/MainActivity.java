package com.phone.custom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.phone.widge.PileLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PileLayout pileLayout = (PileLayout) findViewById(R.id.pileLayout);
		pileLayout.setAdapter(new MyAdapter());
	}

	class MyAdapter extends BaseAdapter {

		private List<String> names;

		public MyAdapter() {
			names = generateNames(20);
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
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			textView.setText(names.get(position));
			return convertView;
		}
	}

	private List<String> generateNames(int count) {
		List<String> datas = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			datas.add("name" + i);
		}
		return datas;
	}
}
