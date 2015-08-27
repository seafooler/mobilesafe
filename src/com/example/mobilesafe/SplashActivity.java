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
				Toast.makeText(getApplicationContext(), "xml解析错误", 1).show();
				break;
			case IO_ERROR:
				Toast.makeText(getApplicationContext(), "I/O错误", 1).show();
				break;
			case PROTOCOL_ERROR:
				Toast.makeText(getApplicationContext(), "协议不支持", 1).show();
				break;
			case SERVER_URL_ERROR:
				Toast.makeText(getApplicationContext(), "服务器路径不正确", 1).show();
				break;
			case SERVER_ERROR:
				Toast.makeText(getApplicationContext(), "服务器内部异常", 1).show();
				break;
			case GET_INFO_SUCCESS:
				String serverversion = info.getVersion();
				String currentversion = getVersion();
				if(currentversion.equals(serverversion)){
					Log.i(TAG, "版本号相同进入主界面");
					loadMainUI();
				} else {
					Log.i(TAG, "版本号不相同,升级对话框");
					showUpdateDialog();
				}
				break;
			case DOWNLOAD_SUCCESS:
				Log.i(TAG, "文件下载成功");
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
        tv_splash_version.setText("版本号："+ getVersion());
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
     * 显示升级提示的对话框
     */
    protected void showUpdateDialog() {
    	AlertDialog.Builder builder = new Builder(this);
    	builder.setIcon(getResources().getDrawable(R.drawable.notification));
    	builder.setTitle("升级提示");
    	builder.setMessage(info.getDescription());
    	pd = new ProgressDialog(SplashActivity.this);
    	pd.setMessage("正在下载");
    	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	builder.setPositiveButton("升级", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i(TAG, "升级，下载" + info.getApkurl());
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
					Toast.makeText(getApplicationContext(), "sd卡不可用", 1).show();
					loadMainUI();
				}
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				loadMainUI();
			}
		});
		builder.create().show();
    }
    
    
    /**
     * 获取当前应用程序的版本号
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
     * 加载主界面
     */
    public void loadMainUI() {
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();// 把当前的Activity从任务栈里面移除
//		Log.d("SplashActivity", "Success !");
//		System.out.println("Success !");
    }
    
    /**
     * 安装一个apk文件
     * 
     * @param file 要安装的完整文件名
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
