package com.example.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;

public class ContactlistviewActivity extends Activity implements
		OnItemClickListener {

	private ListView listView;
	EditText edt;
	Button select;
	ContactlistModel objContact;
	List<ContactlistModel> list = new ArrayList<ContactlistModel>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist);
		select = (Button) findViewById(R.id.select);
		listView = (ListView) findViewById(R.id.list);
		edt = (EditText) findViewById(R.id.editText);
		listView.setOnItemClickListener(this);
		select.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("preserve_list", SessionManager.preserve_list.size()
						+ " " + SessionManager.preserve_list);
				finish();
			}
		});

		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		while (phones.moveToNext()) {

			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			objContact = new ContactlistModel(name, phoneNumber);
			list.add(objContact);
			get(name, phoneNumber);
		}
		phones.close();
		edt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String s1 = s.toString();
				s = s1.toLowerCase();
				if (s != null) {
					List<ContactlistModel> list2 = new ArrayList<ContactlistModel>();
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).getName().toLowerCase().contains(s)) {

							objContact = new ContactlistModel(list.get(i)
									.getName(), list.get(i).getPhoneNo());
							list2.add(objContact);

						}
						ContactListArrayAdapter objAdapter1 = new ContactListArrayAdapter(
								ContactlistviewActivity.this, list2);
						listView.setAdapter(objAdapter1);
					}
				} else {
					ContactListArrayAdapter objAdapter = new ContactListArrayAdapter(
							ContactlistviewActivity.this, list);
					listView.setAdapter(objAdapter);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}

		});
		ContactListArrayAdapter objAdapter = new ContactListArrayAdapter(
				ContactlistviewActivity.this, list);
		listView.setAdapter(objAdapter);

		Collections.sort(list, new Comparator<ContactlistModel>() {

			@Override
			public int compare(ContactlistModel lhs, ContactlistModel rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});

	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		return convertView;

	}

	private ContactlistModel get(String name, String phoneNo) {
		return new ContactlistModel(name, phoneNo);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

}
