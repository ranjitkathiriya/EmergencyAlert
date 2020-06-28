package com.example.emergency;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.emergencyalert.GPSTracker;
import com.example.emergencyalert.R;
import com.example.emergencyalert.R.id;
import com.example.emergencyalert.R.layout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EmergencyDescriptionMap extends Activity {

	// Google Map
	private GoogleMap googleMap;
	GPSTracker gps;
	double latitude;
	double longitude;
	String address = null;
	String city = null;

	Geocoder geocorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergency_description_map);

		try {
			Intent i = getIntent();
			String latitude1 = i.getStringExtra("latitude");
			String longitude1 = i.getStringExtra("longitude");
			Log.i("latitude and longitude", latitude1 + "," + longitude1);
			gps = new GPSTracker(EmergencyDescriptionMap.this);

			latitude = Double.parseDouble(latitude1);
			longitude = Double.parseDouble(longitude1);

			initilizeMap();

			// Changing map type
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			
			// Showing / hiding your current location
			googleMap.setMyLocationEnabled(true);

			// Enable / Disable zooming controls
			googleMap.getUiSettings().setZoomControlsEnabled(false);

			// Enable / Disable my location button
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);

			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);

			try {
				geocorder = new Geocoder(EmergencyDescriptionMap.this,
						Locale.getDefault());
				List<Address> addresses = null;
				addresses = geocorder.getFromLocation(latitude, longitude, 1);
				address = addresses.get(0).getAddressLine(0);
				city = addresses.get(0).getAddressLine(1);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("hello Error..", e + "");
			}
			MarkerOptions marker = new MarkerOptions().position(
					new LatLng(latitude, longitude))
					.title(address + "," + city);

			// Changing marker icon
			marker.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

			// adding marker
			googleMap.addMarker(marker);

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(15).build();

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to fetch location of emergency", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
}
