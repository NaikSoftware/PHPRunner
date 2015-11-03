package ua.naiksoftware.phprunner;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import ua.naiksoftware.phprunner.log.L;

/**
 * @author Naik
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private ServerUtils utils;
    private String pathToInstallServer;
    private String docFolder;
    public static final int INSTALL_OK = 1;
    public static final int INSTALL_ERR = -1;
    private static final int INSTALL = 1;
    private static final int RUN = 2;
    private static final int STOP = 3;
    private Context context;
    private Installator installator;
    private TextView info, infoDir;
    private Button btnStart;
    private Button btnDocFolder;
    private Button btnSiteEditor;
    private Button btnPMA;
    private Button btnLogPhp, btnLogMysql, btnLogServer;
    private AlertDialog dialogInputDocFolder, dialogSelectDocFolder;
    private EditText editTextDocFolder;
    private View viewDocFolder;
    private Boolean onLighttpd, onPhp, onMysqld;
    private int flag = 0;// for top button
    private String selected;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        L.write("Main", "......onCreate......\n <<<<<<<DEBUG BUILD>>>>>>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;
        if (getIntent().getBooleanExtra("start_from_service", false)) {
            stopService(new Intent(context, AutostartService.class));
        }

        utils = new ServerUtils(context);
        pathToInstallServer = utils.getPathToInstallServer();
        docFolder = utils.getDocFolder();

        //L.write("PATH_TO_INSTALL_SERVER=", pathToInstallServer);
        //L.write("DOC_FOLDER_EXT_DEFAULT=", utils.getDocFolderExtDefault());
        //L.write("DOC_FOLDER_LOCAL_DEFAULT=", utils.getDocFolderLocalDefault());
        //L.write("DOC_FOLDER=", docFolder);
        //mountRw("/");//create tmp/ directory, if exists superuser
        //mkdirSu("/tmp");// for mysql view table

        /* Init views */
        viewDocFolder = LayoutInflater.from(context).inflate(R.layout.input_field, null);
        editTextDocFolder = (EditText) viewDocFolder.findViewById(R.id.editText_docFolder);
        info = (TextView) findViewById(R.id.info);
        btnStart = (Button) findViewById(R.id.btn_start);
        infoDir = (TextView) findViewById(R.id.info_root_dir);
        btnDocFolder = (Button) findViewById(R.id.btn_root_dir);
        btnSiteEditor = (Button) findViewById(R.id.btn_site_editor);
        btnPMA = (Button) findViewById(R.id.btn_run_phpmyadmin);
        btnLogPhp = (Button) findViewById(R.id.btnLog_php);
        btnLogMysql = (Button) findViewById(R.id.btnLog_mysql);
        btnLogServer = (Button) findViewById(R.id.btnLog_server);

        /* Set views */
        infoDir.setText(docFolder);
        btnStart.setOnClickListener(this);
        btnDocFolder.setOnClickListener(this);
        btnSiteEditor.setOnClickListener(this);
        btnLogPhp.setOnClickListener(this);
        btnLogMysql.setOnClickListener(this);
        btnLogServer.setOnClickListener(this);
        btnPMA.setOnClickListener(this);
        if (utils.checkInstall()) {
            createUIRunOrStop();
        } else {
            createUIInstall();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * *
     * создаем кнопку Install
     */
    private void createUIInstall() {
        //Log.i("Main", "createUIInstall");
        btnStart.setEnabled(true);
        btnDocFolder.setEnabled(false);
        btnSiteEditor.setEnabled(false);
        btnPMA.setEnabled(false);
        btnLogPhp.setEnabled(false);
        btnLogMysql.setEnabled(false);
        btnLogServer.setEnabled(false);

        btnStart.setText(R.string.install_btn_label);
        btnPMA.setText(R.string.download_phpmyadmin);
        flag = INSTALL;
    }
    Handler handlerInstallServer = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            //Log.i("Main", "handleMessage with " + message.what);
            switch (message.what) {
                case INSTALL_OK:
                    btnStart.setEnabled(true);
                    btnDocFolder.setEnabled(true);
                    btnLogPhp.setEnabled(true);
                    btnLogMysql.setEnabled(true);
                    btnLogServer.setEnabled(true);
                    createUIRunOrStop();
                    break;
                case INSTALL_ERR:
                    createUIInstall();
                    break;
            }
        }
    };
    Handler handlerInstallPMA = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            //Log.i("Main", "handleMessage with " + message.what);
            createUIRunOrStop();
        }
    };

    /**
     * *
     * Запускаем инсталлятор
     */
    private void install() {
        //L.write("Main", "install");
        btnStart.setEnabled(false);
        installator = new Installator(this, handlerInstallServer, true);
        installator.execute(Const.ARCHIVE_NAME, pathToInstallServer, docFolder);
        //L.write("Main", "Returned from Installator into install() method");
    }

    /**
     * *
     * Проверяем работающие процессы. Если все флаги процессов true - выводим
     * кнопку stop Иначе кнопку - start
     */
    public void createUIRunOrStop() {
        //Log.i("Main", "createUIRunOrStop");
        boolean[] flags = utils.checkRun();
        onLighttpd = flags[0];
        onPhp = flags[1];
        onMysqld = flags[2];
        if (onMysqld && onPhp && onLighttpd) {
            btnSiteEditor.setEnabled(true);
            btnStart.setText(R.string.stop_btn_label);
            info.setText(getString(R.string.successfully_run));
            flag = STOP;
        } else {
            btnSiteEditor.setEnabled(false);
            btnStart.setText(R.string.run_btn_label);
            if (!onLighttpd && !onPhp && !onMysqld) {
                info.setText(getString(R.string.not_exists_processes));
            } else {
                info.setText(getString(R.string.exists_processes) + " " + (onLighttpd ? "" : "lighttpd ") + (onPhp ? " " : "PHP ") + (onMysqld ? " " : "MySQL"));
            }
            flag = RUN;
        }
        if (utils.checkInstallPMA()) {// если загружено
            btnPMA.setText(R.string.start_phpmyadmin);
            btnPMA.setEnabled(true);
            if (!onMysqld || !onPhp || !onLighttpd) {// ничего не запущено
                btnPMA.setEnabled(false);
            }
        } else {// если не загружено
            btnPMA.setEnabled(true);
            btnPMA.setText(R.string.download_phpmyadmin);
        }
        if (installator != null && installator.getStatus() == AsyncTask.Status.RUNNING) {
            btnPMA.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        //Log.i("Main", "onClick");
        int id = view.getId();
        if (id == R.id.btn_start) {
            btnStart.setEnabled(false);
            switch (flag) {
                case INSTALL:
                    install();
                    break;
                case RUN:
                    utils.runSrv();
                    btnStart.setEnabled(true);
                    createUIRunOrStop();
                    break;
                case STOP:
                    utils.stopSrv();
                    btnStart.setEnabled(true);
                    createUIRunOrStop();
                    break;
            }
        } else if (id == R.id.btn_root_dir) {
            final String[] items = new String[]{utils.getDocFolderExtDefault(), utils.getDocFolderLocalDefault(), getString(R.string.input_other_path)};
            if (dialogSelectDocFolder == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.select_doc_folder_title)).setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int i) {
                        if (i == 0) {//ext
                            selected = utils.getDocFolderExtDefault();
                            applyDocDirChanges();
                        } else if (i == 1) {//local
                            selected = utils.getDocFolderLocalDefault();
                            applyDocDirChanges();
                        } else {//create dialog for input and save some path with check
                            dialogSelectDocFolder.dismiss();
                            if (dialogInputDocFolder == null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(R.string.input_other_path);
                                builder.setIcon(android.R.drawable.ic_menu_edit);
                                builder.setView(viewDocFolder);
                                builder.setCancelable(false);
                                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface p1, int p2) {
                                        selected = editTextDocFolder.getText().toString();
                                        applyDocDirChanges();
                                    }
                                });
                                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface p1, int p2) {
                                        dialogInputDocFolder.dismiss();
                                    }
                                });
                                dialogInputDocFolder = builder.create();
                            }
                            dialogInputDocFolder.show();
                        }
                    }
                }).setCancelable(true);

                dialogSelectDocFolder = builder.create();
            }
            dialogSelectDocFolder.show();

        } else if (id == R.id.btn_site_editor) {
            Intent intent = new Intent(context, SiteTools.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("docFolder", docFolder);
            startActivity(intent);
        } else if (id == R.id.btn_run_phpmyadmin) {
            if (btnPMA.getText().equals(getString(R.string.download_phpmyadmin))) {// start download PMA
                //L.write("Main onClick", "click to install pma button");
                btnPMA.setEnabled(false);
                installator = new Installator(this, handlerInstallPMA, false);
                installator.execute(Const.PMA_NAME, docFolder + "/phpmyadmin", docFolder);
                //L.write("Main onClick", "Returned from Installator into install() method (PMA)");
            } else {// run PMA
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebViewActivity.DEFAULT_URL + "/phpmyadmin/"), context, SiteTools.class);
                intent.putExtra("docFolder", docFolder);
                startActivityForResult(intent, 1);
            }
        } else if (id == R.id.btnLog_php) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(docFolder + "/fcgiserver.log"), this, EditorActivity.class);
            startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
        } else if (id == R.id.btnLog_mysql) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(pathToInstallServer + "/mysql.log"), this, EditorActivity.class);
            startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
        } else if (id == R.id.btnLog_server) {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(pathToInstallServer + "/lighttpd.log"), this, EditorActivity.class);
            startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
        }
    }

    private void applyDocDirChanges() {
        // Split to two part - first line (change doc root at her) and other config.
        final String[] conf = FileUtils.readFile(pathToInstallServer + "/lighttpd.conf").split("\n", 2);
        final String newconf = conf[0].replace(docFolder, selected) + "\n" + conf[1];
        new File(selected).mkdirs();
        try {
            FileUtils.saveCode(newconf, "utf-8", pathToInstallServer + "/lighttpd.conf");
        } catch (IOException e) {
        }
        SharedPreferences myPrefs = getSharedPreferences(Const.MY_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString("docFolder", selected);
        editor.commit();
        docFolder = selected;
        utils.updateDocFolder(docFolder);
        infoDir.setText(selected);
        Toast t = Toast.makeText(context, context.getResources().getString(R.string.select_doc_folder_message), Toast.LENGTH_LONG);
        t.show();
        utils.stopSrv();
        createUIRunOrStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(R.string.settings);
        mi.setIntent(new Intent(context, SettingsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (installator != null && installator.getStatus() == AsyncTask.Status.RUNNING) {
            installator.cancel(false);
        }
    }

    // Defined in xml layout
    public void onForceStop(View v) {
        utils.stopSrv();
        createUIRunOrStop();
    }
}
