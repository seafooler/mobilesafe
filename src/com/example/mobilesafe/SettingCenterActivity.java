package com.example.mobilesafe;

import com.example.mobilesafe.service.ShowCallLocationService;
import com.example.mobilesafe.utils.ServiceStatusUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingCenterActivity extends Activity implements OnClickListener{
	
	private SharedPreferences sp;
	private TextView tv_setting_autoupdate_status;
	
	private CheckBox cb_setting_autoupdate;
	
	private TextView tv_setting_show_location_status;// 显示来显归属地是否开启的状态
	private CheckBox cb_setting_show_location;// 是否开启来电归属地的Checkbox
	
	private RelativeLayout rl_setting_show_location;// “来电归属地是否开启”控件的父控件
	
	private Intent showLocationIntent;// 开启来电归属地信息显示的意图
	
	// 归属地显示背景控件的声明
	private RelativeLayout rl_setting_change_bg;// “来电归属地风格设置”控件的父控件
	private TextView tv_setting_show_bg;// “来电归属地风格设置”下用于显示当前的风格文字
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.setting_center);
		super.onCreate(savedInstanceState);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		cb_setting_autoupdate = (CheckBox)findViewById(R.id.cb_setting_autoupdate);
		
		tv_setting_autoupdate_status = (TextView) findViewById(R.id.tv_setting_autoupdate_status);
		boolean autoupdate = sp.getBoolean("autoupdate", true);
		
		if (autoupdate) {
			tv_setting_autoupdate_status.setText("自动更新已经开启");
			cb_setting_autoupdate.setChecked(true);
		} else {
			tv_setting_autoupdate_status.setText("自动更新已经关闭");
			cb_setting_autoupdate.setChecked(false);
		}
		
		cb_setting_autoupdate
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			// 参数一：当前的Checkbox 第二个参数：当前的Checkbox是否处于勾选状态
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// 获取编辑器
				Editor editor = sp.edit();
				// 持久化存储当前Checkbox的状态，当下次进入时，依然可以保存当前设置的状态
				editor.putBoolean("autoupdate", isChecked);
				// 将数据真正提交到sp里面
				editor.commit();
				if (isChecked) {// Checkbox处于选中效果
					// 当Checkbox处于勾选状态时，表示我们的自动更新已经开启，同时修改字体颜色
					tv_setting_autoupdate_status
							.setTextColor(Color.WHITE);
					tv_setting_autoupdate_status.setText("自动更新已经开启");
				} else {// Checkbox处于未勾选状态
						// 当Checkbox未处于勾选状态时，表示我们的自动更新已经开启，同时修改字体颜色
					tv_setting_autoupdate_status
							.setTextColor(Color.RED);
					tv_setting_autoupdate_status.setText("自动更新已经关闭");
				}
			}
		});
		// 显示归属地信息的ui初始化
		tv_setting_show_location_status = (TextView) findViewById(R.id.tv_setting_show_location_status);
		cb_setting_show_location = (CheckBox) findViewById(R.id.cb_setting_show_location);
		rl_setting_show_location = (RelativeLayout) findViewById(R.id.rl_setting_show_location);
		showLocationIntent = new Intent(this, ShowCallLocationService.class);
		
		rl_setting_show_location.setOnClickListener(this);
		
		// 归属地显示背景的声明
		rl_setting_change_bg = (RelativeLayout) findViewById(R.id.rl_setting_change_bg);
		tv_setting_show_bg = (TextView) findViewById(R.id.tv_setting_show_bg);
		rl_setting_change_bg.setOnClickListener(this);
		
	}
	
	/**
	 * 当界面显示在前台时，立即设置Checkbox的状态
	 */
	@Override
	protected void onResume() {
		if (ServiceStatusUtil.isServiceRunning(this,
				"com.example.mobilesafe.service.ShowCallLocationService")) {
			cb_setting_show_location.setChecked(true);
			tv_setting_show_location_status.setText("来电归属地显示已经开启");
		} else {
			cb_setting_show_location.setChecked(false);
			tv_setting_show_location_status.setText("来电归属地显示没有开启");
		}
		super.onResume();
	}
	// 响应点击事件
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.rl_setting_show_location:// 来电归属地是否开启
				if (cb_setting_show_location.isChecked()) {
					tv_setting_show_location_status.setText("来电归属地显示没有开启");
					stopService(showLocationIntent);
					Log.i("dai", "incomingnumber_no");
					cb_setting_show_location.setChecked(false);
				} else {
					tv_setting_show_location_status.setText("来电归属地显示已经开启");
					startService(showLocationIntent);
					Log.i("dai", "incomingnumber_yes");
					cb_setting_show_location.setChecked(true);
				}
				break;
			case R.id.rl_setting_change_bg:
				showChooseBgDialog();
				break;
			}
		}

		/**
		 * 更改背景颜色的对话框
		 */
		private void showChooseBgDialog() {
			// 获取一个对话框构造器
			AlertDialog.Builder builder = new Builder(this);
			// 设置对话框标题的图标
			builder.setIcon(R.drawable.notification);
			// 设置对话框的标题
			builder.setTitle("归属地提示框风格");
			// 对话框中item的对应显示文字
			final String[] items = { "半透明", "活力橙", "卫士蓝", "苹果绿", "金属灰" };
			// 用于显示对话框中那一个条目被选中。默认的是第一个条目
			int which = sp.getInt("which", 0);
			// 设置单个选择条目。Item中，只能有一个处于选中状态
			builder.setSingleChoiceItems(items, which,
					new DialogInterface.OnClickListener() {
						// 处理Item的点击事件
						public void onClick(DialogInterface dialog, int which) {
							// 将条目的id存入sp中
							Editor editor = sp.edit();
							editor.putInt("which", which);
							editor.commit();
							// 设置Item的文字信息
							tv_setting_show_bg.setText(items[which]);
							// 关闭对话框
							dialog.dismiss();
						}
					});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

				}
			});
			// 创建并显示出对话框
			builder.create().show();
		}
}
