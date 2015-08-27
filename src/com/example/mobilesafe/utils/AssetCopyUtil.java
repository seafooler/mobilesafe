package com.example.mobilesafe.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;

public class AssetCopyUtil {
	private Context context;

	public AssetCopyUtil(Context context) {
		this.context = context;
	}
	/**
	 * 拷贝资产目录下的文件
	 * 
	 * @param srcfilename
	 *            源文件的名称
	 * @param file
	 *            目标文件的对象
	 * @param pd
	 *            进度条对话框
	 * @return 是否拷贝成功
	 */
	public boolean copyFile(String srcfilename, File file, ProgressDialog pd) {
		try {
			AssetManager am = context.getAssets();
			InputStream is = am.open(srcfilename);
			int max = is.available();
			pd.setMax(max);
			FileOutputStream fos = new FileOutputStream(file);
			// 创建一个缓存区
						byte[] buffer = new byte[1024];
						int len = 0;
						// 进度条的最开始的位置应该为0
						int process = 0;
						while ((len = is.read(buffer)) != -1) {
							fos.write(buffer, 0, len);
							// 让进度条不断的动态显示当前的拷贝进度
							process += len;
							pd.setProgress(process);
						}
						// 刷新缓冲区，关流
						fos.flush();
						fos.close();
						return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
		
}
