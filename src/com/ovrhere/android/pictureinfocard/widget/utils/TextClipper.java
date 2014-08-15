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
package com.ovrhere.android.pictureinfocard.widget.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Paint;
import android.util.Log;


/** 
 * Breaks text into lines.
 * @author Jason J.
 * @version 0.1.0-20140813
 */
public class TextClipper {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = TextClipper.class.getSimpleName();
	/** Whether or not to debug. */
	final static private boolean DEBUG = false;
	/** The pattern for character breaks in lines. */
	final static private Pattern CHARACTER_BREAKS = Pattern.compile("\\W\\w+?$");
	
	
	/** Shows the index to clip text at for a given line width.
	 * @param text The text to test.
	 * @param textSize The text size given in px 
	 * @param targetWidth The targetWidth to end at given in px
	 * @return The number of characters that will fit on a given line.
	 */
	static public int clipTextCount(String text, float textSize, float targetWidth){
		Paint paintHandle = new Paint();
		paintHandle.setTextSize(textSize);
		// http://stackoverflow.com/questions/14280046/understanding-paint-measuretext
		paintHandle.setSubpixelText(true); //required for i, w, l
		return _breakText(text, targetWidth, paintHandle); 
	}

	
	
	/** Takes a string and clips the text into chunks based on the 
	 * given width and line count.
	 * @param text The text to clip. 
	 * @param textSize The text size given in px
	 * @param targetWidth The target width given in px
	 * @param lineCount The number of lines (per block).
	 * @return The text blocks that <code>text</code> has been clipped into.
	 */
	static public List<String> clipText(String text, float textSize, 
			float targetWidth, int lineCount){
		List<String > list = new ArrayList<String>();
		Paint paint = new Paint();
		paint.setTextSize(textSize);
		paint.setSubpixelText(true); //required for i, w, l
		
		while (text.length() > 0){
			String textBlock = ""; //the text to concat.
			if (DEBUG){
				Log.d(CLASS_NAME, "block index: "+list.size());
				Log.d(CLASS_NAME, "text: [["+text+"]]");
			}
			for(int count = 0; count < lineCount && !text.isEmpty(); count++){
				int clipIndex = _breakText(text, targetWidth, paint);
				String line = text.substring(0, clipIndex);
				int breakIndex = checkForBreakChars(line)+1;
				if (DEBUG){
					Log.d(CLASS_NAME, "count: "+count);
					Log.d(CLASS_NAME, "breakIndex: "+breakIndex);
				}
				if (breakIndex > 0){
					clipIndex = breakIndex;
				}
				textBlock += text.substring(0, clipIndex);
				text = text.substring(clipIndex);
				if (DEBUG){
					Log.d(CLASS_NAME, "text-block: "+textBlock);
					Log.d(CLASS_NAME, "text: [["+text+"]]");
				}
			}
			list.add(textBlock.trim());
		}
		return list;
	}

	

	/** Checks for the last character that could break the line in a readable way.
	 * @param line The line to test for breaking characters.
	 * @return The new index or -1 if none are found.
	 */
	private static int checkForBreakChars(String line) {
		Matcher matcher = CHARACTER_BREAKS.matcher(line);
		int index = line.indexOf("\n");
		if (index >= 0){
			if (DEBUG){
				Log.d(CLASS_NAME, "newline found at: "+index);
			}
			return index;
		} else if (matcher.find()){
			index = matcher.start();
		}
		return index;		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Private Utility methods 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Break text based upon given paint object.
	 * @param text The text to break.
	 * @param targetWidth The target width
	 * @param paint The preset paint handle (with text size + font set).
	 * @return The number of characters that will fit.
	 */
	private static int _breakText(String text, float targetWidth,
			Paint paint) {
		// See: https://android-review.googlesource.com/#/c/48690/
		return paint.breakText(text, true, targetWidth, null);
	}
}
