/*
 * Run server, php, mysql and print msg in tray
 */
package ua.naiksoftware.phprunner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

/**
 * @author Naik
 */
public class AutostartService extends Service {

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
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.launching))
                .setAutoCancel(false);
        manager.notify(id, builder.build());

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
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        flags = utils.checkRun();
        String description = (flags[0] && flags[1] && flags[2]) ? getString(R.string.successfully_run) : getString(R.string.launch_err);
        builder.setContentText(description)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        manager.notify(id, builder.build());
    }
}
