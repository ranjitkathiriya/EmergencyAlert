package com.example.emergencyalert;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contact.SelectContact;
import com.example.parser.RoundedTransformation;

public class DpUpload extends Activity implements OnClickListener {

	Button btn_select_image;
	ImageView img_profile_picture;
	TextView txt_skip;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	Uri fileUri;// to store image file
	private Uri mImageCaptureUri;

	// Variable for ImageUpload
	String ServerUploadPath, mCurrentPhotoPath, result;
	String result_upload;
	Dialog d;
	private static final int SELECT_PICTURE = 1;
	private static final String TAG = "upload";
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CROP_FROM_CAMERA = 2;

	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";
	public String Response_code;
	private int serverResponseCode = 0;
	private ProgressDialog dialog = null;
	static File mediaFile, file;
	Bitmap rotatedBMP;
	static String selectedImagePath;
	HttpEntity resEntity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_dp_upload);

		session = new SessionManager(getApplicationContext());
		HashMap<String, String> user = session.getUserDetails();
		useremail = user.get(SessionManager.KEY_EMAIL);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContent();

		ServerUploadPath = StaticData.SERVER_URL + "picture_upload.php";
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

	}

	private void setContent() {
		// TODO Auto-generated method stub

		img_profile_picture = (ImageView) findViewById(R.id.img_profile_picture);
		txt_skip = (TextView) findViewById(R.id.txt_skip);
		btn_select_image = (Button) findViewById(R.id.btn_select_image);

		btn_select_image.setOnClickListener(this);
		txt_skip.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_select_image:

			openDialoge();
			break;

		case R.id.txt_skip:

			Intent i = new Intent(DpUpload.this, SelectContact.class);
			startActivity(i);
			finish();
		default:
			break;
		}

	}

	// Image Upload Start
	private void openDialoge() {
		// TODO Auto-generated method stub
		d = new Dialog(DpUpload.this);
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
			Toast.makeText(getApplicationContext(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
			// will close the app if the device does't have camera
			finish();
		}

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

	private void takePhotoGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select File"),
				SELECT_PICTURE);
		// Intent intent = new Intent();
		// intent.setType("image/*");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// startActivityForResult(Intent.createChooser(intent,
		// "Select Picture"),
		// SELECT_PICTURE);
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
					Bitmap photo1 = new RoundedTransformation(360, 0)
							.transform(photo);
					img_profile_picture.setImageBitmap(photo1);

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

	private class ImageUpload extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(DpUpload.this);

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
				Toast.makeText(getApplicationContext(), "Upload Successfully.",
						Toast.LENGTH_SHORT).show();
				Intent i = new Intent(DpUpload.this, SelectContact.class);
				startActivity(i);
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

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(ServerUploadPath);

			Log.d("ServerPath", "Path" + ServerUploadPath);

			FileBody bin1 = new FileBody(file_path);
			Log.d("Enter", "Filebody complete " + bin1);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("uploaded_file", bin1);
			reqEntity.addPart("email", new StringBody(useremail));

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

	public int uploadFile(String sourceFileUri) {

		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile()) {

			dialog.dismiss();

			Log.e("uploadFile", "Source File not exist :" + mCurrentPhotoPath);

			runOnUiThread(new Runnable() {
				public void run() {
				}
			});

			return 0;

		} else {
			try {

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				URL url = new URL(ServerUploadPath);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);

				dos = new DataOutputStream(conn.getOutputStream());
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"email\""
						+ lineEnd);
				dos.writeBytes(lineEnd);

				// assign value
				dos.writeBytes(useremail);
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {

					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(DpUpload.this,
									"File Upload Complete.", Toast.LENGTH_SHORT)
									.show();
						}
					});
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(DpUpload.this, "MalformedURLException",
								Toast.LENGTH_SHORT).show();
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {

				// dialog.dismiss();
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						// Toast.makeText(UserProfileActivity.this,
						// "Got Exception : see logcat ",
						// Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
			return serverResponseCode;

		} // End else block
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
