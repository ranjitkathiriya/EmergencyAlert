package com.example.emergency;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.contact.ContactlistviewActivity;
import com.example.emergencyalert.GPSTracker;
import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.FriendDetail;
import com.example.parser.GroupDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.google.common.base.Joiner;
import com.squareup.picasso.Picasso;

public class CreateEmergency extends Fragment implements OnClickListener {
	View activityView;
	LinearLayout ll_create_emergency, ll2;

	// Create emergency elements
	Spinner sp_emergency_type;
	ToggleButton tb_surrounding, tb_contact;
	EditText edt_emergency_description;
	Button btn_area, btn_contacts, btn_send, btn_reset, btn_friend, btn_group,
			btn_select_images, btn_select_group_or_friend;
	TextView tv_contact, txt_please_wait_friend, txt_please_wait_group;

	// String for spinner items
	String[] categories = { "Other emergency", "Happen accident",
			"Blood Requirement", "Feel Unsafe", "Need help for other person" };

	String[] selection_group_or_friend = { "None", "Select Friends",
			"Select Group" };
	Integer[] select_surrounding_km;

	// Data will pass through these variables
	String emergency_description = "Emergency generated need help";
	String selected_default_emergency;
	String selected_km = "0";
	String friend_email = "";
	// Variable for get Current user Email
	SessionManager session;
	String useremail;
	int spinner_postion = 0;
	// Different Services calling
	String link = StaticData.SERVER_URL;
	String url;
	JSONObject json;

	// Variable for Background process
	JSONObject jobj;
	String Response_code, Response_msg;
	String result = "";
	public String login_result;

	// Profile picture upload
	ListView lv_select_friends;
	ImageView img_frienddp;
	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();
	List<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> array_addlist = new ArrayList<HashMap<String, String>>();

	// flag for either group or friend selection
	Boolean group_or_friend = false;

	ListView lv_select_group;
	ImageView img_groupdp;
	ArrayList<GroupDetail> arr_gdetail = new ArrayList<GroupDetail>();
	String selected_group = "", selected_group_id;
	ArrayList<HashMap<String, String>> array_group_addlist = new ArrayList<HashMap<String, String>>();
	Boolean flag = true;
	int send_fg = 0;

	// Google Map
	double latitude = 0;
	double longitude = 0;
	String address = null;
	String city = null;
	String emergency_address = "emergency_address";
	Geocoder geocorder;
	GPSTracker gps;

	// Emergency images variable;
	Dialog dialog;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	Uri fileUri;// to store image file
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final String IMAGE_DIRECTORY_NAME = "EmergencyAlert";
	static File mediaFile, file, imageFile;
	static String selectedImagePath;
	private Uri mImageCaptureUri;
	ArrayList<Bitmap> rotatedBMP = null;
	ListView lv_imagelist;
	SimpleAdapter_for_capture_images simpleadapter;
	ArrayList<File> imageFilesForUpload;

	FileOutputStream os;
	static File mediaStorageDir;
	Typeface USERNAME;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activityView = inflater.inflate(R.layout.create_emergency, container,
				false);

		ll_create_emergency = (LinearLayout) activityView
				.findViewById(R.id.ll_create_emergency);

		StaticData.setFont(ll_create_emergency, USERNAME);

		simpleadapter = new SimpleAdapter_for_capture_images(getActivity());
		imageFilesForUpload = new ArrayList<File>();
		rotatedBMP = new ArrayList<Bitmap>();

		if (StaticData.isNetworkAvailable(getActivity())) {
			session = new SessionManager(getActivity());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			bindClickListeners();
			fillSpinner();
			checkToggle();
			checkCheckBox();
		} else {
			activityView = inflater.inflate(R.layout.global_internet_not_found,
					container, false);

		}
		select_surrounding_km = new Integer[10];

		for (int i = 0; i < 10; i++) {

			select_surrounding_km[i] = (i + 1);
		}
		return activityView;
	}

	private void setContent() {
		// TODO Auto-generated method stub
		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");
		sp_emergency_type = (Spinner) activityView
				.findViewById(R.id.sp_emergency_type);

		tb_surrounding = (ToggleButton) activityView
				.findViewById(R.id.tb_surrounding);
		edt_emergency_description = (EditText) activityView
				.findViewById(R.id.edt_emergency_description);
		tb_contact = (ToggleButton) activityView.findViewById(R.id.tb_contact);

		btn_area = (Button) activityView.findViewById(R.id.btn_area);
		btn_contacts = (Button) activityView.findViewById(R.id.btn_contacts);
		btn_send = (Button) activityView.findViewById(R.id.btn_send);
		btn_reset = (Button) activityView.findViewById(R.id.btn_reset);
		btn_select_images = (Button) activityView
				.findViewById(R.id.btn_select_images);
		btn_select_group_or_friend = (Button) activityView
				.findViewById(R.id.btn_select_group_or_friend);

		tv_contact = (TextView) activityView.findViewById(R.id.tv_contact);

	}

	private void bindClickListeners() {
		// TODO Auto-generated method stub
		btn_area.setOnClickListener(this);
		btn_contacts.setOnClickListener(this);
		btn_send.setOnClickListener(this);
		btn_reset.setOnClickListener(this);
		btn_select_images.setOnClickListener(this);
		btn_select_group_or_friend.setOnClickListener(this);

	}

	private void fillSpinner() {
		// TODO Auto-generated method stub
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.create_emergency_spinner_layout_emergency_type,
				categories);
		sp_emergency_type.setAdapter(adapter);
		sp_emergency_type
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						selected_default_emergency = sp_emergency_type
								.getSelectedItem().toString();
						if (arg2 == 0) {
							selected_default_emergency = "Emergency,need help!";
						}
					}

					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
				});
	}

	private void checkToggle() {
		// TODO Auto-generated method stub
		tb_surrounding
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// The toggle is enabled
							btn_area.setEnabled(true);
							btn_area.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									setSurroundingArea();
								}
							});

						} else {
							// The toggle is disabled
							btn_area.setEnabled(false);
							selected_km = "0";

						}
					}
				});
	}

	private void checkCheckBox() {
		// TODO Auto-generated method stub
		tb_contact
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// The toggle is enabled
							btn_contacts.setEnabled(true);
							btn_contacts
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											Intent intent = new Intent(
													getActivity(),
													ContactlistviewActivity.class);
											startActivity(intent);
										}
									});

						} else {
							// The toggle is disabled
							btn_contacts.setEnabled(false);

						}
					}
				});
	}

	protected void sendSMSMessage(String contact) {
		String link = "http://maps.google.com/maps?q=";
		String message = emergency_description + "\nMy Location:" + link
				+ latitude + "," + longitude;

		try {

			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(contact, null, message, null, null);
			Toast.makeText(getActivity(), "Message Send Succesfully",
					Toast.LENGTH_SHORT).show();
			Log.d("msg", message + "");

		} catch (Exception e) {
			Toast.makeText(getActivity(), "SMS faild, please try again.",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_contact:
			if (tb_contact.isChecked()) {
				tb_contact.setChecked(false);
			} else {
				tb_contact.setChecked(true);
			}
			checkCheckBox();
			break;

		case R.id.btn_send:
			gps = new GPSTracker(getActivity());

			if (!gps.canGetLocation()) {
				gps.showSettingsAlert();

			} else {
				if (!edt_emergency_description.getText().toString().equals("")) {
					emergency_description = edt_emergency_description.getText()
							.toString();
				}
				getCurrentLocation();
			}
			if (tb_contact.isChecked()) {
				for (int i = 0; i < SessionManager.preserve_list.size(); i++) {
					String selected_contact = SessionManager.preserve_list
							.get(i);
					Log.i("Selected_contact", selected_contact);
					sendSMSMessage(selected_contact);
				}
			}

			break;

		case R.id.btn_select_images:
			dialog = new Dialog(getActivity());
			dialog.getWindow();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.create_emergency_images_dialog);
			Window window = dialog.getWindow();
			window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			lv_imagelist = (ListView) dialog.findViewById(R.id.lv_imagelist);
			simpleadapter.notifyDataSetChanged();
			lv_imagelist.setAdapter(simpleadapter);
			ImageButton btn_capture_image = (ImageButton) dialog
					.findViewById(R.id.btn_capture_image);
			btn_capture_image.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (rotatedBMP.size() < 5) {
						// TODO Auto-generated method stub
						captureImage();

						simpleadapter.notifyDataSetChanged();
						lv_imagelist.setAdapter(simpleadapter);
					} else {
						Toast.makeText(v.getContext(),
								"You can select Only five images",
								Toast.LENGTH_SHORT).show();
					}
				}
			});

			dialog.show();
			break;

		case R.id.btn_select_group_or_friend:
			dialog = new Dialog(getActivity());
			dialog.getWindow();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.create_emergency_select_friends_or_group_dialog);
			Window window1 = dialog.getWindow();
			window1.setLayout(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);

			final Button btn_select_friend = (Button) dialog
					.findViewById(R.id.btn_select_friend);
			btn_select_friend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					send_fg = 1;
					selectFriends();
					dialog.dismiss();
				}
			});
			final Button btn_select_group = (Button) dialog
					.findViewById(R.id.btn_select_group);
			btn_select_group.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					send_fg = 2;
					selectGroups();
					dialog.dismiss();
				}
			});
			dialog.show();
			break;
		case R.id.btn_reset:
			StaticData.resetFlag = true;
			Intent i = new Intent(getActivity(),
					SlidingMenuFragmentActivity.class);
			startActivity(i);
			getActivity().finish();
			break;
		default:
			break;
		}
	}

	private void getCurrentLocation() {
		// TODO Auto-generated method stub

		// check if GPS enabled
		try {

			if (gps.canGetLocation()) {

				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				try {
					Log.i("Creater email:", useremail);
					Log.i("latitude", "" + latitude);
					Log.i("longitude", "" + longitude);
					Log.i("emergency_address", emergency_address);
					Log.i("Create Emergency type:", selected_default_emergency
							+ "");
					Log.i("emergency_description: ", emergency_description + "");
					Log.i("emergency_images ", imageFilesForUpload + "");
					Log.i("selected_km: ", selected_km + "");
					if (send_fg == 1) {
						Log.i("friend_email", friend_email);
					} else if (send_fg == 2) {
						Log.i("selected_group", selected_group);

					}
					if (!friend_email.equals("") || !selected_group.equals("")) {
						new SendEmergency().execute("");
					} else {
						Toast.makeText(getActivity(),
								"Please select Group or Friends",
								Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class SendEmergency extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub

			doFileUpload();
			return null;

		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {
				if (result.endsWith("1")) {
					Toast.makeText(getActivity(), "Send Successfully",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "Can't able to send",
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doFileUpload() {
		Log.d("Uri", "Do file path" + imageFilesForUpload);
		try {
			String ServerUploadPath = StaticData.SERVER_URL
					+ "create_emergency_new.php";

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(ServerUploadPath);

			Log.d("ServerPath", "Path" + ServerUploadPath);

			MultipartEntity reqEntity = new MultipartEntity();

			for (int i = 0; i < imageFilesForUpload.size(); i++) {

				FileBody bin1 = new FileBody(imageFilesForUpload.get(i));

				reqEntity.addPart(("uploaded_file" + i), bin1);
			}

			reqEntity.addPart("email", new StringBody(useremail));
			reqEntity.addPart("selected_default_emergency", new StringBody(
					selected_default_emergency));
			reqEntity.addPart("emergency_description", new StringBody(
					emergency_description));
			reqEntity.addPart("latitude", new StringBody(latitude + ""));
			reqEntity.addPart("longitude", new StringBody(longitude + ""));
			reqEntity.addPart("selected_km", new StringBody(selected_km));
			reqEntity.addPart("emergency_address", new StringBody(
					emergency_address));
			reqEntity.addPart("send_fg", new StringBody(send_fg + ""));

			if (send_fg == 1) {
				reqEntity.addPart("friend_email", new StringBody(friend_email));
			} else if (send_fg == 2) {
				reqEntity.addPart("selected_group", new StringBody(
						selected_group));
				reqEntity.addPart("selected_group_id", new StringBody(
						selected_group_id));
			}

			post.setEntity(reqEntity);
			Log.d("Enter", "Image send complete");

			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			Log.d("Enter", "Get Response");
			try {

				final String response_str = EntityUtils.toString(resEntity);
				if (resEntity != null) {
					Log.i("RESPONSE", response_str);
					JSONObject jobj = new JSONObject(response_str);
					result = jobj.getString("ResponseCode");
					Log.e("Result", "...." + result);

				}

			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
			}
		} catch (Exception e) {
			Log.e("Upload Exception", "");
			e.printStackTrace();
		}
	}

	private void setSurroundingArea() {
		// TODO Auto-generated method stub
		final Dialog d = new Dialog(getActivity());
		d.getWindow();
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.create_emergency_select_km_dialog);

		LinearLayout ll_dialog_setkilometer = (LinearLayout) d
				.findViewById(R.id.ll_dialog_setkilometer);
		StaticData.setFont(ll_dialog_setkilometer, USERNAME);
		final Spinner spinner1 = (Spinner) d.findViewById(R.id.spinner1);
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(
				getActivity(), R.layout.create_emergency_spinner_layout_km,
				select_surrounding_km);
		spinner1.setAdapter(adapter);
		d.show();
		spinner1.setSelection(spinner_postion);
		Button btn_selected_km_set = (Button) d
				.findViewById(R.id.btn_selected_km_set);
		btn_selected_km_set.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated
				// method stub
				selected_km = spinner1.getSelectedItem().toString();
				spinner_postion = spinner1.getSelectedItemPosition();
				d.dismiss();
			}
		});

	}

	private void selectFriends() {
		// TODO Auto-generated method stub
		final Dialog d = new Dialog(getActivity());
		d.getWindow();
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.friends_select_friends);
		url = StaticData.SERVER_URL + "myfriend.php";
		new FillSelectList().execute("");
		d.show();

		lv_select_friends = (ListView) d.findViewById(R.id.lv_select_friends);
		txt_please_wait_friend = (TextView) d
				.findViewById(R.id.txt_please_wait_friend);
		Button btn_add_selected_friends = (Button) d
				.findViewById(R.id.btn_add_selected_friends);
		btn_add_selected_friends.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayList<String> femail = new ArrayList<String>();
				friend_email = "";
				for (int i = 0; i < array_addlist.size(); i++) {

					femail.add(array_addlist.get(i).get("email"));
				}

				Joiner joiner = Joiner.on(",").skipNulls();
				friend_email = joiner.join(femail);
				if (array_addlist.size() == 0) {
					btn_select_group_or_friend.setText("You are select none.");
				} else {
					btn_select_group_or_friend.setText("You are select "
							+ array_addlist.size() + " friend.");

				}
				d.dismiss();
			}
		});

	}

	private void selectGroups() {
		// TODO Auto-generated method stub
		final Dialog d = new Dialog(getActivity());
		d.getWindow();
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.groups_select_group);
		d.onBackPressed();
		url = StaticData.SERVER_URL + "displaygroup.php";
		new FillSelectList().execute("");
		d.show();

		lv_select_group = (ListView) d.findViewById(R.id.lv_select_group);
		txt_please_wait_group = (TextView) d
				.findViewById(R.id.txt_please_wait_group);
		Button btn_add_selected_group = (Button) d
				.findViewById(R.id.btn_add_selected_group);
		btn_add_selected_group.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selected_group = "";
				for (int i = 0; i < array_group_addlist.size(); i++) {

					selected_group = array_group_addlist.get(i).get(
							"group_name");
					selected_group_id = array_group_addlist.get(i).get(
							"group_id");
				}
				// Log.i("selected_group", selected_group);

				if (selected_group.equals("")) {
					btn_select_group_or_friend.setText("None");

				} else {

					btn_select_group_or_friend.setText("You are select "
							+ selected_group + " group.");

				}
				d.dismiss();

			}
		});
	}

	private class FillSelectList extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

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
		arr_fdetail.clear();
		arr_gdetail.clear();
		String result = "";
		try {
			JSONObject json = new JSONObject();
			json.put("email", useremail);

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
			Response_code = result;
			Log.e("Result....", "...." + Response_code);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Result", "...." + e.toString());
		}

		try {
			jobj = new JSONObject(Response_code);
			login_result = jobj.getString("ResponseCode");

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
			JSONArray name = null;

			try {

				if (url.equals(StaticData.SERVER_URL + "myfriend.php")) {
					name = jobj.getJSONArray("userdetail");

					for (int i = 0; i < name.length(); i++) {
						JSONObject c = name.getJSONObject(i);
						FriendDetail fd = new FriendDetail();
						fd.setFname(c.getString("fname"));
						fd.setLname(c.getString("lname"));
						fd.setEmail(c.getString("email"));

						fd.setImage(StaticData.SERVER_IMAGE_URL
								+ c.getString("profile_pic"));
						arr_fdetail.add(fd);

					}
					// For friendlist fill
					fillFriendList();
				} else {
					name = jobj.getJSONArray("userdetail");

					for (int i = 0; i < name.length(); i++) {
						JSONObject c = name.getJSONObject(i);
						GroupDetail gd = new GroupDetail();
						gd.setId(c.getString("group_id"));
						gd.setName(c.getString("group_name"));
						gd.setAdmin(c.getString("group_admin"));
						gd.setImage(StaticData.SERVER_IMAGE_URL
								+ c.getString("group_image"));

						arr_gdetail.add(gd);

					}
					fillGroupList();
				}
			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			txt_please_wait_friend.setText("You have no friends.");
			txt_please_wait_group.setText("You have no groups.");
		}

	}

	private void fillFriendList() {
		// TODO Auto-generated method stub.
		alist.clear();
		for (int i = 0; i < arr_fdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", arr_fdetail.get(i).getFname());
			tempName.put("lname", arr_fdetail.get(i).getLname());
			tempName.put("email", arr_fdetail.get(i).getEmail());
			alist.add(tempName);
		}
		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_friendname, R.id.txt_lastname,
				R.id.txt_friendemail };

		SpecialAdapter adapter = new SpecialAdapter(getActivity(), alist,
				R.layout.friends_select_friend_row_cell_cb_dialog, from, to);

		lv_select_friends.setAdapter(adapter);

	}

	public class SpecialAdapter extends SimpleAdapter {
		public SpecialAdapter(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			final CheckBox chb_selectedfriend = (CheckBox) view
					.findViewById(R.id.chb_selectedfriend);
			final TextView txt_friendemail = (TextView) view
					.findViewById(R.id.txt_friendemail);
			txt_please_wait_friend.setVisibility(View.GONE);
			chb_selectedfriend.setChecked(false);

			img_frienddp = (ImageView) view.findViewById(R.id.img_frienddp);
			if (arr_fdetail.get(position).getImage() != null) {
				Picasso.with(getActivity())
						.load(arr_fdetail.get(position).getImage())
						.resize(165, 165)
						.transform(new RoundedTransformation(360, 0))
						.into(img_frienddp);
				;
			}

			try {
				for (int i = 0; i < array_addlist.size(); i++) {
					if (txt_friendemail.getText().toString()
							.equals(array_addlist.get(i).get("email"))) {
						chb_selectedfriend.setChecked(true);
						break;
					}

				}
			} catch (Exception e) {

			}

			chb_selectedfriend.setOnClickListener(new OnClickListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (chb_selectedfriend.isChecked()) {
						HashMap<String, String> hashmap = new HashMap<String, String>();
						hashmap.put("fname", arr_fdetail.get(position)
								.getFname());
						hashmap.put("lname", arr_fdetail.get(position)
								.getLname());
						hashmap.put("email", arr_fdetail.get(position)
								.getEmail());
						hashmap.put("profile_pic", arr_fdetail.get(position)
								.getImage());

						array_addlist.add(hashmap);

						@SuppressWarnings("rawtypes")
						HashSet d = new HashSet();
						d.addAll(array_addlist);
						array_addlist.clear();
						array_addlist.addAll(d);

					} else {
						for (int i = 0; i < array_addlist.size(); i++) {
							if (txt_friendemail.getText().toString()
									.equals(array_addlist.get(i).get("email"))) {

								array_addlist.remove(i);

								@SuppressWarnings("rawtypes")
								HashSet d = new HashSet();
								d.addAll(array_addlist);
								array_addlist.clear();
								array_addlist.addAll(d);

								break;
							}

						}

					}

				}
			});

			return view;

		}
	}

	@SuppressWarnings("static-access")
	private void fillGroupList() {
		// TODO Auto-generated method stub.
		alist.clear();
		for (int i = 0; i < arr_gdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("group_name", arr_gdetail.get(i).getName());
			tempName.put("group_id", arr_gdetail.get(i).getId());
			alist.add(tempName);
		}
		String[] from = { "group_name", "group_id" };

		int[] to = { R.id.txt_groupname, R.id.txt_groupid };

		SpecialAdapter1 adapter1 = new SpecialAdapter1(getActivity(), alist,
				R.layout.groups_row_cell_cb_select, from, to);

		lv_select_group.setAdapter(adapter1);
		lv_select_group.setChoiceMode(lv_select_group.CHOICE_MODE_SINGLE);
	}

	public class SpecialAdapter1 extends SimpleAdapter {

		public SpecialAdapter1(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			final TextView txt_groupid = (TextView) view
					.findViewById(R.id.txt_groupid);

			final CheckBox chb_selected_group = (CheckBox) view
					.findViewById(R.id.chb_selected_group);

			txt_groupid.setVisibility(view.GONE);
			txt_please_wait_group.setVisibility(View.GONE);
			chb_selected_group.setChecked(false);

			img_groupdp = (ImageView) view.findViewById(R.id.img_groupdp);
			if (arr_gdetail.get(position).getImage() != null) {
				Picasso.with(getActivity())
						.load(arr_gdetail.get(position).getImage())
						.resize(165, 165)
						.transform(new RoundedTransformation(360, 0))
						.into(img_groupdp);
			}
			try {
				for (int i = 0; i < array_group_addlist.size(); i++) {
					if (txt_groupid.getText().toString()
							.equals(array_group_addlist.get(i).get("group_id"))) {

						chb_selected_group.setChecked(true);

						break;
					}

				}
			} catch (Exception e) {

			}
			chb_selected_group.setOnClickListener(new OnClickListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (chb_selected_group.isChecked()) {
						if (flag) {
							HashMap<String, String> hashmap = new HashMap<String, String>();
							hashmap.put("group_name", arr_gdetail.get(position)
									.getName());
							hashmap.put("group_id", arr_gdetail.get(position)
									.getId());


							array_group_addlist.add(hashmap);

							@SuppressWarnings("rawtypes")
							HashSet d = new HashSet();
							d.addAll(array_group_addlist);
							array_group_addlist.clear();
							array_group_addlist.addAll(d);
							flag = false;

						} else {
							Toast.makeText(v.getContext(),
									"You can select only one group.",
									Toast.LENGTH_SHORT).show();
							chb_selected_group.setChecked(false);
						}
					} else {
						for (int i = 0; i < array_group_addlist.size(); i++) {
							if (txt_groupid
									.getText()
									.toString()
									.equals(array_group_addlist.get(i).get(
											"group_id"))) {

								array_group_addlist.remove(i);

								@SuppressWarnings("rawtypes")
								HashSet d = new HashSet();
								d.addAll(array_group_addlist);
								array_group_addlist.clear();
								array_group_addlist.addAll(d);
								flag = true;
								break;
							}
						}
					}
				}
			});

			return view;
		}
	}

	// ////////////////////////////////////////Select Emergency
	// Images//////////////////////////////////////////////////////

	public class SimpleAdapter_for_capture_images extends BaseAdapter {

		Context cont;

		public SimpleAdapter_for_capture_images(Context context) {
			// TODO Auto-generated constructor stub
			cont = context;

		}

		public int getCount() {
			// TODO Auto-generated method stub
			return rotatedBMP.size();
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
						R.layout.create_emergency_image_view_list, null);

			}

			if (rotatedBMP != null) {

				ImageView imgview = (ImageView) convertView
						.findViewById(R.id.imgv_piv);
				imgview.setScaleType(ScaleType.CENTER_CROP);
				imgview.setImageBitmap(rotatedBMP.get(position));

				Log.i("Image Added", "Image added to ListView");

			}

			if (rotatedBMP != null) {
				ImageView img_delete = (ImageView) convertView
						.findViewById(R.id.img_delete);
				img_delete.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub

						delete_image(position);
						Log.i("Image deleted", "Image deleted from ListView");

					}
				});
			}
			return convertView;

		}

	}

	public void delete_image(int position) {
		Bitmap temp = rotatedBMP.get(position);
		temp.recycle();

		rotatedBMP.remove(position);
		rotatedBMP.trimToSize();

		imageFilesForUpload.remove(position);
		imageFilesForUpload.trimToSize();

		simpleadapter.notifyDataSetChanged();
		lv_imagelist.setAdapter(simpleadapter);
	}

	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File getOutputMediaFile(int type) {

		// External sdcard location
		mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());

		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else {
			return null;
		}
		Log.e("path", "media file:-" + mediaFile);
		return mediaFile;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {

			if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				if (resultCode == getActivity().RESULT_OK) {
					// successfully captured the image
					// display it in image view

					selectedImagePath = mediaFile.toString();
					mImageCaptureUri = Uri.fromFile(mediaFile);

					Log.i("selectedImagePath", selectedImagePath);
					Log.i("mImageCaptureUri", mImageCaptureUri + "");

					previewCapturedImage();
				} else if (resultCode == getActivity().RESULT_CANCELED) {
					// user cancelled Image capture
					Toast.makeText(getActivity(),
							"User cancelled image capture", Toast.LENGTH_SHORT)
							.show();
				} else {
					// failed to capture image
					Toast.makeText(getActivity(),
							"Sorry! Failed to capture image",
							Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		final float totalPixels = width * height;
		final float totalReqPixelsCap = reqWidth * reqHeight * 2;
		while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
			inSampleSize++;
		}

		return inSampleSize;
	}

	private void previewCapturedImage() {
		try {
			int targetW = 450;
			int targetH = 450;
			Log.d("Get w", "width" + targetW);
			Log.d("Get H", "height" + targetH);
			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(selectedImagePath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			// int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			// bmOptions.inSampleSize = scaleFactor << 1;
			bmOptions.inSampleSize = calculateInSampleSize(bmOptions, targetH,
					targetW);
			bmOptions.inPurgeable = true;
			bmOptions.inInputShareable = true;
			bmOptions.inTempStorage = new byte[16 * 1024];
			Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath,
					bmOptions);

			Matrix mtx = new Matrix();

			try {
				int last_image_position = 0;
				imageFile = new File(selectedImagePath);

				ExifInterface exif = new ExifInterface(
						imageFile.getAbsolutePath());
				int orientation = exif.getAttributeInt(
						ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);

				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:

					mtx.postRotate(270);
					rotatedBMP.add(Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mtx, true));

					if (rotatedBMP.size() != 0) {
						last_image_position = rotatedBMP.size() - 1;
					}
					imageFilesForUpload.add(new File(createFile(rotatedBMP
							.get(last_image_position))));
					// bitmap.recycle();

					break;
				case ExifInterface.ORIENTATION_ROTATE_180:

					mtx.postRotate(180);

					rotatedBMP.add(Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mtx, true));

					if (rotatedBMP.size() != 0) {
						last_image_position = rotatedBMP.size() - 1;
					}
					imageFilesForUpload.add(new File(createFile(rotatedBMP
							.get(last_image_position))));
					// bitmap.recycle();
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:

					mtx.postRotate(90);

					rotatedBMP.add(Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mtx, true));

					if (rotatedBMP.size() != 0) {
						last_image_position = rotatedBMP.size() - 1;
					}
					imageFilesForUpload.add(new File(createFile(rotatedBMP
							.get(last_image_position))));
					// bitmap.recycle();
					break;
				case ExifInterface.ORIENTATION_NORMAL:

					mtx.postRotate(0);

					rotatedBMP.add(Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mtx, true));
					if (rotatedBMP.size() != 0) {
						last_image_position = rotatedBMP.size() - 1;
					}
					imageFilesForUpload.add(new File(createFile(rotatedBMP
							.get(last_image_position))));

					// bitmap.recycle();
					break;
				default:
					mtx.postRotate(0);

					rotatedBMP.add(Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mtx, true));
					if (rotatedBMP.size() != 0) {
						last_image_position = rotatedBMP.size() - 1;

					}
					imageFilesForUpload.add(new File(createFile(rotatedBMP
							.get(last_image_position))));

					// bitmap.recycle();
				}
				Log.i("RotateImage", "Exif orientation: " + orientation);
				simpleadapter.notifyDataSetChanged();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public String createFile(Bitmap bmp) throws IOException {
		String filename = null;
		Log.i("mediaStorageDir Before", mediaStorageDir + "");
		Log.i("filename Before", filename + "");

		filename = String.valueOf(System.currentTimeMillis()) + ".jpg";
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(mediaStorageDir, filename));
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your
																// Bitmap
																// instance

			// PNG is a lossless format, the compression factor (100) is ignored
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.i("mediaStorageDir after", mediaStorageDir + "");
		Log.i("filename after", filename + "");

		return mediaStorageDir + "/" + filename;
	}

}
