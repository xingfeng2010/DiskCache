package com.example.disklrucacheexample;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

public class ImageUtil {
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
    
    public static int calculateInSampleSize(BitmapFactory.Options options,
    		int reqWidth,int reqHeight) {
    	//源图片的高度和宽度
    	final int height = options.outHeight;
    	final int width = options.outWidth;
    	int inSampleSize = 1;
    	if (height > reqHeight || width > reqWidth) {
    		//计算出实际宽度和目标宽度的比率
    		final int heightRatio = Math.round((float)height / (float)reqHeight);
    		final int widthRatio = Math.round((float)width / (float)reqWidth);
    		//选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高一定都会大于
    		//目标的宽和高
    		inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    	}
    	return inSampleSize;
    }
    
    //缩小资源图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
    		int resId, int reqWidth, int reqHeight) {
    	//第一次解析将inJustDecodeBounds设置为true，来获取图片大小
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeResource(res, resId, options);
    	options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
    	//使用获取到的inSampleSize值再次解析图片
    	return BitmapFactory.decodeResource(res, resId, options);  
    }

    //缩小网络图片
    public static Bitmap decodeSampledBitmapFromStream(InputStream is,int reqWidth, int reqHeight) {
    	//第一次解析将inJustDecodeBounds设置为true，来获取图片大小
    	Log.i(TAG,"decodeSampledBitmapFromStream is:" + is);
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeStream(is, new Rect(), options);
    	options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
    	Log.i(TAG,"decodeSampledBitmapFromStream inSampleSize:" + options.inSampleSize);
    	options.inJustDecodeBounds = false;
    	//使用获取到的inSampleSize值再次解析图片
    	return BitmapFactory.decodeStream(is, null, options);
    }
}
