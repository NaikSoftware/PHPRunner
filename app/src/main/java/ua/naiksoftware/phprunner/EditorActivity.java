package ua.naiksoftware.phprunner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import ua.naiksoftware.phprunner.editor.CodeChangedListener;
import ua.naiksoftware.phprunner.editor.SourceEditor;
import ua.naiksoftware.phprunner.editor.VerticalNumsLine;
import ua.naiksoftware.widget.IconSpinnerAdapter;
import android.widget.ImageButton;

/**
 *
 * @author Naik
 */
public class EditorActivity extends Activity implements View.OnClickListener {

    private static final String tag = "EditorActivity";
    public static final String CODE = "code";
    public static final String CODE_TYPE = "code_type";

    public static final int CONF = 5;
    public static final int CSS = 4;
    public static final int JS = 3;
    public static final int PHP = 2;
    public static final int HTML = 1;
    public static final int NONE = 0;

    public static final int REQUEST_VIEW_SOURCE = 1;
    private static final String FILES_ID = "files";
	private static final int MAX_OPENED = 10;
    private String code, url;
    private Spinner spinnerFiles;
    private TreeSet<String> fileSet = new TreeSet<String>();
    private SourceEditor editorView;
    private VerticalNumsLine vertLine;
    private Toast toastSavedOk;
    private boolean saved, exists;
    private int codeType;
    private int defaultTextSize = 25;
    private int defaultTextColorLine = 0xffcccc11;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        fileSet = new TreeSet<String>(prefs.getStringSet(FILES_ID, new TreeSet<String>()));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.editor_layout);

        editorView = (SourceEditor) findViewById(R.id.editor_editCode);
        vertLine = (VerticalNumsLine) findViewById(R.id.verticalNumsLine);
        toastSavedOk = Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT);
        Spinner spinnerMenu = (Spinner) findViewById(R.id.spinner_menu);
        spinnerMenu.setAdapter(new IconSpinnerAdapter(getResources().getStringArray(R.array.test_arr), this));
        spinnerMenu.setBackground(null);
        spinnerMenu.setPadding(0, 0, 0, 0);

        Intent intent = getIntent();

        final String act = intent.getAction();
        url = intent.getDataString();
        codeType = intent.getIntExtra(CODE_TYPE, NONE);

        if (act.equals(Intent.ACTION_VIEW)) {       // get code from extra
            exists = false;
            saved = false;
            code = intent.getStringExtra(CODE);
            if (code == null) {
                code = "";
            }
            url = getString(R.string.new_file_not_saved);
        } else if (act.equals(Intent.ACTION_EDIT)) {// load code from URL
            exists = true;
            saved = true;
            if (url == null || !new File(url).exists()) {
                Toast.makeText(this, getString(R.string.not_found_file) + url, Toast.LENGTH_LONG).show();
                finish();
            }
            code = FileUtils.readFile(url);
			fileSet.add(url);
			if (fileSet.size() > MAX_OPENED) {
				fileSet.pollFirst();
			}
			spinnerFiles = (Spinner) findViewById(R.id.spinnerListFiles);
			spinnerFiles.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, fileSet.toArray(new String[]{})));
			((ImageButton)findViewById(R.id.editor_btnRemove)).setOnClickListener(this);
        } else {
            throw new RuntimeException("Invalid action for EditorActivity, "
									   + "must be ACTION_VIEW or ACTION_EDIT!");
        }
        //fileNameView.setText(url);
        setCode(code);
        editorView.setCodeChangedListener(new CodeChangedListener() {
				public void codeChanged() {
					//fileNameView.setText("*" + url);
					vertLine.setLines(editorView.getLineCount());
				}
			});
        code = null;

    }

    private void setCode(String code) {
        editorView.setTextHighlighted(code, codeType);
        int textSize = defaultTextSize;// TODO: get from prefs
        int textColor = defaultTextColorLine;//... 
        vertLine.setTextSize(textSize);
        vertLine.setTextColor(textColor);
        editorView.setTextSize(textSize);
        editorView.setTextColor(0xffcccccc);
        editorView.getPaint().set(vertLine.getPaint());
        editorView.post(new Runnable() {

				public void run() {
					vertLine.setLines(editorView.getLineCount());
				}
			});
    }

    private void saveCode() {
        if (!exists) {
            // pick path
            return;
        }
        try {
            FileUtils.saveCode(editorView.getText().toString(), "utf-8", url);
        } catch (IOException e) {
            Toast.makeText(this, "Error IO: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        //fileNameView.setText(url);
        toastSavedOk.show();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.editor_btnRemove:
				fileSet.remove(url);
				spinnerFiles.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, fileSet.toArray(new String[]{})));
				url = fileSet.first();
				if (url == null) {
					finish();
					return;
				}
				if (!new File(url).exists()) {
					onClick(v);
				} else {
					setCode(FileUtils.readFile(url));
				}
				break;
		}
	}

	@Override
	protected void onDestroy() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(FILES_ID, fileSet);
		editor.commit();
		super.onDestroy();
	}
}
