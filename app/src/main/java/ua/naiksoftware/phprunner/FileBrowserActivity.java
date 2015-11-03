/*
 * This is a file browser window in SiteTools
 */
package ua.naiksoftware.phprunner;

import android.app.Activity;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;
import java.io.IOException;
import java.util.*;
//import ua.naiksoftware.phprunner.log.L;

/**
 *
 * @author Naik
 */
public class FileBrowserActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private TextView fullPath;
    ArrayList<Item> items;
    private static final String tag = "FileBrowserActivity";
    private String currPath, prevPath;
    private Map<String, Integer> mapExt = new HashMap<String, Integer>();
    private boolean chooseFile = false;
    private AlphabeticComparator alphabeticComparator;
    private Map<String, Integer> supportedFiles = new HashMap<String, Integer>() {
        {
            put(".php", EditorActivity.PHP);
            put(".js", EditorActivity.JS);
            put(".htm", EditorActivity.HTML);
            put(".html", EditorActivity.HTML);
            put(".css", EditorActivity.CSS);
            put(".config", EditorActivity.CONF);
            put(".conf", EditorActivity.CONF);
            put(".cfg", EditorActivity.CONF);
            put(".ini", EditorActivity.CONF);
            put(".txt", EditorActivity.NONE);
			//put(".json");
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.file_browser_layout);
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            //стартовал для выбора файла
            chooseFile = true;
            //Log.d(tag, "action equals ACTION_GET_CONTENT");
        }
        currPath = intent.getDataString();
        //L.write(tag, "onCreate started with " + currPath);
        if (currPath == null) {
            currPath = "";
            //L.write(tag, "in onCreate currPath was obtained as null, set /");
        }
        prevPath = calcBackPath();
        fullPath = (TextView) findViewById(R.id.full_path);
        listView = (ListView) findViewById(R.id.file_list);
        listView.setOnItemClickListener(this);
        initMapExt();
        alphabeticComparator = new AlphabeticComparator();
		Toast.makeText(this, "FileBrowser onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //L.write(tag, "onResume()");
        readFolder(currPath);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //L.write(tag, "onRestart()");
    }

    private void readFolder(String folderStr) {
        //L.write(tag, "read : " + folderStr);
        String[] lsOutputDet;//Детальная информация
        String[] names;
        String error;
        try {
            java.lang.Process proc = new ProcessBuilder().command("ls", "-l", "-a", folderStr + "/").start();
            lsOutputDet = ServerUtils.readFromProcess(proc, false).split("\n");
            error = ServerUtils.readFromProcess(proc, true);
            names = ServerUtils.readFromProcess(new ProcessBuilder().command("ls", "-a", folderStr + "/").start(), false).split("\n");
            if (!error.equals("")) {
                /*
                 * Комманда ls ничего не вернула.
                 * Папка не существует
                 */
                currPath = prevPath;
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            //L.write(tag, "read ls" + e.getLocalizedMessage());
            return;
        }
        items = new ArrayList<Item>();
        ArrayList<Item> listFolder = new ArrayList<Item>();
        ArrayList<Item> listFile = new ArrayList<Item>();
        StringBuilder subheader = new StringBuilder();
        if (!currPath.equals("")) {
            items.add(new Item(R.drawable.folder_in, "..", "Parent folder", Const.TYPE_BACK));
        }
        if (names[0].equals("")) {//если папка пустая
            listView.setAdapter(new MyAdapter(this, items));
            fullPath.setText(currPath);
            return;
        }
        int j = 0;//счетчик для names
        for (String str : lsOutputDet) {
            String arr[] = str.split("\\s+");
            char id = arr[0].charAt(0);
            if (id != '-' && id != 'd' && id != 'l') {
                /*Если не файл, не папка, не ссылка,
                 *а какая-то фигня, то от греха подальше, пропускаем
                 */
                //L.write(tag, id + " not known");
                continue;
            }
            subheader.delete(0, subheader.length()).append(' ');//cls subheader
            subheader.append(arr[0].substring(1)).append(' ');//add permissions to subheader
            if (id == 'd' || id == 'l') {//если папка или ссылка
                subheader.append(arr[3]).append(' ').append(arr[4]);//date folder
                listFolder.add(new Item(R.drawable.folder, names[j], subheader.toString(), Const.TYPE_FOLDER));
            } else {//если файл
                subheader.append(arr[4]).append(' ').append(arr[5]);//date file
                subheader.append(' ').append(calcSize(Long.parseLong(arr[3])));
                String ext = getExtension(names[j]);// get extension from name
                int iconId = R.drawable.file;
                if (mapExt.containsKey(ext)) {
                    iconId = mapExt.get(ext);
                }
                listFile.add(new Item(iconId, names[j], subheader.toString(), Const.TYPE_FILE));
            }
            j++;
        }
        Collections.sort(listFolder, alphabeticComparator);
        Collections.sort(listFile, alphabeticComparator);
        items.addAll(listFolder.subList(0, listFolder.size()));
        items.addAll(listFile.subList(0, listFile.size()));
        //Collections.sort(items, alphabeticComparator);
        listView.setAdapter(new MyAdapter(this, items));
        fullPath.setText(currPath);
    }

    /*
     * calc file size in b, Kb or Mb
     */
    private String calcSize(long length) {
        if (length < 1024) {
            return String.valueOf(length).concat(" b");
        } else if (length < 1048576) {
            return String.valueOf(round((float) length / 1024f)).concat(" Kb");
        } else {
            return String.valueOf(round((float) length / 1048576f)).concat(" Mb");
        }
    }

    /* 
     * rounded to two decimal places
     */
    public static float round(float sourceNum) {
        int temp = (int) (sourceNum / 0.01f);
        return temp / 100f;
    }

    private void initMapExt() {
        mapExt.put(".php", R.drawable.icon_php);
        mapExt.put(".html", R.drawable.icon_html);
        mapExt.put(".txt", R.drawable.icon_txt);
        mapExt.put(".cfg", R.drawable.icon_config);
        mapExt.put(".conf", R.drawable.icon_config);
        mapExt.put(".config", R.drawable.icon_config);
        mapExt.put(".ini", R.drawable.icon_config);
        mapExt.put(".sh", R.drawable.icon_config);
        mapExt.put(".css", R.drawable.icon_css);
        mapExt.put(".mp3", R.drawable.icon_music);
        mapExt.put(".amr", R.drawable.icon_music);
        mapExt.put(".wav", R.drawable.icon_music);
        mapExt.put(".mid", R.drawable.icon_music);
        mapExt.put(".midi", R.drawable.icon_music);
        mapExt.put(".ogg", R.drawable.icon_music);
        mapExt.put(".mp4", R.drawable.icon_video);
        mapExt.put(".3gp", R.drawable.icon_video);
        mapExt.put(".apk", R.drawable.icon_apk);
        mapExt.put(".sql", R.drawable.icon_db);
        mapExt.put(".doc", R.drawable.icon_doc);
        mapExt.put(".docx", R.drawable.icon_doc);
        mapExt.put(".ico", R.drawable.icon_image);
        mapExt.put(".jpg", R.drawable.icon_image);
        mapExt.put(".bmp", R.drawable.icon_image);
        mapExt.put(".gif", R.drawable.icon_image);
        mapExt.put(".png", R.drawable.icon_image);
        mapExt.put(".pdf", R.drawable.icon_pdf);
        mapExt.put(".ppt", R.drawable.icon_ppt);
        mapExt.put(".zip", R.drawable.icon_zip);
        mapExt.put(".jar", R.drawable.icon_zip);
    }

    @Override
    public void onItemClick(AdapterView<?> p1, View p2, int sel, long p4) {
        prevPath = currPath;
        Item it = items.get(sel);
        switch (it.getType()) {
            case Const.TYPE_FOLDER:
                currPath = currPath + "/" + it.getHeader();// build URL
                readFolder(currPath);
                break;
            case Const.TYPE_BACK:
                currPath = calcBackPath();
                readFolder(currPath);
                break;
            case Const.TYPE_FILE:
                selectAction(currPath + '/' + it.getHeader());// build URL
                break;
        }
    }

    private String calcBackPath() {
        try {
            return currPath.substring(0, currPath.lastIndexOf('/'));
        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }

    private void selectAction(String path) {
        if (chooseFile) {
            Intent intent = getIntent();
            intent.setData(Uri.parse("file://" + path));
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        String mimeType;
        String ext = getExtension(path);
        if (supportedFiles.containsKey(ext)) {
			Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(path), getParent(), EditorActivity.class);
			intent.putExtra(EditorActivity.CODE_TYPE, supportedFiles.get(ext));
			TabGroupActivity tabGroupActivity = (TabGroupActivity) getParent();
			tabGroupActivity.push("SecondActivity", intent);
			
            
			
            //startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
            return;
        }
        if (ext != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(ext.substring(1));
            if (mimeType != null) {
                //Log.d(tag, mimeType);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path), mimeType);
                intent.putExtra("data", path);
                intent.putExtra(Intent.EXTRA_TITLE, "Что использовать?");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
        }
    }

    private static String getExtension(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".")).toLowerCase();
        }
        return null;
    }
}
