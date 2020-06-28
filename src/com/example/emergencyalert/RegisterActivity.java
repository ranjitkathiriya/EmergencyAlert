package com.example.emergencyalert;

import java.io.InputStream;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.parser.RestClient;
import com.google.android.gcm.GCMRegistrar;

public class RegisterActivity extends Activity {
	// Registration Variables
	ImageView img_userdp;
	EditText edt_fname, edt_lname, edt_contactno, edt_email, edt_password,
			edt_confirmpassword;

	Button btn_register;
	private Pattern pattern;
	private Matcher matcher;
	String fname, lname, email, password, confirmpassword, contactno;

	// E-mail and Contact number pattern matching
	// matches 9999999999, 1-999-999-9999 and 999-999-9999
	final String MobilePattern = "^(1\\-)?[0-9]{3}\\-?[0-9]{3}\\-?[0-9]{4}$";
	final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	// Variable for Getting address
	GPSTracker gps;
	double latitude = 0.0;
	double longitude = 0.0;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	// Session maintain for Registered user
	SessionManager session;
	Bitmap map;

	// GCM gata
	Controller aController;
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_register_user);
		map = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_user);
		setContent();

		btn_register.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				fname = edt_fname.getText().toString();
				lname = edt_lname.getText().toString();
				contactno = edt_contactno.getText().toString();
				email = edt_email.getText().toString();
				password = edt_password.getText().toString();
				confirmpassword = edt_confirmpassword.getText().toString();

				pattern = Pattern.compile(EMAIL_PATTERN);
				matcher = pattern.matcher(email);
				if (fname.equals("")
						|| !fname.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {

					edt_fname.setError("Enter valid firstname format");

				}

				else if (lname.equals("")
						|| !lname.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {

					edt_lname.setError("Enter valid lastname format");

				} else if (contactno.equals("")
						|| !contactno.matches(MobilePattern)) {

					edt_contactno.setError("Enter valid contact number");

				} else if (email.equals("")) {

					edt_email.setError("Please enter email");

				}

				else if (!matcher.matches()) {
					edt_email.setError("Enter valid email.");
				}

				else if (password.equals("")) {

					edt_password.setError("Please enter password");

				} else if (password.length() < 6) {

					edt_password
							.setError("Choose password length minimum 6 character.");

				} else if (confirmpassword.equals("")
						|| !password.matches(confirmpassword)) {

					edt_confirmpassword.setError("Password mismatch");

				}

				else if (StaticData.isNetworkAvailable(getApplicationContext())) {

					getlocation();

				} else {
					Toast.makeText(getApplicationContext(),
							"Internet not connected", Toast.LENGTH_SHORT)
							.show();
				}

			}

			private void getlocation() {
				// TODO Auto-generated method stub
				// check if GPS enabled
				try {

					gps = new GPSTracker(RegisterActivity.this);
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
		});

	}

	private void setContent() {
		// TODO Auto-generated method stub

		session = new SessionManager(getApplicationContext());

		edt_fname = (EditText) findViewById(R.id.edt_fname);
		edt_lname = (EditText) findViewById(R.id.edt_lname);
		edt_contactno = (EditText) findViewById(R.id.edt_contactno);
		edt_email = (EditText) findViewById(R.id.edt_email);
		edt_password = (EditText) findViewById(R.id.edt_password);
		edt_confirmpassword = (EditText) findViewById(R.id.edt_confirmpassword);

		btn_register = (Button) findViewById(R.id.btn_register);
		// edt_email.setClickable(false);
	}

	private class getLoc extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(
				RegisterActivity.this);

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

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				new SearchUserOperation().execute("");

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	private class SearchUserOperation extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(
				RegisterActivity.this);

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
			Log.d("LOgg", "INpost");
			JSONObject json = new JSONObject();
			json.put("firstname", fname);
			json.put("lastname", lname);
			json.put("phone", contactno);
			json.put("password", password);
			json.put("email", email);
			json.put("longitude", longitude);
			json.put("latitude", latitude);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);
			String link = StaticData.SERVER_URL;
			String url = link + "register.php";

			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(json.toString().getBytes(
					"UTF8")));

			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			request.setHeader("json", json.toString());

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
		if (login_result.equals("1")) {
			Toast.makeText(getApplicationContext(),
					"Registration Successfully", Toast.LENGTH_SHORT).show();
			// moi.edit().putBoolean("login", true).commit();

			// For store user's email and password
			Log.d("Register Session", email + " " + password + " " + fname
					+ " " + lname);
			session.createLoginSession(email, password, fname, lname, map);
			Log.d("ses", "end");
			// Registration on GCM
			try {
				gcmRegister();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Intent i = new Intent(RegisterActivity.this, DpUpload.class);
			startActivity(i);
			finish();

		} else if (login_result.equals("3")) {
			Toast.makeText(getApplicationContext(),
					"Email already exist, Please select other email",
					Toast.LENGTH_LONG).show();
		}

		else {
			Toast.makeText(getApplicationContext(), "Registration Failed",
					Toast.LENGTH_SHORT).show();
			Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}

	}

	private void gcmRegister() {
		// TODO Auto-generated method stub
		// GCM
		aController = (Controller) getApplicationContext();

		// Check if Internet present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(RegisterActivity.this,
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

						aController.register(context, email, regId);
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