package com.example.disklrucacheexample;
import com.example.disklrucacheexample.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;


public class GridViewAdapter extends BaseAdapter implements OnScrollListener {

	private LayoutInflater mLayoutInflater;
	private String[] mImageUrl;
	private Bitmap mDefaultBitmap;
	ImageCacheHelper mCacheHelper;
	public GridViewAdapter(Context context,String[] imageUrl) {
		mLayoutInflater = LayoutInflater.from(context);
		mImageUrl = imageUrl; 
		BitmapDrawable drawable = (BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_launcher);
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
		
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.grid_view_item, null);
			holder = new ViewHolder();
			holder.mImamgeView = (ImageView)convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Bitmap bitmap = mCacheHelper.getBitmapFromMemoryCache(url);
		if (bitmap == null) {
			bitmap = mCacheHelper.getBitmapFromDisk(url);
		}
		
		if (bitmap != null) {
			holder.mImamgeView.setImageBitmap(bitmap);
		} else {
			holder.mImamgeView.setImageBitmap(mDefaultBitmap);
			mCacheHelper.downLoadBitmap(url, new OnLoadUrlFinish() {

				@Override
				public void onLoadFinish(Bitmap bitmap, String url) {
					if (bitmap != null) {
						holder.mImamgeView.setImageBitmap(bitmap);
					}
				}
				
			});
		}
		
		return convertView;
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {

	}

}
