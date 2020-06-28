package com.example.emergencyalert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parser.JSONParser;
import com.example.parser.RestClient;
import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.gcm.GCMRegistrar;

public class LoginActivity extends Activity implements OnClickListener {

	private Facebook mFacebook;
	private static final String[] PERMISSIONS = new String[] {
			"publish_stream", "email", "publish_actions" };

	private static final String TOKEN = "access_token";
	private static final String EXPIRES = "expires_in";
	private static final String KEY = "facebook-credentials";

	EditText edt_email;
	EditText edt_password;

	TextView btn_login;

	// Variable for Getting address
	GPSTracker gps;
	double latitude = 0.0;
	double longitude = 0.0;

	Button btn_signup;
	Button btn_loginwithfb;

	String user_email;
	String user_password;
	String Response_code;

	public static String APP_ID = "";

	String fname, lname, id, picture;

	public String Response_code1;
	public String login_result1;

	public String Response_msg;

	public static String email1;
	String accesstoken;
	SharedPreferences pref;
	Editor editor;
	int PRIVATE_MODE = 0;
	private static final String PREF_NAME = "Username";

	private Pattern pattern;
	private Matcher matcher;
	private String EMAIL_PATTERN;
	CheckBox cb_rememberme;
	TextView txt_forgetme;
	LinearLayout layout_login;
	int remember_check = 0;
	String login_result;
	SharedPreferences sh_Pref;
	Editor toEdit;
	SessionManager session;

	JSONObject jobj, jobj1;

	// Forget-Password Data
	EditText edt_forgetemail;
	Button btn_okey;
	Button btn_cancel;
	public String forget_email;

	int FLAG_LOGIN_FORGET_PASSWORD = 0;
	String url;

	// for session manager
	String picture_path, fn, ln, photo;
	static Bitmap map;

	// GCM data
	Controller aController;
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	Typeface USERNAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);
		map = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_user);
		pref = this.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
		setContent();
		StaticData.setFont(layout_login, USERNAME);

		txt_forgetme.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		btn_loginwithfb.setOnClickListener(this);
		btn_signup.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	private void setContent() {
		// TODO Auto-generated method stub
		session = new SessionManager(getApplicationContext());

		APP_ID = this.getString(R.string.FB_App_ID);

		mFacebook = new Facebook(APP_ID);
		restoreCredentials(mFacebook);
		USERNAME = Typeface.createFromAsset(getAssets(), "Fonts/USERNAME.TTF");
		edt_email = (EditText) findViewById(R.id.edt_email);
		edt_password = (EditText) findViewById(R.id.edt_password);

		cb_rememberme = (CheckBox) findViewById(R.id.cb_rememberme);

		txt_forgetme = (TextView) findViewById(R.id.txt_forget);
		layout_login = (LinearLayout) findViewById(R.id.layout_login);

		btn_login = (Button) findViewById(R.id.btn_login);
		btn_loginwithfb = (Button) findViewById(R.id.btn_loginwithfb);
		btn_signup = (Button) findViewById(R.id.btn_signup);

	}
	
	@SuppressWarnings("deprecation")
	public boolean restoreCredentials(Facebook facebook) {
		SharedPreferences sharedPreferences = getApplicationContext()
				.getSharedPreferences(KEY, Context.MODE_PRIVATE);
		facebook.setAccessToken(sharedPreferences.getString(TOKEN, null));
		facebook.setAccessExpires(sharedPreferences.getLong(EXPIRES, 0));
		return facebook.isSessionValid();
	}

	private void SearchUser() {
		// TODO Auto-generated method stub

		try {

			EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
			pattern = Pattern.compile(EMAIL_PATTERN);
			matcher = pattern.matcher(user_email);
			Log.i("Email matcher", "" + matcher.matches());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (user_email.equals("")) {
			edt_email.setError("Enter Email");

		} else if (!matcher.matches()) {
			edt_email.setError("Enter valid email.");
		}

		else if (user_password.equals("")) {
			edt_password.setError("Enter Password");
		} else {
			if (StaticData.isNetworkAvailable(getApplicationContext())) {

				new SearchUserOperation().execute("");

			} else {
				Toast.makeText(getApplicationContext(),
						"Internet not connected", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class SearchUserOperation extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			postData();
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				result();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void postData() {
		// TODO Auto-generated method stub

		String result = "";
		try {
			String link = StaticData.SERVER_URL;
			JSONObject json = new JSONObject();
			if (FLAG_LOGIN_FORGET_PASSWORD == 1) {
				json.put("email", forget_email);
				url = link + "forgot_password.php";

			} else {
				json.put("email", user_email);
				json.put("pass", user_password);
				url = link + "login.php";

			}

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);

			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(json.toString().getBytes(
					"UTF8")));

			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			// request.setHeader("json", json.toString());

			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need

			if (entity != null) {
				InputStream instream = entity.getContent();

				result = RestClient.convertStreamToString(instream);
				Log.i("Read from server", result);
			}

		} catch (Exception t) {
			Log.e("postData", t.toString());
		}

		try {
			Log.i("result", result);
			Response_code = result;
			Log.e("Result....", "...." + Response_code);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Result", "...." + e.toString());
		}

		try {
			jobj = new JSONObject(Response_code);
			login_result = jobj.getString("ResponseCode");
			// Response_msg = jobj.getString("ResponseMsg");
			Log.e("Result", "...." + login_result);
			// JSONObject jobj1 = new JSONObject("userdetail");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.w("JSONException", e.toString());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			Log.w("null", e.toString());
		}

	}

	private void result() {
		if (FLAG_LOGIN_FORGET_PASSWORD == 1) {
			if (login_result.equals("1")) {
				Toast.makeText(getApplicationContext(), "Send Successfully",
						Toast.LENGTH_SHORT).show();
				FLAG_LOGIN_FORGET_PASSWORD = 0;
			} else {
				Toast.makeText(getApplicationContext(), "Login Failed",
						Toast.LENGTH_SHORT).show();
				FLAG_LOGIN_FORGET_PASSWORD = 0;

			}
		} else {
			if (login_result.equals("1")) {
				JSONArray name = null;
				try {
					name = jobj.getJSONArray("userdetail");
					for (int i = 0; i < name.length(); i++) {
						JSONObject c = name.getJSONObject(i);
						fn = c.getString("fname");
						ln = c.getString("lname");

						Log.i("NAMe", fn + ln);

						picture_path = c.getString("profile_pic");
						String link = StaticData.SERVER_IMAGE_URL;

						photo = link + picture_path;
						Log.d("PHOTO", photo);

						new DPinSession().execute("");
						// map = getBitmapFromURL(photo);
						Log.d("BITmap", "complit");
					}
				} catch (NullPointerException e) {

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// For store user's email and password
				if (cb_rememberme.isChecked()) {
					try {

						// session.createLoginSession(user_email,
						// user_password);
//						Toast.makeText(getApplicationContext(),
//								"Details are saved", Toast.LENGTH_SHORT).show();
						// finish();
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Toast.makeText(getApplicationContext(),
//								"Details are not saved", Toast.LENGTH_SHORT)
//								.show();
						e.printStackTrace();
					}

				}
				// Registration on GCM
				try {
					// Use Facebook email Variable 'email1' see line number 754
					email1 = user_email;
					gcmRegister();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Toast.makeText(getApplicationContext(), "Login Failed",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class DPinSession extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			getBitmapFromURL(photo);
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {
				Log.d("Login session", user_email + "   " + user_password + " "
						+ fn + " " + ln + " " + photo);
				session.createLoginSession(user_email, user_password, fn, ln,
						map);

				Intent i = new Intent(LoginActivity.this,
						SlidingMenuFragmentActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
				finish();

				Toast.makeText(getApplicationContext(), "Login Successfully",
						Toast.LENGTH_SHORT).show();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void getBitmapFromURL(String src) {
		try {
			Log.d("in Bitmp", "on");
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			map = BitmapFactory.decodeStream(input);
			Log.d("in Bitmp", "off");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.txt_forget:

			final Dialog dialog = new Dialog(LoginActivity.this);
			dialog.getWindow();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.login_forget_password);
			dialog.show();

			Button btn_yes = (Button) dialog.findViewById(R.id.btn_okey);
			btn_yes.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					edt_forgetemail = (EditText) dialog
							.findViewById(R.id.edt_forgetemail);
					forget_email = edt_forgetemail.getText().toString();

					try {
						EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
						pattern = Pattern.compile(EMAIL_PATTERN);
						matcher = pattern.matcher(forget_email);

						if (forget_email.equals("")) {
							Toast.makeText(v.getContext(), "Enter email",
									Toast.LENGTH_SHORT).show();
						} else if (!matcher.matches()) {
							Toast.makeText(v.getContext(), "Enter valid email",
									Toast.LENGTH_SHORT).show();
						} else {
							if (StaticData
									.isNetworkAvailable(getApplicationContext())) {
								FLAG_LOGIN_FORGET_PASSWORD = 1;
								Toast.makeText(v.getContext(),
										"send successfully", Toast.LENGTH_SHORT)
										.show();
								new SearchUserOperation().execute("");

							} else {
								Toast.makeText(getApplicationContext(),
										"Internet not connected",
										Toast.LENGTH_SHORT).show();
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			});
			Button btn_no = (Button) dialog.findViewById(R.id.btn_cancel);
			btn_no.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub

					dialog.dismiss();

				}
			});

			break;
		case R.id.btn_login:
			try {
				user_email = edt_email.getText().toString();
				user_password = edt_password.getText().toString();
				cb_rememberme = (CheckBox) findViewById(R.id.cb_rememberme);
				SearchUser();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btn_signup:
			intent = new Intent(LoginActivity.this, RegisterActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.btn_loginwithfb:
			getlocation();
			break;

		default:
			break;
		}
	}

	private void getlocation() {
		// TODO Auto-generated method stub
		// check if GPS enabled
		try {

			gps = new GPSTracker(LoginActivity.this);
			Log.d("LOgggg", "inCurrentlocation");

			// check if GPS enabled
			if (gps.canGetLocation()) {

				latitude = gps.getLatitude();
				longitude = gps.getLongitude();

				Log.i("latitude", "" + latitude);
				Log.i("longitude", "" + longitude);

				new getLoc().execute("");

			} else {
				gps.showSettingsAlert();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class getLoc extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage("Getting Loaction...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			for (int i = 0; i < 7; i++) {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (longitude > 0.0) {
					break;
				}
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				mFacebook.authorize(LoginActivity.this, PERMISSIONS,
						Facebook.FORCE_DIALOG_AUTH, new LoginDialogListener());

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {

			session.saveCredentials(mFacebook);

			getEmail();

		}

		public void onCancel() {
			// EMNOTE error message regarding Facebook Cancellation
			Toast.makeText(getApplicationContext(), "Login Cancel",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "Login Failed",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "Error on facebook Login",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void getEmail() {
		// TODO Auto-generated method stub

		new LoginOperation1().execute("");
	}

	private class LoginOperation1 extends AsyncTask<String, Void, Void> {

		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		// Call after onPreExecute method
		protected Void doInBackground(String... urls) {
			postDataLogin();
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();

			new filldata().execute("");
		}
	}

	public void postDataLogin() {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences(KEY,
				Context.MODE_PRIVATE);
		String IDurl = "https://graph.facebook.com/me?fields=id,first_name,last_name,email,picture&access_token="
				+ sharedPreferences.getString(TOKEN, null);

		JSONParser jsonParser = new JSONParser();

		String jsonString = jsonParser.getJSONFromUrl(IDurl);

		try {
			JSONObject obj = new JSONObject(jsonString);
			id = obj.getString("id");
			fname = obj.getString("first_name");
			lname = obj.getString("last_name");
			email1 = obj.getString("email");
			JSONObject objpicture = new JSONObject();
			objpicture = obj.getJSONObject("picture");
			JSONObject objurl = new JSONObject();
			objurl = objpicture.getJSONObject("data");

			picture = objurl.getString("url");
			Log.d("Test", id + " " + fname + " " + lname);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void checkonService() {
		// TODO Auto-generated method stub

	}

	private class filldata extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {

			Log.i("fill data", "fill data");
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			postfbData();
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				resultfb();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void postfbData() {
		// TODO Auto-generated method stub

		String result = "";
		try {

			JSONObject json = new JSONObject();
			json.put("firstname", fname);
			json.put("lastname", lname);
			json.put("email", email1);
			json.put("image", picture);
			json.put("longitude", longitude);
			json.put("latitude", latitude);

			Log.i("FaceBook Data", fname + "," + lname + "," + email1 + ","
					+ picture);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);
			String link = StaticData.SERVER_URL;
			String url = link + "facebookregister.php";

			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(json.toString().getBytes(
					"UTF8")));

			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();

				result = RestClient.convertStreamToString(instream);
				Log.i("Read from server", result);
			}

		} catch (Exception t) {
			Log.e("postData", t.toString());
		}

		try {
			Log.i("result", result);
			Response_code1 = result;
			Log.e("Result....", "...." + Response_code1);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Result", "...." + e.toString());
		}

		try {
			jobj1 = new JSONObject(Response_code1);
			login_result1 = jobj1.getString("ResponseCode");
			Log.i("Key", login_result1);
			Log.e("Result", "...." + login_result1);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.w("JSONException", e.toString());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			Log.w("null", e.toString());
		}

	}

	private void resultfb() {
		if (login_result1.equals("1") || login_result1.equals("3")) {

			// session.createFbLoginSession(email1);
			// Registration on GCM
			try {
				gcmRegister();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new FBinSession().execute("");

		} else if (login_result1.equals("2")) {
			Toast.makeText(getApplicationContext(), "Registration Failed",
					Toast.LENGTH_SHORT).show();

		}

	}

	private class FBinSession extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			getBitmapFromURL(picture);
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {
				Log.d("Fb Session", email1 + " " + fname + " " + lname);

				session.createFbLoginSession(email1, fname, lname, map);

				Intent i = new Intent(LoginActivity.this,
						SlidingMenuFragmentActivity.class);
				startActivity(i);
				finish();
				Toast.makeText(getApplicationContext(), "Login Successfully",
						Toast.LENGTH_SHORT).show();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// //////////////////////////////////////////////GCM_registration for Login
	// with Facebook///////////////////////////////
	private void gcmRegister() {
		// TODO Auto-generated method stub
		// GCM
		aController = (Controller) getApplicationContext();

		// Check if Internet present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(LoginActivity.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			// stop executing code by return
			return;
		}
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest permissions was properly set
		GCMRegistrar.checkManifest(this);
		// Register custom Broadcast receiver to show messages on activity
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);
		Log.i("regId", regId);

		// Check if regid already presents
		if (regId.equals("")) {

			// Register with GCM
			GCMRegistrar.register(getApplicationContext(),
					Config.GOOGLE_SENDER_ID);

		} else {

			// Device is already registered on GCM Server
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						// Register on our server
						// On server creates a new user

						aController.register(context, email1, regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};

				// execute AsyncTask
				mRegisterTask.execute(null, null, null);

			} else {

			}
		}
	}

	@Override
	protected void onDestroy() {
		// Cancel AsyncTask
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			// Unregister Broadcast Receiver
			unregisterReceiver(mHandleMessageReceiver);

			// Clear internal resources.
			GCMRegistrar.onDestroy(getApplicationContext());

		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	// Create a broadcast receiver to get message and show on screen
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// String newMessage = intent.getExtras().getString(
			// Config.EXTRA_MESSAGE);

			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());

			// Display message on the screen
			// lblMessage.append(newMessage + "\n");

			// Toast.makeText(getApplicationContext(),
			// "Got Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};

}
