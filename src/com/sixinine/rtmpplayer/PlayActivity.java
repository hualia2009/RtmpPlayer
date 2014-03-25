package com.sixinine.rtmpplayer;

import android.app.Activity;
import android.os.Bundle;

public class PlayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.surfaceview);
		
		PlayerViewController playerViewController = new PlayerViewController(this,null);
		playerViewController.initPlayerView();
		
//		if("1".equals(mHost.getIsPlay())){
//			playerViewController.startPlay(mHost.getRoomTag(), mHost.getRoomId());
//		}
	}


}
