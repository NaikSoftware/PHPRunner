/*
 * Run server, php, mysql and print msg in tray
 */
package ua.naiksoftware.phprunner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Naik
 */
public class AutostartService extends Service {

    NotificationManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServerUtils utils = new ServerUtils(this);
        if (!utils.checkInstall()) {
            return;
        }
        int id = 999;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification(R.drawable.logo, getString(R.string.launching), System.currentTimeMillis());
        notif.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.launching), null);
        notif.flags |= Notification.FLAG_NO_CLEAR;
        manager.notify(id, notif);

        // Crutches to wait for the detection of sdcard
        boolean loop;
        int count = 10;
        boolean[] flags;
        do {
            try {
                TimeUnit.SECONDS.sleep(3);// wait while sdcard was detected
            } catch (InterruptedException ex) {
                //ignore
            }
            count--;
            utils.runSrv();
            flags = utils.checkRun();
            loop = !(flags[0] && flags[1] && flags[2]);
        } while (loop && count > 0);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("start_from_service", true);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        flags = utils.checkRun();
        String descript = (flags[0] && flags[1] && flags[2]) ? getString(R.string.successfully_run) : getString(R.string.launch_err);
        notif.setLatestEventInfo(this, getString(R.string.app_name), descript, pIntent);
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(id, notif);
    }
}
