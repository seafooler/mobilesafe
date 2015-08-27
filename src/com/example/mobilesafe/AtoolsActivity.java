package com.example.mobilesafe;

import java.io.File;
import java.security.PublicKey;

import com.example.mobilesafe.utils.AssetCopyUtil;

import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class AtoolsActivity extends Activity implements OnClickListener {

	protected static final int COPY_SUCCESS = 30;
	protected static final int COPY_FAILED = 31;
	protected static final int COPY_COMMON_NUMBER_SUCCESS = 32;
	private TextView tv_atools_address_query;
	private TextView tv_atools_common_num;// ���ú���
	private ProgressDialog pd;
	
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			pd.dismiss();
			switch (msg.what) {
			case COPY_SUCCESS:
				loadQueryUI();
				break;
			case COPY_COMMON_NUMBER_SUCCESS:
				//�������ݿ�ɹ��󣬽��볣�ú�����ʾ�Ľ���
				loadCommNumUI();
				break;
			case COPY_FAILED:
				Toast.makeText(getApplicationContext(), "��������ʧ��", 0).show();
				break;
			}
		};
	};
	
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.atools);
		tv_atools_address_query = (TextView) findViewById(R.id.tv_atools_address_query);
		tv_atools_address_query.setOnClickListener(this);
		pd = new ProgressDialog(this);
		tv_atools_common_num = (TextView) findViewById(R.id.tv_atools_common_num);
		tv_atools_common_num.setOnClickListener(this);
		// ���ý�������ʾ�ķ��
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	};
	
	
	
	public AtoolsActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_atools_address_query:
			// ���������ݿ�Ҫ��������ϵͳ�ļ���data\data\����\files\address.db
			final File file = new File(getFilesDir(), "address.db");
			// �ж����ݿ��Ƿ���ڣ�������ڣ���ֱ�ӽ����������صĲ�ѯ���棬����ִ�п�������
			if (file.exists() && file.length() > 0) {
				// ���ݿ��ļ������ɹ��������ѯ��������ؽ���
				loadQueryUI();
			} else {
				// ���ݿ�Ŀ���.��ʼ����ʱ��Ҫ��ʼ��ʾ������
				pd.show();
				// �������ݿ�Ҳ��һ����Ժ�ʱ�Ĳ����������߳���ִ�иò���
				new Thread() {
					public void run() {
						AssetCopyUtil asu = new AssetCopyUtil(
								getApplicationContext());
						// ���ؿ����ɹ����Ľ��
						boolean result = asu.copyFile("naddress.db", file, pd);
						if (result) {// �����ɹ�
							Message msg = Message.obtain();
							msg.what = COPY_SUCCESS;
							handler.sendMessage(msg);
						} else {// ����ʧ��
							Message msg = Message.obtain();
							msg.what = COPY_FAILED;
							handler.sendMessage(msg);
						}
					};
				}.start();
			}
			break;
		case R.id.tv_atools_common_num:// ���ú����ѯ
			// �ж����ݿ��Ƿ��Ѿ�������ϵͳĿ¼�� data/data/����/files/address.db��
			final File commonnumberfile = new File(getFilesDir(),
					"commonnum.db");
			if (commonnumberfile.exists() && commonnumberfile.length() > 0) {
				loadCommNumUI();// ���빫���������ʾ����
			} else {
				// ���ݿ�Ŀ���.
				pd.show();
				// �������ݿ���һ����Ժ�ʱ�Ĺ���������Ϊ�俪��һ�����߳�
				new Thread() {
					public void run() {
						// �����ݿ⿽�����ֻ�ϵͳ��
						AssetCopyUtil asu = new AssetCopyUtil(
								getApplicationContext());
						boolean result = asu.copyFile("commonnum.db",
								commonnumberfile, pd);
						if (result) {// �����ɹ�
							Message msg = Message.obtain();
							msg.what = COPY_COMMON_NUMBER_SUCCESS;
							handler.sendMessage(msg);
						} else {// ����ʧ��
							Message msg = Message.obtain();
							msg.what = COPY_FAILED;
							handler.sendMessage(msg);
						}
					};
				}.start();
			}
			break;
		}
	}
	
	/**
	 * ���볣�ú������
	 */
	private void loadCommNumUI() {
		Intent intent = new Intent(this, CommonNumActivity.class);
		startActivity(intent);
	}

	/**
	 * ���뵽��������ز�ѯ����
	 */
	private void loadQueryUI() {
		Intent intent = new Intent(this, NumberQueryActivity.class);
		startActivity(intent);
	}
}
