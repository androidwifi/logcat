package com.example.logcatmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class LoggerUtil {

	public static final String ANR_FILE_PATH1 = "/data/anr/traces.txt";
	public static final String ANR_FILE_PATH2 = "/data/anr/traces_com.changba.txt";
	
	public static File getSdcardDir() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		return sdDir;
	}

	public static String getLogRootDir() {
		File sdDir = getSdcardDir();
		if (sdDir != null && sdDir.isDirectory()) {
			File ktv = new File(sdDir, "ktv/log");
			if (!ktv.exists())
				ktv.mkdirs();
			return ktv.getAbsolutePath();
		}
		return null;
	}

	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}

	public static boolean copyFile(File from, File to) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(from);
			fos = new FileOutputStream(to);
			byte[] bytes = new byte[1 << 10];
			int c;
			while ((c = fis.read(bytes)) > 0) {
				fos.write(bytes, 0, c);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

}
