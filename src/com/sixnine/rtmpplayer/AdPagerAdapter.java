package com.sixnine.rtmpplayer;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sixinine.rtmpplayer.R;

public class AdPagerAdapter extends PagerAdapter {
	private final List<AdInfo> mImages;
	private final LayoutInflater mInflater;
	private Context context;
	protected MemoryCacheAware<String, Bitmap> mMemoryCache;
	protected DisplayImageOptions mOptions;
	protected ImageLoader mImageLoader = null;

	@SuppressWarnings("deprecation")
	public AdPagerAdapter(List<AdInfo> images, Context context) {
		mImages = images;
		this.context = context;
		mInflater = LayoutInflater.from(context);

		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(context));
		mMemoryCache = mImageLoader.getMemoryCache();
		mOptions = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.default_720x291)
				.cacheInMemory()
				.cacheOnDisc()
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.build();

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		if (mImages != null) {
			return mImages.size();
		}
		return 0;
	}

	private void displayImage(ImageView imageView, String url) {
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null && !bitmap.isRecycled()) {
			imageView.setImageBitmap(bitmap);
		} else {
			mImageLoader.displayImage(url, imageView, mOptions, null);
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View view = mInflater.inflate(R.layout.item_home_ad, container, false);
		final ImageView icon = (ImageView) view.findViewById(R.id.icon);

		
		displayImage(icon,mImages.get(position).imageUrl);
		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AdInfo info = mImages.get(position);
				if (info.adType != null && info.adType.length() > 6) {
					startWebView(info);
				} else {
					LiveFragment.startChatRoom(context,
							LiveFragment.getLiveHost());
				}
			}

		});
		container.addView(view);
		return view;
	}

	private void startWebView(AdInfo info) {
		if (!LiveFragment.isPluginApkInstalled(context)) {
			return;
		}

		String targetUrl;
		if (info.adType.contains("&amp;")) {
			targetUrl = info.adType.replace("&amp;", "&");
		} else {
			targetUrl = info.adType;
		}
		ComponentName componentName = new ComponentName("com.ninexiu.live",
				"com.ninexiu.live.AdActivity");
		Intent intent = new Intent();
		intent.putExtra("url", targetUrl);
		intent.setComponent(componentName);
		context.startActivity(intent);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

}