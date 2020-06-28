package com.example.friend;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.emergencyalert.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class FriendsTab extends Fragment implements OnClickListener {

	ViewPager Tab;
	FriendsTabPagerAdapter TabAdapter;
	// ActionBar actionBar;
	Button btn1, btn2;
	View view1, view2;
	Typeface USERNAME;

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.friends_tab, null);

		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");

		btn1 = (Button) view.findViewById(R.id.btn_my_friends);
		btn2 = (Button) view.findViewById(R.id.btn_pending_requests);

		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);

		view1 = view.findViewById(R.id.view_btn_my_friends);
		view2 = view.findViewById(R.id.view_btn_pending_requests);

		TabAdapter = new FriendsTabPagerAdapter(getActivity()
				.getSupportFragmentManager());

		Tab = (ViewPager) view.findViewById(R.id.friend_pager);

		Tab.setAdapter(TabAdapter);

	

		Tab.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
		Tab.setOnDragListener(new OnDragListener() {

			public boolean onDrag(View v, DragEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v) {
		if (v == btn1) {
			Tab.setCurrentItem(0);
			view1.setVisibility(View.VISIBLE);
			view2.setVisibility(View.GONE);

		} else if (v == btn2) {
			Tab.setCurrentItem(1);
			view1.setVisibility(View.GONE);
			view2.setVisibility(View.VISIBLE);
		}

	}

}
