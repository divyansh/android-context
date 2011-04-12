package edu.fsu.cs.contextprovider.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsService extends AbstractContextService {

	private static final String TAG = "SMS Service";
	private static final boolean DEBUG = true;

	private static final int MAX_LENGTH = 30;
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_SMS_RECEIVED.equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();

				if (bundle != null) {
					Object[] pdu = (Object[]) bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdu.length];

					for (int i = 0; i < pdu.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdu[i]);
					}

					for (SmsMessage message : messages) {
						String msg = message.getMessageBody();
						// String msgSender =
						// message.getDisplayOriginatingAddress(); // not need
						// for sender in this example

						if (msg.length() > MAX_LENGTH)
							msg = msg.substring(0, MAX_LENGTH);

						if (DEBUG)
							Log.d(TAG, "send: " + msg);
						// Amarino.sendDataFromPlugin(context, pluginId, msg);
					}
				}
			}

		}
	};

	@Override
	public void init() {
		if (!serviceEnabled) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//			pluginId = prefs.getInt(SmsEditActivity.KEY_PLUGIN_ID, -1);

			IntentFilter filter = new IntentFilter(ACTION_SMS_RECEIVED);
			registerReceiver(receiver, filter);

			// don't forget to set plug-in enabled if everything was initialized
			// fine
			serviceEnabled = true;
		}
	}

	@Override
	public void onDestroy() {
		if (serviceEnabled) {
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}

	@Override
	public String getTAG() {
		return TAG;
	}

}
