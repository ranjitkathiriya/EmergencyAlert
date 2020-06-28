package com.example.group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import android.content.res.Configuration;
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

public class CreateGroup extends Activity implements OnClickListener {

	// Elements of Create group
	ImageView img_group, img_frienddp;
	EditText edt_get_groupname;
	Button btn_add_groupdp, btn_add_friend, btn_create_group;
	ListView lv_friendlist;
	String groupname;
	TextView txt_please_wait_friend;

	// Variable for get Current user Email
	SessionManager session;
	String useremail, profile_pic;

	// Variable for Background process
	JSONObject jobj;
	String Response_code, result;
	HttpEntity resEntity;
	public String login_result;
	int flag = 0;

	String ServerUploadPath, TAG = "UploadFile", mCurrentPhotoPath;
	File file;
	ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
	String friend_email;
	boolean check_listfill = false;

	// Variable for image upload
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

	// Different Services calling
	String link = StaticData.SERVER_URL;
	String url;
	JSONObject json;

	// Profile picture upload
	ListView lv_select_friends;

	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();
	ArrayList<HashMap<String, String>> array_addlist = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groups_create_group);
		if (StaticData.isNetworkAvailable(getApplicationContext())) {
			session = new SessionManager(getApplicationContext());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);
			setContent();
			bindClickListeners();
			try {
				fillist();
				Log.i("check_listfill", "" + check_listfill);
				check_listfill = true;
			} catch (Exception e) {
				Log.i("check_listfill", "" + check_listfill);
			}

		} else {
			setContentView(R.layout.global_internet_not_found);

		}

	}

	private void setContent() {
		// TODO Auto-generated method stub
		img_group = (ImageView) findViewById(R.id.img_group);

		edt_get_groupname = (EditText) findViewById(R.id.edt_get_groupname);

		btn_add_groupdp = (Button) findViewById(R.id.btn_add_groupdp);
		btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
		btn_create_group = (Button) findViewById(R.id.btn_create_group);

		lv_friendlist = (ListView) findViewById(R.id.lv_friendlist);

	}

	private void bindClickListeners() {
		// TODO Auto-generated method stub
		btn_add_groupdp.setOnClickListener(this);
		btn_add_friend.setOnClickListener(this);
		btn_create_group.setOnClickListener(this);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btn_create_group:

			groupname = edt_get_groupname.getText().toString();
			if (!check_listfill) {
				Toast.makeText(getApplicationContext(),
						"Please select friends.", Toast.LENGTH_SHORT).show();
			} else {
				if (groupname.equals("")) {
					edt_get_groupname.setError("Please enter group name.");
					edt_get_groupname.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							edt_get_groupname.setError(null);
						}
					});

				} else {
					try {
						new CreateGroupDataUpload().execute();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;

		case R.id.btn_add_friend:

			final Dialog d = new Dialog(CreateGroup.this);
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.friends_select_friends);
			url = StaticData.SERVER_URL + "myfriend.php";
			new FillSelectList().execute("");
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
					fillist();
					d.dismiss();
				}
			});
			break;
		case R.id.btn_add_groupdp:
			try {

				openDialoge();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	private class FillSelectList extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(CreateGroup.this);

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
			// Log.i("result", result);
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
				}
			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			txt_please_wait_friend.setText("No friends to show");

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

		SpecialAdapter adapter = new SpecialAdapter(getApplicationContext(),
				alist, R.layout.friends_select_friend_row_cell_cb_dialog, from,
				to);

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
				Picasso.with(CreateGroup.this)
						.load(arr_fdetail.get(position).getImage())
						.transform(new RoundedTransformation(360, 0))
						.resize(165, 165).into(img_frienddp);
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

						Log.e("add", "Check");
						Log.i("add fname", arr_fdetail.get(position).getFname());
						Log.i("add lname", arr_fdetail.get(position).getLname());
						Log.i("add email", arr_fdetail.get(position).getEmail());
						Log.i("add profile_pic", arr_fdetail.get(position)
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

								Log.e("Remove", "UnCheck");
								Log.i("Remove fname", arr_fdetail.get(position)
										.getFname());
								Log.i("Remove lname", arr_fdetail.get(position)
										.getLname());
								Log.i("Remove email", arr_fdetail.get(position)
										.getEmail());
								Log.i("Remove profile_pic",
										arr_fdetail.get(position).getImage());

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

	private void fillist() {
		// TODO Auto-generated method stub.

		ArrayList<String> femail = new ArrayList<String>();
		friend_email = "";
		alist.clear();
		for (int i = 0; i < array_addlist.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", array_addlist.get(i).get("fname"));
			tempName.put("lname", array_addlist.get(i).get("lname"));
			tempName.put("email", array_addlist.get(i).get("email"));
			tempName.put("profile_pic", array_addlist.get(i).get("profile_pic"));
			femail.add(array_addlist.get(i).get("email"));
			alist.add(tempName);
		}

		Joiner joiner = Joiner.on(",").skipNulls();
		friend_email = joiner.join(femail);

		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_friendname, R.id.txt_lastname,
				R.id.txt_friendemail };
		Log.e("Group", "List:-" + alist);
		SpecialAdapter1 adapter = new SpecialAdapter1(this, alist,
				R.layout.friends_row_cell_display, from, to);

		lv_friendlist.setAdapter(adapter);

	}

	public class SpecialAdapter1 extends SimpleAdapter {
		public SpecialAdapter1(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			img_frienddp = (ImageView) view.findViewById(R.id.img_frienddp);
			if (alist.get(position).get("profile_pic") != null) {
				Picasso.with(CreateGroup.this)
						.load(alist.get(position).get("profile_pic"))
						.transform(new RoundedTransformation(360, 0))
						.resize(165, 165).into(img_frienddp);
			}
			return view;
		}
	}

	// Image Upload Start
	private void openDialoge() {
		// TODO Auto-generated method stub
		d = new Dialog(CreateGroup.this);
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
					// setting flag for upload group image or use default
					flag = 1;
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
							"User cancelled image capture", Toast.LENGTH_SHORT)
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
				// previewCapturedImage();
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

	private class CreateGroupDataUpload extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(CreateGroup.this);

		protected void onPreExecute() {
			Dialog.setMessage("Uploading group data...");
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
				Toast.makeText(getApplicationContext(),
						"Create Group Successfully.", Toast.LENGTH_SHORT)
						.show();
				// Intent i = new Intent(CreateGroup.this, HomeScreen.class);
				// startActivity(i);
				finish();

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
			ServerUploadPath = StaticData.SERVER_URL + "create_group.php";
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(ServerUploadPath);
			MultipartEntity reqEntity = new MultipartEntity();

			Log.d("ServerPath", "Path" + ServerUploadPath);
			Log.d("File file", file + "");
			if (flag == 1) {
				FileBody bin1 = new FileBody(file_path);
				Log.d("Enter", "Filebody complete " + bin1);

				reqEntity.addPart("uploaded_file", bin1);
				Log.i("File uploading", "");
			}
			reqEntity.addPart("group_name", new StringBody(groupname));
			reqEntity.addPart("group_admin", new StringBody(useremail));
			reqEntity.addPart("group_member", new StringBody(friend_email));
			Log.i("Group sended data", groupname + "," + useremail + ","
					+ friend_email);

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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume: " + this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

}
