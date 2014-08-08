/*
 * Copyright 2014 Jason J.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.pictureinfocard.widget.ui;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ovrhere.android.pictureinfocard.widget.R;
import com.ovrhere.android.pictureinfocard.widget.prefs.PreferenceUtils;

/**
 * The configuration Activity (derived from MainActivity) in progress.
 * (To be) Used to configure the widget at launch and otherwise.
 *  
 * @author Jason J.
 * @version 0.2.0-20140806
 */
public class PICWidgetConfigurationActivity extends Activity 
implements OnClickListener, OnFocusChangeListener {
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The class name. */
	final static private String CLASS_NAME = 
			PICWidgetConfigurationActivity.class.getSimpleName();
	/** Bundle key. The app widget intent id. Int. */
	final static private String KEY_APP_WIDGET_ID = 
			CLASS_NAME + ".KEY_APP_WIDGET_ID";
	/** Bundle key. The current content of the edittext. String. */
	final static private String KEY_INFO_TEXT = 
			CLASS_NAME + ".KEY_INFO_TEXT";
	/** Bundle key. The current index of the key selected. Int */
	final static private String KEY_TIME_DISPLAY_CHOICE = 
			CLASS_NAME + ".KEY_DISPLAY_TIME";
	
	/** The request id for the image picker. */
	final static private int REQUEST_IMAGE_PICKER_SELECT = 0x123;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The app widget id for accessing views. */
	private int mAppWidgetId = -1; 
	
	/** The info text input. */
	private EditText et_infoText = null;
	/** The spinner to determine how to display time. */
	private Spinner sp_timeDisplay = null;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_APP_WIDGET_ID, mAppWidgetId);
		outState.putString(KEY_INFO_TEXT, et_infoText.getText().toString());
		outState.putInt(KEY_TIME_DISPLAY_CHOICE, sp_timeDisplay.getSelectedItemPosition());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picwidget_configuration);
		initViews();
		
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		if (savedInstanceState != null){
			mAppWidgetId = savedInstanceState.getInt(KEY_APP_WIDGET_ID);
			String infoText = savedInstanceState.getString(KEY_INFO_TEXT);
			et_infoText.setText(infoText == null ? "" : infoText);
			int pos = savedInstanceState.getInt(KEY_TIME_DISPLAY_CHOICE);
			sp_timeDisplay.setSelection( 
					(pos < 0 || pos >= sp_timeDisplay.getCount()) ? 0 : pos );
		} 
		
		if (mAppWidgetId < 0) {
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
			    mAppWidgetId = extras.getInt(
			            AppWidgetManager.EXTRA_APPWIDGET_ID, 
			            AppWidgetManager.INVALID_APPWIDGET_ID);
			}
		}
		checkPreferences();
		
		//TODO finish the settings & configuring view		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initializer helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes all views. */
	private void initViews() {
		initButtons();
		
		et_infoText = (EditText) 
				findViewById(R.id.com_ovrhere_picwidget_activity_config_textin_info);
		et_infoText.setOnFocusChangeListener(this);
	
		sp_timeDisplay = (Spinner)
				findViewById(R.id.com_ovrhere_picwidget_activity_config_spinner_clockChoice);
		ArrayList<String> choices = 
				new ArrayList<String>(Arrays.asList(
						getResources().getStringArray(
								R.array.com_ovrhere_picwidget_spinner_timeOptions)
						));
		ArrayAdapter<String> adapter = 
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item	, choices);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_timeDisplay.setAdapter(adapter);
	}
	/** Initialize buttons and set listeners. */
	private void initButtons() {
		((Button) findViewById(R.id.com_ovrhere_picwidget_activity_config_button_confirm))
			.setOnClickListener(this);
		((Button) findViewById(R.id.com_ovrhere_picwidget_activity_config_button_cancel))
			.setOnClickListener(this);
		((Button) findViewById(R.id.com_ovrhere_picwidget_activity_config_button_selectPicture))
			.setOnClickListener(this);
		((Button) findViewById(R.id.com_ovrhere_picwidget_activity_config_button_removePicture))
			.setOnClickListener(this);
	}
	/** Checks to see if preferences are set. If not, sets them. 
	 * If set, set's view's to their content. Requires view be set
	 * @see initViews */
	private void checkPreferences(){
		if(PreferenceUtils.isFirstRun(this, mAppWidgetId)){
			PreferenceUtils.setToDefault(this, mAppWidgetId);
		}
		SharedPreferences prefs = PreferenceUtils.getPreferences(this, mAppWidgetId);
		
		setInfoText(prefs);
		setTimeDisplaySpinner(prefs);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets the info text based on preferences. 
	 * @param prefs The preference reference to use.
	 */
	private void setInfoText(SharedPreferences prefs){
		et_infoText.setText(prefs.getString(
				getResources().getString(R.string.com_ovrhere_picwidget_pref_KEY_INFO_CONTENT), 
				"")) ;
	}	
	
	/** Sets the spinner selection based on preferences
	 * @param prefs The preference reference to use. 
	 */
	private void setTimeDisplaySpinner(SharedPreferences prefs){
		Resources r = getResources();
		String display = prefs.getString(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_TIME_DISPLAY), 
				"");
		int selected = 0; //default
		
		if (display.equals(r.getString(R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_NONE))){
			selected = 0;
		} else if (display.equals(r.getString(R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_ANALOG))){
			selected = 1;
		}
		sp_timeDisplay.setSelection(selected);
	}
	
	
	/** Collects the settings, sets the  preferences, and returns. */
	private void collectAndSet(){
		SharedPreferences.Editor editor = 
				PreferenceUtils.getPreferences(this, mAppWidgetId).edit();
		Resources r = getResources();
		
		editor.putString(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_INFO_CONTENT),
				et_infoText.getText().toString()
				);
		setTimeDisplayPreference(editor);
		
		editor.commit();
	}
	
	/** Sets the time display preference based upon the current setting. 
	 * @param editor The preference editor to set on. */
	private void setTimeDisplayPreference(SharedPreferences.Editor editor){
		Resources r = getResources();
		int selection = R.string.com_ovrhere_picwidget_pref_DEF_VALUE_TIME_DISPLAY;
		switch (sp_timeDisplay.getSelectedItemPosition()){
			case 0: //assume none
				selection = R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_NONE;
				break;
			case 1: //assume analog
				selection = R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_ANALOG;
				break;
			default: //assume broken
				selection = R.string.com_ovrhere_picwidget_pref_DEF_VALUE_TIME_DISPLAY;
		}
		editor.putString(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_TIME_DISPLAY), 
				r.getString(selection));
	}
	
	/** Launchers image picker with {@link #REQUEST_IMAGE_PICKER_SELECT}. */
	private void launchImagePicker(){
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
		startActivityForResult(i, REQUEST_IMAGE_PICKER_SELECT);		
	}
	/** Hides the keyboard. */
	private void hideKeyboard(View v){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()){
			case R.id.com_ovrhere_picwidget_activity_config_textin_info:
				if (!hasFocus){
					hideKeyboard(v);
				}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.com_ovrhere_picwidget_activity_config_button_confirm:
			collectAndSet();
			//final step
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			break;
		case R.id.com_ovrhere_picwidget_activity_config_button_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
			
		case R.id.com_ovrhere_picwidget_activity_config_button_selectPicture:
			launchImagePicker();
			//TODO handle results
			break;
		case R.id.com_ovrhere_picwidget_activity_config_button_removePicture:
			
			break;
		default:
			//nothing here
		}
	}
}
