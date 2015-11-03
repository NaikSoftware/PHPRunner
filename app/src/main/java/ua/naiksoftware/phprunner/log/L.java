package ua.naiksoftware.phprunner.log;

import java.io.*;
import android.os.*;
import android.util.*;
import java.util.*;

public class L {

	private static final String token = " : ";
	private static final long MAX_LEN = 51200;//50 Kb

	public static void write(String tag, String message) {
		try {
			boolean noClear;
			File file = new File(Environment.getExternalStorageDirectory(), "log_phprunner.txt");
			if (file.length() > MAX_LEN) {
				noClear = false;
			} else {
				noClear = true;
			}
			FileWriter fw = new FileWriter(file, noClear);
			String msg = "\n" + new Date().toLocaleString() + token + tag + token + message;
			fw.write(msg);
			fw.flush();
			fw.close();
			//Log.d("L", msg);
		} catch (IOException e) {
			Log.e("L", "err in logging", e);
		}
	}

}
