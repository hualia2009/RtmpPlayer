package com.sixnine.rtmpplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sixinine.rtmpplayer.R;

public class AnchorListAdapter extends BaseAdapter {
	private List<Anchor> hostDatas = new ArrayList<Anchor>();
	private Context context;
	protected MemoryCacheAware<String, Bitmap> mMemoryCache;
	protected DisplayImageOptions mOptions;
	protected ImageLoader mImageLoader = null;
	
	private static final int ITEM_LEFT = 0;
	private static final int ITEM_MIDDLE = 1;
	private static final int ITEM_RIGHT = 2;
	

	@SuppressWarnings("deprecation")
	public AnchorListAdapter(List<Anchor> hostDatas, Context context) {
		super();
		this.hostDatas = hostDatas;
		this.context = context;
		
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(context));
		mMemoryCache = mImageLoader.getMemoryCache();
		mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.default_240x240)
		.cacheInMemory()
		.cacheOnDisc()
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.build();
	}

	public void resetList(List<Anchor> hostDatas) {
		this.hostDatas = hostDatas;
	}

	public List<Anchor> getDatas() {
		return hostDatas;
	}

	@Override
	public int getCount() {
		if(hostDatas == null){
			return 0;
		}
		return hostDatas.size() / 3;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
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
	public View getView(final int position, View view, ViewGroup arg2) {
		ItemHolder holder;
		if (view == null) {
			holder = new ItemHolder();
			view = LayoutInflater.from(context).inflate(R.layout.item_direct, null);
			initViews(holder.leftHolder, view.findViewById(R.id.item_left),
					getHost(position, ITEM_LEFT));
			initViews(holder.middleHolder, view.findViewById(R.id.item_middle),
					getHost(position, ITEM_MIDDLE));
			initViews(holder.rightHolder, view.findViewById(R.id.item_right),
					getHost(position, ITEM_RIGHT));
			view.setTag(holder);
		} else {
			holder = (ItemHolder) view.getTag();
		}

		view.findViewById(R.id.item_left).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getHost(position, ITEM_LEFT) == null) {
					return;
				}
				LiveFragment.startChatRoom(context,getHost(position, ITEM_LEFT));
			}
		});
		view.findViewById(R.id.item_middle).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (getHost(position, ITEM_MIDDLE) == null) {
					return;
				}
				LiveFragment.startChatRoom(context,getHost(position, ITEM_MIDDLE));
			}
		});
		view.findViewById(R.id.item_right).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getHost(position, ITEM_RIGHT) == null) {
					return;
				}
				LiveFragment.startChatRoom(context,getHost(position, ITEM_RIGHT));
			}
		});
		initItem(getHost(position, ITEM_LEFT), holder.leftHolder);
		initItem(getHost(position, ITEM_MIDDLE), holder.middleHolder);
		initItem(getHost(position, ITEM_RIGHT), holder.rightHolder);
		return view;
	}

	private void initViews(ViewHolder holder, View view, final Anchor host) {
		if (host == null) {
			return;
		}
		holder.room_thumb = (ImageView) view.findViewById(R.id.icon);
		holder.room_name = (TextView) view.findViewById(R.id.name);
		holder.room_mem_count = (TextView) view.findViewById(R.id.watching);
		holder.room_play_icon = (ImageView) view.findViewById(R.id.play);
	}

	private Anchor getHost(int position, int type) {
		Anchor host = null;
		switch (type) {
		case ITEM_LEFT:
			host = hostDatas.get(3 * position);
			break;
		case ITEM_MIDDLE:
			if (hostDatas.size() > (3 * position + 1)) {
				host = hostDatas.get(3 * position + 1);
			}
			break;
		case ITEM_RIGHT:
			if (hostDatas.size() > (3 * position + 2)) {
				host = hostDatas.get(3 * position + 2);
			}
			break;
		default:
			break;
		}
		return host;
	}

	private void initItem(Anchor host, final ViewHolder holder) {
		if (host == null) {
			return;
		}
		holder.room_mem_count.setText(String.format(context.getString(R.string.watching),
				host.getAudice()));
		holder.room_name.setText(host.getNickName());
		if (host.getIsPlay().equals("1")) {
			holder.room_play_icon.setVisibility(View.VISIBLE);
		} else {
			holder.room_play_icon.setVisibility(View.GONE);
		}
		displayImage(holder.room_thumb, host.getHostImage());
	}

	class ItemHolder {
		ViewHolder leftHolder = new ViewHolder();
		ViewHolder middleHolder = new ViewHolder();
		ViewHolder rightHolder = new ViewHolder();
	}

	class ViewHolder {
		public ImageView room_thumb;
		public TextView room_mem_count;
		public TextView room_name; 
		public ImageView room_play_icon; 
		public View parent;
	}

}

