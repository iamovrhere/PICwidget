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
package com.ovrhere.android.picwidget.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ovrhere.android.picwidget.R;
import com.ovrhere.android.picwidget.prefs.PreferenceUtils;
import com.ovrhere.android.picwidget.ui.provider.PICWidgetProvider;
import com.ovrhere.android.picwidget.utils.BitmapUtil;
import com.ovrhere.android.picwidget.utils.FileUtil;
import com.ovrhere.android.picwidget.utils.ImagePickerUtil;

/**
 * The configuration Activity (derived from MainActivity) in progress.
 * (To be) Used to configure the widget at launch and otherwise.
 *  
 * @author Jason J.
 * @version 0.6.0-20140819
 */
public class PICWidgetConfigurationActivity extends Activity 
implements OnClickListener, OnFocusChangeListener, OnCheckedChangeListener {
	
	
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
	/** Bundle key. The current #isFirstRun value. Boolean. */
	final static private String KEY_FIRST_RUN = 
			CLASS_NAME + ".KEY_FIRST_RUN";
	/** Bundle key. The current {@link #isPictureSet} value. Boolean. */
	final static private String KEY_PICTURE_SET = 
			CLASS_NAME + ".KEY_PICTURE_SET";
	/** Bundle key. The value of {@link #cb_allowPhone}. Boolean. */
	final static private String KEY_ALLOW_PHONE_CHECK = 
			CLASS_NAME + ".KEY_UNDERSTAND_PHONE_CHECK";
	/** Bundle key. The content of {@link #et_infoText}. String. */
	final static private String KEY_PHONE_NUMBER = 
			CLASS_NAME + ".KEY_PHONE_NUMBER";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End bundle keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The request id for the image picker. */
	final static private int REQUEST_IMAGE_PICKER_SELECT = 0x123;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The app widget id for accessing views. */
	private int mAppWidgetId = -1; 
	
	/** The display image being previewed. */
	private ImageView displayImage = null;
	/** The info text input. */
	private EditText et_infoText = null;
	/** The phone number text input. */
	private EditText et_phoneNumber = null;
	/** Checkbox defining if they understand conditions regarding checkbox. */ 
	private CheckBox cb_allowPhone = null;
	
	/** The spinner to determine how to display time. */
	private Spinner sp_timeDisplay = null;
	/** The button to remove the current picture. */
	private Button btn_remove = null;
	
	/** The value for whether is the first run. */
	private boolean isFirstRun = false;
	/** Whether or not the picture is set. */
	private boolean isPictureSet = false;
	/** The name to give the display picture. */
	private String displayImageName = "";
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_APP_WIDGET_ID, mAppWidgetId);
		outState.putString(KEY_INFO_TEXT, et_infoText.getText().toString());
		outState.putInt(KEY_TIME_DISPLAY_CHOICE, 
				sp_timeDisplay.getSelectedItemPosition());
		outState.putBoolean(KEY_FIRST_RUN, isFirstRun);
		outState.putBoolean(KEY_PICTURE_SET, isPictureSet);
		outState.putBoolean(KEY_ALLOW_PHONE_CHECK, 				
				cb_allowPhone.isChecked());
		outState.putString(KEY_PHONE_NUMBER, et_phoneNumber.getText().toString());
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
			isFirstRun = savedInstanceState.getBoolean(KEY_FIRST_RUN);
			isPictureSet = savedInstanceState.getBoolean(KEY_PICTURE_SET);			
		} 
		
		Bundle extras = getIntent().getExtras();
		if (mAppWidgetId < 0 && extras != null) {
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		displayImageName =
				getResources().getString(R.string.com_ovrhere_picwidget_filename_imgStub) + 
				mAppWidgetId;
		
		checkPreferences();
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_PICKER_SELECT && 
			resultCode == Activity.RESULT_OK){  
			String sourcePath = ImagePickerUtil.getPathFromCameraData(this, data);
			
			if (resizeAndCopyImage(sourcePath)){
				isPictureSet = true;
				setDisplayImage();
			} else {
				isPictureSet = false;
			}
		}
		
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
		//TODO set maximum lines for edit text
	
		initPhoneViews();
		
		displayImage = (ImageView) 
				findViewById(R.id.com_ovrhere_picwidget_activity_config_img_preview);
		initSpinner();
	}
	/** Initializes the phone views (check + edit text). */
	private void initPhoneViews() {
		et_phoneNumber = (EditText)
				findViewById(R.id.com_ovrhere_picwidget_activity_config_textin_phoneNum);
		et_phoneNumber.setEnabled(false);
		et_phoneNumber.setOnFocusChangeListener(this);
		
		cb_allowPhone = (CheckBox)
				findViewById(R.id.com_ovrhere_picwidget_activity_config_check_phoneAllow);
		cb_allowPhone.setOnCheckedChangeListener(this);
	}
	/** Initializes the spinner. */
	private void initSpinner() {
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
		btn_remove = (Button) 
				findViewById(R.id.com_ovrhere_picwidget_activity_config_button_removePicture);
		btn_remove.setOnClickListener(this);
	}
	/** Checks to see if preferences are set. If not, sets them. 
	 * If set, set's view's to their content. Requires view be set
	 * @see initViews */
	private void checkPreferences(){
		isFirstRun = PreferenceUtils.isFirstRun(this, mAppWidgetId);
		if(isFirstRun){
			PreferenceUtils.setToDefault(this, mAppWidgetId);
		}
		SharedPreferences prefs = PreferenceUtils.getPreferences(this, mAppWidgetId);
		
		setDisplayPicture(prefs);
		setInfoText(prefs);
		setTimeDisplaySpinner(prefs);
		
		setPhoneNumber(prefs);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Resizes and copies image to internal memory. 
	 * @param sourcePath The location of the original image.
	 * @return <code>true</code> if successful, <code>false</code> on failure.	 */
	private boolean resizeAndCopyImage(String sourcePath) {
		int px = getResources().getDimensionPixelOffset(
				R.dimen.com_ovhere_picwidget_internal_resizeImgSize);
		Bitmap selectedImg = BitmapFactory.decodeFile(sourcePath);
		selectedImg = BitmapUtil.scaleDownBitmap(selectedImg, px);
		InputStream input = new ByteArrayInputStream(
								BitmapUtil.bitmapToBytes(selectedImg));
		try {
			FileUtil.writeFileToInternal(this, input, displayImageName);
		} catch (IOException e){
			return false;
		}
		return true;
	}
	
	/**
	 * Set's picture display based on existing file & the accompanying button.
	 */
	private void setDisplayImage() {
		Bitmap bitmap = BitmapUtil.readBitmapFromInternal(this, displayImageName);
		boolean imgExists = bitmap != null;
		if (imgExists){
			displayImage.setImageBitmap(bitmap);
		}
		if (btn_remove != null){
			btn_remove.setEnabled(imgExists);
		} 
	}
	
	/** Sets the display picture enviroment based upon preference.
	 * @param prefs The preference to check.
	 */
	private void setDisplayPicture(SharedPreferences prefs) {
		Resources r = getResources();
		isPictureSet = prefs.getBoolean(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_DISPLAY_PICTURE),
				r.getBoolean(R.bool.com_ovrhere_picwidget_pref_DEF_VALUE_DISPLAY_PICTURE));
		setDisplayImage();
	}
	
	/** Sets the phone number and partner checkbox according to preference.
	 * If there is value, checkbox is true, otherwise, it is set false.
	 * @param prefs The prefs to use.
	 */
	private void setPhoneNumber(SharedPreferences prefs) {
		String phone = prefs.getString(
				getResources().getString(
						R.string.com_ovrhere_picwidget_pref_KEY_CONTACT_PHONE_NUMBER),
				"");
		if (phone.trim().isEmpty()){
			cb_allowPhone.setChecked(false);
		} else {
			cb_allowPhone.setChecked(true);
			et_phoneNumber.setText(phone);
		}
	}
	
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
				et_infoText.getText()	.toString()
										.trim()
										//.replace("\n", "")
				);
		String phone = 	cb_allowPhone.isChecked() ? 
						et_phoneNumber.getText().toString().trim() :  "";
		
		editor.putString(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_CONTACT_PHONE_NUMBER), 
				phone);
		
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
		editor.putBoolean(
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_DISPLAY_PICTURE),
				isPictureSet
				);
	}
	
	
	/** Hides the keyboard. */
	private void hideKeyboard(View v){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	/** Notifies the widget the the preferences have been updated. */
	private void updateWidget(){
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		//ComponentName widgetComponent = new ComponentName(this, PICWidgetProvider.class);
		
		int[] widgetIds = new int[]{mAppWidgetId}; 
				//widgetManager.getAppWidgetIds(widgetComponent);
		Intent update = new Intent();
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		update.setClass(this, PICWidgetProvider.class);
		
		//Required to ensure the text views update
		widgetManager.notifyAppWidgetViewDataChanged(
        		mAppWidgetId, 
        		R.id.com_ovrhere_picwidget_widget_viewAnimator_infoText);
		
		sendBroadcast(update);
	}
	/** Sets up the widget for the first time. */
	private void setUpWidget() {
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Action Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Defines actions to be don at completetion. */
	private void configComplete() {
		collectAndSet();
		//final step	
		if (isFirstRun){
			setUpWidget();
			finish();
			updateWidget();
		} else {
			updateWidget();
			finish();
		}
	}
	
	/** The actions to perform when removing the picture. */
	private void removePicture() {
		isPictureSet = false;
		displayImage.setImageDrawable(
				getResources().getDrawable(R.drawable.ic_no_picture)
				);
		FileUtil.deleteFromInternal(this, displayImageName);
		btn_remove.setEnabled(false);
	}
	
	/** Launchers image picker with {@link #REQUEST_IMAGE_PICKER_SELECT}. */
	private void launchImagePicker(){
		ImagePickerUtil.launchPicker(this, REQUEST_IMAGE_PICKER_SELECT);	
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!isChecked){
			//if not allowed, clear text.
			//et_phoneNumber.setText(""); //actually, let's not for now.
		}
		et_phoneNumber.setEnabled(isChecked);
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()){
			case R.id.com_ovrhere_picwidget_activity_config_textin_phoneNum:
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
			configComplete();			
			break;
		case R.id.com_ovrhere_picwidget_activity_config_button_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;			
		case R.id.com_ovrhere_picwidget_activity_config_button_selectPicture:
			launchImagePicker();
			break;
		case R.id.com_ovrhere_picwidget_activity_config_button_removePicture:
			removePicture();
			break;
		default:
			//nothing here
		}
	}
	
		
}
