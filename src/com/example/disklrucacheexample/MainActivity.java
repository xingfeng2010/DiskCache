package com.example.disklrucacheexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
	private GridView mGridView;
	private static final String IMAGE_URL = "http://img.hb.aicdn.com/8f0355999f4f61d2b379aee7f09d9f99213eaa9c14ad9-2PeDy9_fw658";
	
	public static final int REFRESH_MSG = 0x11;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGridView = (GridView)this.findViewById(R.id.grid);
		
		GridViewAdapter adapter = new GridViewAdapter(this,Images.imageThumbUrls2,mGridView);
		mGridView.setAdapter(adapter);		
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

	@Override
	protected void onDestroy() {
		ImageCacheHelper.iniInstance(this).cancelDownLoad();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ImageCacheHelper.iniInstance(this).fluchCache();
	}
}
