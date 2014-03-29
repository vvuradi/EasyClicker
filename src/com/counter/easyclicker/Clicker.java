package com.counter.easyclicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Clicker extends Activity {

	private int count;
	private TextView countTextView;
	private EditText intValueView;
	private final int VIBRATION_TIME = 10;
	private final int MAX_COUNT = 99999;
	Vibrator vibe;
	SharedPreferences SP;
	SharedPreferences.Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clicker);
				
		findViewById(R.id.sub).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (SP.getBoolean("vibration_checkbox", false))
					vibe.vibrate(VIBRATION_TIME);
				changeValue(getValue(), false);				
			}
		});
	}
	
	private int getValue(){
		int value = 1;
		if(intValueView.getVisibility()==View.VISIBLE){
			String s = intValueView.getText().toString();
			value = (s.equals("") ? 0 : Integer.parseInt(s));
		}
		return value;
	}
	
	private void checkManualInput(){
		if(SP.getBoolean("manual_input_checkbox", false))
			intValueView.setVisibility(View.VISIBLE);
		else
			intValueView.setVisibility(View.GONE);
	}

	public void incCount(View view) {
		if (SP.getBoolean("vibration_checkbox", false))
			vibe.vibrate(VIBRATION_TIME);
		changeValue(getValue(), true);		
	}
	
	private void changeValue(int value, boolean add){
		if (Math.abs(count) > MAX_COUNT){
			Toast.makeText(getBaseContext(), R.string.limit_reached, Toast.LENGTH_LONG).show();
			return;
		}
		count = add ? (count + value) : (count - value);
		display();		
	}	

	private void display() {
		//TextView c = ((TextView) findViewById(R.id.countView));
		countTextView.setText(Integer.toString(count));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:			
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.id_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.id_reset:
			reset();
			return true;		
		case R.id.id_prev_value:
			showPreviousCount();
			return true;
		case R.id.id_copy_count:
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			    clipboard.setText(Integer.toString(count));
			} else {
			    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			    ClipData clip = ClipData.newPlainText("Copied Text", Integer.toString(count));
			    clipboard.setPrimaryClip(clip);
			}
			Toast.makeText(getBaseContext(), R.string.copy_clipboard, Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showPreviousCount(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		int pCount = SP.getInt("prevCount", 0);
		builder.setMessage(Integer.toString(pCount));
		builder.setTitle(R.string.prev_value_dialog_title);
		builder.setPositiveButton(R.string.ok, null);		
		AlertDialog dialog = builder.show();
		TextView msgTxt = (TextView) dialog.findViewById(android.R.id.message);
		msgTxt.setGravity(Gravity.CENTER);
		msgTxt.setTextSize(50);
		dialog.show();
	}
	
	private void reset() {
		editor = SP.edit();
	    editor.putInt("prevCount", count);
	    editor.commit();
		count = 0;
		display();
	}	

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {		
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
				if (SP.getBoolean("hardware_checkbox", false))
					changeValue(1, true);
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				if (SP.getBoolean("hardware_checkbox", false))
					changeValue(1, false);
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}	
	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first
	    editor = SP.edit();
	    editor.putInt("count", count);
	    editor.commit();
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
	    
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_general, false);
		SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		count = SP.getInt("count", 0);		
		
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		countTextView = (TextView)findViewById(R.id.countView);
		intValueView = (EditText) findViewById(R.id.intValue);	
		
		countTextView.setKeepScreenOn(SP.getBoolean("stay_awake_checkbox", true));
		
		checkManualInput();		
		display();					
	}
}







