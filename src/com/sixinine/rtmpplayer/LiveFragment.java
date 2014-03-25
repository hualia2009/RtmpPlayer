package com.sixinine.rtmpplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sixnine.live.R;
import com.sixnine.live.bean.AdInfo;
import com.sixnine.live.bean.Anchor;
import com.sixnine.live.bean.Version;
import com.sixnine.live.data.AdPagerAdapter;
import com.sixnine.live.data.AnchorListAdapter;
import com.sixnine.live.data.MySharedPrefs;
import com.sixnine.live.install.FileUtil;
import com.sixnine.live.install.UpdateService;
import com.sixnine.live.thread.ThreadPoolWrap;
import com.sixnine.live.view.CustomViewpager;

public class LiveFragment extends Fragment {

	private View view;
	private View mLoadingView;
	private View mRetryView;
	private View headerView;
	private PullToRefreshListView pullToRefreshListView;
	private ListView listView;
	private CustomViewpager viewPager;
	private View[] adPlans;
	private LinearLayout pagerDot;
	private RelativeLayout loadMoreLayout;
	private Handler handler;
	protected int mCurrentPageIndex;
	private static List<Anchor> hostList;
	private List<AdInfo> adInfoList;
	private static Version version;
	private AnchorListAdapter anchorAdapter;
	private int pageNum = 1;
	private boolean isDataFirstLoaded;

	private static final int MSG_VIEWPAGER = 1;
	private static final int MSG_ANCHOR = 2;
	private static final int MSG_ANCHOR_PAGE = 3;
	private static final int MSG_VIEWPAGER_CIRCLE = 4;
	private static final int NETWORK_ERROR = 5;

	private static final int TIME_CIRCLE_DELAY = 5000;

	private static final String URL_AD = "http://www.69xiu.com/index.php?action=MBAdvertising";
	private static final String URL_ANCHOR = "http://www.69xiu.com/index.php?action=getcommonroom";
	private static final String URL_VERSION = "http://42.62.31.17/version.json";

	private static final String TAG = "LiveFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isDataFirstLoaded = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(!isDataFirstLoaded && view != null){
			((ViewGroup) view.getParent()).removeView(view);
			return view;
		}
		view = inflater.inflate(R.layout.cotent_layout, container, false);
		initViews();
		return view;
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(!isDataFirstLoaded){
			return;
		}
		
		handler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				setLoadingView(false);
				switch (msg.what) {
				case MSG_VIEWPAGER:
					// ViewPager load data success
					viewPager.setAdapter(new AdPagerAdapter(adInfoList,
							getActivity()));
					if (adInfoList == null) {
						break;
					}
					initAdPlan(pagerDot, adInfoList.size());
					handler.sendEmptyMessageDelayed(MSG_VIEWPAGER_CIRCLE,
							TIME_CIRCLE_DELAY);
					break;
				case MSG_ANCHOR:
					// AnchorList load data success
					pullToRefreshListView.onRefreshComplete();
					anchorAdapter = new AnchorListAdapter(hostList,
							getActivity());
					listView.setAdapter(anchorAdapter);
					loadMoreLayout.setVisibility(View.GONE);
					break;
				case MSG_VIEWPAGER_CIRCLE:
					// ViewPager circle message
					mCurrentPageIndex++;
					viewPager.setCurrentItem(mCurrentPageIndex
							% viewPager.getAdapter().getCount());
					break;
				case MSG_ANCHOR_PAGE:
					List<Anchor> tempList = (List<Anchor>) msg.obj;
					if (tempList == null || tempList.isEmpty()) {
						setNoMoreLayoutText(R.string.loading_end, false);
						return;
					}
					hostList.addAll((List<Anchor>) msg.obj);
					anchorAdapter.notifyDataSetChanged();
					loadMoreLayout.setVisibility(View.GONE);
					break;
				case NETWORK_ERROR:
					pullToRefreshListView.onRefreshComplete();
					setNoMoreLayoutText(R.string.loading_error, false);
					break;
				default:
					break;
				}
			}
		};

		listView.addFooterView(loadMoreLayout);
		listView.addHeaderView(headerView);
		setLoadingView(true);
		loadVersion();
		loadAdsData();
		loadAnchorData();
		pullToRefreshListView.setOnRefreshListener(onRefreshListener);
		pullToRefreshListView.setOnScrollListener(onScrollListener);
		mRetryView.setOnClickListener(mOnClickListener);
	}

	private void setNoMoreLayoutText(int stringId, boolean isProgressbarShow) {
		loadMoreLayout.setVisibility(View.VISIBLE);
		ProgressBar progressBar = (ProgressBar) loadMoreLayout
				.findViewById(R.id.list_more_progressbar);
		progressBar.setVisibility(View.VISIBLE);
		if (!isProgressbarShow) {
			progressBar.setVisibility(View.GONE);
		}
		TextView textView = (TextView) loadMoreLayout
				.findViewById(R.id.list_more_textview);
		textView.setText(stringId);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mRetryView) {
				setLoadingView(true);
				setRetryView(false);
				return;
			}
		}
	};

	private OnScrollListener onScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_IDLE
					&& 1 + view.getLastVisiblePosition() == view.getCount()) {
				if(ThreadPoolWrap.getThreadPool().isThreadPoolActive()){
					return;
				}
				setNoMoreLayoutText(R.string.loading_begin, true);
				pageNum++;
				loadAnchorDataByPage();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	};

	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			mCurrentPageIndex = arg0;
			setAdPlanSelected(arg0);
			handler.removeMessages(MSG_VIEWPAGER_CIRCLE);
			handler.sendEmptyMessageDelayed(MSG_VIEWPAGER_CIRCLE,
					TIME_CIRCLE_DELAY);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	private void initAdPlan(LinearLayout linearLayout, int size) {
		linearLayout.removeAllViews();
		adPlans = new View[size];
		for (int i = 0; i < size; i++) {
			View view = getActivity().getLayoutInflater().inflate(
					R.layout.item_plan, null);
			adPlans[i] = view;
			linearLayout.addView(view);
		}
		setAdPlanSelected(0);
	}

	private void setAdPlanSelected(int position) {
		for (int i = 0; i < adPlans.length; i++) {
			if (i == position) {
				adPlans[i].setSelected(true);
			} else {
				adPlans[i].setSelected(false);
			}
		}
	}

	private String getLastUpdateTimeStamp() {
		SimpleDateFormat formatter = new SimpleDateFormat("M月d�?  mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String tmStr = formatter.format(curDate);
		return tmStr;
	}

	private OnRefreshListener2 onRefreshListener = new OnRefreshListener2() {

		@Override
		public void onPullDownToRefresh() {
			if(ThreadPoolWrap.getThreadPool().isThreadPoolActive()){
				return;
			}
			pageNum = 1;
			pullToRefreshListView.setLastUpdatedLabel(getLastUpdateTimeStamp());
			loadAnchorData();
			setNoMoreLayoutText(R.string.loading_begin, true);
		}

		@Override
		public void onPullUpToRefresh() {

		}

	};

	public void onDestroyView() {
		handler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	};

	private void setLoadingView(boolean isLoading) {
		if (isLoading) {
			mLoadingView.setVisibility(View.VISIBLE);
		} else {
			mLoadingView.setVisibility(View.GONE);
		}
	}

	private void setRetryView(boolean isRetry) {
		if (isRetry) {
			mRetryView.setVisibility(View.VISIBLE);
		} else {
			mRetryView.setVisibility(View.GONE);
		}
	}

	private void initViews() {
		mLoadingView = view.findViewById(R.id.l_loadingview);
		mRetryView = view.findViewById(R.id.l_retryview);

		loadMoreLayout = (RelativeLayout) LayoutInflater.from(getActivity())
				.inflate(R.layout.video_list_more, null);
		loadMoreLayout.setVisibility(View.GONE);

		pullToRefreshListView = (PullToRefreshListView) view
				.findViewById(R.id.lv_content);
		pullToRefreshListView.setMode(Mode.PULL_DOWN_TO_REFRESH);
		listView = pullToRefreshListView.getRefreshableView();

		headerView = LayoutInflater.from(getActivity()).inflate(
				R.layout.layout_home_ad_viewpager, null);

		viewPager = (CustomViewpager) headerView.findViewById(R.id.pager);
		viewPager.setmPager(viewPager);
		viewPager.setOnPageChangeListener(onPageChangeListener);

		pagerDot = (LinearLayout) headerView.findViewById(R.id.plan);

	}

	/**
	 * banner host data
	 * 
	 * @param hostLists
	 *            host list
	 * @return random host
	 */
	public static Anchor getLiveHost() {
		Anchor randomHost = null;
		List<Anchor> playHostList = new ArrayList<Anchor>();
		for (Anchor host : hostList) {
			if (host.getIsPlay().equals("1")) {
				playHostList.add(host);
			}
		}
		if(playHostList != null && playHostList.size()!=0){
			randomHost = playHostList.get(new Random().nextInt(playHostList.size()));
		}
		return randomHost;
	}

	private String doHttpRequest(String URL) throws Exception {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpGet httpGet = new HttpGet(URL);
		HttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}

	private void loadAdsData() {
		String adListStr = MySharedPrefs.read(getActivity(), "adList",
				"adListStr");
		adInfoList = parseAds(adListStr);
		if (adInfoList != null) {
			handler.sendEmptyMessage(MSG_VIEWPAGER);
		}

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					String results = doHttpRequest(URL_AD);
					MySharedPrefs.write(getActivity(), "adList", "adListStr",
							results);
					adInfoList = parseAds(results);
					if (adInfoList == null) {
						return;
					}
					handler.sendEmptyMessage(MSG_VIEWPAGER);
				} catch (Exception e) {
					Log.e(TAG, "loadAdsData error!");
					handler.sendEmptyMessage(NETWORK_ERROR);
				}
			}
		};
		ThreadPoolWrap.getThreadPool().executeTask(runnable);
	}

	private void loadVersion() {
		String adListStr = MySharedPrefs.read(getActivity(), "version",
				"versionStr");
		version = parseVersion(adListStr);

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					String results = doHttpRequest(URL_VERSION);
					MySharedPrefs.write(getActivity(), "version", "versionStr",
							results);
					version = parseVersion(results);
				} catch (Exception e) {
					Log.e(TAG, "loadVersion error!");
				}
			}
		};
		ThreadPoolWrap.getThreadPool().executeTask(runnable);
	}

	private List<AdInfo> parseAds(String jsonString) {
		List<AdInfo> list = new ArrayList<AdInfo>();
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			if (jsonObject.optBoolean("success")) {
				JSONArray jaArray = jsonObject.getJSONArray("retval");
				for (int i = 0; i < jaArray.length(); i++) {
					JSONObject jo = jaArray.getJSONObject(i);
					AdInfo info = new AdInfo();
					info.setImageUrl(jo.getString("imgurl"));
					info.setAdType(jo.getString("advurl"));
					list.add(info);
				}
			} else {
				list = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "parseAds error!");
			list = null;
		}
		return list;
	}

	private Version parseVersion(String jsonString) {
		if (jsonString == null || jsonString.equals("")) {
			return null;
		}
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			Version version = new Version();
			version.setAppName(jsonObject.optString("appName"));
			version.setVersionCode(Integer.parseInt(jsonObject
					.optString("versionCode")));
			version.setDownloadURL(jsonObject.optString("downloadURL"));
			return version;
		} catch (Exception e) {
			Log.e(TAG, "parseVersion error");
			return null;
		}
	}

	private void loadAnchorData() {
		// load data from local file
		String anchorListStr = MySharedPrefs.read(getActivity(), "anchorList",
				"anchorListStr");
		hostList = parseAnchor(anchorListStr);
		if (hostList != null) {
			handler.sendEmptyMessage(MSG_ANCHOR);
		}
		// load data from network
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					String results = doHttpRequest(getURL());
					MySharedPrefs.write(getActivity(), "anchorList",
							"anchorListStr", results);
					hostList = parseAnchor(results);
					if (hostList == null) {
						return;
					}
					handler.sendEmptyMessage(MSG_ANCHOR);
				} catch (Exception e) {
					Log.e(TAG, "loadAnchorData error!");
					handler.sendEmptyMessage(NETWORK_ERROR);
				}
			}
		};
		ThreadPoolWrap.getThreadPool().executeTask(runnable);
	}

	private void loadAnchorDataByPage() {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					String results = doHttpRequest(getURL());
					List<Anchor> tempList = parseAnchor(results);
					Message msg = new Message();
					msg.what = MSG_ANCHOR_PAGE;
					msg.obj = tempList;
					handler.sendMessage(msg);
				} catch (Exception e) {
					Log.e(TAG, "loadAnchorDataByPage error!");
					handler.sendEmptyMessage(NETWORK_ERROR);
				}
			}
		};
		ThreadPoolWrap.getThreadPool().executeTask(runnable);
	}

	private String getURL() {
		String url = String.format(URL_ANCHOR + "&p=%s", pageNum);
		Log.i(TAG, url);
		return url;
	}

	private List<Anchor> parseAnchor(String jsonString) {
		JSONObject jsonObject;
		List<Anchor> list = new ArrayList<Anchor>();
		try {
			jsonObject = new JSONObject(jsonString);
			if (jsonObject.optBoolean("success")) {
				JSONArray array = jsonObject.optJSONArray("retval");
				for (int i = 0; i < array.length(); i++) {
					JSONObject jsonObject2 = array.optJSONObject(i);
					Anchor host = new Anchor();
					host.setNickName(jsonObject2.optString("nickname"));
					host.setRoomId(jsonObject2.optString("rid"));
					host.setAudice(jsonObject2.optString("roomcount"));
					host.setRoomTag(jsonObject2.optString("room_ext1"));
					host.setIsPlay(jsonObject2.optString("openstatic"));
					host.setImpress(jsonObject2.optString("impress"));
					host.setHostImage(jsonObject2.optString("mobilepic"));
					host.setUid(jsonObject2.optString("uid"));
					host.setWeath(jsonObject2.optString("wealth"));
					host.setCredit(jsonObject2.optString("credit"));
					host.setUserType(jsonObject2.optString("usertype"));
					host.setUserNum(jsonObject2.optString("usernum"));
					host.setDetail(jsonObject2.toString());
					host.setAvatar(jsonObject2.optString("avatar"));
					list.add(host);
				}
			} else {
				list = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "parseAnchor error");
			list = null;
		}
		return list;
	}

	public static boolean isPluginApkInstalled(Context context) {
		int versionCode = 0;
		if (version != null) {
			versionCode = version.getVersionCode();
		}
		int state = FileUtil.doType(context.getPackageManager(),
				"com.ninexiu.live", versionCode);
		if (state == FileUtil.INSTALLED) {
			return true;
		}

		if (state == FileUtil.UNINSTALLED) {
			installApk(context, R.string.dialog_title_install);
			return false;
		} else if (state == FileUtil.INSTALLED_UPDATE) {
			installApk(context, R.string.dialog_title_update);
			return false;
		}
		return false;
	}

	public static void installApk(final Context context, int dialogTitle) {
		Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_launcher);
		Dialog dialog = builder
				.setTitle(dialogTitle)
				.setPositiveButton(R.string.dialog_positive,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(context,
										UpdateService.class);
								if (version != null) {
									intent.putExtra("KeyAppName",
											version.getAppName());
									intent.putExtra("KeyDownUrl",
											version.getDownloadURL());
								}
								context.startService(intent);
							}
						})
				.setNegativeButton(R.string.dialog_negative,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();
		dialog.show();
	}

	public static void startChatRoom(final Context context, Anchor host) {
		if (!isPluginApkInstalled(context)) {
			return;
		}
		if (null != LiveFragment.getLiveHost()) {
			ComponentName componentName = new ComponentName("com.ninexiu.live",
					"com.ninexiu.live.ChatRoomActivity");
			Intent intent = new Intent();
			intent.putExtra("roomId", host.getRoomId());
			intent.putExtra("isPlay", host.getIsPlay());
			intent.putExtra("roomTag", host.getRoomTag());
			intent.putExtra("uid", host.getUid());
			intent.putExtra("nickName", host.getNickName());
			intent.putExtra("audice", host.getAudice());
			intent.putExtra("credit", host.getCredit());
			intent.putExtra("userNum", host.getUserNum());
			intent.putExtra("impress", host.getImpress());
			intent.putExtra("avatar", host.getAvatar());
			intent.setComponent(componentName);
			context.startActivity(intent);
		}
	}

}
