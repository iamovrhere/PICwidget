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
package com.ovrhere.android.picwidget.ui.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.ovrhere.android.picwidget.R;
import com.ovrhere.android.picwidget.broadcastreceivers.ConfigurationBroadcastReceiver;
import com.ovrhere.android.picwidget.prefs.PreferenceUtils;
import com.ovrhere.android.picwidget.ui.PICWidgetConfigurationActivity;
import com.ovrhere.android.picwidget.ui.remoteviews.AdapterViewFlipperWidgetService;
import com.ovrhere.android.picwidget.utils.BitmapUtil;
import com.ovrhere.android.picwidget.utils.FileUtil;

/**
 * The {@link AppWidgetProvider} for the widget. 
 * Provides an interface for widget interactions (deletions, creations, updates, etc).
 * 
 * Requires:
 * <code>&lt;uses-permission android:name="android.permission.CALL_PHONE" /&gt;</code>
 *  
 * @author Jason J.
 * @version 0.6.0-20140819
 */
public class PICWidgetProvider extends AppWidgetProvider 
	implements ConfigurationBroadcastReceiver.OnConfigChangeListener {
	/** Class name for debugging purposes. */
	//@SuppressWarnings("unused")
	final static private String CLASS_NAME = 
			PICWidgetProvider.class.getSimpleName();
	
	/** Broadcast intent name. Remember to keep this consistent with the manifest.
	 * Expects: {@link #EXTRA_KEY_PHONE} */
	final static private String BROADCAST_INTENT_CALL = 
			"com.ovrhere.android.picwidget.ui.provider.PICWidgetProvider.BROADCAST_INTENT_CALL";
			//PICWidgetProvider.class.getName()+".BROADCAST_INTENT_CALL";
	/** Extra Key: The key for the phone number ONLY. String. */
	final static private String EXTRA_KEY_PHONE = 
			CLASS_NAME + ".EXTRA_KEY_PHONE";
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		ConfigurationBroadcastReceiver configReceiver =
				new ConfigurationBroadcastReceiver();
		configReceiver.setOnConfigChangeListener(this);
		context.getApplicationContext().registerReceiver(
				configReceiver, 
				new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
		
        final int SIZE = appWidgetIds.length;
        for (int i = 0; i < SIZE; i++) {
            int appWidgetId = appWidgetIds[i];
            SharedPreferences prefs = 
            		PreferenceUtils.getPreferences(context, appWidgetId);
            
            RemoteViews remoteViews = 
    				new RemoteViews( 
    						context.getPackageName(), 
    						R.layout.appwidget_pic_info_call);                       
	        
            configPicture(context, appWidgetId, remoteViews, prefs);
            configClock(context, appWidgetId, remoteViews, prefs);
	        configViewFlipper(context, appWidgetId, remoteViews);
	        
            setPendingIntents(context, appWidgetId, remoteViews, prefs);
            	        	        
	        //clear remote views
	        appWidgetManager.updateAppWidget( appWidgetId, null ); 
        	appWidgetManager.updateAppWidget( appWidgetId, remoteViews );
        }
	}

	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		final int SIZE = appWidgetIds.length;
        for (int i = 0; i < SIZE; i++) {
            int appWidgetId = appWidgetIds[i];
            SharedPreferences.Editor editor = 
            		PreferenceUtils.getPreferences(context, appWidgetId).edit();
            //clear all preferences
            editor.clear().commit();
            String fileName = context.getResources()
						.getString(R.string.com_ovrhere_picwidget_filename_imgStub) + 
						appWidgetId;
            FileUtil.deleteFromInternal(context, fileName);
        }
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(BROADCAST_INTENT_CALL)){
			launchCallIntent(context, intent);
		} 
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Receive actions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Launches the call intent. 
	 * @param context Context straight from {@link #onReceive(Context, Intent)}
	 * @param intent Intent straight from {@link #onReceive(Context, Intent)} */
	static private void launchCallIntent(Context context, Intent intent) {
		String phone = intent.getStringExtra(EXTRA_KEY_PHONE);
		
		if (phone == null || phone.isEmpty()){
			Log.e(CLASS_NAME, BROADCAST_INTENT_CALL+": No number supplied.");
			return; //no number, no go.
		}
		//start *actual* call intent.
		Intent callIntent = new Intent(Intent.ACTION_CALL);           
		callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		callIntent.setData(Uri.parse("tel:"+phone));
		context.startActivity(callIntent);
	}
	
	/** Sends broadcast update to all widgets. 
	 * @param context Context straight from {@link #onReceive(Context, Intent)}
	 * @param intent Intent straight from {@link #onReceive(Context, Intent)} */
	static private void sendUpdateBroadcast(Context context, Intent intent){
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = 
				new ComponentName(context, PICWidgetProvider.class);
		
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
		Intent update = new Intent();
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		update.setClass(context, PICWidgetProvider.class);
		
		for (int index = 0; index < widgetIds.length; index++) {
			//Required to ensure the text views update
            widgetManager.notifyAppWidgetViewDataChanged(
            		widgetIds[index], 
	        		R.id.com_ovrhere_picwidget_widget_viewAnimator_infoText);
		}
		
		context.sendBroadcast(update);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**Sets the pending intents for the widget. 
	 * @param context The current context.
	 * @param appWidgetId The current widget id.
	 * @param remoteViews The remoteviews to attach intents to
	 * @param prefs The current shared preference for this widget 
	 */
	static private void setPendingIntents(Context context, int appWidgetId,
			RemoteViews remoteViews, SharedPreferences prefs) {
		Resources r = context.getResources();
		//if there is a number to call
		String phoneNumber = prefs.getString(
					r.getString(
						R.string.com_ovrhere_picwidget_pref_KEY_CONTACT_PHONE_NUMBER),
						"");
		boolean useMultipleIntents =  !phoneNumber.isEmpty();
		if (useMultipleIntents){
	        setCallIntent(context, appWidgetId, remoteViews, phoneNumber);
		}
        setConfigIntent(context, appWidgetId, remoteViews, useMultipleIntents);
        //pending intent complete.
	}
	
	/** Sets the pending launch intent for #PICWidgetConfigurationActivity 
	 * @param context Current context
	 * @param appWidgetId The widget id of the configuration to load 
	 * @param remoteViews The remoteView to attach the intent to
	 * @param multipleIntents <code>true</code> to attach to body, <code>false</code>
	 * to entire widget
	 */
	static private void setConfigIntent(Context context, int appWidgetId,
			RemoteViews remoteViews, boolean multipleIntents) {
		Intent launchIntent = new Intent(context, PICWidgetConfigurationActivity.class); 
		launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		PendingIntent pendingIntent = 
				PendingIntent.getActivity(	context,
						//unique request code (or uri) required for unique pendingintent 
											appWidgetId /* requestCode */, 
											launchIntent, 
											0 /* flags */);
		if (multipleIntents){
			remoteViews.setOnClickPendingIntent(
					R.id.com_ovrhere_picwidget_widget_layout_bodyContainer, 
					pendingIntent);
		} else {
			remoteViews.setOnClickPendingIntent(
					R.id.com_ovrhere_picwidget_widget_layout, 
					pendingIntent);
		}
	}
	
	 /** Sets the pending launch intent for call 
	 * @param context Current context
	 * @param appWidgetId The widget id of the configuration to load 
	 * @param remoteViews The remoteView to attach the intent to
	 * @param phone The phone number to call.
	 */
	static private void setCallIntent(Context context, int appWidgetId,
			RemoteViews remoteViews, String phone) {
		phone.replaceAll("[^0-9]", ""); //remove all non digits
		
		Intent callBroadcastIntent = new Intent(BROADCAST_INTENT_CALL);
		callBroadcastIntent.putExtra(EXTRA_KEY_PHONE, phone);
		
        //set broadcast, so the provider can then launch it at the lock screen.
        PendingIntent pendingIntent = 
				PendingIntent.getBroadcast(	context,
						//unique request code (or uri) required for unique pendingintent 
											appWidgetId /* requestCode */, 
											callBroadcastIntent, 
											PendingIntent.FLAG_UPDATE_CURRENT /* flags */);
        remoteViews.setOnClickPendingIntent(
				R.id.com_ovrhere_picwidget_widget_layout_imgContainer, 
				pendingIntent);         
	 }
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End intents
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Configures the picture to display/hide for the widget.
	 * @param context The current context.
	 * @param appWidgetId The id of this widget.
	 * @param remoteViews The remote views to set up the image of.
	 * @param prefs The reference to this widget's preferences
	 */
	static private void configPicture(Context context, int appWidgetId, 
			RemoteViews remoteViews, SharedPreferences prefs){
		Resources r = context.getResources();
		String key = 
				r.getString(R.string.com_ovrhere_picwidget_pref_KEY_DISPLAY_PICTURE);
		String fileName =
				r.getString(R.string.com_ovrhere_picwidget_filename_imgStub) + 
				appWidgetId;
		String phone = 
				prefs.getString(
						r.getString(R.string.com_ovrhere_picwidget_pref_KEY_CONTACT_PHONE_NUMBER),
						"");
		boolean showCallText = 
				r.getBoolean(R.bool.com_ovrhere_picwidget_widget_callText);
		
		final int phoneImgId = R.id.com_ovrhere_picwidget_widget_img_call;
		final int phoneText = R.id.com_ovrhere_picwidget_widget_text_call;
		if (phone.isEmpty() || !showCallText){
			remoteViews.setViewVisibility(phoneImgId, View.GONE);
			remoteViews.setViewVisibility(phoneText, View.GONE);
		}
		
		final int imgContainId = R.id.com_ovrhere_picwidget_widget_layout_imgContainer;
		
		Bitmap bitmap = BitmapUtil.readBitmapFromInternal(context, fileName);
		if (bitmap != null ){ 
			remoteViews.setImageViewBitmap(
						R.id.com_ovrhere_picwidget_widget_img_mainPicture, 
						bitmap);
			remoteViews.setViewVisibility(imgContainId, View.VISIBLE);
			return;
		}
		//we must inform the widget that the image is gone.
		prefs	.edit()
				.putBoolean(key, false)
				.commit();
	 
		if (!phone.isEmpty()){
			//set image view as call button
			remoteViews.setViewVisibility(phoneImgId, View.GONE);			
			remoteViews.setImageViewResource(
					R.id.com_ovrhere_picwidget_widget_img_mainPicture, 
					android.R.drawable.ic_menu_call);
		} else {
			//if either the image is gone, or boolean false.
			remoteViews.setViewVisibility(imgContainId, View.GONE);
		}
	}
	
	/** Configures the clock to display/hide for the widget.
	 * @param context The current context.
	 * @param appWidgetId The id of this widget.
	 * @param remoteViews The remote views to set up the clock in.
	 * @param prefs The reference to this widget's preferences
	 */
	static private void configClock(Context context, int appWidgetId, 
			RemoteViews remoteViews, SharedPreferences prefs){
		Resources r = context.getResources();
		boolean displayClock = false ==
				prefs.getString(
						r.getString(R.string.com_ovrhere_picwidget_pref_KEY_TIME_DISPLAY),
							"")
					.equals(
						r.getString(R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_NONE)	
							);
		final int id = R.id.com_ovrhere_picwidget_widget_frame_clock;
		if (displayClock){
			remoteViews.setViewVisibility(id, View.VISIBLE);
		} else {
			remoteViews.setViewVisibility(id, View.GONE);
		}
	}
	/** Configures the #AdapterViewFlipperWidgetService for the widget, preparing
	 * The view and connecting it to the service.
	 * @param context The current context.
	 * @param appWidgetId The id of this widget.
	 * @param remoteViews The remote views to set up with this service.
	 */
	static private void configViewFlipper(Context context, int appWidgetId,
			RemoteViews remoteViews) {
		Intent flipperIntent = 
				new Intent(context, AdapterViewFlipperWidgetService.class);
		
		flipperIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// extras are ignored, so we need to embed the extras to be not ignored
		flipperIntent.setData(Uri.parse(flipperIntent.toUri(Intent.URI_INTENT_SCHEME)));
		
		remoteViews.setRemoteAdapter(
				R.id.com_ovrhere_picwidget_widget_viewAnimator_infoText, 
				flipperIntent);	        
		// empty view to displayed when adapter is empty.
		remoteViews.setEmptyView(
				R.id.com_ovrhere_picwidget_widget_viewAnimator_infoText, 
				R.id.com_ovrhere_picwidget_widget_progress_noText);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onConfigChange(ConfigurationBroadcastReceiver congfigBroadcastReceiver,
			Context context, Intent intent) {
		context.getApplicationContext().unregisterReceiver(congfigBroadcastReceiver);
		sendUpdateBroadcast(context, intent);
	}
}
