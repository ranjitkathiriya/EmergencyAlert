package com.example.emergencyalert;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StaticData {

	public static String SERVER_URL = "http://192.168.1.133:8080/emergency/";
	public static String SERVER_IMAGE_URL = SERVER_URL + "Images/";
	public static Boolean resetFlag=false;
	public static Boolean resetMyFriendsFlag=false;
	public static Boolean resetPendingRequestFlag=false;
	
	public static Boolean notifyEmergencyHelper=false;
	public static Boolean notifyFriendRequest=false;
	public static Boolean loginTypeFb=false;

	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	public static void setFont(ViewGroup group, Typeface font) {
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView || v instanceof EditText
					|| v instanceof Button) {
				((TextView) v).setTypeface(font);
			} else if (v instanceof ViewGroup)
				setFont((ViewGroup) v, font);
		}
	}
}
