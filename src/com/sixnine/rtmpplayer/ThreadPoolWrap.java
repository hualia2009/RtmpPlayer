package com.sixnine.rtmpplayer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolWrap {
	private static final int DEFAULT_COREPOOLSIZE = 2;
	private static final long DEFAULT_KEEP_ALIVE_TIME = 30L;
	private static final int DEFAULT_MAXIMUM_POOLSIZE = 20;
	private static ThreadPoolWrap instance;
	private BlockingQueue<Runnable> bq;
	private ThreadPoolExecutor executor;

	private ThreadPoolWrap() {
		executor = null;
		bq = new ArrayBlockingQueue<Runnable>(50);
		executor = new ThreadPoolExecutor(DEFAULT_COREPOOLSIZE,
				DEFAULT_MAXIMUM_POOLSIZE, DEFAULT_KEEP_ALIVE_TIME,
				TimeUnit.SECONDS, bq);
	}

	public static ThreadPoolWrap getThreadPool() {
		if (instance == null) {
			instance = new ThreadPoolWrap();
		}
		return instance;

	}

	public void executeTask(Runnable runnable) {
		executor.execute(runnable);
	}

	public void removeTask(Runnable runnable) {
		executor.remove(runnable);
	}
	
	public boolean isThreadPoolActive(){
		if(executor.getActiveCount()>=1){
			return true;
		}
		return false;
	}

	public void shutdown() {
		executor.shutdown();
		instance = null;
	}
}
