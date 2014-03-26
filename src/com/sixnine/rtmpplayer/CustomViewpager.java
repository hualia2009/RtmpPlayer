package com.sixnine.rtmpplayer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewpager extends ViewPager {

	private ViewPager mPager;
	private int abc = 1;
	private float mLastMotionX;

	public ViewPager getmPager() {
		return mPager;
	}

	public void setmPager(ViewPager mPager) {
		this.mPager = mPager;
	}

	public CustomViewpager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomViewpager(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		final float x = ev.getX();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPager.requestDisallowInterceptTouchEvent(true);
			abc = 1;
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (abc == 1) {
				if (x - mLastMotionX > 5 && getCurrentItem() == 0) {
					abc = 0;
					mPager.requestDisallowInterceptTouchEvent(false);
				}

				if (x - mLastMotionX < -5 && getCurrentItem() == getAdapter().getCount() - 1) {
					abc = 0;
					mPager.requestDisallowInterceptTouchEvent(false);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mPager.requestDisallowInterceptTouchEvent(false);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
}
