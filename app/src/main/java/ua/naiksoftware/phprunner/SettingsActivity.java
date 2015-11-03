package ua.naiksoftware.phprunner;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
