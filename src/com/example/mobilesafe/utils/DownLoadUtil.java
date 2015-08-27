package com.example.mobilesafe.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;

public class DownLoadUtil {
	/**
	 * 下载一个文件
	 * 
	 * @param urlpath
	 * 路径
	 * @param filepath
	 * 保存到本地的文件路径
	 * @param pd
	 * 进度条对话框
	 * @return 下载后的apk
	 */
	public static File getFile(String urlpath, String filepath, 
			ProgressDialog pd){
		try {
			URL url = new URL(urlpath);
			File file = new File(filepath);
			FileOutputStream fos = new FileOutputStream(file);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			int max = conn.getContentLength();
			pd.setMax(max);
			InputStream is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			int process = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				process+=len;
				pd.setProgress(process);
				Thread.sleep(30);
			}
			fos.flush();
			fos.close();
			is.close();
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取一个路径中的文件名
	 * 
	 * @param urlpath
	 * @return
	 */
	public static String getFilename(String urlpath) {
		return urlpath
				.substring(urlpath.lastIndexOf("/")+1,urlpath.length());
	}
}
