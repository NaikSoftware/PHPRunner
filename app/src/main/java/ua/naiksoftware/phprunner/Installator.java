package ua.naiksoftware.phprunner;

import android.app.ProgressDialog;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import ua.naiksoftware.phprunner.log.L;

/**
 * 
 * @author Naik
 */
public class Installator extends AsyncTask<String, String, Boolean> implements DialogInterface.OnClickListener {

    private static final String tag = Installator.class.getName();

    private Context context;
    //private static final int MAX_ERR = 100;
    private String DOC_FOLDER;
    private String err = "";
    private long contentLength;
    private ProgressDialog dialog;
    private Handler h;
    //private Loader loader;
    private boolean setRights;
    private long currProgress;

    public Installator(Context context, Handler h, boolean setRights) {
        this.context = context;
        this.h = h;
        this.setRights = setRights;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public void setErr(int resid) {
        this.err = context.getResources().getString(resid);
    }

    public void setErr(int resid, String strAdd) {
        this.err = context.getResources().getString(resid) + " " + strAdd;
    }

    public String getErr() {
        return err;
    }

    public void update(int add) {
        //Log.i("Installator", "update: " + add + "bytes (" + (add / 1024) + "Kbytes)");
        publishProgress(String.valueOf(add));
    }

    protected Boolean doInBackground(String[] p1) {
        final String nameInAssets = p1[0];
        final String urlToInstall = p1[1];
        DOC_FOLDER = p1[2];
        //final String saveAs = p1[3];
        //L.write("Installator", "doInBackground started");

        /*ЭТАП I - ПОДГОТОВКА ФАЙЛОВОЙ СИСТЕМЫ*/
        File folderInstall = new File(urlToInstall);// сюда распаковываем
        File fileDocFolder = new File(DOC_FOLDER);//корневая папка сервера (сюда загружаем архив)
        //File fileArchive = new File(fileDocFolder, saveAs);//загружаемый файл
        if (!folderInstall.exists()) {
            folderInstall.mkdirs();
        }
        if (!fileDocFolder.exists()) {
            fileDocFolder.mkdirs();
        }
        /*if (!fileArchive.exists()) {
         try {
         fileArchive.createNewFile();
         } catch (IOException e) {
         //L.write("Installator", "Error in create new file (archive)" + e.getLocalizedMessage());
         setErr(R.string.err_create_new_file);
         return false;
         }
         }*/

        /*ЭТАП II - ЗАГРУЗКА АРХИВА*/
        // Создаем загрузчик
        //loader = new Loader(context, this, nameInAssets, fileArchive);
        //contentLength = loader.getLength();
        //L.write("Installator", "contentLength() = " + contentLength);
        //if (contentLength == -1) {
        //L.write("Installator", "contentLength() return -1");
        //setErr(R.string.not_have_internet, getErr());
        //return false;
        //}
        // Пропускем уже загруженное, если есть
        //long skipThis = fileArchive.length();
        //L.write("Installator", "long skipThis = " + skipThis);
        //loader.setReaded(skipThis);
        // проверяем достаточно ли памяти на флешке для архива
        //long free = fileDocFolder.getFreeSpace();
        //if (free < (contentLength - skipThis)) {
        //L.write("Installator", "free = " + free + " b, " + free / 1024576 + " Mb");
        //setErr(R.string.out_of_memory_htdocs, (contentLength - skipThis - free) / 1024576 + " Mb");
        //return false;
        //}
        // Собственно сама загрузка
        //dialog.setMax((int) contentLength);
        //dialog.setProgress((int) skipThis);
        //int countErr = 0;//колличество попыток перезапуска загрузки
        //while (!loader.load()) {
        //if (isCancelled()) {
        //return false;
        //} else if (countErr > MAX_ERR) {
        //setErr(R.string.copy_error);
        //return false;
        //} else {
        //countErr++;
        //}
        //}
        //L.write("Installator", "loaded with " + countErr + " error(s)");

        /* ЭТАП III - РАСПАКОВКА ЗАГРУЖЕННОГО АРХИВА*/
        // Свободная локальная память контроллируется при распаковке в классе Unzip
        try {
            dialog.setIndeterminate(true);
            long maxBytes = Unzip.calcUnzipped(context.getAssets().open(nameInAssets));
            int maxKb = (int) (maxBytes / 1024L);
            dialog.setMax(maxKb);
            dialog.setIndeterminate(false);
            dialog.setProgress(0);
            if (!Unzip.unzip(context.getAssets().open(nameInAssets), folderInstall, this, setRights)) {
                return false;
            } else {
                // OK, replace paths in configs to actual in this ROM.
                //fileArchive.delete();
                ServerUtils utils = new ServerUtils(context);
                final String conf = FileUtils.readFile(utils.getPathToInstallServer() + "/lighttpd.conf");
                final String newconf = conf.replaceFirst("server\\.document-root.*\\n", "server.document-root = \"" + utils.getDocFolder() + "\"\n");
                new File(utils.getDocFolderExtDefault()).mkdirs();
                try {
                    FileUtils.saveCode(newconf, "utf-8", utils.getPathToInstallServer() + "/lighttpd.conf");
                } catch (IOException e) {
                }
                return true;
            }
        } catch (IOException e) {
            //L.write("Installator", "error in calling unzip" + e.getLocalizedMessage());
            setErr(e.toString());
            return false;
        }
    }

    @Override
    public void onProgressUpdate(String... s) {
        currProgress += Integer.parseInt(s[0]);
        dialog.setProgress((int) (currProgress / 1024L));
    }

    @Override
    public void onPreExecute() {
        //Log.i("Installator", "onPreExecute");
        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.downloading_wait);
        dialog.setButton(context.getResources().getString(R.string.stop_btn_label), this);
        dialog.setMessage(context.getResources().getString(R.string.progress_dialog_message));
        dialog.show();
    }

    @Override
    public void onPostExecute(Boolean result) {
        //Log.i("Installator", "onPostExecute with: " + result);
        if (result) {
            Toast t = Toast.makeText(context, context.getResources().getString(R.string.install_complete), Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            if (!new File(DOC_FOLDER + "/index.php").exists()) {
                try {
                    FileUtils.saveCode("<?php phpinfo(); ?>", "utf-8", DOC_FOLDER + "/index.php");
                } catch (IOException e) {
                }
            }
            h.sendEmptyMessage(MainActivity.INSTALL_OK);
        } else {
            Toast t = Toast.makeText(context, getErr().replace("annimon", "pentagon"), Toast.LENGTH_LONG);
            t.show();
            dialog.dismiss();
            setErr("");
            h.sendEmptyMessage(MainActivity.INSTALL_ERR);
        }
        //L.write("Installator", "onPostExecuted");
    }

    public void onClick(DialogInterface p1, int p2) {
        //L.write("Installator", "calcel task in onClick()");
        setErr(R.string.install_calcel);
        this.cancel(false);
        onPostExecute(false);

    }
}
