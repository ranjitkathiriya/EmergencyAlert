package com.example.emergencyalert;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.emergency.EmergencyDescription;
import com.example.friend.PendingFriendProfile;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	// Variable for get Current user Email
	SessionManager session;
	String useremail;
	static String type;

	private static final String TAG = "GCMIntentService";

	private Controller aController = null;

	public GCMIntentService() {
		// Call extended class Constructor GCMBaseIntentService
		super(Config.GOOGLE_SENDER_ID);
	}

	/**
	 * Method called on device registered
	 **/
	@Override
	protected void onRegistered(Context context, String registrationId) {

		// Get Global Controller Class object (see application tag in
		// AndroidManifest.xml)
		if (aController == null)
			aController = (Controller) getApplicationContext();

		Log.i(TAG, "Device registered: regId = " + registrationId);
		// aController.displayMessageOnScreen(context,
		// "Your device registred with GCM");
		// Log.d("NAME", MainActivity.name);

		session = new SessionManager(getApplicationContext());
		HashMap<String, String> user = session.getUserDetails();
		useremail = user.get(SessionManager.KEY_EMAIL);

		aController.register(context, useremail, registrationId);
	}

	/**
	 * Method called on device unregistred
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		if (aController == null)
			aController = (Controller) getApplicationContext();
		Log.i(TAG, "Device unregistered");
		// aController.displayMessageOnScreen(context,
		// getString(R.string.gcm_unregistered));
		aController.unregister(context, registrationId);
	}

	/**
	 * Method called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {

		if (aController == null)
			aController = (Controller) getApplicationContext();

		Log.i(TAG, "Received deleted messages notification");
		// String message = getString(R.string.gcm_deleted, total);
		// aController.displayMessageOnScreen(context, message);
		// notifies user
		// generateNotification(context, message);
	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {

		if (aController == null)
			aController = (Controller) getApplicationContext();

		Log.i(TAG, "Received error: " + errorId);
		// aController.displayMessageOnScreen(context,
		// getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {

		if (aController == null)
			aController = (Controller) getApplicationContext();

		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		// aController.displayMessageOnScreen(context,
		// getString(R.string.gcm_recoverable_error,
		// errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Method called on Receiving a new message from GCM server
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {

		Log.e("notification", "noty ::::: "
				+ intent.getExtras().getSerializable("SubjectCode") + "  "
				+ intent.getExtras().getSerializable("Subject"));

		if (aController == null)
			aController = (Controller) getApplicationContext();

		Log.i(TAG, "Received message");
		type = intent.getExtras().getString("SubjectCode");

		// For friend Request

		if (type.equals("4")) {
			String message = intent.getExtras().getString("Subject");
			String FRcreator_fullname = intent.getExtras().getString(
					"creator_fullname");
			String FRFriendRequest = intent.getExtras().getString(
					"FriendRequest");
			String FRprofile_pic = intent.getExtras().getString("profile_pic");
			String FRfname = intent.getExtras().getString("fname");
			String FRlname = intent.getExtras().getString("lname");
			String FRemail = intent.getExtras().getString("email");

			frgenerateNotification(context, message, FRcreator_fullname,
					FRFriendRequest, FRprofile_pic, FRfname, FRlname, FRemail);

		} else if (type.equals("3")) {
			String message = intent.getExtras().getString("Subject");

			int alert_id = Integer.parseInt(intent.getExtras().getString(
					"alert_id"));

			String creator_fullname = intent.getExtras().getString(
					"creator_fullname");

			generateNotification(context, alert_id, message, creator_fullname);
		}

		else if (type.equals("5") || type.equals("6")) {
			String title = intent.getExtras().getString("Subject");

			int alert_id = Integer.parseInt(intent.getExtras().getString(
					"alert_id"));

			String full_name = intent.getExtras().getString("full_name");
			Log.i("full_name", full_name);

			helper_alert_generateNotification(context, alert_id, title,
					full_name);

		}

	}

	/**
	 * Create a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, int alert_id,
			String message, String creator_fullname) {
		int icon = R.drawable.emergency_icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = creator_fullname;

		StaticData.notifyEmergencyHelper = true;

		Intent notificationIntent = new Intent(context,
				EmergencyDescription.class);
		Log.i("Alert id in generate notification", alert_id + "");
		notificationIntent.putExtra("alert_id", alert_id);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

	}

	/**
	 * Create a notification for Friend Request
	 */
	private static void frgenerateNotification(Context context, String message,
			String FRcreator_fullname, String FRFriendRequest,
			String FRprofile_pic, String FRfname, String FRlname, String FRemail) {
		int icon = R.drawable.emergency_icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = FRcreator_fullname;

		HashMap<String, String> send = new HashMap<String, String>();
		send.put("fname", FRfname);
		send.put("lname", FRlname);
		send.put("email", FRemail);

		StaticData.notifyFriendRequest = true;

		String dp_path = FRprofile_pic;
		// Remove When service on
		// server......................#######################
		send.put("image", StaticData.SERVER_IMAGE_URL + dp_path);

		Intent notificationIntent = new Intent(context,
				PendingFriendProfile.class);

		notificationIntent.putExtra("from_addfriend", send);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification
				.setLatestEventInfo(context, title, FRFriendRequest, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

	}

	/**
	 * Create a notification to inform the user that server has sent a message.
	 */
	private static void helper_alert_generateNotification(Context context,
			int alert_id, String title, String full_name) {
		int icon = R.drawable.emergency_icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, title, when);
		String message = null;
		if (type.equals("5")) {
			message = full_name + " goes as helper";
		} else if (type.equals("6")) {
			message = full_name + " want to help you";
		}
		Intent notificationIntent = new Intent(context,
				EmergencyDescription.class);

		StaticData.notifyEmergencyHelper = true;

		Log.i("Alert id in generate notification", alert_id + "");
		notificationIntent.putExtra("alert_id", alert_id);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

	}
}
