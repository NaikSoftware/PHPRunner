package ua.naiksoftware.phprunner;

import android.content.*;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("auto_start", false)) {
            Intent i = new Intent(context, AutostartService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }
}
