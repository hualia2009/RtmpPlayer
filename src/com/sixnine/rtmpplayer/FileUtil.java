package com.sixnine.rtmpplayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

public class FileUtil {

	public static File updateDir = null;
	public static File updateFile = null;
	public static final String SixNineLive = "SixNineLive";
	public static boolean isCreateFileSucess;

	public static int INSTALLED = 0;
	public static int UNINSTALLED = 1;
	public static int INSTALLED_UPDATE = 2;

	public static void createFile(String appName) {

		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			isCreateFileSucess = true;
			updateDir = new File(Environment.getExternalStorageDirectory()
					+ "/" + SixNineLive + "/");
			updateFile = new File(updateDir + "/" + appName + ".apk");

			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					isCreateFileSucess = false;
					e.printStackTrace();
				}
			}
		} else {
			isCreateFileSucess = false;
		}
	}

	public static int doType(PackageManager pm, String packageName, int versionCode) {
		List<PackageInfo> pakageinfos = pm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pi : pakageinfos) {
			String piPackageName = pi.packageName;
			int piVersionCode = pi.versionCode;
			if (packageName.endsWith(piPackageName)) {
				if (versionCode == piVersionCode) {
					return INSTALLED;
				} else if (versionCode > piVersionCode) {
					return INSTALLED_UPDATE;
				}
			}
		}
		return UNINSTALLED;
	}
	
}