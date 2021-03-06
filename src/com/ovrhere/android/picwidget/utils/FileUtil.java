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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/** File utility for copying and viewing files.
 * @author Jason J.
 * @version 0.3.0-20140819
 */
public class FileUtil {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = FileUtil.class.getSimpleName();
	/** Used for debugging. */
	final static private boolean DEBUG = false;

	/**
	 * Writes file from a path to a destination
	 * @param context The current context
	 * @param inputStream The input stream file to write
	 * @param destName The destination name to use. 
	 * @throws IOException Re-throws exception caused in writing.
	 */
	public static void writeFileToInternal(Context context, 
			InputStream inputStream, String destName) throws IOException{
		try {
			OutputStream out = 
					context.openFileOutput(destName, Context.MODE_PRIVATE);
			// Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len = 1;
	        while ((len = inputStream.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        inputStream.close();
	        out.close();
		} catch (IOException e){
			if (DEBUG){
				Log.e(CLASS_NAME, "For stream: \""+inputStream+"\"; "+e);
				e.printStackTrace();
			}
			throw e;
		}         
	}
	
	/**
	 * Copies file from a path to a destination
	 * @param context The current context
	 * @param sourcePath The source file to copy.
	 * @param destName The destination name to use. 
	 * @return <code>true</code> for success, <code>false</code> for failure.
	 */
	public static boolean copyFileToInternal(Context context, 
			String sourcePath, String destName){
		try {
			InputStream in = new FileInputStream(sourcePath);
			writeFileToInternal(context, in, destName);
	        in.close();
		} catch (FileNotFoundException e){
			if (DEBUG){
				Log.e(CLASS_NAME, "For file: \""+sourcePath+"\"; "+e);
				e.printStackTrace();
			}
			return false;
		} catch (IOException e){
			if (DEBUG){
				Log.e(CLASS_NAME, "For file: \""+sourcePath+"\"; "+e);
				e.printStackTrace();
			}
			return false;
		}         
		return true;
	}
	/**
	 * See {@link BitmapUtil}.
	 * The image complement to {@link #copyFileToInternal(Context, String, String, int)}.
	 * Reads (bitmap) file from internal and returns it's bitmap.
	 * @param context The currenct context.
	 * @param fileName The file name 
	 * @return The bitmap on success, <code>null</code> when file not found.
	 */
	@Deprecated
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
	/** Deletes the file from internal. Convenience function for 
	 * {@link Context#deleteFile(String)}.
	 * @param context The current context.
	 * @param fileName The file to delete
	 * @return <code>true</code> if successfully deleted, <code>false</code> otherwise.
	 */
	public static boolean deleteFromInternal(Context context, String fileName){
		return context.deleteFile(fileName);
	}
	
	//TODO define more graceful behaviour
	/** Gets the file path from the uri.
	 * May or may not throw an exception if used incorrectly.
	 * @param cameraData The data to process (such as that from onActivityResult())
	 * @param context The current context
	 * @return The path of the image.
	 */
	public static String getPath(Context context, Uri uri){
		String[] filePathColumn = { MediaStore.Images.Media.DATA }; 
		Cursor cursor = context	.getContentResolver()
								.query(uri,filePathColumn, null, null, null); 
		cursor.moveToFirst();
		//TODO get path column by more stable means
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]); 
		String path = cursor.getString(columnIndex); 
		cursor.close();		
		return path;
	}
	
}
