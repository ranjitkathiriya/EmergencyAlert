package com.example.emergency;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.FriendDetail;
import com.example.parser.RecentEmergencyDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.squareup.picasso.Picasso;

public class EmergencyDescription extends Activity implements OnClickListener {

	TextView txt_desc, txt_username, txt_useremail, txt_totalhelper, txt_type,
			txt_time;
	Button btn_map, btn_images, btn_show_helper, btn_show_complete;
	LinearLayout linear_ask_for_helper, linear_show_helper;

	ToggleButton tbtn_helper;
	ImageView img_posreduser;

	LinearLayout linear_complete, linear_ongoing, linear_show_complete,
			ll_emergency_description;

	SimpleAdapter_for_images adapter;
	SimpleAdapter_for_helper_detail helper_adapter;

	// Local Variables
	List<String> images = null;
	String latitude, longitude;
	String profile_pic;
	int alert_id;
	int total_helper = 0;
	String url = StaticData.SERVER_URL;
	String service_link;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	// Variable for Background process
	JSONObject jobj_detail;
	String Response_code;
	public String login_result;

	// Veriables to store data from service
	ArrayList<RecentEmergencyDetail> arr_edetail = new ArrayList<RecentEmergencyDetail>();
	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();

	// Helper Friend Dialog components
	ImageView img_frienddp;
	Typeface USERNAME;
	TextView txt_friendname, txt_lastname, txt_friendemail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_description);

		if (StaticData.isNetworkAvailable(getApplicationContext())) {

			session = new SessionManager(getApplicationContext());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			USERNAME = Typeface.createFromAsset(getAssets(),
					"Fonts/USERNAME.TTF");
			ll_emergency_description = (LinearLayout) findViewById(R.id.ll_emergency_description);
			StaticData.setFont(ll_emergency_description, USERNAME);

			Intent i = getIntent();
			alert_id = i.getIntExtra("alert_id", 0);
			Log.i("Alert_id", alert_id + "");
			setContent();
			service_link = url + "emergency_description.php";
			new EmergeniesDetails().execute("");

			tbtn_helper
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							if (isChecked) {
								helperToggel();
							} else {
							}

						}

					});

		} else {
			Toast.makeText(getApplicationContext(), "Internet not connected",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Log.i("StaticData.notifyEmergencyHelper out",
				StaticData.notifyEmergencyHelper+"");

		if (StaticData.notifyEmergencyHelper) {
			StaticData.notifyEmergencyHelper = false;
			Intent i = new Intent(this, SlidingMenuFragmentActivity.class);
			startActivity(i);
			Log.i("StaticData.notifyEmergencyHelper in",
					StaticData.notifyEmergencyHelper+"");
			finish();
		}
	}

	private void helperToggel() {
		// TODO Auto-generated method stub

		final Dialog dialog = new Dialog(EmergencyDescription.this);

		dialog.getWindow();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.recent_emergencies_goas_helper_yes_no_dialog);
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.show();
		Button btn_yes_help = (Button) dialog.findViewById(R.id.btn_yes_help);
		Button btn_no_help = (Button) dialog.findViewById(R.id.btn_no_help);

		btn_yes_help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				service_link = url + "helper1.php";
				new EmergeniesDetails().execute("");
				dialog.cancel();
			}
		});

		btn_no_help.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method
				tbtn_helper.setChecked(false);
				dialog.cancel();
			}
		});

	}

	private void setContent() {
		// TODO Auto-generated method stub
		txt_desc = (TextView) findViewById(R.id.txt_desc);
		txt_username = (TextView) findViewById(R.id.txt_username);
		txt_type = (TextView) findViewById(R.id.txt_type);
		txt_useremail = (TextView) findViewById(R.id.txt_useremail);
		txt_totalhelper = (TextView) findViewById(R.id.txt_totalhelper);
		txt_time = (TextView) findViewById(R.id.txt_time);

		btn_map = (Button) findViewById(R.id.btn_map);
		btn_images = (Button) findViewById(R.id.btn_images);
		btn_show_complete = (Button) findViewById(R.id.btn_show_complete);
		btn_show_helper = (Button) findViewById(R.id.btn_show_helper);
		tbtn_helper = (ToggleButton) findViewById(R.id.tbtn_helper);

		linear_ask_for_helper = (LinearLayout) findViewById(R.id.linear_ask_for_helper);
		linear_show_helper = (LinearLayout) findViewById(R.id.linear_show_helper);
		linear_complete = (LinearLayout) findViewById(R.id.linear_complete);
		linear_ongoing = (LinearLayout) findViewById(R.id.linear_ongoing);
		linear_show_complete = (LinearLayout) findViewById(R.id.linear_show_complete);

		img_posreduser = (ImageView) findViewById(R.id.img_posreduser);

		btn_map.setOnClickListener(this);
		btn_images.setOnClickListener(this);
		btn_show_helper.setOnClickListener(this);
		btn_show_complete.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btn_map:
			if (!latitude.equals("") || !longitude.equals("")) {
				Intent i = new Intent(EmergencyDescription.this,
						EmergencyDescriptionMap.class);
				i.putExtra("latitude", latitude);
				i.putExtra("longitude", longitude);
				startActivity(i);
			} else {
				Toast.makeText(getApplicationContext(), "Location not found",
						Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.btn_images:
			if (!images.get(0).equals("")) {
				Dialog dialog = new Dialog(EmergencyDescription.this);
				dialog.getWindow();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.emergency_description_images);
				Window window = dialog.getWindow();
				window.setLayout(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);

				ListView lv_images = (ListView) dialog
						.findViewById(R.id.lv_images);
				adapter = new SimpleAdapter_for_images(dialog.getContext());

				lv_images.setAdapter(adapter);
				dialog.show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Emergency images not found.", Toast.LENGTH_SHORT)
						.show();
			}
			break;

		case R.id.btn_show_helper:
			if (!arr_fdetail.isEmpty()) {
				Dialog dialog = new Dialog(EmergencyDescription.this);
				dialog.getWindow();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.emergency_description_helper_list);
				Window window = dialog.getWindow();
				window.setLayout(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);

				ListView lv_friendlist = (ListView) dialog
						.findViewById(R.id.lv_friendlist);

				helper_adapter = new SimpleAdapter_for_helper_detail(
						dialog.getContext());

				lv_friendlist.setAdapter(helper_adapter);
				dialog.show();
			} else {
				Toast.makeText(getApplicationContext(), "No helper found",
						Toast.LENGTH_SHORT).show();
			}

			break;

		case R.id.btn_show_complete:
			final Dialog dialog = new Dialog(EmergencyDescription.this);
			dialog.getWindow();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.recent_emergencies_over_emergency_yes_no_dialog);
			Window window = dialog.getWindow();
			window.setLayout(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);

			Button btn_yes_over = (Button) dialog
					.findViewById(R.id.btn_yes_over);
			Button btn_no_over = (Button) dialog.findViewById(R.id.btn_no_over);

			btn_yes_over.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					service_link = url + "emergency_complete.php";
					new EmergeniesDetails().execute("");
					dialog.cancel();
					Intent i = new Intent(EmergencyDescription.this,
							EmergencyDescription.class);
					i.putExtra("alert_id", alert_id);
					startActivity(i);
					finish();

				}
			});
			btn_no_over.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.cancel();

				}
			});
			dialog.show();
			break;
		default:
			break;
		}

	}

	private class EmergeniesDetails extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(
				EmergencyDescription.this);

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
		JSONObject json = new JSONObject();

		try {
			if (service_link.equals(url + "emergency_description.php")) {
				json.put("alert_id", alert_id);
				json.put("useremail", useremail);
			} else if (service_link.equals(url + "helper1.php")) {
				json.put("alert_id", alert_id);
				json.put("useremail", useremail);
			} else if (service_link.equals(url + "emergency_complete.php")) {
				json.put("alert_id", alert_id);
			}
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);

			HttpPost request = new HttpPost(service_link);
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
			// Log.i("result", result);
			Response_code = result;
			Log.e("Result....", "...." + Response_code);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Result", "...." + e.toString());
		}

		try {
			jobj_detail = new JSONObject(Response_code);
			login_result = jobj_detail.getString("ResponseCode");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.w("JSONException", e.toString());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			Log.w("null", e.toString());
		}

	}

	private void result() {
		if (service_link.equals(url + "emergency_complete.php")) {
			if (login_result.equals("1")) {

				linear_complete.setVisibility(View.VISIBLE);
				linear_ongoing.setVisibility(View.GONE);
				linear_ask_for_helper.setVisibility(View.GONE);

				Toast.makeText(getApplicationContext(),
						"Emergency set as complete", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Problem in set emergency as complete",
						Toast.LENGTH_SHORT).show();
			}

		} else if (service_link.equals(url + "helper1.php")) {
			if (login_result.equals("1")) {
				Toast.makeText(getApplicationContext(), "Send successfully",
						Toast.LENGTH_SHORT).show();
				service_link = url + "emergency_description.php";
				new EmergeniesDetails().execute("");

			} else {
			}

		}

		else {

			if (login_result.equals("1") || login_result.equals("2")) {
				JSONArray alert_details = null;
				JSONArray user_details = null;
				JSONArray helper_details = null;

				try {
					// Fetching Alert Detail if available
					alert_details = jobj_detail.getJSONArray("alert_details");
					JSONObject c = alert_details.getJSONObject(0);
					txt_type.setText(c.getString("alert_type"));
					txt_desc.setText(c.getString("alert_description"));
					txt_useremail.setText(c.getString("alert_creater_email"));
					txt_time.setText(c.getString("create_time"));
					latitude = c.getString("alert_latitude");
					longitude = c.getString("alert_longitude");

					int complete_or_not = c.getInt("complete");
					Log.i("Complete or not", complete_or_not + "");
					if (complete_or_not == 1) {
						linear_complete.setVisibility(View.VISIBLE);
						linear_ongoing.setVisibility(View.GONE);
						linear_show_complete.setVisibility(View.GONE);

					} else {
						linear_ongoing.setVisibility(View.VISIBLE);
						linear_complete.setVisibility(View.GONE);
					}

					txt_totalhelper.setText(c.getInt("total_helper") + "");
					String eimages = c.getString("images");
					images = Arrays.asList(eimages.split(","));

					if (c.getString("alert_creater_email").equals(useremail)) {
						linear_ask_for_helper.setVisibility(View.GONE);
					} else {
						linear_ask_for_helper.setVisibility(View.VISIBLE);
						linear_show_complete.setVisibility(View.GONE);
					}

					// Fetching User Detail if available
					user_details = jobj_detail.getJSONArray("user_details");
					JSONObject c_user = user_details.getJSONObject(0);
					String full_username = c_user.getString("fname") + " "
							+ c_user.getString("lname");

					txt_username.setText(full_username);
					txt_useremail.setText(c_user.getString("email"));
					profile_pic = StaticData.SERVER_IMAGE_URL
							+ c_user.getString("profile_pic");

					img_posreduser.setScaleType(ScaleType.CENTER_CROP);

					Log.i("profile_pic", profile_pic);
					if (!profile_pic.equals("")) {
						Picasso.with(getApplicationContext()).load(profile_pic)
								.resize(250, 250)
								.transform(new RoundedTransformation(360, 0))
								.into(img_posreduser);
					}

					// Fetching Helper Detail if available
					if (login_result.equals("1")) {
						helper_details = jobj_detail
								.getJSONArray("helper_details");
						for (int i = 0; i < helper_details.length(); i++) {
							JSONObject c_helper = helper_details
									.getJSONObject(i);
							FriendDetail fd = new FriendDetail();
							fd.setFname(c_helper.getString("fname"));
							fd.setLname(c_helper.getString("lname"));
							fd.setEmail(c_helper.getString("email"));

							fd.setImage(StaticData.SERVER_IMAGE_URL
									+ c_helper.getString("profile_pic"));
							arr_fdetail.add(fd);

							// Hide "Will you go as helper" option
							if (useremail.equals(c_helper.getString("email"))) {
								linear_ask_for_helper.setVisibility(View.GONE);

							}
						}

					}

				} catch (NullPointerException e) {

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Toast.makeText(getApplicationContext(),
						"You have no currently any emergencies.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public class SimpleAdapter_for_images extends BaseAdapter {

		Context cont;

		public SimpleAdapter_for_images(Context context) {
			// TODO Auto-generated constructor stub
			cont = context;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return images.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(cont);
				convertView = inflater.inflate(
						R.layout.emergency_description_images_row_cell, null);
			}

			ImageView img = (ImageView) convertView
					.findViewById(R.id.img_edescription);
			img.setScaleType(ScaleType.CENTER_CROP);

			if (!images.get(position).equals("")) {
				Picasso.with(getApplicationContext())
						.load(StaticData.SERVER_IMAGE_URL
								+ images.get(position)).resize(250, 300)
						.into(img);
			}
			return convertView;
		}
	}

	public class SimpleAdapter_for_helper_detail extends BaseAdapter {

		Context cont;

		public SimpleAdapter_for_helper_detail(Context context) {
			// TODO Auto-generated constructor stub
			cont = context;

		}

		public int getCount() {
			// TODO Auto-generated method stub
			return arr_fdetail.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arr_fdetail.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(cont);
				convertView = inflater.inflate(
						R.layout.friends_row_cell_display, null);
			}

			img_frienddp = (ImageView) convertView
					.findViewById(R.id.img_frienddp);

			txt_friendemail = (TextView) convertView
					.findViewById(R.id.txt_friendemail);
			txt_friendname = (TextView) convertView
					.findViewById(R.id.txt_friendname);
			txt_lastname = (TextView) convertView
					.findViewById(R.id.txt_lastname);

			txt_friendemail.setText(arr_fdetail.get(position).getEmail());
			txt_friendname.setText(arr_fdetail.get(position).getFname());
			txt_lastname.setText(arr_fdetail.get(position).getLname());

			img_frienddp.setScaleType(ScaleType.CENTER_CROP);

			if (!arr_fdetail.get(position).getImage().equals("")) {
				Picasso.with(getApplicationContext())
						.load(arr_fdetail.get(position).getImage())
						.resize(250, 250)
						.transform(new RoundedTransformation(360, 0))
						.into(img_frienddp);
			}
			return convertView;

		}
	}
}
