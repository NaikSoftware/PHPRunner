package ua.naiksoftware.phprunner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import ua.naiksoftware.phprunner.log.L;

/**
 *
 * @author Naik
 */
public class ServerUtils {

    private static final String TAG = ServerUtils.class.getSimpleName();

    private final String PATH_TO_INSTALL_SERVER;
    private String DOC_FOLDER;
    private final String DOC_FOLDER_EXT_DEFAULT;
    private final String DOC_FOLDER_LOCAL_DEFAULT;
    public static final String PHP_BINARY = "php-fpm_7_0_0_arm";

    public ServerUtils(Context context) {
        SharedPreferences myPrefs = context.getSharedPreferences(Const.MY_PREFS, Activity.MODE_PRIVATE);
        PATH_TO_INSTALL_SERVER = context.getApplicationInfo().dataDir;
        DOC_FOLDER_EXT_DEFAULT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/htdocs";
        DOC_FOLDER_LOCAL_DEFAULT = PATH_TO_INSTALL_SERVER + "/htdocs";
        DOC_FOLDER = myPrefs.getString("docFolder", DOC_FOLDER_EXT_DEFAULT);
    }

    public String getPathToInstallServer() {
        return PATH_TO_INSTALL_SERVER;
    }

    public String getDocFolder() {
        return DOC_FOLDER;
    }

    public void updateDocFolder(String path) {
        DOC_FOLDER = path;
    }

    public String getDocFolderExtDefault() {
        return DOC_FOLDER_EXT_DEFAULT;
    }

    public String getDocFolderLocalDefault() {
        return DOC_FOLDER_LOCAL_DEFAULT;
    }

    public static String readFromProcess(java.lang.Process process, boolean err) {
        StringBuilder result = new StringBuilder();
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(err ? process.getErrorStream() : process.getInputStream()));
        try {
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            //Log.e("Main", "read From Process", e);
        }
        return result.toString();
    }

    public void stopSrv() {
        //Log.i("Main", "stopSrv");
        String[] output = null;
        try {
            java.lang.Process process = Runtime.getRuntime().exec("ps");//parse output
            process.waitFor();
            output = readFromProcess(process, false).split("\\n");
        } catch (IOException e) {
            //Log.e("Stop srv", "Create process or open streams", e);
        } catch (InterruptedException e) {
        }
        for (String line : output) {
            //if (line.contains(PATH_TO_INSTALL_SERVER)) {
            if (line.contains("php-fpm") || line.contains("lighttpd") || line.contains("mysqld")) {
                L.write(TAG, "stopSrv:parsed string= " + line);
                int pid = Integer.parseInt(line.split("\\s+")[1]);
                //Log.d("Stop srv", "Stop PID=" + pid);
                try {
                    java.lang.Runtime.getRuntime().exec("kill " + pid).waitFor();
                } catch (IOException ex) {
                    //Log.e("Stop srv", "Error in exec stopping commands", ex);
                } catch (InterruptedException ex) {
                    L.write(TAG, "kill process err:" + ex);
                }
            }
        }
    }

    public void runSrv() {
        L.write(TAG, "runSrv");
        File htdocs = new File(DOC_FOLDER);
        if (!htdocs.exists()) {
            htdocs.mkdir();
        }
        try {
            ProcessBuilder procBuilder;

            String[] param1 = new String[3];
            //param1[0] = PATH_TO_INSTALL_SERVER + "/lighttpd";
            //param1[1] = "-f" + PATH_TO_INSTALL_SERVER + "/lighttpd.conf";
            //param1[2] = "-D";
            param1[0] = PATH_TO_INSTALL_SERVER + "/lighttpd";
            param1[1] = "-f" + PATH_TO_INSTALL_SERVER + "/lighttpd.conf";
            param1[2] = "-D";

            String[] param2 = new String[3];
            //param2[0] = PATH_TO_INSTALL_SERVER + "/" + PHP_BINARY;
            //param2[1] = "-b127.0.0.1:9001";
            //param2[2] = "-c" + PATH_TO_INSTALL_SERVER + "/php.ini";
            param2[0] = PATH_TO_INSTALL_SERVER + "/" + PHP_BINARY;//"/data/data/ua.naiksoftware.phprunner/tmp/php.sock"
            param2[1] = "-c" + PATH_TO_INSTALL_SERVER + "/php.ini";
            param2[2] = "-y" + PATH_TO_INSTALL_SERVER + "/fpm.conf";

            //String[] param3 = new String[3];
            //param3[0] = PATH_TO_INSTALL_SERVER + "/mysqld";
            //param3[1] = "--defaults-file=" + PATH_TO_INSTALL_SERVER + "/my.ini";
            //param3[2] = "--user=root";
            String[] param3 = new String[4];
            param3[0] = PATH_TO_INSTALL_SERVER + "/mysqld";
            param3[1] = "--defaults-file=" + PATH_TO_INSTALL_SERVER + "/my.ini";
            param3[2] = "--user=root";
            param3[3] = "--language=" + PATH_TO_INSTALL_SERVER + "/share/mysql/english";

            //procBuilder = new ProcessBuilder(param1);
            //Process proc = procBuilder.start();
            //L.write(tag, "Run lighttpd: " + readFromProcess(proc, true));
            Runtime.getRuntime().exec(param1);
            //Log.d("Main", "run lighttpd");

            //Runtime.getRuntime().exec(param2, new String[]{"PHP_FCGI_MAX_REQUESTS=1000"});
            Process process = Runtime.getRuntime().exec(param2, new String[]{"PHP_FCGI_CHILDREN=4", "PHP_FCGI_MAX_REQUESTS=10000", ("TMPDIR=" + PATH_TO_INSTALL_SERVER + "/tmp")});
            L.write(TAG, "PHP startup errors: " + readFromProcess(process, true));

            procBuilder = new ProcessBuilder(param3);
            Process proc = procBuilder.start();
            L.write(TAG, "MySQL startup errors: " + readFromProcess(proc, true));
            //Process process1 = Runtime.getRuntime().exec(param3);
            //L.write(TAG, "MySQL startup errors: " + readFromProcess(process, true));
            //Log.d("Main", "run mysqld");  
        } catch (IOException e) {
            L.write(TAG, "Not executed or other:" + e);
        }
    }

    /*
     * @return flags lighttpd, php, mysqld
     */
    public boolean[] checkRun() {
        //Log.i("Main", "checkRun");
        //ставим флаги процессов
        boolean onLighttpd = false;
        boolean onPhp = false;
        boolean onMysqld = false;
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
        }// sleep while server stopped
        try {
            java.lang.Process process = Runtime.getRuntime().exec("ps");
            try {
                process.waitFor();
            } catch (InterruptedException e) {
            }
            String list = readFromProcess(process, false);
            //L.write(tag, "PS:______________________\n" + list);
            if (list.contains("lighttpd")) {
                onLighttpd = true;
            }
            if (list.contains("php-fpm")) {
                onPhp = true;
            }
            if (list.contains("mysqld")) {
                onMysqld = true;
            }
            //Log.i("Check run", Boolean.toString(onLighttpd) + Boolean.toString(onPhp) + Boolean.toString(onMysqld));
        } catch (IOException e) {
            //Log.e("Check run", "Create process or open streams", e);
        }
        return new boolean[]{onLighttpd, onPhp, onMysqld};
    }

    public boolean checkInstall() {
        //TODO: check all files
        //Log.i("Main", "checkInstall");
        if (new File(PATH_TO_INSTALL_SERVER + "/lighttpd").exists()
                && new File(PATH_TO_INSTALL_SERVER + "/" + PHP_BINARY).exists()
                && new File(PATH_TO_INSTALL_SERVER + "/mysqld").exists()
                && new File(PATH_TO_INSTALL_SERVER + "/tmp").exists()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkInstallPMA() {
        //TODO: check all files
        //Log.i("ServerUtils", "checkInstall");
        if (new File(DOC_FOLDER, "/phpmyadmin/index.php").exists()
                && new File(DOC_FOLDER, "/phpmyadmin/webapp.php").exists()
                && new File(DOC_FOLDER, "/phpmyadmin/themes/svg_gradient.php").exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void mkdirSu(String folder) {
        try {
            java.lang.Process proc = Runtime.getRuntime().exec("su");
            BufferedOutputStream bos = new BufferedOutputStream(proc.getOutputStream());
            bos.write(("mkdir " + folder + "\n").getBytes());
            bos.flush();
            bos.close();
            proc.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException ie) {
        }
    }

    private void mountRw(String folder) {
        try {
            java.lang.Process proc = Runtime.getRuntime().exec("su");
            BufferedOutputStream bos = new BufferedOutputStream(proc.getOutputStream());
            bos.write(("mount -o remount, rw " + folder + "\n").getBytes());
            bos.flush();
            bos.close();
            proc.waitFor();
        } catch (IOException ioe) {
        } catch (InterruptedException e) {
        }
    }
}
