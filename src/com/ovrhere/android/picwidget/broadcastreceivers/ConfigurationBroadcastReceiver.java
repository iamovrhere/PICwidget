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
package com.ovrhere.android.picwidget.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * <p>Broadcaster receiver for intent {@link Intent#ACTION_CONFIGURATION_CHANGED }.
 * Note that this class must be registered via
 * {@link Context#registerReceiver(BroadcastReceiver, android.content.IntentFilter)}
 * in the form:</p>
 * <p><code>
 * &nbsp;&nbsp;&nbsp;context.registerReceiver(new ConfigurationBroadcastReceiver(), 
 * new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
 * </code>
 * </p>
 * <p>Remember to use {@link Context#unregisterReceiver(BroadcastReceiver)} 
 * when finish with the broadcast (such as in 
 * {@link OnConfigChangeListener#onConfigChange(ConfigurationBroadcastReceiver, Context, Intent)}).
 * </p><p>
 * Requires the use of {@link #setOnConfigChangeListener(OnConfigChangeListener)}
 * to be useful.</p>
 * @author Jason J. 
 * @version 0.1.0-20140819
 */
public class ConfigurationBroadcastReceiver extends BroadcastReceiver {
	
	/** The listener to provide actions. */
	private OnConfigChangeListener mListener = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)){
			if (mListener != null){
				mListener.onConfigChange(this, context, intent);
			}
		}
	}
	/** Sets the single {@link OnConfigChangeListener} listener.
	 * @param listener The listener.	 */
	public void setOnConfigChangeListener
		(OnConfigChangeListener listener){
		this.mListener = listener;
	}
	/** The Listener used to response to configuration changes. 
	 * @author Jason J. 
	 * @version 0.1.0-20140819
	 */
	public interface OnConfigChangeListener {
		/** Sent when configuration changes.
		 * @param congfigBroadcastReceiver The spawning receiver
		 * @param context Context from onReceive
		 * @param intent Intent from onReceive
		 */
		public void onConfigChange(
				ConfigurationBroadcastReceiver congfigBroadcastReceiver, 
				Context context, Intent intent);
	}

}
