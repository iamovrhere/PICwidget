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

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * The service to retrieve factories for the AdapterViewFlipper.
 * @author Jason J.
 * @version 0.1.0-20140811
 */
public class AdapterViewFlipperWidgetService extends RemoteViewsService {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = 
			AdapterViewFlipperWidgetService.class.getSimpleName();
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		//Add switches/Class Registration in there is for supporting multiple factories.
		return new InfoViewFlipperRemoteViewsFactory(this.getApplicationContext(), intent);		
	}

}
