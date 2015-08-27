package com.example.mobilesafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.http.auth.MalformedChallengeException;
import org.xmlpull.v1.XmlPullParserException;

import com.example.mobilesafe.domain.UpdateInfo;
import com.example.mobilesafe.engine.UpdateInfoParser;
import com.example.mobilesafe.utils.DownLoadUtil;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.webkit.DownloadListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	
	private TextView tv_splash_version;
	private UpdateInfo info;
	private static final int GET_INFO_SUCCESS =10;
	private static final int SERVER_ERROR = 11;
	private static final int SERVER_URL_ERROR = 12;
	private static final int PROTOCOL_ERROR = 13;
	private static final int IO_ERROR = 14;
	private static final int XML_PARSE_ERROR = 15;
	private static final int DOWNLOAD_SUCCESS = 16;
	private static final int DOWNLOAD_ERROR = 17;
	protected static final String TAG = "SplashActivity";
	private long startTime;
	private RelativeLayout rl_splash;
	private long endTime;
	private ProgressDialog pd;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case XML_PARSE_ERROR:
				Toast.makeText(getApplicationContext(), "xml��������", 1).show();
				break;
			case IO_ERROR:
				Toast.makeText(getApplicationContext(), "I/O����", 1).show();
				break;
			case PROTOCOL_ERROR:
				Toast.makeText(getApplicationContext(), "Э�鲻֧��", 1).show();
				break;
			case SERVER_URL_ERROR:
				Toast.makeText(getApplicationContext(), "������·������ȷ", 1).show();
				break;
			case SERVER_ERROR:
				Toast.makeText(getApplicationContext(), "�������ڲ��쳣", 1).show();
				break;
			case GET_INFO_SUCCESS:
				String serverversion = info.getVersion();
				String currentversion = getVersion();
				if(currentversion.equals(serverversion)){
					Log.i(TAG, "�汾����ͬ����������");
					loadMainUI();
				} else {
					Log.i(TAG, "�汾�Ų���ͬ,�����Ի���");
					showUpdateDialog();
				}
				break;
			case DOWNLOAD_SUCCESS:
				Log.i(TAG, "�ļ����سɹ�");
				File file = (File)msg.obj;
				installApk(file);
				break;
			}
		}
	};
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        rl_splash = (RelativeLayout)findViewById(R.id.r1_splash);
        tv_splash_version = (TextView)findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("�汾�ţ�"+ getVersion());
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(2000);
        rl_splash.startAnimation(aa);
        new Thread(new CheckVersionTask()){
        }.start();
    }

    
    private class CheckVersionTask implements Runnable {
    	public void run() {
    		startTime = System.currentTimeMillis();
    		Message msg = Message.obtain();
    		try {
    			String serverurl = getResources().getString(R.string.serverurl);
    			URL url = new URL(serverurl);
    			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    			conn.setRequestMethod("GET");
    			conn.setConnectTimeout(5000);
    			int code = conn.getResponseCode();
    			if(code == 200) {
    				InputStream is = conn.getInputStream();
    				info = UpdateInfoParser.getUpdateInfo(is);
    				endTime = System.currentTimeMillis();
    				long resulttime = endTime - startTime;
    				if (resulttime < 2000) {
    					try {
    						Thread.sleep(2000 - resulttime);
    					} catch (InterruptedException e) {
							// TODO: handle exception
    						e.printStackTrace();
						}
    				}
    				msg.what = GET_INFO_SUCCESS;
    				handler.sendMessage(msg);
    			} else {
    				msg.what = SERVER_ERROR;
    				handler.sendMessage(msg);
    				endTime = System.currentTimeMillis();
    				long resultTime = endTime - startTime;
    				if (resultTime < 2000) {
    					try {
    						Thread.sleep(2000 - resultTime);
    					} catch (InterruptedException e) {
							// TODO: handle exception
    						e.printStackTrace();
						}
    				}
    			}
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    			msg.what = SERVER_URL_ERROR;
    			handler.sendMessage(msg);
    		} catch (ProtocolException e) {
				// TODO: handle exception
    			msg.what = PROTOCOL_ERROR;
    			handler.sendMessage(msg);
				e.printStackTrace();
			}catch (IOException e) {
				msg.what = IO_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				msg.what = XML_PARSE_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * ��ʾ������ʾ�ĶԻ���
     */
    protected void showUpdateDialog() {
    	AlertDialog.Builder builder = new Builder(this);
    	builder.setIcon(getResources().getDrawable(R.drawable.notification));
    	builder.setTitle("������ʾ");
    	builder.setMessage(info.getDescription());
    	pd = new ProgressDialog(SplashActivity.this);
    	pd.setMessage("��������");
    	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	builder.setPositiveButton("����", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i(TAG, "����������" + info.getApkurl());
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
					pd.show();
					new Thread() {
						public void run() {
							String path = info.getApkurl();
							String filename = DownLoadUtil.getFilename(path);
							File file = new File(Environment.getExternalStorageDirectory(), filename);
							file = DownLoadUtil.getFile(path, file.getAbsolutePath(), pd);
							if (file != null) {
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_SUCCESS;
								msg.obj = file;
								handler.sendMessage(msg);
							} else {
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_ERROR;
								handler.sendMessage(msg);
							}
							pd.dismiss();
						};
					}.start();
				} else {
					Toast.makeText(getApplicationContext(), "sd��������", 1).show();
					loadMainUI();
				}
			}
		});
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				loadMainUI();
			}
		});
		builder.create().show();
    }
    
    
    /**
     * ��ȡ��ǰӦ�ó���İ汾��
     * 
     * @return
     */
    private String getVersion() {
    	PackageManager pm = this.getPackageManager();
    	try {
    		PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
    		return info.versionName;
    	} catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
    		return "";
		}
    }
    
    
    /**
     * ����������
     */
    public void loadMainUI() {
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();// �ѵ�ǰ��Activity������ջ�����Ƴ�
//		Log.d("SplashActivity", "Success !");
//		System.out.println("Success !");
    }
    
    /**
     * ��װһ��apk�ļ�
     * 
     * @param file Ҫ��װ�������ļ���
     */	
    
    
    protected void installApk(File file) {
    	Intent intent = new Intent();
    	intent.setAction("android.intent.action.VIEW");
    	intent.addCategory("android.intent.category.DEFAULT");
    	intent.setDataAndType(Uri.fromFile(file), 
    			"application/vnd.android.package-archive");
    	startActivity(intent);
    	
    }
    
}
