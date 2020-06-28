package com.example.slidingmenu;

import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.emergency.EmergencyTab;
import com.example.emergency.SendQuickEmergency;
import com.example.emergencyalert.MyProfile;
import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.friend.FriendsTab;
import com.example.group.MyGroups;
import com.example.parser.RoundedTransformation;

public class SlidingMenuFragment extends ListFragment {
	public String[] menus = { "", "Emergencies", "Send Quick Emergency",
			"My Profile", "Friends", "Groups", "Logout" };
	public int[] icon = { 0, R.drawable.icon_emergencies,
			R.drawable.icon_send_quick_emergency, R.drawable.icon_my_profile,
			R.drawable.icon_friend, R.drawable.icon_group,
			R.drawable.icon_logout };
	public int[] icon_on_pressed = { 0, R.drawable.icon_emergencies_hover,
			R.drawable.icon_send_quick_emergency_hover,
			R.drawable.icon_my_profile_hover, R.drawable.icon_friend_hover,
			R.drawable.icon_group_hover, R.drawable.icon_logout_hover };
	public SampleAdapter adapter;
	Typeface USERNAME;
	SessionManager session;
	String useremail, fn, ln, dp;
	public static boolean back = true;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.slidingmenu_main_list, null);

		session = new SessionManager(getActivity());
		HashMap<String, String> user = session.getUserDetails();
		useremail = user.get(SessionManager.KEY_EMAIL);
		fn = user.get(SessionManager.KEY_fname);
		ln = user.get(SessionManager.KEY_lname);
		dp = user.get(SessionManager.KEY_dp);
		Log.i("fn---ln", fn + " " + ln + " " + useremail);
		sess(v);
		return v;
	}

	public void sess(View v) {

		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");
		adapter = new SampleAdapter(getActivity());

		for (int i = 0; i < menus.length; i++) {
			adapter.add(new SampleItem(menus[i], icon[i]));
		}
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Fragment newContent = null;
		adapter.clear();

		for (int i = 0; i < menus.length; i++) {
			if (position == i) {
				adapter.add(new SampleItem(menus[i], icon_on_pressed[i]));
			} else {
				adapter.add(new SampleItem(menus[i], icon[i]));
			}
		}

		adapter.notifyDataSetChanged();
		switch (position) {
		case 0:
			back = true;
			((SlidingMenuFragmentActivity) getActivity()).title
					.setText("My Profile");
			newContent = new MyProfile();
			break;
		case 1:
			back = false;
			((SlidingMenuFragmentActivity) getActivity()).title
					.setText("Emergencies");
			newContent = new EmergencyTab();
			break;
		case 2:
			back = false;
			((SlidingMenuFragmentActivity) getActivity()).title
					.setText("Quick Emergency");
			newContent = new SendQuickEmergency();
			break;
		case 3:
			back = false;
			((SlidingMenuFragmentActivity) getActivity()).title
					.setText("My Profile");
			newContent = new MyProfile();
			break;
		case 4:
			back = false;
			((SlidingMenuFragmentActivity) getActivity()).title
					.setText("Friends");
			newContent = new FriendsTab();
			break;
		case 5:
			back = false;
			((SlidingMenuFragmentActivity) getActivity()).title
			.setText("Groups");
			newContent = new MyGroups();
			break;
		case 6:
			Logout();
			break;

		}
		if (newContent != null)
			switchFragment(newContent);
	}

	private void Logout() {
		// TODO Auto-generated method stub
		final Dialog dialog1 = new Dialog(getActivity());
		dialog1.getWindow();
		dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog1.setContentView(R.layout.login_logout);
		dialog1.show();

		Button btn_yess = (Button) dialog1.findViewById(R.id.btn_yess);
		btn_yess.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					session.logoutUser();
					dialog1.dismiss();
					getActivity().finish();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("Session error", "Not work.......");
				}

			}
		});
		Button btn_noo = (Button) dialog1.findViewById(R.id.btn_noo);
		btn_noo.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog1.dismiss();
			}
		});

	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof SlidingMenuFragmentActivity) {
			SlidingMenuFragmentActivity fca = (SlidingMenuFragmentActivity) getActivity();
			fca.switchContent(fragment);
		}

	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(final int position, View view, ViewGroup parent) {
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(
						R.layout.slidingmenu_row, null);
			}
			LinearLayout row_ll_profile = (LinearLayout) view
					.findViewById(R.id.row_ll_profile);
			ImageView row_iv_profile = (ImageView) view
					.findViewById(R.id.row_iv_profile);
			TextView row_iv_name = (TextView) view
					.findViewById(R.id.row_iv_name);
			LinearLayout ll_main = (LinearLayout) view
					.findViewById(R.id.ll_main);
			StaticData.setFont(ll_main, USERNAME);
			LinearLayout ll_row = (LinearLayout) view.findViewById(R.id.ll_row);
			if (position == 0) {
				ll_row.setVisibility(View.GONE);
				row_ll_profile.setVisibility(View.VISIBLE);
			} else {
				row_ll_profile.setVisibility(View.GONE);
				ll_row.setVisibility(View.VISIBLE);
			}
			row_iv_name.setText(fn + " " + ln);
			Log.d("names", fn + " " + ln);
			if (!dp.equals(" ")) {
				byte[] b = Base64.decode(dp, Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
				Bitmap round_photo = new RoundedTransformation(360, 0)
						.transform(bitmap);
				row_iv_profile.setImageBitmap(round_photo);
			} else {
				row_iv_profile.setImageResource(R.drawable.default_user);
			}
			ImageView icon = (ImageView) view.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			final TextView title = (TextView) view.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return view;
		}
	}
}
