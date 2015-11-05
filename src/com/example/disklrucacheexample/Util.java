package com.example.disklrucacheexample;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public class Util {
	private static final String TAG = "Util";
	
    public static File getDiskCacheDir(Context context, String dataType) {
    	String tempPath;
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
    			|| !Environment.isExternalStorageRemovable()) {
    		Log.i(TAG,"if getDiskCacheDir context:" + context);
    		Log.i(TAG,"if getDiskCacheDir getExternalCacheDir:" + context.getExternalCacheDir());
    		tempPath = context.getExternalCacheDir().getPath();
    		Log.i(TAG,"if getDiskCacheDir tempPath:" + tempPath);
    	} else {
    		tempPath = context.getCacheDir().getPath();
    		Log.i(TAG,"else getDiskCacheDir tempPath:" + tempPath);
    	}
    	
    	return new File(tempPath + File.separator + dataType);
    }

    public static int getAppVersion(Context context) {
    	try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	
    	return 1;
    }
}
