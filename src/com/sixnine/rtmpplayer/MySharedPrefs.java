package com.sixnine.rtmpplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MySharedPrefs {
	
	public static String read(Context context,String fileName,String key){
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE); 
		String value = sharedPreferences.getString(key, "");
		return value;
	}
	
	public static void write(Context context,String fileName,String key,String value){
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE); 
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

}
