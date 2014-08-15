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
package com.ovrhere.android.pictureinfocard.widget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.ovrhere.android.pictureinfocard.widget.R;
import com.ovrhere.android.pictureinfocard.widget.prefs.PreferenceUtils;
import com.ovrhere.android.pictureinfocard.widget.remoteviews.AdapterViewFlipperWidgetService;
import com.ovrhere.android.pictureinfocard.widget.ui.PICWidgetConfigurationActivity;
import com.ovrhere.android.pictureinfocard.widget.utils.FileUtil;

/**
 * The {@link AppWidgetProvider} for the widget. 
 * Provides an interface for widget interactions (deletions, creations, updates, etc).
 *  
 * @author Jason J.
 * @version 0.4.0-20140815
 */
public class PictureInfoCardWidgetProvider extends AppWidgetProvider {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = 
			PictureInfoCardWidgetProvider.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
        final int SIZE = appWidgetIds.length;
        for (int i = 0; i < SIZE; i++) {
            int appWidgetId = appWidgetIds[i];
            SharedPreferences prefs = 
            		PreferenceUtils.getPreferences(context, appWidgetId);
            
            RemoteViews remoteViews = 
    				new RemoteViews( 
    						context.getPackageName(), 
    						R.layout.pic_info_card_appwidget);
            
            setConfigIntent(context, appWidgetId, remoteViews);
	        //pending intent complete.
	        
            configPicture(context, appWidgetId, remoteViews, prefs);
            configClock(context, appWidgetId, remoteViews, prefs);
	        configViewFlipper(context, appWidgetId, remoteViews);
	        	        
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets the pending launch intent for #PICWidgetConfigurationActivity 
	 * @param context Current context
	 * @param appWidgetId The widget id of the configuration to load 
	 * @param remoteViews The remoteView to attach the intent to
	 */
	static private void setConfigIntent(Context context, int appWidgetId,
			RemoteViews remoteViews) {
		Intent launchIntent = new Intent(context, PICWidgetConfigurationActivity.class); 
		launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		PendingIntent pendingIntent = 
				PendingIntent.getActivity(	context,
						//unique request code (or uri) required for unique pendingintent 
											appWidgetId /* requestCode */, 
											launchIntent, 
											0 /* flags */);
		

		remoteViews.setOnClickPendingIntent(R.id.com_ovrhere_picwidget_widget_layout, 
											pendingIntent);
	}
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
		boolean displayPicture = 
				prefs.getBoolean(key,false);
		
		final int id = R.id.com_ovrhere_picwidget_widget_layout_imgContainer;
		if (displayPicture){
			Bitmap bitmap = FileUtil.readBitmapFromInternal(context, fileName);
			if (bitmap != null){
				remoteViews.setImageViewBitmap(
							R.id.com_ovrhere_picwidget_widget_img_mainPicture, 
							bitmap);
				remoteViews.setViewVisibility(id, View.VISIBLE);
				return;
			}
			//we must inform the widget that the image is gone.
			prefs	.edit()
					.putBoolean(key, false)
					.commit();
		} 
		//if either the image is gone, or boolean false.
		remoteViews.setViewVisibility(id, View.GONE);
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
}
