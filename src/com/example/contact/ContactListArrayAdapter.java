package com.example.contact;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;

public class ContactListArrayAdapter extends ArrayAdapter<ContactlistModel> {
	private ContactlistModel objBean;


	public ContactListArrayAdapter(Activity context, List<ContactlistModel> list) {
		super(context, R.layout.contactlist_layout, list);
		// TODO Auto-generated constructor stub

		this.context = context;
		this.list = list;
	}

	private final List<ContactlistModel> list;
	private final Activity context;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		objBean = list.get(position);
		View view;
		view = inflater.inflate(R.layout.contactlist_layout, parent, false);

		final TextView tvname = (TextView) view.findViewById(R.id.tvname);
		final TextView tvPhoneNo = (TextView) view.findViewById(R.id.tvphone);
		final CheckBox checkbox = (CheckBox) view.findViewById(R.id.check);
		if (tvname != null && null != objBean.getName()
				&& objBean.getName().trim().length() > 0) {
			tvname.setText(Html.fromHtml(objBean.getName()));
		}
		if (tvPhoneNo != null && null != objBean.getPhoneNo()
				&& objBean.getPhoneNo().trim().length() > 0) {
			tvPhoneNo.setText(Html.fromHtml(objBean.getPhoneNo()));
		}
		checkbox.setChecked(false);

		for (int i = 0; i < SessionManager.preserve_list.size(); i++) {
			if (tvPhoneNo.getText().toString()
					.equalsIgnoreCase(SessionManager.preserve_list.get(i))) {
				checkbox.setChecked(true);
			}
		}

		tvname.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkbox.setChecked(true);
			}
		});
		tvPhoneNo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkbox.setChecked(true);
			}
		});
		checkbox.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (checkbox.isChecked()) {

					SessionManager.preserve_list.add(tvPhoneNo.getText()
							.toString());
					Log.d("log", position + "");

				} else {
					SessionManager.preserve_list.remove(tvPhoneNo.getText()
							.toString());
				}

			}
		});
		return view;
	}

	public class ViewHolder {
		public TextView tvname, tvPhoneNo;
		public CheckBox checkbox;
	}

}
