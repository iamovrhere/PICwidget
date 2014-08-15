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
import android.net.Uri;
import android.widget.RemoteViews;

import com.ovrhere.android.pictureinfocard.widget.R;
import com.ovrhere.android.pictureinfocard.widget.prefs.PreferenceUtils;
import com.ovrhere.android.pictureinfocard.widget.remoteviews.AdapterViewFlipperWidgetService;
import com.ovrhere.android.pictureinfocard.widget.ui.PICWidgetConfigurationActivity;

/**
 * The {@link AppWidgetProvider} for the widget. 
 * Provides an interface for widget interactions (deletions, creations, updates, etc).
 *  
 * @author Jason J.
 * @version 0.3.0-20140812
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
        
            RemoteViews remoteViews = 
    				new RemoteViews( context.getPackageName(), R.layout.pic_info_card_appwidget);
            
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
	        //pending intent complete.
	        
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
	        		R.id.com_ovrhere_picwidget_widget_text_noText);
	        
	        
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
        }
	}
}
