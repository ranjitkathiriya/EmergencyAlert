package com.example.emergency;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class EmergencyTabPagerAdapter extends FragmentStatePagerAdapter {
	public EmergencyTabPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			// Fragement for Android Tab
			return new RecentEmergencies();
		case 1:
			// Fragment for Ios Tab
			return new CreateEmergency();

		}
		return null;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 2; // No of Tabs
	}

}