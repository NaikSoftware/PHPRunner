package ua.naiksoftware.phprunner;

import android.content.Context;
import android.net.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
//import ua.naiksoftware.phprunner.log.*;

public class Loader {

    private HttpURLConnection urlConn;
    private Installer inst;
    private long readed, contentLength;
    private ConnectivityManager connManager;
    private String url;
	private File file;

    public Loader(Context context, Installer inst, String url, File file) {
        this.connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.url = url;
        this.file = file;
        this.inst = inst;
        this.readed = 0;
    }

    /*
     * Не гарантирует подключение
     */
    private boolean setConnection() {
        //L.write("Loader", "setConnection()");
        try {
            urlConn = (HttpURLConnection) new URL(url).openConnection();
			urlConn.setRequestProperty("Range", "Bytes=" + readed + "-");
			urlConn.setReadTimeout(10000);
			return true;
        } catch (IOException e) {
            //L.write("Loader", "setConnection(String url) err " + e.getLocalizedMessage());
			inst.setErr("Set connection error: " + e.getMessage());
			return false;
        }
    }

    /*
     * При любой ошибке или при отмене загрузки
     * возвращает false, если все скачалось - true.
     */
    public boolean load() {
        //L.write("Loader", "method load started");
		contentLength = getLength();
        if (contentLength == readed) {
            return true;
        }
        try {
			//L.write("Loader", "bis false");
            BufferedInputStream bis = new BufferedInputStream(checkInet());
			//L.write("Loader", "bis true");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, true);
                //skip bytes in file
            } catch (FileNotFoundException ex) {
                //L.write("Loader", "Save downloaded archive err: " + ex.getLocalizedMessage());
                return false;
            }

            byte[] bytes = new byte[16384];
            int c;
            while ((c = bis.read(bytes)) != -1) {
                readed += c;
                fos.write(bytes, 0, c);
                inst.update(c);
                if (inst.isCancelled()) {
					fos.flush();
					fos.close();
                    //L.write("Loader", "task was cancelled in load archive, readed = " + readed);
                    return false;
                }
            }
			fos.flush();
            fos.close();
        } catch (IOException ioe) {
            //L.write("Loader", "IOErr in load: " +ioe.getMessage());
            return false;
        }
        return true;
    }

    /*
     * не гарантирует наличие подключения
     */
    private InputStream checkInet() {
        //L.write("Loader", "checkInet() started");
        NetworkInfo nInfo = connManager.getActiveNetworkInfo();
        while (nInfo == null || !nInfo.isConnected()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e){}
            setConnection();
            nInfo = connManager.getActiveNetworkInfo();
            if (inst.isCancelled()) {
                //L.write("Loader", "task was cancelled in checkInet(), readed = " + readed);
                return null;
            }
        }
        try {
            return urlConn.getInputStream();
        } catch (IOException e) {
            //L.write("Loader", "checkInet() getInputStream err: " + e.getLocalizedMessage());
            return null;
        }
    }

    public int getLength() {
		if (!setConnection()) {
			return -1;
		}
        contentLength = urlConn.getContentLength();
        return (int) contentLength;
    }

    public void setReaded(long skipThis) {
        this.readed = skipThis;
    }
}
