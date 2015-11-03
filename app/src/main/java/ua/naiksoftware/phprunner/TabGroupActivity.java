package ua.naiksoftware.phprunner;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import java.util.Stack;

public class TabGroupActivity extends ActivityGroup {
	
	private Stack<String> stack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (stack == null) stack = new Stack<String>();
		// start first default activity
		push("FirstStackActivity", getIntent().setClass(this, FileBrowserActivity.class) /*new Intent(Intent.ACTION_EDIT, Uri.parse(getIntent().getStringExtra("docFolder")), this, FileBrowserActivity.class)*/);
		Toast.makeText(this, "Group onCreate (start FileBrowser)", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void finishFromChild(Activity child) {
		pop();
	}
	
	@Override
	public void onBackPressed() {
		pop();
		Toast.makeText(this, "Group onBackPressed", Toast.LENGTH_SHORT).show();
	}
	
	public void push(String id, Intent intent) {
		Window window = getLocalActivityManager().startActivity(id, intent/*.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)*/);
		if (window != null) {
			stack.push(id);
			setContentView(window.getDecorView());
		}
	}
	public void pop() {
		if (stack.size() < 2) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			Toast.makeText(this, "Group onBackPressed -> pop", Toast.LENGTH_SHORT).show();
			return;
		}
		LocalActivityManager manager = getLocalActivityManager();
		manager.destroyActivity(stack.pop(), true);
		if (stack.size() > 0) {
			Intent lastIntent = manager.getActivity(stack.peek()).getIntent();
			Window newWindow = manager.startActivity(stack.peek(), lastIntent);
			setContentView(newWindow.getDecorView());
		}
	}
}
