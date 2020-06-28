package com.example.emergency;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.RecentEmergencyDetail;
import com.example.parser.RestClient;
import com.squareup.picasso.Picasso;

public class RecentEmergencies extends Fragment {

	// Local Widgets
	ListView lv_recent_emergencies;

	// row_cell_recent_emergencies.xml
	TextView txt_recent_emergency_type, txt_recent_emergency_description,
			txt_emergency_time;
	ImageView img_recent_emergencies;
	LinearLayout linear_complete, linear_ongoing, ll_recent_emergencies_cell;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	// Details From Service
	ArrayList<RecentEmergencyDetail> arr_edetail = new ArrayList<RecentEmergencyDetail>();
	List<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
	List<String> array_image_list;

	// Get view instance
	View android;
	Typeface USERNAME;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		android = inflater.inflate(R.layout.recent_emergencies, container,
				false);
		setContent();
		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");

		if (StaticData.isNetworkAvailable(getActivity())) {

			session = new SessionManager(getActivity());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			lv_recent_emergencies
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							Intent i = new Intent(getActivity(),
									EmergencyDescription.class);
							i.putExtra("alert_id", arr_edetail.get(arg2)
									.getAlert_id());
							startActivity(i);

						}
					});

		} else {
			android = inflater.inflate(R.layout.global_internet_not_found,
					container, false);
		}

		return android;
	}

	private void setContent() {
		// TODO Auto-generated method stub
		lv_recent_emergencies = (ListView) android
				.findViewById(R.id.lv_recent_emergencies);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		arr_edetail.clear();
		alist.clear();
		new RecentEmergeniesDetails().execute("");

	}

	private class RecentEmergeniesDetails extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(getActivity());

		protected void onPreExecute() {
			Dialog.setMessage("Please wait...");
			Dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			postData();
			return null;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
			try {

				result();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	private void postData() {
		// TODO Auto-generated method stub

		String result = "";
		try {
			JSONObject json = new JSONObject();
			json.put("email", useremail);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);

			String url = StaticData.SERVER_URL + "recent_emergency.php";

			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(json.toString().getBytes(
					"UTF8")));

			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			request.setHeader("json", json.toString());

			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need

			if (entity != null) {
				InputStream instream = entity.getContent();

				result = RestClient.convertStreamToString(instream);
				Log.i("Read from server", result);
			}

		} catch (Exception t) {
			Log.e("postData", t.toString());
		}

		try {
			// Log.i("result", result);
			Response_code = result;
			Log.e("Result....", "...." + Response_code);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Result", "...." + e.toString());
		}

		try {
			jobj = new JSONObject(Response_code);
			login_result = jobj.getString("ResponseCode");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.w("JSONException", e.toString());
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			Log.w("null", e.toString());
		}

	}

	private void result() {

		if (login_result.equals("1")) {
			JSONArray detail = null;

			try {
				detail = jobj.getJSONArray("alert_details");

				for (int i = (detail.length() - 1); i >= 0; i--) {
					JSONObject c = detail.getJSONObject(i);
					RecentEmergencyDetail rd = new RecentEmergencyDetail();
					rd.setAlert_id(c.getInt("alert_id"));
					rd.setAlert_type(c.getString("alert_type"));
					rd.setAlert_description(c.getString("alert_description"));
					rd.setAlert_receiver_emails("alert_creater_email");
					rd.setImages(c.getString("images"));
					rd.setTotal_helper(c.getString("total_helper"));
					rd.setAlert_receiver_emails(c
							.getString("alert_receiver_emails"));
					rd.setHelper_emails(c.getString("helper_emails"));
					rd.setCreate_time(c.getString("create_time"));
					rd.setComplete(c.getInt("complete"));
					rd.setAlert_location(c.getString("alert_location"));
					rd.setAlert_longitude(c.getString("alert_longitude"));
					rd.setAlert_latitude(c.getString("alert_latitude"));

					arr_edetail.add(rd);

				}
				fillist();

			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			TextView txt_emergencies_not_found = (TextView) android
					.findViewById(R.id.txt_emergencies_not_found);
			lv_recent_emergencies.setVisibility(View.GONE);
			txt_emergencies_not_found.setVisibility(View.VISIBLE);
		}

	}

	private void fillist() {
		// TODO Auto-generated method stub.
		alist.clear();
		for (int i = 0; i < arr_edetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("alert_type", arr_edetail.get(i).getAlert_type());
			tempName.put("alert_description", arr_edetail.get(i)
					.getAlert_description());
			tempName.put("create_time", arr_edetail.get(i).getCreate_time());
			tempName.put("images", arr_edetail.get(i).getImages());

			alist.add(tempName);
		}
		String[] from = { "alert_type" };

		int[] to = { R.id.txt_type };

		SpecialAdapter adapter = new SpecialAdapter(getActivity(), alist,
				R.layout.recent_emergencies_row_cell, from, to);

		lv_recent_emergencies.setAdapter(adapter);
	}

	public class SpecialAdapter extends SimpleAdapter {
		public SpecialAdapter(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@SuppressWarnings("static-access")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			txt_recent_emergency_type = (TextView) view
					.findViewById(R.id.txt_type);
			txt_recent_emergency_description = (TextView) view
					.findViewById(R.id.txt_recent_emergency_description);
			txt_emergency_time = (TextView) view.findViewById(R.id.txt_time);
			linear_complete = (LinearLayout) view
					.findViewById(R.id.linear_complete);
			linear_ongoing = (LinearLayout) view
					.findViewById(R.id.linear_ongoing);

			ll_recent_emergencies_cell = (LinearLayout) view
					.findViewById(R.id.ll_recent_emergencies_cell);
			StaticData.setFont(ll_recent_emergencies_cell, USERNAME);

			if (arr_edetail.get(position).getComplete() == 1) {
				linear_ongoing.setVisibility(View.GONE);
				linear_complete.setVisibility(View.VISIBLE);
			} else {
				linear_complete.setVisibility(View.GONE);
				linear_ongoing.setVisibility(View.VISIBLE);

			}

			txt_recent_emergency_type.setText(arr_edetail.get(position)
					.getAlert_type());
			txt_recent_emergency_description.setText(arr_edetail.get(position)
					.getAlert_description());
			txt_emergency_time.setText(arr_edetail.get(position)
					.getCreate_time());

			img_recent_emergencies = (ImageView) view
					.findViewById(R.id.img_recent_emergencies);
			img_recent_emergencies.setImageDrawable(getResources().getDrawable(
					R.drawable.emergency_icon));

			if (!arr_edetail.get(position).getImages().equals("")) {
				List<String> images = Arrays.asList(arr_edetail.get(position)
						.getImages().split(","));
				Picasso.with(getActivity())
						.load(StaticData.SERVER_IMAGE_URL + images.get(0))
						.resize(165, 165).into(img_recent_emergencies);
				Log.i("Images", images.get(0) + "");

			} else {
				img_recent_emergencies.setImageDrawable(getResources()
						.getDrawable(R.drawable.emergency_icon));
			}

			return view;
		}
	}

}
