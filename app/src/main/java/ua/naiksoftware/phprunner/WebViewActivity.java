package ua.naiksoftware.phprunner;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;

public class WebViewActivity extends Activity {

    public static final String DEFAULT_URL = "http://localhost:8080";
    public static final int FILE_CHOOSER_RESULT = 0x01;
    private static final String tag = "WebViewActivity";
    private static final int DELAY = 1, ADD_DELAY = 2, CLEAR_DELAY = 3;
    private static final long DISPLAY_TIME = 4000L;
    private static final String GET_HTML = "GET_HTML";
    private WebView webView;
    private ProgressBar urlLoading;
    private ImageView favicon;
    private TextView htmlTitle;
    private Button btnPrev, btnRefresh, btnNext;
    private LinearLayout webToolsPanel;
    private RelativeLayout webTitlePanel;
    private String url;
    private EditText gotoUrl;
    private boolean isLoading = false;
    private InputMethodManager imm;
    private ValueCallback<Uri> mFileChooserCallback;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.web_view_layout);
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());// for access to favicon in WebView
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            //стартовал из SiteTools
        }
        url = intent.getDataString();
        if (url == null) {
            url = DEFAULT_URL;
        }

        gotoUrl = (EditText) findViewById(R.id.goto_url);
        urlLoading = (ProgressBar) findViewById(R.id.progressBar_url_loading);
        favicon = (ImageView) findViewById(R.id.favicon);
        htmlTitle = (TextView) findViewById(R.id.html_title);
        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnNext = (Button) findViewById(R.id.btnNext);
        webToolsPanel = (LinearLayout) findViewById(R.id.webToolsPanel);
        webTitlePanel = (RelativeLayout) findViewById(R.id.webTitlePanel);

        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true); // включаем поддержку JavaScript
        webView.addJavascriptInterface(new MyJavascriptInterface(), GET_HTML);
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//		webView.getSettings().setPluginsEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//Log.d(tag, "onClick");
					if (h.hasMessages(DELAY)) {
						h.sendEmptyMessage(ADD_DELAY);
						return false;
					}
					webTitlePanel.setVisibility(View.VISIBLE);
					h.sendEmptyMessageDelayed(DELAY, DISPLAY_TIME);
					return false;
				}
			});
        webView.loadUrl(url);

        btnRefresh.setOnClickListener(NavListener);
        btnPrev.setOnClickListener(NavListener);
        btnNext.setOnClickListener(NavListener);
        gotoUrl.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
						webView.loadUrl(gotoUrl.getText().toString());
						return true;
					}
					return false;
				}
			});
    }
    private View.OnClickListener NavListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btnRefresh:
                    if (isLoading) {
                        webView.stopLoading();
                    } else {
                        webView.reload();
                    }
                    break;
                case R.id.btnPrev:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    break;
                case R.id.btnNext:
                    if (webView.canGoForward()) {
                        webView.goForward();
                    }
                    break;
            }
        }
    };

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap fav) {
            if (imm != null) {
                imm.hideSoftInputFromWindow(gotoUrl.getWindowToken(), 0);
            }
            urlLoading.setVisibility(View.VISIBLE);
            gotoUrl.setText(url);
            htmlTitle.setText(url);
            favicon.setImageBitmap(fav);
            btnRefresh.setText("✖");
            isLoading = true;
			WebViewActivity.this.url = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            urlLoading.setVisibility(View.GONE);
            btnRefresh.setText("↺");
            isLoading = false;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            favicon.setImageBitmap(icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            htmlTitle.setText(title);
        }

        //@Override
        public void openFileChooser(ValueCallback<Uri> fileChooserCallback, String acceptType, String capture) {
            // Log.d(tag, "openFileChooser with: acceptType = " + acceptType + " capture = " + capture);
            mFileChooserCallback = fileChooserCallback;
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()), WebViewActivity.this, FileBrowserActivity.class), FILE_CHOOSER_RESULT);
        }
    }
    private Handler h = new Handler() {
        private long up = 0;

        @Override
        public void handleMessage(android.os.Message message) {
            //Log.d(tag, "handleMessage");
            switch (message.what) {
                case DELAY:
                    if (up != 0) {
                        sendEmptyMessageDelayed(DELAY, DISPLAY_TIME - (System.currentTimeMillis() - up));
                        up = 0;
                        return;
                    }
                    webTitlePanel.setVisibility(View.GONE);
                    break;
                case ADD_DELAY:
                    up = System.currentTimeMillis();
                    break;
                case CLEAR_DELAY:
                    up = 0;
                    removeMessages(DELAY);
                    webTitlePanel.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //super.onActivityResult(requestCode, resultCode, intent);
        //Log.d(tag, "onActivityResult");
        if (requestCode == FILE_CHOOSER_RESULT) {
            if (mFileChooserCallback == null) {
                //Log.d(tag, "callback null");
                return;
            }
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            mFileChooserCallback.onReceiveValue(result);
            mFileChooserCallback = null;
            //Log.d(tag, "callback result: " + result);
        }
    }

    private class MyJavascriptInterface {

        public void getHtml(String html) {
            Intent intent = new Intent(Intent.ACTION_VIEW, null, WebViewActivity.this, EditorActivity.class);
            intent.putExtra(EditorActivity.CODE, html);
            intent.putExtra(EditorActivity.CODE_TYPE, EditorActivity.HTML);
            startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, 1, R.string.view_html)
			.setIcon(R.drawable.ic_menu_html);
		menu.add(Menu.FIRST, 2, 1, R.string.view_cookies)
			.setIcon(R.drawable.ic_menu_cookies);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1: {
					webView.loadUrl("javascript:window." + GET_HTML + ".getHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
					break;
				}
			case 2: {
					CookieManager cman = CookieManager.getInstance();
                    Intent intent = new Intent(Intent.ACTION_VIEW, null, this, EditorActivity.class);
					intent.putExtra(EditorActivity.CODE, cman.getCookie(url));
					intent.putExtra(EditorActivity.CODE_TYPE, EditorActivity.NONE);
					startActivityForResult(intent, EditorActivity.REQUEST_VIEW_SOURCE);
				}
		}
        return true;
    }
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		Toast.makeText(this, "WebViewActivity onBackPressed", Toast.LENGTH_SHORT).show();
	}
}
