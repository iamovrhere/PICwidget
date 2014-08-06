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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ovrhere.android.pictureinfocard.widget.R;

/**
 * The configuration Activity (derived from MainActivity) in progress.
 * (To be) Used to configure the widget at launch and otherwise.
 *  
 * @author Jason J.
 * @version 0.1.0-20140806
 */
public class PICWidgetConfigurationActivity extends Activity {
	/** The class name. */
	final static private String CLASS_NAME = 
			PICWidgetConfigurationActivity.class.getSimpleName();
	/** Bundle key. The app widget intent id. Int. */
	final static private String KEY_APP_WIDGET_ID = 
			CLASS_NAME + ".KEY_APP_WIDGET_ID";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The app widget id for accessing views. */
	private int mAppWidgetId = -1; 
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_APP_WIDGET_ID, mAppWidgetId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picwidget_configuration);
		
		if (savedInstanceState != null){
			mAppWidgetId = savedInstanceState.getInt(KEY_APP_WIDGET_ID);
		} else {
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
			    mAppWidgetId = extras.getInt(
			            AppWidgetManager.EXTRA_APPWIDGET_ID, 
			            AppWidgetManager.INVALID_APPWIDGET_ID);
			}
		}
		//TODO in between steps of settings, configuring view
		
		//final step
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
}
