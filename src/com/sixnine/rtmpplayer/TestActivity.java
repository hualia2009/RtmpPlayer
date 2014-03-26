package com.sixnine.rtmpplayer;

import com.sixinine.rtmpplayer.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class TestActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        LiveFragment liveFragment = new LiveFragment();
        fragmentTransaction.add(R.id.live_fragment, liveFragment);
        //fragmentTransaction.add(exampleFragment, "");
        fragmentTransaction.commit();
		
	}


}
