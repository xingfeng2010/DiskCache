package com.example.disklrucacheexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
	private ImageView image;
	private static final String IMAGE_URL = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
	public static final int REFRESH_MSG = 0x11;
	private Handler mHandler = new Handler(this.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                image.setImageBitmap((Bitmap)msg.obj);
            }
        }
	    
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		image = (ImageView) this.findViewById(R.id.image);
		
		ImageCacheHelper cacheHelper = ImageCacheHelper.iniInstance(this.getApplicationContext());
		Bitmap bitmap = cacheHelper.getBitmap(IMAGE_URL);
		Log.i(TAG,"onCreate bitmap = " + bitmap);
		if (bitmap == null) {
		    cacheHelper.downLoadBitmap(IMAGE_URL);
		} else {
		    image.setImageBitmap(bitmap);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
