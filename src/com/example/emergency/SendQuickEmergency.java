package com.example.emergency;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contact.ContactDetail;
import com.example.emergencyalert.DBhelper;
import com.example.emergencyalert.GPSTracker;
import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class SendQuickEmergency extends Fragment implements OnClickListener {

	Button btn1;
	View quickView;
	Typeface USERNAME;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	// Other DATA
	DBhelper dbh;
	String link = "http://maps.google.com/maps?q=";
	double latitude;
	double longitude;
	GPSTracker gps;
	String phoneno, message;
	String address, city, country;

	// DATA for retrive 5 contacts 1st time
	String contact = "";
	EditText[] edt_ph = new EditText[6];
	ImageButton[] im = new ImageButton[6];
	String ph1, ph2, ph3, ph4, ph5, ph = "";
	int a = 0;
	Button btn_set;
	final String MobilePattern = "^(1\\-)?[0-9]{3}\\-?[0-9]{3}\\-?[0-9]{4}$";
	Dialog dialog2;
	TextView textVi;

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		quickView = inflater.inflate(R.layout.send_quick_emergency, null);

		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");

		btn1 = (Button) quickView.findViewById(R.id.btn_yes);

		btn1.setOnClickListener(this);

		dbh = new DBhelper(getActivity());

		session = new SessionManager(getActivity());
		HashMap<String, String> user = session.getUserDetails();
		useremail = user.get(SessionManager.KEY_EMAIL);

		session = new SessionManager(getActivity());

		return quickView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_yes:
			contact = dbh.selectrecord(useremail);
			if (contact == null) {
				selectContacts();
			} else {
				if (StaticData.isNetworkAvailable(getActivity())) {
					getlocation();
				} else {
					sendSMSofline();
				}
			}
			break;

		default:
			break;
		}

	}

	private void selectContacts() {

		// TODO Auto-generated method stub
		dialog2 = new Dialog(getActivity());
		dialog2.getWindow();
		dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog2.setContentView(R.layout.contactlist_select_contact_dialog);
		Window window = dialog2.getWindow();
		window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setcontectd();
		im[1].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent1 = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);

				startActivityForResult(intent1, 1);
			}
		});
		im[2].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent2 = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);

				startActivityForResult(intent2, 2);
			}
		});
		im[3].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent3 = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);

				startActivityForResult(intent3, 3);
			}
		});
		im[4].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent4 = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);

				startActivityForResult(intent4, 4);
			}
		});
		im[5].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent5 = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);

				startActivityForResult(intent5, 5);
			}
		});
		btn_set.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				add();
				if (edt_ph[1].getText().toString().length() != 0) {

					if (!ph1.matches(MobilePattern)) {
						edt_ph[1].setError("Enter valid Number");
						a++;
					}

				}
				if (edt_ph[2].getText().toString().length() != 0) {
					if (!ph2.matches(MobilePattern)) {
						edt_ph[2].setError("Enter valid Number");
						a++;

					}
				}
				if (edt_ph[3].getText().toString().length() != 0) {
					if (!ph3.matches(MobilePattern)) {
						edt_ph[3].setError("Enter valid Number");
						a++;

					}
				}
				if (edt_ph[4].getText().toString().length() != 0) {
					if (!ph4.matches(MobilePattern)) {
						edt_ph[4].setError("Enter valid Number");
						a++;

					}
				}
				if (edt_ph[5].getText().toString().length() != 0) {
					if (!ph5.matches(MobilePattern)) {
						edt_ph[5].setError("Enter valid Number");
						a++;
					}
				}

				if (a == 0) {

					convert();
					ContactDetail cd = new ContactDetail();
					cd.setEmail(useremail);
					cd.setContact(ph);

					dbh.insertRecord(cd);
					Toast.makeText(getActivity(), ph + " Added Successfully",
							Toast.LENGTH_SHORT).show();
					dialog2.dismiss();
				} else {

					a = 0;
				}
			}

			private void convert() {
				// TODO Auto-generated method stub
				if (ph1.length() != 0) {
					ph = ph + edt_ph[1].getText().toString() + ",";
				}
				if (ph2.length() != 0) {
					ph = ph + edt_ph[2].getText().toString() + ",";
				}
				if (ph3.length() != 0) {
					ph = ph + edt_ph[3].getText().toString() + ",";
				}
				if (ph4.length() != 0) {
					ph = ph + edt_ph[4].getText().toString() + ",";
				}
				if (ph5.length() != 0) {
					ph = ph + edt_ph[5].getText().toString() + ",";
				}

			}

			private void add() {
				// TODO Auto-generated method stub
				ph1 = edt_ph[1].getText().toString();
				ph2 = edt_ph[2].getText().toString();
				ph3 = edt_ph[3].getText().toString();
				ph4 = edt_ph[4].getText().toString();
				ph5 = edt_ph[5].getText().toString();
			}

		});
		dialog2.show();

	}

	private void setcontectd() {
		// TODO Auto-generated method stub
		edt_ph[1] = (EditText) dialog2.findViewById(R.id.edt_ph1);
		edt_ph[2] = (EditText) dialog2.findViewById(R.id.edt_ph2);
		edt_ph[3] = (EditText) dialog2.findViewById(R.id.edt_ph3);
		edt_ph[4] = (EditText) dialog2.findViewById(R.id.edt_ph4);
		edt_ph[5] = (EditText) dialog2.findViewById(R.id.edt_ph5);
		btn_set = (Button) dialog2.findViewById(R.id.set);
		im[1] = (ImageButton) dialog2.findViewById(R.id.im1);
		im[2] = (ImageButton) dialog2.findViewById(R.id.im2);
		im[3] = (ImageButton) dialog2.findViewById(R.id.im3);
		im[4] = (ImageButton) dialog2.findViewById(R.id.im4);
		im[5] = (ImageButton) dialog2.findViewById(R.id.im5);
		textVi = (TextView) dialog2.findViewById(R.id.textVi);
		textVi.setVisibility(View.GONE);

	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			Cursor c = getActivity().managedQuery(contactData, null, null,
					null, null);
			if (c.moveToFirst()) {
				String hasPhone = c
						.getString(c
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

				String id = c.getString(c
						.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

				if (hasPhone.equalsIgnoreCase("1")) {
					Cursor phones = getActivity().getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + id, null, null);
					phones.moveToFirst();
					String cNumber = phones.getString(phones
							.getColumnIndex("data1"));

					try {

						edt_ph[reqCode].setText(trimContact(cNumber + ""));
						if (reqCode < 5)
							edt_ph[reqCode + 1].requestFocus();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}

			}
		}
	}

	String trimContact(String m) {
		// TODO Auto-generated method stub

		m = m.replace(" ", "");
		m = m.replace("+91", "");
		m = m.replace("-", "");
		m = m.replace(")", "");
		m = m.replace("(", "");
		return m;

	}

	protected void sendSMSMessage() {
		String contact = dbh.selectrecord(useremail);
		String message = "I need help.My location is " + address + "," + city
				+ "," + link + latitude + "," + longitude;

		if (message.length() > 160) {
			try {
				message = "I need help.My location is " + address + "," + city;
				for (String phoneNo : contact.split(",")) {

					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(phoneNo, null, message, null,
							null);
					Toast.makeText(getActivity(),
							"Message Send Succesfully." + message,
							Toast.LENGTH_SHORT).show();
					Log.d("msg", message + "");
					Log.d("phone", phoneNo + "");

				}
			} catch (Exception e) {
				Toast.makeText(getActivity(), "SMS faild, please try again.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

		} else {
			try {
				for (String phoneNo : contact.split(",")) {

					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(phoneNo, null, message, null,
							null);
					Toast.makeText(getActivity(),
							"Message Send Succesfully." + message,
							Toast.LENGTH_SHORT).show();
					Log.d("msg", message + "");
					Log.d("phone", phoneNo + "");

				}
			} catch (Exception e) {
				Toast.makeText(getActivity(), "SMS faild, please try again.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}

	protected void sendSMSofline() {

		String message = "I am in Emergency,I need help ";

		try {
			for (String phoneNo : contact.split(",")) {

				Toast.makeText(getActivity(), contact, Toast.LENGTH_SHORT)
						.show();
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(phoneNo, null, message, null, null);
				Toast.makeText(getActivity(),
						"Message Send Succesfully." + message,
						Toast.LENGTH_LONG).show();

			}
		} catch (Exception e) {
			Toast.makeText(getActivity(), "SMS faild, please try again.",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

	}

	protected void getlocation() {

		try {
			gps = new GPSTracker(getActivity());

			// check if GPS enabled
			if (gps.canGetLocation()) {

				latitude = gps.getLatitude();
				longitude = gps.getLongitude();
				getAddress();

			} else {
				gps.showSettingsAlert();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void getAddress() {

		try {
			Geocoder geocorder = new Geocoder(getActivity(),
					Locale.getDefault());
			List<Address> addresses = null;
			addresses = geocorder.getFromLocation(latitude, longitude, 1);
			address = addresses.get(0).getAddressLine(0);
			city = addresses.get(0).getAddressLine(1);
			country = addresses.get(0).getAddressLine(2);
			sendSMSMessage();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
