package com.example.friend;

import java.io.InputStream;
import java.util.HashMap;

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
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.squareup.picasso.Picasso;

public class PendingFriendProfile extends Activity implements OnClickListener {
	TextView txt_fname, txt_lname, txt_email;
	ImageView img_userdp;
	Intent send;
	Button btn_accept, btn_decline;
	String friendemail;
	Boolean flag;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	LinearLayout ll_pending_friend_request_profile;
	Typeface USERNAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pending_request_friend_profile);

		USERNAME = Typeface.createFromAsset(this.getAssets(),
				"Fonts/USERNAME.TTF");
		ll_pending_friend_request_profile = (LinearLayout) findViewById(R.id.ll_pending_friend_request_profile);
		StaticData.setFont(ll_pending_friend_request_profile, USERNAME);
		if (StaticData.isNetworkAvailable(getApplicationContext())) {

			session = new SessionManager(getApplicationContext());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			fillContent();

		} else {
			Toast.makeText(getApplicationContext(), "Internet not connected",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void setContent() {
		// TODO Auto-generated method stub
		txt_fname = (TextView) findViewById(R.id.txt_fname);
		txt_lname = (TextView) findViewById(R.id.txt_lname);
		txt_email = (TextView) findViewById(R.id.txt_email);

		img_userdp = (ImageView) findViewById(R.id.img_userdp);

		btn_accept = (Button) findViewById(R.id.btn_accept);
		btn_decline = (Button) findViewById(R.id.btn_decline);

		btn_accept.setOnClickListener(this);
		btn_decline.setOnClickListener(this);

	}

	private void fillContent() {
		// TODO Auto-generated method stub
		Intent getval = getIntent();
		@SuppressWarnings("unchecked")
		HashMap<String, String> hashMap = (HashMap<String, String>) getval
				.getSerializableExtra("from_addfriend");
		txt_fname.setText(hashMap.get("fname").toString());
		txt_lname.setText(hashMap.get("lname").toString());
		txt_email.setText(hashMap.get("email").toString());
		friendemail = hashMap.get("email").toString();
		String image = hashMap.get("image").toString();

		if (image != null) {
			Picasso.with(PendingFriendProfile.this).load(image)
					.resize(165, 165)
					.transform(new RoundedTransformation(360, 0))
					.into(img_userdp);
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (StaticData.notifyFriendRequest) {
			// StaticData.notifyFriendRequest = false;
			Intent i = new Intent(this, SlidingMenuFragmentActivity.class);
			startActivity(i);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_accept:

			try {
				// Indicate to service whether to accept or not
				flag = true;
				new AcceptFriendRequest().execute("");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case R.id.btn_decline:

			try {
				// Indicate to service whether to accept or not
				flag = false;
				new AcceptFriendRequest().execute("");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		default:
			break;
		}

	}

	private class AcceptFriendRequest extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(
				PendingFriendProfile.this);

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
			JSONObject json = new JSONObject();

			// -------------------session data---------------------//
			json.put("user_email", useremail);
			json.put("friend_email", friendemail);
			json.put("flag", flag);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);
			String link = StaticData.SERVER_URL;
			String url = link + "acceptrequest.php";

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

		if (login_result.equals("1")) {
			Toast.makeText(getApplicationContext(), "Request accepted",
					Toast.LENGTH_SHORT).show();

			StaticData.resetPendingRequestFlag = true;
			StaticData.resetMyFriendsFlag = true;

			if (StaticData.notifyFriendRequest) {
				Intent i = new Intent(this, SlidingMenuFragmentActivity.class);
				startActivity(i);
				finish();
			} else {
				finish();
			}
		} else if (login_result.equals("2")) {
			Toast.makeText(getApplicationContext(), "Request declined.",
					Toast.LENGTH_SHORT).show();

			StaticData.resetPendingRequestFlag = true;
			if (StaticData.notifyFriendRequest) {
				Intent i = new Intent(this, SlidingMenuFragmentActivity.class);
				startActivity(i);
				finish();
			} else {
				finish();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Problem in accept request.", Toast.LENGTH_SHORT).show();
		}

	}

}
