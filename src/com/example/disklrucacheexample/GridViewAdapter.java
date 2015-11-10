package com.example.disklrucacheexample;
import com.example.disklrucacheexample.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class GridViewAdapter extends BaseAdapter implements OnScrollListener {

	private static final String TAG = "GridViewAdapter";
	private LayoutInflater mLayoutInflater;
	private String[] mImageUrl;
	private Bitmap mDefaultBitmap;
	private ImageCacheHelper mCacheHelper;
	private GridView mGridView;
	
	private int mFirstVisibleItem = 0;
	private int mVisibleItemCount = 0;
	private boolean isFirstEnter = true;
	
	public GridViewAdapter(Context context,String[] imageUrl,GridView gridView) {
		mLayoutInflater = LayoutInflater.from(context);
		mImageUrl = imageUrl; 
		mGridView = gridView;
		mGridView.setOnScrollListener(this);
		BitmapDrawable drawable = (BitmapDrawable)context.getResources().getDrawable(R.drawable.jujingyi);
		mDefaultBitmap = drawable.getBitmap();
		mCacheHelper = ImageCacheHelper.iniInstance(context);
	}
	
	@Override
	public int getCount() {
		return mImageUrl.length;
	}

	@Override
	public Object getItem(int position) {
		return mImageUrl[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		public ImageView mImamgeView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String url = mImageUrl[position];
		
		final ImageView view;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.grid_view_item, null);
		}
		
		view = (ImageView)convertView.findViewById(R.id.image);
		view.setTag(url);
		
		Bitmap bitmap = mCacheHelper.getBitmapFromMemoryCache(url);
		Log.i(TAG, "getView getBitmapFromMemoryCache bitmap:" + bitmap);
		if (bitmap != null) {
			view.setImageBitmap(bitmap);
		} else {
			view.setImageBitmap(mDefaultBitmap);
		}
		
		return convertView;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,  
            int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		Log.i(TAG, "onScroll mFirstVisibleItem: " + mFirstVisibleItem);
		
		if (isFirstEnter && visibleItemCount > 0) {
			downloadImage(firstVisibleItem,visibleItemCount);
			isFirstEnter = false;
		}
	}

	private void downloadImage(int firstVisibleItem, int visibleItemCount) {
		for (int i = firstVisibleItem;i < firstVisibleItem + visibleItemCount; i++) {
			String url = mImageUrl[i];
			final ImageView imageView = (ImageView) mGridView.findViewWithTag(url);
			if (!showImage(url,imageView)) {
				mCacheHelper.downLoadBitmap(url, new OnLoadUrlFinish() {

					@Override
					public void onLoadFinish(Bitmap bitmap, String url) {
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						}
					}
				});
			}
		}
	}

	private boolean showImage(String url, ImageView imageView) {
		Bitmap bitmap = mCacheHelper.getBitmapFromMemoryCache(url);
		Log.i(TAG, "getView getBitmapFromMemoryCache bitmap:" + bitmap);

		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return true;
		}
		imageView.setImageBitmap(mDefaultBitmap);
		return false;
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, int scrollState) {

		Log.i(TAG, "onScrollStateChanged scrollState: " + scrollState);
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			downloadImage(mFirstVisibleItem,mVisibleItemCount);
		} else {
		    mCacheHelper.cancelDownLoad();
		}
	}

}
