/*******************************************************************************
 * Copyright 2009, 2010 Omnidroid - http://code.google.com/p/omnidroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.fsu.cs.contextprovider.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * The service creates monitors for System Events, and could later support third
 * party applications.
 */
public class EventMonitoringService extends Service {
	// Log tag
	private static final String TAG = EventMonitoringService.class.getSimpleName();

	// Internal binding to provide service bindings
	private final IBinder mBinder = new LocalBinder();

	private final SystemServiceEventMonitor MONITORS[] = { new PhoneStateMonitor(this), new LocationMonitor(this), new TimeMonitor(this), new NetworkStateMonitor(this) };

	// Keep track if already running or not
	private static boolean isAlreadyRunning = false;

	public class LocalBinder extends Binder {
		EventMonitoringService getService() {
			return EventMonitoringService.this;
		}
	}

	public static void startService(Context context) {
		/*
		 * I don't think the isAlreadyRunning check is really necessary as I
		 * think Android really only doesn't run the onCreate code twice anyway,
		 * but just to be sure.
		 */
		if (!isAlreadyRunning) {
			ComponentName service = context.startService(new Intent(context, EventMonitoringService.class));
			if (null == service) {
				Toast.makeText(context, "Failed to start Event Monitoring Service", Toast.LENGTH_LONG).show();
				Log.d(TAG, "EventMonitoringService did not start.");
			} else {
				Log.d(TAG, "Started EventMonitoringService.");
			}
		} else {
			// We're already running, don't start again.
			return;
		}
	}

	public static void stopService(Context context) {
		if (context.stopService(new Intent(context, EventMonitoringService.class))) {
			Log.d(TAG, "EventMonitoringService stopped");
		}
	}

	/**
	 * Actions performed on service initialization. EventMonitoringService
	 * performs initialization of each SystemServiceMonitor.
	 */
	@Override
	public void onCreate() {
		// Don't restart these monitors if we're already running.
		synchronized (this) {
			if (!isAlreadyRunning) {
				isAlreadyRunning = true;
			} else {
				return;
			}
		}

		// TODO(acase): Move this to OmnidroidManager or BCReceiver
		// Let the user know we're activating rules
		alertUserOnStartStop(true);

		// Start System Monitors
		for (SystemServiceEventMonitor monitor : MONITORS) {
			try {
				monitor.init();
				Log.d(TAG, monitor.getMonitorName() + ": Start\n");
			} catch (Exception e) {
				Log.d(TAG, monitor.getMonitorName() + " did not start.\nThe following error occurred: " + e + e.getMessage() + e.getStackTrace());
			}
		}
	}

	/**
	 * Actions performed on service shut down. EventMonitoringService performs
	 * the shut down of each SystemServiceMonitor.
	 */
	@Override
	public void onDestroy() {
		for (SystemServiceEventMonitor monitor : MONITORS) {
			try {
				monitor.stop();
				Log.d(TAG, monitor.getMonitorName() + " stopped");
			} catch (Exception e) {
				Log.d(TAG, monitor.getMonitorName() + " did not stop.\nThe following error occurred: " + e + e.getMessage() + e.getStackTrace());
			}
		}
		isAlreadyRunning = false;

		// Let the user know we're de-activating rules
		alertUserOnStartStop(false);

	}

	/**
	 * Send a Toast alert to the user about how many rules are being
	 * activated/deactivited.
	 * 
	 * @param enabling
	 *            - whether Omnidroid is being enabled or disabled
	 */
	private void alertUserOnStartStop(boolean enabled) {
		String messageText;
		if (enabled) {
			messageText = "Enabled";
		} else {
			messageText = "Disabled";
		}

		Toast.makeText(this, messageText, Toast.LENGTH_LONG).show();
		Log.d(TAG, messageText);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
