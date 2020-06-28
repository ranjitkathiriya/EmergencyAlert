package com.example.group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.FriendDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.google.common.base.Joiner;
import com.squareup.picasso.Picasso;

public class MyGroupsInfo extends Activity implements OnClickListener {

	ImageView img_group, img_frienddp;
	Button btn_add_groupdp, btn_add_newfriends, btn_delete_group;
	ListView lv_group_friendlist;
	TextView txt_group_name, txt_please_wait_friend;

	ListView lv_select_friends;

	String group_id_from_mygroups, group_name, group_image;
	String ServerUploadPath, TAG = "UploadFile", mCurrentPhotoPath;
	File file;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	// Variable for Background process
	JSONObject jobj;
	String Response_code, Response_msg;
	public String login_result;

	// Profile picture upload
	ImageView img_cell, img_delete_symbol;
	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();
	ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> array_addlist = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> alist1 = new ArrayList<HashMap<String, String>>();

	// Different Services calling
	String link = StaticData.SERVER_URL;
	String url;
	JSONObject json;

	// Update group list
	String friend_email;

	// Variable for image upload
	String result;

	HttpEntity resEntity;
	Dialog d;
	Uri fileUri;// to store image file
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
	static File mediaFile;
	private static final int SELECT_PICTURE = 1;
	private Uri mImageCaptureUri;
	static String selectedImagePath;
	private static final int CROP_FROM_CAMERA = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groups_my_groups_info);

		if (StaticData.isNetworkAvailable(getApplicationContext())) {

			session = new SessionManager(getApplicationContext());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			fillContent();

		} else {
			setContentView(R.layout.global_internet_not_found);
		}

	}

	private void setContent() {
		// TODO Auto-generated method stub
		img_group = (ImageView) findViewById(R.id.img_group);
		btn_add_groupdp = (Button) findViewById(R.id.btn_add_groupdp);
		txt_group_name = (TextView) findViewById(R.id.txt_group_name);
		btn_add_newfriends = (Button) findViewById(R.id.btn_add_newfriends);
		btn_delete_group = (Button) findViewById(R.id.btn_delete_group);
		lv_group_friendlist = (ListView) findViewById(R.id.lv_group_friendlist);
		txt_group_name = (TextView) findViewById(R.id.txt_group_name);

		btn_add_groupdp.setOnClickListener(this);
		txt_group_name.setOnClickListener(this);
		btn_add_newfriends.setOnClickListener(this);
		btn_delete_group.setOnClickListener(this);

	}

	private void fillContent() {
		// TODO Auto-generated method stub
		Intent getval = getIntent();

		group_id_from_mygroups = getval.getStringExtra("group_id");
		group_name = getval.getStringExtra("group_name");
		group_image = getval.getStringExtra("group_image");

		txt_group_name.setText(group_name);
		Picasso.with(MyGroupsInfo.this).load(group_image).resize(165, 165)
				.transform(new RoundedTransformation(360, 0)).into(img_group);

		Log.i(group_id_from_mygroups, group_id_from_mygroups + "," + group_name
				+ "," + group_image);

		url = link + "dispalyfriendgroup.php";
		new MyGroupsProfile().execute("");

	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.txt_group_name:
			final Dialog dialog = new Dialog(MyGroupsInfo.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.groups_edit_groupname);
			dialog.show();

			final EditText edt_change_groupname = (EditText) dialog
					.findViewById(R.id.edt_change_groupname);
			edt_change_groupname.setText(group_name);

			Button btn_okay = (Button) dialog.findViewById(R.id.btn_okay);
			btn_okay.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					group_name = edt_change_groupname.getText().toString();
					if (group_name.equals("")) {
						edt_change_groupname
								.setError("Please enter group name.");
						edt_change_groupname
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View arg0) {
										// TODO Auto-generated method stub
										edt_change_groupname.setError(null);
									}
								});
					} else if (!group_name
							.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {

						edt_change_groupname.setError("Only use alphabets.");

					} else {
						txt_group_name.setText(group_name);
						url = link + "group_name_update.php";
						new MyGroupsProfile().execute("");
						dialog.dismiss();
					}

				}
			});
			Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
			btn_cancel.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub

					dialog.dismiss();

				}
			});

			break;

		case R.id.btn_add_newfriends:

			final Dialog d = new Dialog(MyGroupsInfo.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.friends_select_friends);
			url = StaticData.SERVER_URL + "myfriend.php";
			new MyGroupsProfile().execute("");
			d.show();
			lv_select_friends = (ListView) d
					.findViewById(R.id.lv_select_friends);
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
					Log.i("friend_email", friend_email);
					url = StaticData.SERVER_URL + "editgroupmember.php";
					new MyGroupsProfile().execute("");
					d.dismiss();

				}
			});
			break;

		case R.id.btn_delete_group:

			url = StaticData.SERVER_URL + "delete_group.php";
			new MyGroupsProfile().execute("");

			break;

		case R.id.btn_add_groupdp:

			openDialoge();
			break;
		default:
			break;
		}

	}

	private class MyGroupsProfile extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(MyGroupsInfo.this);

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
		LinkedHashSet<FriendDetail> hashset = new LinkedHashSet<FriendDetail>();
		hashset.addAll(arr_fdetail);
		arr_fdetail.clear();
		arr_fdetail.addAll(hashset);

		json = new JSONObject();

		if (url.equals(link + "dispalyfriendgroup.php")) {
			try {
				json.put("group_id", group_id_from_mygroups);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (url.equals(link + "myfriend.php")) {
			try {
				json.put("email", useremail);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (url.equals(link + "group_name_update.php")) {
			try {
				json.put("group_id", group_id_from_mygroups);
				json.put("group_name", group_name);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (url.equals(link + "editgroupmember.php")) {
			try {
				Log.i("EditGroup Called", "calling editgroupmember.php");
				json.put("user_email", friend_email);
				json.put("group_id", group_id_from_mygroups);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (url.equals(link + "delete_group.php")) {
			try {
				json.put("group_id", group_id_from_mygroups);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.i("Json Passing", "Can't pass json data.");
		}
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);

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
			Response_msg = jobj.getString("ResponseMsg");
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

			JSONArray name = null;

			try {
				if (url.equals(link + "dispalyfriendgroup.php")) {
					name = jobj.getJSONArray("user");

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
					fillist();

				} else if (url.equals(link + "myfriend.php")) {
					arr_fdetail.clear();
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
					fillist1();
					Log.i("myfriend", "" + arr_fdetail);
				} else if (url.equals(link + "group_name_update.php")) {

					Intent pass = new Intent(MyGroupsInfo.this,
							MyGroupsInfo.class);
					pass.putExtra("group_id", group_id_from_mygroups);
					pass.putExtra("group_name", group_name);
					pass.putExtra("group_image", group_image);
					startActivity(pass);
					finish();

				} else if (url.equals(link + "editgroupmember.php")) {

					Log.i("Edit Successfully", "Edit Successfully");
					Intent pass = new Intent(MyGroupsInfo.this,
							MyGroupsInfo.class);
					pass.putExtra("group_id", group_id_from_mygroups);
					pass.putExtra("group_name", group_name);
					pass.putExtra("group_image", group_image);
					startActivity(pass);
					finish();

				} else if (url.equals(link + "delete_group.php")) {

					Log.i("Group delete", "Group delete");
					finish();

				} else {

				}

			} catch (NullPointerException e) {
				e.printStackTrace();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

		}

	}

	private void fillist() {
		// TODO Auto-generated method stub.
		alist.clear();
		friend_email = "";
		ArrayList<String> femail = new ArrayList<String>();
		for (int i = 0; i < arr_fdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", arr_fdetail.get(i).getFname());
			tempName.put("lname", arr_fdetail.get(i).getLname());
			tempName.put("email", arr_fdetail.get(i).getEmail());
			alist.add(tempName);
			femail.add(arr_fdetail.get(i).getEmail());
		}
		Joiner joiner = Joiner.on(",").skipNulls();
		friend_email = joiner.join(femail);
		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_cell_firstname, R.id.txt_cell_lastname,
				R.id.txt_cell_email };

		SpecialAdapter adapter = new SpecialAdapter(this, alist,
				R.layout.groups_row_cell_delete, from, to);

		lv_group_friendlist.setAdapter(adapter);
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

			final TextView txt_cell_email = (TextView) view
					.findViewById(R.id.txt_cell_email);
			img_cell = (ImageView) view.findViewById(R.id.img_cell);

			img_delete_symbol = (ImageView) view
					.findViewById(R.id.img_delete_symbol);
			Log.i("alist", alist + "");
			Log.i("alist", alist1 + "");
			Log.i("array_addlist", "" + array_addlist);
			Log.i("array_fdetail", "" + arr_fdetail);
			img_delete_symbol.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					alist.remove(position);
					arr_fdetail.remove(position);
					fillist();
					notifyDataSetChanged();
					url = StaticData.SERVER_URL + "editgroupmember.php";

					new MyGroupsProfile().execute("");

					HashSet d = new HashSet();
					d.addAll(array_addlist);
					array_addlist.clear();
					array_addlist.addAll(d);

				}

			});
			if (arr_fdetail.get(position).getImage() != null) {
				Picasso.with(MyGroupsInfo.this)
						.load(arr_fdetail.get(position).getImage())
						.transform(new RoundedTransformation(360, 0))
						.resize(165, 165).into(img_cell);
			}
			return view;
		}
	}

	private void fillist1() {
		// TODO Auto-generated method stub.
		array_addlist.clear();
		alist1.clear();
		for (int i = 0; i < arr_fdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", arr_fdetail.get(i).getFname());
			tempName.put("lname", arr_fdetail.get(i).getLname());
			tempName.put("email", arr_fdetail.get(i).getEmail());
			alist1.add(tempName);
		}
		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_friendname, R.id.txt_lastname,
				R.id.txt_friendemail };

		SpecialAdapter1 adapter = new SpecialAdapter1(this, alist1,
				R.layout.friends_select_friend_row_cell_cb_dialog, from, to);

		lv_select_friends.setAdapter(adapter);
	}

	public class SpecialAdapter1 extends SimpleAdapter {
		public SpecialAdapter1(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@SuppressWarnings("unchecked")
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
				Picasso.with(MyGroupsInfo.this)
						.load(arr_fdetail.get(position).getImage())
						.transform(new RoundedTransformation(360, 0))
						.resize(165, 165).into(img_frienddp);
			}

			try {

				for (int i = 0; i < alist.size(); i++) {
					if (txt_friendemail.getText().toString()
							.equals(alist.get(i).get("email"))) {
						chb_selectedfriend.setChecked(true);
						HashMap<String, String> hashmap = new HashMap<String, String>();
						hashmap.put("fname", alist.get(i).get("fname"));
						hashmap.put("lname", alist.get(i).get("lname"));
						hashmap.put("email", alist.get(i).get("email"));
						hashmap.put("profile_pic",
								alist.get(i).get("profile_pic"));
						array_addlist.add(hashmap);
						Log.e("array_addlist1", "1List:" + alist);
						Log.e("Accept Array", "List:" + array_addlist);
						@SuppressWarnings("rawtypes")
						HashSet d = new HashSet();
						d.addAll(array_addlist);
						array_addlist.clear();
						// array_addlist1.clear();
						array_addlist.addAll(d);

					}
				}

				Log.e("Accept Array duplicat", "List:" + array_addlist);
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
							hashmap.put("profile_pic", arr_fdetail
									.get(position).getImage());
							array_addlist.add(hashmap);

						} else {
							Log.e("array_addlist.Size",
									"" + array_addlist.size());

							for (int i = 0; i < array_addlist.size(); i++) {
								if (txt_friendemail
										.getText()
										.toString()
										.equals(array_addlist.get(i).get(
												"email"))) {
									array_addlist.remove(i);
									alist.remove(i);
									break;
								}

							}

						}
					}
				});
			} catch (Exception e) {

			}
			return view;

		}
	}

	private void openDialoge() {
		// TODO Auto-generated method stub
		d = new Dialog(MyGroupsInfo.this);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.global_image_picker_dialog);
		d.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		d.show();

		// Button of Dialog box from image_picker_dialog
		Button btn_camera = (Button) d.findViewById(R.id.btn_camera);
		Button btn_gallery = (Button) d.findViewById(R.id.btn_gallery);

		btn_camera.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				d.dismiss();
				try {
					// takePhotoCamrea();
					captureImage();
				} catch (Exception e) {

				}

			}
		});

		btn_gallery.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				d.dismiss();
				try {
					takePhotoGallery();
				} catch (Exception e) {

				}
			}
		});

		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
			// will close the app if the device does't have camera
			finish();
		}

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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on scren orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}

	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
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

	private void takePhotoGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select File"),
				SELECT_PICTURE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK
				&& data != null) {
			// Let's read picked image data - its URI
			Uri pickedImage = data.getData();
			mImageCaptureUri = pickedImage;
			// Let's read picked image path using content resolver
			String[] filePath = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(pickedImage, filePath,
					null, null, null);
			cursor.moveToFirst();
			String imagePath = cursor.getString(cursor
					.getColumnIndex(filePath[0]));
			selectedImagePath = imagePath;

			/*** Crop image related code ***/

			doCrop();
			Log.e("gallery", "imagepath" + mCurrentPhotoPath);
			// RuntimeException!
			cursor.close();
		}
		if (requestCode == CROP_FROM_CAMERA) {
			Bundle extras = data.getExtras();
			/**
			 * After cropping the image, get the bitmap of the cropped image and
			 * display it on imageview.
			 */
			if (extras != null) {

				Bitmap photo = extras.getParcelable("data");
				int w = photo.getWidth();
				int h = photo.getHeight();
				Log.d("bitmap W", "width" + w);
				Log.d("bitmap H", "height" + h);
				String root = Environment.getExternalStorageDirectory()
						.toString();
				File myDir = new File(root + "/saved_images");
				myDir.mkdirs();
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);
				String fname = "Image-" + n + ".jpg";
				file = new File(myDir, fname);
				if (file.exists())
					file.delete();
				try {
					FileOutputStream out = new FileOutputStream(file);
					photo.compress(Bitmap.CompressFormat.PNG, 100, out);
					Bitmap round_photo = new RoundedTransformation(360, 0)
							.transform(photo);
					img_group.setImageBitmap(round_photo);
					out.flush();
					out.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// uploadFile(file.toString());
			new ImageUpload().execute();
			Log.d("current Uri", "uri" + mImageCaptureUri);

			/**
			 * Delete the temporary image
			 */
		}
		try {

			if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				if (resultCode == RESULT_OK) {
					// successfully captured the image
					// display it in image view

					selectedImagePath = mediaFile.toString();
					mImageCaptureUri = Uri.fromFile(mediaFile);
					// previewCapturedImage();
					doCrop();
				} else if (resultCode == RESULT_CANCELED) {
					// user cancelled Image capture
					Toast.makeText(getApplicationContext(),
							"you canceled image capture", Toast.LENGTH_SHORT)
							.show();
				} else {
					// failed to capture image
					Toast.makeText(getApplicationContext(),
							"Sorry! Failed to capture image",
							Toast.LENGTH_SHORT).show();
				}
			}
			// previewCapturedImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
		/**
		 * Open image crop app by starting an intent
		 * ‘com.android.camera.action.CROP‘.
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		/**
		 * Check if there is image cropper app installed.
		 */
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		/**
		 * If there is no image cropper app, display warning message
		 */
		if (size == 0) {

			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			/**
			 * Specify the image path, crop dimension and scale
			 */
			Log.i("mImageCaptureUri", "" + mImageCaptureUri);

			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 350);
			intent.putExtra("outputY", 350);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);
			/**
			 * There is posibility when more than one image cropper app exist,
			 * so we have to check for it first. If there is only one app, open
			 * then app.
			 */

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				/**
				 * If there are several app exist, create a custom chooser to
				 * let user selects the app.
				 */
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	public class CropOptionAdapter extends ArrayAdapter<CropOption> {
		private ArrayList<CropOption> mOptions;
		private LayoutInflater mInflater;

		public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
			super(context, R.layout.global_crop_selector, options);

			mOptions = options;

			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.global_crop_selector,
						null);

			CropOption item = mOptions.get(position);

			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon))
						.setImageDrawable(item.icon);
				((TextView) convertView.findViewById(R.id.tv_name))
						.setText(item.title);

				return convertView;
			}

			return null;
		}
	}

	public class CropOption {
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}

	private class ImageUpload extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(MyGroupsInfo.this);

		protected void onPreExecute() {
			Dialog.setMessage("Uploading group image...");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {

				doFileUpload(file);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			if (result.endsWith("1")) {
				Toast.makeText(getApplicationContext(), "Upload Successfully.",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getApplicationContext(), "Server Problem...",
						Toast.LENGTH_SHORT).show();

			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void doFileUpload(File file_path) {

		Log.d("Uri", "Do file path" + file_path);

		try {
			ServerUploadPath = StaticData.SERVER_URL + "group_dp_change.php";
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(ServerUploadPath);

			Log.d("ServerPath", "Path" + ServerUploadPath);

			FileBody bin1 = new FileBody(file_path);
			Log.d("Enter", "Filebody complete " + bin1);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("uploaded_file", bin1);
			reqEntity.addPart("group_id",
					new StringBody(group_id_from_mygroups));

			post.setEntity(reqEntity);
			Log.d("Enter", "Image send complete");

			HttpResponse response = client.execute(post);
			resEntity = response.getEntity();
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
}
