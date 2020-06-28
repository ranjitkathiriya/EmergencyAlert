package com.example.friend;

import java.io.InputStream;
import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.parser.FriendDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.squareup.picasso.Picasso;

public class PendingFriendRequests extends Fragment {

	// Variable for Pending Request
	Button btn_friends, btn_pending, btn_find_friend;
	ListView lv_friendlist;
	TextView empty;

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	String[] fnamearray, emailarray, imagearray;
	String strImage;

	// Profile picture upload
	ImageView img_frienddp;
	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();
	List<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();

	LinearLayout linear_friendlist_listview;
	View pendingRequestview;
	Typeface USERNAME;

	// Load first time
	private boolean firstTimeLoad = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		pendingRequestview = inflater.inflate(R.layout.friends_my_friends_list,
				null);

		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");
		linear_friendlist_listview = (LinearLayout) pendingRequestview
				.findViewById(R.id.linear_friendlist_listview);
		StaticData.setFont(linear_friendlist_listview, USERNAME);

		if (StaticData.isNetworkAvailable(getActivity())) {

			session = new SessionManager(getActivity());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			firstTimeLoad = true;
			// fillFriendlist();

			lv_friendlist.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub

					HashMap<String, String> send = new HashMap<String, String>();
					send.put("fname", arr_fdetail.get(arg2).getFname());
					send.put("lname", arr_fdetail.get(arg2).getLname());
					send.put("email", arr_fdetail.get(arg2).getEmail());

					String dp_path = arr_fdetail.get(arg2).getImage();
					send.put("image", dp_path);

					Intent pass = new Intent(getActivity(),
							PendingFriendProfile.class);

					pass.putExtra("from_addfriend", send);
					startActivity(pass);
				}

			});

		} else {
			pendingRequestview = inflater.inflate(
					R.layout.global_internet_not_found, container, false);
		}
		return pendingRequestview;

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StaticData.resetPendingRequestFlag = false;
		firstTimeLoad = false;
		alist.clear();
		arr_fdetail.clear();
		fillFriendlist();

	}

	private void setContent() {
		// TODO Auto-generated method stub

		lv_friendlist = (ListView) pendingRequestview
				.findViewById(R.id.lv_friendlist);
		empty = (TextView) pendingRequestview.findViewById(R.id.empty);
		empty.setVisibility(View.GONE);

		btn_find_friend = (Button) pendingRequestview
				.findViewById(R.id.btn_find_friend);
		btn_find_friend.setVisibility(View.GONE);

	}

	private void fillFriendlist() {
		// TODO Auto-generated method stub

		if (StaticData.isNetworkAvailable(getActivity())) {

			new FriendlistContentDisplay().execute("");

		}
	}

	private class FriendlistContentDisplay extends
			AsyncTask<String, Void, Void> {
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

			String url = StaticData.SERVER_URL + "pendingrequest.php";

			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(json.toString().getBytes(
					"UTF8")));

			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			// request.setHeader("json", json.toString());

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
			JSONArray name = null;

			try {
				name = jobj.getJSONArray("userdetail");

				for (int i = 0; i < name.length(); i++) {
					JSONObject c = name.getJSONObject(i);
					FriendDetail fd = new FriendDetail();
					fd.setFname(c.getString("fname"));
					fd.setLname(c.getString("lname"));
					fd.setEmail(c.getString("email"));

					fd.setImage(StaticData.SERVER_IMAGE_URL
							+ c.getString("profile_pic"));
					arr_fdetail.add(fd);

				}
				fillist();

			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

		} else {
			empty.setText("No friend requests to show");
			lv_friendlist.setEmptyView(empty);
		}

	}

	private void fillist() {
		// TODO Auto-generated method stub.
		alist.clear();
		for (int i = 0; i < arr_fdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", arr_fdetail.get(i).getFname());
			tempName.put("lname", arr_fdetail.get(i).getLname());
			tempName.put("email", arr_fdetail.get(i).getEmail());
			alist.add(tempName);
		}
		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_friendname, R.id.txt_lastname,
				R.id.txt_friendemail };

		SpecialAdapter adapter = new SpecialAdapter(getActivity(), alist,
				R.layout.friends_row_cell_display, from, to);
		adapter.notifyDataSetChanged();

		lv_friendlist.setAdapter(adapter);
	}

	public class SpecialAdapter extends SimpleAdapter {
		public SpecialAdapter(Context context,
				List<HashMap<String, String>> items, int resource,
				String[] from, int[] to) {
			super(context, items, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			img_frienddp = (ImageView) view.findViewById(R.id.img_frienddp);
			if (arr_fdetail.get(position).getImage() != null) {
				Picasso.with(getActivity())
						.load(arr_fdetail.get(position).getImage())
						.resize(165, 165)
						.transform(new RoundedTransformation(360, 0))
						.into(img_frienddp);
			}

			return view;

		}
	}

}
