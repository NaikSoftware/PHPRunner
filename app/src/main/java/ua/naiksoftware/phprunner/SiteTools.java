package ua.naiksoftware.phprunner;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/**
 *
 * @author Naik
 */
public class SiteTools extends TabActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.site_tools_layout);
		Toast.makeText(this, "SiteTools onCreate", Toast.LENGTH_SHORT).show();
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        TabHost.TabSpec tabSpec;
        //set web view
        tabSpec = tabHost.newTabSpec("tag_web");
        tabSpec.setIndicator(getLayoutInflater().inflate(R.layout.site_tools_header, null));
		//tabSpec.setIndicator(getString(R.string.web_tab_header));
        tabSpec.setContent(new Intent(Intent.ACTION_VIEW, getIntent().getData(), this, WebViewActivity.class));
        tabHost.addTab(tabSpec);
        //set file browser
        tabSpec = tabHost.newTabSpec("tag_file");
        tabSpec.setIndicator(getLayoutInflater().inflate(R.layout.file_list_header, null));
		//tabSpec.setIndicator(getString(R.string.file_tab_header));
        tabSpec.setContent(new Intent(Intent.ACTION_EDIT, Uri.parse(getIntent().getStringExtra("docFolder")), this, TabGroupActivity.class));
        tabHost.addTab(tabSpec);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Toast.makeText(this, "SiteTools onNewIntent", Toast.LENGTH_SHORT).show();
	}
	
	
}
