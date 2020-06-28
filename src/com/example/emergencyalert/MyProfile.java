package com.example.emergencyalert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emergencyalert.R;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.example.slidingmenu.SlidingMenuFragmentActivity;
import com.squareup.picasso.Picasso;

public class MyProfile extends Fragment implements OnClickListener {

	private LinearLayout ll_view_profile;
	ImageView img_userdp;
	EditText edt_fname, edt_lname, edt_contactno, edt_pass;
	TextView txt_fname, txt_lname, txt_email, txt_contactno, myprof_username;

	String fname, lname, email, password, confirmpassword, contactno;

	// E-mail and Contact number pattern matching
	// matches 9999999999, 1-999-999-9999 and 999-999-9999
	final String MobilePattern = "^(1\\-)?[0-9]{3}\\-?[0-9]{3}\\-?[0-9]{4}$";
	final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	Button btn_edit, btn_saves;
	ImageButton btn_changedp;
	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	String mCurrentPhotoPath;
	String ServerUploadPath;
	String upload_id, photo_click_id, selectedImagePath;

	File photoFile = null;
	HttpEntity resEntity;
	String result_upload;
	Bitmap rotatedBMP;

	int SELECT_PICTURE = 1, REQUEST_TAKE_PHOTO = 1;
	ImageView img_profile_picture;

	File file;

	// for session manager
	String fn, ln, photo, user_email, user_password;
	static Bitmap map;

	// Profile picture upload
	Bitmap bitmap;
	ProgressDialog pDialog;
	String picture_path;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	Uri fileUri;// to store image file
	private Uri mImageCaptureUri;

	// Variable for ImageUpload
	String result;
	Dialog d;
	private static final String TAG = "upload";
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CROP_FROM_CAMERA = 2;

	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
	static File mediaFile;

	View viewProfile;
	Typeface USERNAME;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		viewProfile = inflater.inflate(R.layout.profile_myprofile, null);

		ll_view_profile = (LinearLayout) viewProfile
				.findViewById(R.id.ll_view_profile);
		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");

		StaticData.setFont(ll_view_profile, USERNAME);
		ServerUploadPath = StaticData.SERVER_URL + "picture_upload.php";

		session = new SessionManager(getActivity());
		HashMap<String, String> user = session.getUserDetails();
		useremail = user.get(SessionManager.KEY_EMAIL);
		user_email = user.get(SessionManager.KEY_EMAIL);
		user_password = user.get(SessionManager.KEY_Password);
		fn = user.get(SessionManager.KEY_fname);
		ln = user.get(SessionManager.KEY_lname);

		setContent();

		if (StaticData.isNetworkAvailable(getActivity())) {

			new ProfileContentDisplay().execute("");

		} else {
			viewProfile = inflater.inflate(R.layout.global_internet_not_found,
					container, false);
		}
		btn_edit.setOnClickListener(this);
		btn_changedp.setOnClickListener(this);
		btn_saves.setOnClickListener(this);

		return viewProfile;
	}

	private void setContent() {
		// TODO Auto-generated method stub

		img_profile_picture = (ImageView) viewProfile
				.findViewById(R.id.img_userdp);
		btn_edit = (Button) viewProfile.findViewById(R.id.btn_edit);
		btn_saves = (Button) viewProfile.findViewById(R.id.btn_saves);
		btn_changedp = (ImageButton) viewProfile
				.findViewById(R.id.btn_changedp);

		txt_fname = (TextView) viewProfile.findViewById(R.id.txt_fname);
		txt_lname = (TextView) viewProfile.findViewById(R.id.txt_lname);
		txt_contactno = (TextView) viewProfile.findViewById(R.id.txt_contactno);
		txt_email = (TextView) viewProfile.findViewById(R.id.txt_email);

		myprof_username = (TextView) viewProfile
				.findViewById(R.id.myprof_user_name);
		edt_fname = (EditText) viewProfile.findViewById(R.id.edt_fname);
		edt_lname = (EditText) viewProfile.findViewById(R.id.edt_lname);
		edt_contactno = (EditText) viewProfile.findViewById(R.id.edt_contactno);
		edt_pass = (EditText) viewProfile.findViewById(R.id.edt_pass);

		edt_contactno.setVisibility(View.GONE);
		edt_fname.setVisibility(View.GONE);
		edt_lname.setVisibility(View.GONE);
		edt_pass.setVisibility(View.GONE);
		btn_saves.setVisibility(View.GONE);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btn_edit:
			btn_edit.setVisibility(View.GONE);
			btn_saves.setVisibility(View.VISIBLE);

			txt_contactno.setVisibility(View.GONE);
			txt_email.setVisibility(View.GONE);
			txt_fname.setVisibility(View.GONE);
			txt_lname.setVisibility(View.GONE);
			edt_contactno.setVisibility(View.VISIBLE);
			edt_fname.setVisibility(View.VISIBLE);
			edt_lname.setVisibility(View.VISIBLE);
			if (!StaticData.loginTypeFb) {
				edt_pass.setVisibility(View.VISIBLE);
			}

			new ProfileContentDisplay().execute("");

			break;

		case R.id.btn_changedp:
			openDialoge();
			break;
		case R.id.btn_saves:
			try {
				checkeditdata();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	private class ProfileContentDisplay extends AsyncTask<String, Void, Void> {
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

		String result = "";
		try {
			JSONObject json = new JSONObject();
			json.put("email", useremail);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);
			String link = StaticData.SERVER_URL;
			String url = link + "viewprofile.php";

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
			JSONArray name = null;
			try {
				name = jobj.getJSONArray("userdetail");
				for (int i = 0; i < name.length(); i++) {
					JSONObject c = name.getJSONObject(i);
					if (edt_contactno.getVisibility() == android.view.View.GONE) {
						txt_fname.setText(c.getString("fname"));
						txt_lname.setText(c.getString("lname"));
						txt_contactno.setText(c.getString("contact_no"));
						txt_email.setText(c.getString("email"));

						myprof_username.setText(c.getString("fname") + " "
								+ c.getString("lname"));
					} else {
						edt_fname.setText(c.getString("fname"));
						edt_lname.setText(c.getString("lname"));
						edt_contactno.setText(c.getString("contact_no"));
						edt_pass.setText(c.getString("password"));

					}
					user_email = c.getString("email");
					user_password = c.getString("password");
					fn = c.getString("fname");
					ln = c.getString("lname");

					Log.i("NAMeMY", fn + ln);

					picture_path = c.getString("profile_pic");
					String link = StaticData.SERVER_IMAGE_URL;

					photo = link + picture_path;
					new DPinSession().execute("");

					Picasso.with(getActivity()).load(link + picture_path)
							.resize(165, 165)
							.transform(new RoundedTransformation(360, 0))
							.into(img_profile_picture);

				}
			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			Toast.makeText(getActivity(), "Registration Failed",
					Toast.LENGTH_SHORT).show();
		}

	}

	private class DPinSession extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

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
				Log.d("mypro_sav session", user_email + "   " + user_password
						+ " " + fn + " " + ln + " " + photo);

				session.createLoginSession(user_email, user_password, fn, ln,
						map);

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

	private void openDialoge() {
		// TODO Auto-generated method stub
		d = new Dialog(getActivity());
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(R.layout.global_image_picker_dialog);
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
			Toast.makeText(getActivity(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
		}

	}

	private boolean isDeviceSupportCamera() {
		if (getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/*
	 * Capturing Camera Image will lauch camera app requrest image capture
	 */
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on scren orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}

	private void takePhotoGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select File"),
				SELECT_PICTURE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PICTURE
				&& resultCode == getActivity().RESULT_OK && data != null) {
			// Let's read picked image data - its URI
			Uri pickedImage = data.getData();
			mImageCaptureUri = pickedImage;
			// Let's read picked image path using content resolver
			String[] filePath = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(
					pickedImage, filePath, null, null, null);
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
					img_profile_picture.setImageBitmap(round_photo);

					out.flush();
					out.close();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			new ImageUpload().execute();
			Log.d("current Uri", "uri" + mImageCaptureUri);

			/**
			 * Delete the temporary image
			 */

		}
		try {

			if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				if (resultCode == getActivity().RESULT_OK) {
					// successfully captured the image
					// display it in image view

					selectedImagePath = mediaFile.toString();
					mImageCaptureUri = Uri.fromFile(mediaFile);
					// previewCapturedImage();
					doCrop();
				} else if (resultCode == getActivity().RESULT_CANCELED) {

				} else {
					// failed to capture image
					Toast.makeText(getActivity(),
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
		List<ResolveInfo> list = getActivity().getPackageManager()
				.queryIntentActivities(intent, 0);

		int size = list.size();

		/**
		 * If there is no image cropper app, display warning message
		 */
		if (size == 0) {

			Toast.makeText(getActivity(), "Can not find image crop app",
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

					co.title = getActivity().getPackageManager()
							.getApplicationLabel(
									res.activityInfo.applicationInfo);
					co.icon = getActivity().getPackageManager()
							.getApplicationIcon(
									res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getActivity(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
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
							getActivity().getContentResolver().delete(
									mImageCaptureUri, null, null);
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

	private class ImageUpload extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

		protected void onPreExecute() {
			Dialog.setMessage("Uploading Image...");
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
				Log.d("Dp chAnge", user_email + "   " + user_password + " "
						+ fn + " " + ln + " " + photo);

				new DPcnginSession().execute("");

				Toast.makeText(getActivity(), "Upload Successfully.",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getActivity(), "Server Problem...",
						Toast.LENGTH_SHORT).show();

			}
		}
	}

	private class DPcnginSession extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

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
				Log.d("mypro_sav session", user_email + "   " + user_password
						+ " " + fn + " " + ln + " " + photo);

				session.createLoginSession(user_email, user_password, fn, ln,
						map);
				Toast.makeText(getActivity(), "Upload Successfully",
						Toast.LENGTH_LONG).show();
				startActivity(new Intent(getActivity(),
						SlidingMenuFragmentActivity.class));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null,
				null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void doFileUpload(File file_path) {

		Log.d("Uri", "Do file path" + file_path);

		try {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(ServerUploadPath);

			Log.d("ServerPath", "Path" + ServerUploadPath);

			FileBody bin1 = new FileBody(file_path);
			Log.d("Enter", "Filebody complete " + bin1);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("uploaded_file", bin1);
			reqEntity.addPart("email", new StringBody(useremail));
			post.setEntity(reqEntity);
			Log.d("Enter", "email send complete");

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
					String Imagepath = jobj.getString("ImageName");
					String link = StaticData.SERVER_IMAGE_URL;

					photo = link + Imagepath;
					Log.i("IMgIMg", photo);

				}
			} catch (Exception ex) {
				Log.e("Debug", "error: " + ex.getMessage(), ex);
			}
		} catch (Exception e) {
			Log.e("Upload Exception", "");
			e.printStackTrace();
		}
	}

	private void checkeditdata() {
		// TODO Auto-generated method stub
		fname = edt_fname.getText().toString();
		lname = edt_lname.getText().toString();
		contactno = edt_contactno.getText().toString();
		password = edt_pass.getText().toString();

		if (fname.equals("") || !fname.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {

			edt_fname.setError("Enter valid firstname format");

		}

		else if (lname.equals("")
				|| !lname.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {

			edt_lname.setError("Enter valid lastname format");

		} else if (contactno.equals("") || !contactno.matches(MobilePattern)) {

			edt_contactno.setError("Enter valid contact number");

		} else if (password.equals("")) {

			edt_pass.setError("Please enter password");

		} else if (password.length() < 6) {

			edt_pass.setError("Choose password length minimum 6 character.");

		}

		else if (StaticData.isNetworkAvailable(viewProfile.getContext())) {

			new SearchUserOperation().execute("");

		} else {
			Toast.makeText(getActivity(), "Internet not connected",
					Toast.LENGTH_SHORT).show();
		}
		Log.i("userdata", "" + fname + "" + lname + "" + contactno + "");

	}

	private class SearchUserOperation extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			postData_edit();
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				result_edit();

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	private void postData_edit() {
		// TODO Auto-generated method stub

		String result = "";
		try {
			JSONObject json = new JSONObject();
			json.put("fname", fname);
			json.put("lname", lname);
			json.put("contact_no", contactno);
			json.put("email", email);
			json.put("pass", password);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);
			String link = StaticData.SERVER_URL;
			String url = link + "editprofile.php";

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

	private void result_edit() {
		if (login_result.equals("1")) {
			Toast.makeText(getActivity(), "Update profile Successfully",
					Toast.LENGTH_SHORT).show();

			btn_edit.setVisibility(View.VISIBLE);
			btn_saves.setVisibility(View.GONE);

			txt_contactno.setVisibility(View.VISIBLE);
			txt_email.setVisibility(View.VISIBLE);
			txt_fname.setVisibility(View.VISIBLE);
			txt_lname.setVisibility(View.VISIBLE);
			edt_contactno.setVisibility(View.GONE);
			edt_fname.setVisibility(View.GONE);
			edt_lname.setVisibility(View.GONE);
			edt_pass.setVisibility(View.GONE);

			new ProfileContentDisplay().execute("");

		} else {
			Toast.makeText(getActivity(), "Update Failed", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Log.i(TAG, "onResume: " + getActivity());
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

}
