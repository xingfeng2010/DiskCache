package com.example.disklrucacheexample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ImageCacheHelper {
    private static final String TAG = "ImageCacheHelper";
    private static final long DEFAULT_CACHE_MOUNT = 10 * 1024 * 1024;
    private static final String BITMAP_TYPE = "bitmap";
    private static final String ALGORITHM_MD5 = "MD5";

    private static ImageCacheHelper sImageCacheHelper;
    private DiskLruCache mDiskLruCache;
    
    private ImageCacheHelper(Context context) {
    	initDiskCache(context);
    }

	public static ImageCacheHelper iniInstance(Context context) {
    	if (sImageCacheHelper == null) {
    		synchronized(ImageCacheHelper.class) {
    			if (sImageCacheHelper == null) {
    				sImageCacheHelper = new ImageCacheHelper(context);
    			}
    		}
    	}
    	
    	return sImageCacheHelper;
    }

    private void initDiskCache(Context context) {
    	try {
			mDiskLruCache = DiskLruCache.open(Util.getDiskCacheDir(context, BITMAP_TYPE), 
					Util.getAppVersion(context), 1, DEFAULT_CACHE_MOUNT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void downLoadBitmap (final String imageUrl) {
    	new Thread(){

			@Override
			public void run() {
				try {
		    		String key = hashKeyForDisk(imageUrl);
					DiskLruCache.Editor editor = mDiskLruCache.edit(key);
					if (editor != null) {
						OutputStream outputStream = editor.newOutputStream(0);
						if (downloadUrlToStream(imageUrl, outputStream)) {
							editor.commit();
						} else {
							editor.abort();
						}
					}
					mDiskLruCache.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    		
    	}.start();
    }
    
    private boolean downloadUrlToStream(String urlString,OutputStream outputStream) {
    	HttpURLConnection urlConnetion = null;
    	BufferedOutputStream out = null;
    	BufferedInputStream in = null;
    	
    	try {
			final URL url = new URL(urlString);
			urlConnetion = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnetion.getInputStream(),8 * 1024);
			out = new BufferedOutputStream(outputStream, 8 * 1024);
			
			int b;
			while((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnetion != null) {
				urlConnetion.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
    	
    	return false;
    }

    private String hashKeyForDisk(String key) {
    	String cacheKey;
    	try {
			MessageDigest mDigest = MessageDigest.getInstance(ALGORITHM_MD5);
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			cacheKey = String.valueOf(key.hashCode());
		}
    	return cacheKey;
    }

	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++ ) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			} 
			sb.append(hex);
		}
		return sb.toString();
	}
    
    public Bitmap getBitmap(String imageUrl) {
        Bitmap bitmap = null;
    	try {
    	    String key = hashKeyForDisk(imageUrl);
    	    DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
    	    if (snapShot != null) {
    	        InputStream is = snapShot.getInputStream(0);
    	        bitmap = BitmapFactory.decodeStream(is);
    	    }
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
        
        
    	return bitmap;
    }
}
