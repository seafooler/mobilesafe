package com.example.mobilesafe.adapter;

import com.example.mobilesafe.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	
	private Context context;
	
	private String newname;
	
	private static final int[] icons = { R.drawable.widget01,
		R.drawable.widget02, R.drawable.widget03, R.drawable.widget04,
		R.drawable.widget05, R.drawable.widget06, R.drawable.widget07,
		R.drawable.widget08, R.drawable.widget09 };
	private static final String[] names = {
		"�ֻ�����","ͨ����ʿ","�������","���̹���","����ͳ��","�ֻ�ɱ��","ϵͳ�Ż�","�߼�����","��������"
	};
	public MainAdapter(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		newname = sp.getString("newname", "");
	}
	
		
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.main_item, null);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_main_item_name);
		ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_main_item_icon);
		tv_name.setText(names[position]);
		iv_icon.setImageResource(icons[position]);
		
		if (position == 0) {
			if (!TextUtils.isEmpty(newname)) {
				tv_name.setText(newname);
			}
		}
		
		return view;
	}

}
