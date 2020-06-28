package com.example.slidingmenu;

import com.example.emergency.EmergencyTab;
import com.example.emergencyalert.R;
import com.example.emergencyalert.StaticData;
import com.example.friend.FriendsTab;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SlidingMenuFragmentActivity extends BaseActivity {

	private Fragment mContent;
	public static TextView title;

	public SlidingMenuFragmentActivity() {
		super(R.string.app_name);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(false);
		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator
				.inflate(R.layout.slidingmenu_titleview_activity, null);
		this.getActionBar().setCustomView(v);

		title = (TextView) v.findViewById(R.id.title);
		title.setText("Emergencies");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(false);
		getSherlock().getActionBar().setIcon(R.drawable.icon_menu);
		getSherlock().getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#CD202C")));

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");

		if (mContent == null)
			if (StaticData.notifyFriendRequest) {
				StaticData.notifyFriendRequest=false;
				mContent = new FriendsTab();
			}else {
				mContent = new EmergencyTab();	
			}
			

		// set the Above View
		setContentView(R.layout.slidingmenu_content_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View
		setBehindContentView(R.layout.slidingmenu_menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SlidingMenuFragment()).commit();
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (SlidingMenuFragment.back) {
			moveTaskToBack(true);
		} else {
			SlidingMenuFragment.back = true;
			Intent i = new Intent(SlidingMenuFragmentActivity.this,
					SlidingMenuFragmentActivity.class);
			startActivity(i);
		}
	}
}
