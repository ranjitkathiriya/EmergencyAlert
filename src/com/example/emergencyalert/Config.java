package com.example.emergencyalert;

public interface Config {

	// CONSTANTS
	// static final String YOUR_SERVER_URL =
	// "http://materialbag.site88.net/register.php";

	static final String YOUR_SERVER_URL = "http://192.168.1.133:8080/emergency/gcmregister.php";

	// YOUR_SERVER_URL : Server url where you have placed your server files
	// Google project id
	static final String GOOGLE_SENDER_ID = "21244102361"; // Place here your
															// Google project id

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCM Android Example";

	static final String DISPLAY_MESSAGE_ACTION = "com.androidexample.gcm.DISPLAY_MESSAGE";

	static final String EXTRA_MESSAGE = "message";

}
