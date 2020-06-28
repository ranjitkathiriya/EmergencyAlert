package com.example.contact;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.emergencyalert.DBhelper;
import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.slidingmenu.SlidingMenuFragmentActivity;

public class SelectContact extends Activity {
	EditText[] edt_ph = new EditText[6];
	ImageButton[] im = new ImageButton[6];
	String ph1, ph2, ph3, ph4, ph5, ph = "";
	int a = 0;
	Button btn_set;
	final String MobilePattern = "^(1\\-)?[0-9]{3}\\-?[0-9]{3}\\-?[0-9]{4}$";
	TextView tv;
	DBhelper dbh;
	SessionManager session;
	String useremail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist_select_contact_dialog);
		setContentView(R.layout.contactlist_select_contact_dialog);
		dbh = new DBhelper(this);

		session = new SessionManager(getApplicationContext());
		HashMap<String, String> user = session.getUserDetails();
		useremail = "kk@k.com";

		session = new SessionManager(getApplicationContext());

		// TODO Auto-generated method stub
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

					Intent i = new Intent(SelectContact.this, SlidingMenuFragmentActivity.class);
					startActivity(i);
					finish();
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
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(),
						SlidingMenuFragmentActivity.class));
				finish();
			}
		});

	}

	private void setcontectd() {
		// TODO Auto-generated method stub
		edt_ph[1] = (EditText) findViewById(R.id.edt_ph1);
		edt_ph[2] = (EditText) findViewById(R.id.edt_ph2);
		edt_ph[3] = (EditText) findViewById(R.id.edt_ph3);
		edt_ph[4] = (EditText) findViewById(R.id.edt_ph4);
		edt_ph[5] = (EditText) findViewById(R.id.edt_ph5);
		btn_set = (Button) findViewById(R.id.set);
		im[1] = (ImageButton) findViewById(R.id.im1);
		im[2] = (ImageButton) findViewById(R.id.im2);
		im[3] = (ImageButton) findViewById(R.id.im3);
		im[4] = (ImageButton) findViewById(R.id.im4);
		im[5] = (ImageButton) findViewById(R.id.im5);
		tv = (TextView) findViewById(R.id.textVi);
		tv.setVisibility(View.VISIBLE);

	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			Cursor c = managedQuery(contactData, null, null, null, null);
			if (c.moveToFirst()) {
				String hasPhone = c
						.getString(c
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

				String id = c.getString(c
						.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

				if (hasPhone.equalsIgnoreCase("1")) {
					Cursor phones = getContentResolver().query(
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
		return m;

	}

}
