package com.example.logcatmonitor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class LoggerService extends Service {
	private static final String TAG = "LoggerService";

	private String mLogPathSdcardDir;
	private OutputStreamWriter mOutputStreamWriter;
	private Process mProcess;
//	private WakeLock mWakeLock; 
	private Handler mCollectHandler;

//	private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat myLogSdf = new SimpleDateFormat(
			"yyyy-MM-dd_HH:mm:ss");

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		new LoggerCollectorThread().start();
	}

	private void init() {
		mLogPathSdcardDir = LoggerUtil.getLogRootDir();
		createLogDir();
//        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(this.POWER_SERVICE);  
//        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	}

	private void createLogDir() {
		boolean mkOk = true;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(mLogPathSdcardDir);
			if (!file.isDirectory()) {
				mkOk = file.mkdirs();
				if (!mkOk) {
					recordLogServiceLog("move file failed,dir is not created succ");
					return;
				}
			}
		}
	}

	class LoggerCollectorThread extends Thread {

		public LoggerCollectorThread() {
			super("LogCollectorThread");
		}

		@Override
		public void run() {
			try {
//				mWakeLock.acquire();
//				clearLogCache();
//				createLogCollector();
		        Looper.prepare();
		        mCollectHandler = new Handler();
		        mCollectHandler.post(runnable);
		        Looper.loop();
				
				Thread.sleep(1000);
//				mWakeLock.release();
			} catch (InterruptedException e) {
                e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				recordLogServiceLog(Log.getStackTraceString(e));
			}
		}
	}

	private Runnable runnable = new Runnable(){
       public void run(){
    	   createLogCollector();
    	   mCollectHandler.postDelayed(runnable, 300);
       }
	};
	
	public void createLogCollector() {
		String logFileName = mSdf.format(new Date()) + ".log";
		
		File file = new File(logFileName);
		if (file.exists()) {
			file.delete();
		}
		
		List<String> cmdList = new ArrayList<String>();
		cmdList.add("logcat");
		cmdList.add("-f");
		cmdList.add(getLogPath());
		cmdList.add("-v");
		cmdList.add("time");
		cmdList.add("*:I");
		try {
			mProcess = Runtime.getRuntime().exec(
					cmdList.toArray(new String[cmdList.size()]));
			recordLogServiceLog("start collecting the log,and log name is:"
					+ logFileName);
		} catch (Exception e) {
			recordLogServiceLog("CollectorThread == >" + e.getMessage());
		}
	}
	
    private void clearLogCache() {  
        Process proc = null;  
        List<String> cmdList = new ArrayList<String>();  
        cmdList.add("logcat");  
        cmdList.add("-c");  
        try {  
            proc = Runtime.getRuntime().exec(  
                    cmdList.toArray(new String[cmdList.size()]));
            if (proc.waitFor() != 0) {  
                Log.e(TAG, " clearLogCache proc.waitFor() != 0");   
            }  
        } catch (Exception e) {  
            Log.e(TAG, "clearLogCache failed", e);  
            recordLogServiceLog("clearLogCache failed");  
        } finally {  
            try {  
                proc.destroy();  
            } catch (Exception e) {  
                Log.e(TAG, "clearLogCache failed", e);  
                recordLogServiceLog("clearLogCache failed");  
            }  
        }  
    }  

	public String getLogPath() {
		createLogDir();
		String logFileName = mSdf.format(new Date()) + ".log";
		return mLogPathSdcardDir + File.separator + logFileName;
	}

	private void recordLogServiceLog(String msg) {
		if (mOutputStreamWriter != null) {
			try {
				Date time = new Date();
				mOutputStreamWriter.write(myLogSdf.format(time) + " : " + msg);
				mOutputStreamWriter.write("\n");
				mOutputStreamWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		recordLogServiceLog("LogService onDestroy");
		if (mOutputStreamWriter != null) {
			try {
				mOutputStreamWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (mProcess != null) {
			mProcess.destroy();
		}
		
		if (mCollectHandler != null) {
//			mCollectHandler.getLooper().quit();
			mCollectHandler.removeCallbacks(runnable);
		}
	}
}
