package com.example.disklrucacheexample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import libcore.io.DiskLruCache;
import libcore.io.DiskLruCache.Snapshot;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

public class ImageCacheHelper {
    private static final String TAG = "ImageCacheHelper";
    private static final long DEFAULT_CACHE_MOUNT = 10 * 1024 * 1024;
    private static final String BITMAP_TYPE = "bitmap";
    private static final String ALGORITHM_MD5 = "MD5";

    private static ImageCacheHelper sImageCacheHelper;
    private DiskLruCache mDiskLruCache;
    private LruCache mMemoryCache;
    private Context mContext;
    /** 
     * 下载Image的线程池 
     */  
    private ExecutorService mImageThreadPool = null;
    
    private ImageCacheHelper(Context context) {
    	initLruCache(context);
    	initDiskCache(context);
    	mContext = context;
    }

    public ExecutorService getImageThreadPool() {
    	if (mImageThreadPool == null) {
    		mImageThreadPool = Executors.newFixedThreadPool(3);
    	}
    	
    	return mImageThreadPool;
    }
    
	private void initLruCache(Context context) {
		int maxSize = (int)Runtime.getRuntime().maxMemory() / 8;
	    mMemoryCache = new LruCache<String,Bitmap>(maxSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
	    };
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
			// 获取图片缓存路径
			File cacheDir = ImageUtil.getDiskCacheDir(context, BITMAP_TYPE);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			mDiskLruCache = DiskLruCache.open(cacheDir,ImageUtil.getAppVersion(context), 1, DEFAULT_CACHE_MOUNT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void downLoadBitmap (final String imageUrl,final OnLoadUrlFinish listener) {
		final Handler handler = new Handler(mContext.getMainLooper()) {
	        @Override
	        public void handleMessage(Message msg) {
	            if (msg != null) {
	            	listener.onLoadFinish((Bitmap)msg.obj,imageUrl);
	            }
	        }
		};
		
    	Runnable run = new Runnable() {
			@Override
			public void run() {
	    	    FileDescriptor fileDescriptor = null;
	    	    FileInputStream fileInputStream = null;
	    	    Snapshot snapShot = null;
	    	    Bitmap bitmap = null;
				try {
		    		String key = hashKeyForDisk(imageUrl);
		    		snapShot = mDiskLruCache.get(key);
		    	    Log.i(TAG,"getBitmapFromDisk snapShot:"+snapShot);
					if (snapShot == null) {
						DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						OutputStream outputStream = editor.newOutputStream(0);
						if (downloadUrlToStream(imageUrl, outputStream,handler)) {
							editor.commit();
						} else {
							editor.abort();
						}
						// 缓存被写入后，再次查找key对应的缓存
						snapShot = mDiskLruCache.get(key);
					}
					
		    	    if (snapShot != null) {
		    	    	fileInputStream = (FileInputStream)snapShot.getInputStream(0);
		    	        fileDescriptor = fileInputStream.getFD();
		    	    }
		    	    Log.i(TAG,"getBitmapFromDisk fileDescriptor:"+fileDescriptor);
		    	    if (fileDescriptor != null) {
		    	    	bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
		    	    }
		    	    
		    	    if (bitmap != null) {
		    			//Bitmap bitmap = BitmapFactory.decodeStream(urlConnetion.getInputStream());
		    			refreshUi(bitmap,handler);
		    			addBitmapToMemoryCache(imageUrl,bitmap);
		    	    }
		    	    Log.i(TAG,"getBitmapFromDisk bitmap:"+bitmap);
					//mDiskLruCache.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    		
    	};
    	getImageThreadPool().execute(run);
    }
    
    private boolean downloadUrlToStream(String urlString,OutputStream outputStream,Handler handler) {
    	HttpURLConnection urlConnetion = null;
    	BufferedOutputStream out = null;
    	BufferedInputStream in = null;
    	
    	try {
			final URL url = new URL(urlString);
			urlConnetion = (HttpURLConnection) url.openConnection();
			//Bitmap bitmap = ImageUtil.decodeSampledBitmapFromStream(urlConnetion.getInputStream(),135,150);
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

	private void refreshUi(Bitmap bitmap, Handler handler) {
		Log.i(TAG,"refreshUi bitmap : " + bitmap);
		Message msg = Message.obtain();
		msg.what = MainActivity.REFRESH_MSG;
		msg.obj = bitmap;
		handler.sendMessage(msg);
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
    
    //添加Bitmap到内存缓存
    public void addBitmapToMemoryCache(String key,Bitmap value) {
    	if (getBitmapFromMemoryCache(key) == null && value != null) {
    		mMemoryCache.put(key, value);
    	}
    }

    //从内存缓冲获取一个Bitmap
    public Bitmap getBitmapFromMemoryCache(String key) {
		return (Bitmap) mMemoryCache.get(key);
	}

	public void cancelDownLoad() {
		if (mImageThreadPool != null) {
			mImageThreadPool.shutdown();
			mImageThreadPool = null;
		}
	}
	
	/**
	 * 将缓存记录同步到journal文件中。
	 */
	public void fluchCache() {
		if (mDiskLruCache != null) {
			try {
				mDiskLruCache.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
