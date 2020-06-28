package com.example.friend;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FriendsTabPagerAdapter extends FragmentStatePagerAdapter {
	public FriendsTabPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			return new MyFriends();
		case 1:
			return new PendingFriendRequests();

		}
		return null;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 2; // No of Tabs
	}

}