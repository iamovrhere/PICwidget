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
package com.ovrhere.android.picwidget.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Collection of utilities useful for bitmaps.
 * @author Jason J. 
 * @version 0.1.0-20140819
 */
public class BitmapUtil {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = BitmapUtil.class.getSimpleName();
	/** Used for debugging. */
	final static private boolean DEBUG = false;
	
	/** Converts bitmap into byte array
	 * @param bitmap The bitmap to convert
	 * @return The byte array of the bitmap (assumes png).
	 */
	public static byte[] bitmapToBytes(Bitmap bitmap){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		bitmap.recycle();
		return stream.toByteArray();
	}
	
	/** Scales down the bitmap. If smaller than the size given, 
	 * the same bitmap is returned.
	 * @param bitmap The bitmap to resize.
	 * @param pxSize The side of the largest side.
	 * @return A bitmap at most pxSize long/wide
	 */
	public static Bitmap scaleDownBitmap(Bitmap bitmap, int pxSize){
		int dstWidth =  bitmap.getWidth();
		int dstHeight = bitmap.getHeight();
		if (dstWidth > dstHeight){
			if (dstWidth <= pxSize){
				//if the same, do not resize.
				return bitmap;
			}
			dstHeight *= (float) ((float)pxSize/(float)dstWidth);//scale the height;
			dstWidth = pxSize;
		} else {
			if (dstHeight <= pxSize){
				return bitmap;
			}
			dstWidth*= (float) ((float)pxSize/(float)dstHeight);//scale the height;
			dstHeight  = pxSize;
		}
		return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
	}
	
	/**
	 * Reads (bitmap) file from internal and returns it's bitmap.
	 * @param context The currenct context.
	 * @param fileName The file name 
	 * @return The bitmap on success, <code>null</code> when file not found.
	 */
	public static Bitmap readBitmapFromInternal(Context context, 
			String fileName){
		try {
			InputStream in = context.openFileInput(fileName);
			return BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			if (DEBUG){
				e.printStackTrace();
			}
		}
		return null;
	}
}
