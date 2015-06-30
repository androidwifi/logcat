package com.example.logcatmonitor;

import java.io.File;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Button mStartMonitorBtn;
	private Button mStopMonitorBtn;
	private Button mAnrMonitorBtn;

	private boolean mIsStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mStartMonitorBtn = (Button) findViewById(R.id.start);
		mStopMonitorBtn = (Button) findViewById(R.id.stop);
		mAnrMonitorBtn = (Button) findViewById(R.id.anr_monitor);

		mStartMonitorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						LoggerService.class);
				startService(intent);
				mStartMonitorBtn.setEnabled(false);
				mStopMonitorBtn.setEnabled(true);
				mAnrMonitorBtn.setEnabled(true);
				mIsStarted = true;
			}
		});

		mStopMonitorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						LoggerService.class);
				stopService(intent);
				mStartMonitorBtn.setEnabled(true);
				mStopMonitorBtn.setEnabled(false);
				mIsStarted = false;
			}
		});

		mAnrMonitorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				File[] fileArray = new File[] {
						new File(LoggerUtil.ANR_FILE_PATH1),
						new File(LoggerUtil.ANR_FILE_PATH2) };
				for (File file : fileArray) {
					if (file.exists()) {
						File dir = new File(LoggerUtil.getLogRootDir());
						if (!dir.exists()) {
							if (!dir.mkdirs()) {
								Toast.makeText(MainActivity.this, "创建生成目录出错",
										Toast.LENGTH_SHORT).show();
								return;
							}
						}
						File newFile = new File(LoggerUtil.getLogRootDir(),
								file.getName());
						if (LoggerUtil.copyFile(file, newFile)) {
							Toast.makeText(MainActivity.this,
									"收集ANR文件" + file.getName() + "成功",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(MainActivity.this,
									"收集ANR文件" + file.getName() + "失败",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(MainActivity.this,
								"文件" + file.getName() + "不存在",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mIsStarted) {
			mStartMonitorBtn.setEnabled(true);
			mStopMonitorBtn.setEnabled(false);
			mAnrMonitorBtn.setEnabled(false);
		} else {
			mStartMonitorBtn.setEnabled(false);
			mStopMonitorBtn.setEnabled(true);
			mAnrMonitorBtn.setEnabled(true);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
