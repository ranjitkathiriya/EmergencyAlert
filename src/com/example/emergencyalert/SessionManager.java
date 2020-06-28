package com.example.emergencyalert;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.facebook.android.Facebook;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;
	SharedPreferences prefforfb;

	String accesstoken;
	public static int count = 0;
	// public static String[] preserve_list = new String[300];

	public static ArrayList<String> preserve_list = new ArrayList<String>();

	public ArrayList<String> getPreserve_list() {
		return preserve_list;
	}

	public void setPreserve_list(ArrayList<String> preserve_list) {
		this.preserve_list = preserve_list;
	}

	public static String logout_id = "0";
	// Editor for Shared preferences
	Editor editor;

	// saving fb credentials
	Editor editorfb;

	private static final String TOKEN = "access_token";
	private static final String EXPIRES = "expires_in";
	private static final String KEY = "facebook-credentials";

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "LoginPref";

	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";

	// Email address (make variable public to access from outside)
	public static final String KEY_EMAIL = "email";

	public static final String KEY_Password = "password";
	public static final String KEY_fname = "fname";
	public static final String KEY_lname = "lname";
	public static final String KEY_dp = "dp";

	SessionManager session;
	HashMap<String, String> user;

	// String account_type;
	// Constructor
	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
		editorfb = _context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
				.edit();
	}

	/**
	 * Create login session
	 * */
	public void createLoginSession(String email, String password, String fname,
			String lname, Bitmap dp) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);
		// Storing email in pref
		editor.putString(KEY_EMAIL, email);
		editor.putString(KEY_Password, password);
		editor.putString(KEY_fname, fname);
		editor.putString(KEY_lname, lname);
		editor.putString(KEY_dp, encodeTobase64(dp));
		// commit changes
		editor.commit();
	}

	private String encodeTobase64(Bitmap dp) {
		// TODO Auto-generated method stub
		Bitmap immage = dp;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immage.compress(Bitmap.CompressFormat.PNG, 10, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
		Log.d("Image Log:", imageEncoded);
		return imageEncoded;
	}

	public void createFbLoginSession(String email, String fname, String lname,
			Bitmap dp) {
		
		StaticData.loginTypeFb = true;

		// Storing login value as TRUE
		
		editor.putBoolean(IS_LOGIN, true);
		// Storing email in pref
		editor.putString(KEY_EMAIL, email);
		editor.putString(KEY_fname, fname);
		editor.putString(KEY_lname, lname);
		editor.putString(KEY_dp, encodeTobase64(dp));
		editor.commit();


	}

	/**
	 * Check login method wil check user login status If false it will redirect
	 * user to login page Else won't do anything
	 * */
	public void checkLogin() {
		// Check login status


		if (!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity
			Intent i = new Intent(_context, LoginActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Staring Login Activity
			_context.startActivity(i);

		} else {
			// Pass if user already login
			Intent i = new Intent(_context, SlidingMenuFragmentActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			_context.startActivity(i);
		}

	}

	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();

		// user email id
		user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
		user.put(KEY_Password, pref.getString(KEY_Password, null));
		user.put(KEY_fname, pref.getString(KEY_fname, null));
		user.put(KEY_lname, pref.getString(KEY_lname, null));
		user.put(KEY_dp, pref.getString(KEY_dp, null));

		// return user
		return user;
	}

	public boolean saveCredentials(Facebook facebook) {

		editorfb.putString(TOKEN, facebook.getAccessToken());
		editorfb.putLong(EXPIRES, facebook.getAccessExpires());

		accesstoken = facebook.getAccessToken();
		return editorfb.commit();
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser() {
		// Clearing all data from Shared Preferences
		try {

			// Yes button clicked, do something
			editor.clear();
			editor.commit();
			StaticData.loginTypeFb = false;

			editorfb.clear();
			editorfb.commit();
			// After logout redirect user to Loing
			// Activity
			Intent i = new Intent(_context, LoginActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			logout_id = "1";
			// Staring Login Activity
			_context.startActivity(i);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(IS_LOGIN, false);
	}
}
