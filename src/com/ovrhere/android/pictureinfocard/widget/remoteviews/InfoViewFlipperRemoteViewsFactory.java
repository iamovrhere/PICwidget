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
package com.ovrhere.android.pictureinfocard.widget.remoteviews;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.ovrhere.android.pictureinfocard.widget.R;
import com.ovrhere.android.pictureinfocard.widget.prefs.PreferenceUtils;

/**
 * The factory to produce remote views for the information view text.
 * @author Jason J.
 * @version 0.1.0-20140812
 */
public class InfoViewFlipperRemoteViewsFactory implements RemoteViewsFactory {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = 
			InfoViewFlipperRemoteViewsFactory.class.getSimpleName();
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The list of strings to show through the flipper. */
	private List<String> mInfoText = new ArrayList<String>();

	/** The current context of the widget. */
	private Context mContext = null;
	/** The id of the widget views being produced. */
	private int mAppWidgetId = -1;
	
	public InfoViewFlipperRemoteViewsFactory(Context context, Intent intent) {
		 mContext = context;
		 mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
	            AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	
	@Override
	public void onCreate() {
		fetchData();		
	}


	@Override
	public void onDataSetChanged() {
		mInfoText.clear();
		fetchData();
	}

	@Override
	public void onDestroy() {
		// Nothing to do for now
	}

	@Override
	public int getCount() {
		return mInfoText.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {		
		String info = mInfoText.get(position);
		if (null != info){
			RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_info_text);
			rv.setTextViewText(R.id.com_ovrhere_picwidget_infotext_text, info);
			
			return rv;
		}
		return null;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Fetches data fresh from the source and stores it in #mInfoText */
	private void fetchData() {
		SharedPreferences prefs = PreferenceUtils.getPreferences(mContext, mAppWidgetId);
		Resources r = mContext.getResources();
		boolean pictureDisplayed = 
				prefs.getBoolean(
						r.getString(R.string.com_ovrhere_picwidget_pref_KEY_DISPLAY_PICTURE), 
						false);
		String timeValue = 
				prefs.getString(
						r.getString(R.string.com_ovrhere_picwidget_pref_KEY_TIME_DISPLAY), 
						"");
		//if not none, time is displayed.
		boolean timeDisplayed = timeValue.equals(
					r.getString(R.string.com_ovrhere_picwidget_pref_VALUE_TIME_DISPLAY_NONE)
				) == false;
		String fullInfoText = 
				prefs.getString(
						r.getString(R.string.com_ovrhere_picwidget_pref_KEY_INFO_CONTENT), 
						"");
		
		clipInfoText(pictureDisplayed, timeDisplayed, fullInfoText);
	}
	
	/**
	 * Clips the text into smaller pieces to be displayed as multiple views.
	 * @param pictureDisplayed Whether or not the picture is currently being displayed.
	 * @param timeDisplayed Whether or not the time is being displayed.
	 * @param fullInfoText The full text to clip into smaller pieces.
	 */
	private void clipInfoText(boolean pictureDisplayed, boolean timeDisplayed,
			String fullInfoText) {
		Resources r = mContext.getResources();
		int maxLines = 
				r.getInteger(R.integer.com_ovrhere_picwidget_widget_text_mainText_lineLimit);
		int charPerLine = 
				r.getInteger(R.integer.com_ovhere_picwidget_widget_infoText_charPerLine_max);
		if (pictureDisplayed && timeDisplayed){
			charPerLine 
				= r.getInteger(R.integer.com_ovhere_picwidget_widget_infoText_charPerLine_min);
		} else if (pictureDisplayed || timeDisplayed){
			charPerLine 
				= r.getInteger(R.integer.com_ovhere_picwidget_widget_infoText_charPerLine_mid);
		} 
		//approx limit of character's per block.
		int blockLimit = maxLines * charPerLine;
		
		splitInfoText(fullInfoText, charPerLine, blockLimit);
	}

	/**
	 * Splits the text into blocks based upon indents, spaces and tabs.
	 * @param fullInfoText The text to split.
	 * @param charPerLine The character limit per line of each block
	 * @param blockLimit The character limit of each block
	 */
	private void splitInfoText(String fullInfoText, int charPerLine,
			int blockLimit) {
		while (fullInfoText.length() > 0){
			String currBlock = "";
			int currBlockLimit = blockLimit;
			
			
			while (fullInfoText.contains("\n") && currBlockLimit > 0){
				int indentIndex_plus = fullInfoText.indexOf("\n") + 1;
				//add the string, including indent.
				currBlock +=  fullInfoText.substring(0, indentIndex_plus);
				fullInfoText = fullInfoText.substring(indentIndex_plus);
				currBlockLimit -= charPerLine; //new line, so remove 1 line
			}
						
			if (fullInfoText.length() <= currBlockLimit){
				//the remaining string will fit.
				currBlock += fullInfoText;
				fullInfoText = "";
			} else if (currBlockLimit > 0){ //no new lines remaining 
				currBlock += fullInfoText.substring(0, currBlockLimit +1);
				//check one past the limit, find last space or tab.				
				if (currBlock.lastIndexOf(" ") > 0){
					currBlockLimit = currBlock.lastIndexOf(" ");
				} else if (currBlock.lastIndexOf("\t") > 0){
					currBlockLimit = currBlock.lastIndexOf("\t");
				}
				
				//re-splice the string.
				currBlock += fullInfoText.substring(0, currBlockLimit);
				fullInfoText = fullInfoText.substring(currBlockLimit);
			}
			//add block to list.
			mInfoText.add(currBlock);
			
		}
		return;
	}
}
