package com.example.group;

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
import android.view.View.OnClickListener;
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
import com.example.parser.GroupDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.squareup.picasso.Picasso;

public class MyGroups extends Fragment implements OnClickListener {

	private LinearLayout ll_my_group;
	ListView lv_grouplist;
	TextView empty;

	// Variable for get Current user Email
	SessionManager session;
	String useremail, profile_pic;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;
	Button btn_create_group;
	ImageView img_frienddp;
	TextView txt_lastname, txt_friendemail;
	ArrayList<GroupDetail> arr_gdetail = new ArrayList<GroupDetail>();
	List<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();

	View viewGroups;
	Typeface USERNAME;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		viewGroups = inflater.inflate(R.layout.groups_my_groups, null);

		ll_my_group = (LinearLayout) viewGroups.findViewById(R.id.ll_my_group);
		USERNAME = Typeface.createFromAsset(getActivity().getAssets(),
				"Fonts/USERNAME.TTF");
		StaticData.setFont(ll_my_group, USERNAME);
		if (StaticData.isNetworkAvailable(getActivity())) {

			session = new SessionManager(getActivity());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			// new GrouplistContentDisplay().execute("");
			btn_create_group.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(), CreateGroup.class));

				}
			});
			lv_grouplist.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub

					Intent pass = new Intent(getActivity(), MyGroupsInfo.class);
					pass.putExtra("group_id", arr_gdetail.get(arg2).getId());
					pass.putExtra("group_name", arr_gdetail.get(arg2).getName());
					pass.putExtra("group_image", arr_gdetail.get(arg2)
							.getImage());

					startActivity(pass);
				}
			});
		} else {
			viewGroups = inflater.inflate(R.layout.global_internet_not_found,
					container, false);
		}

		return viewGroups;
	}

	private void setContent() {
		// TODO Auto-generated method stub
		lv_grouplist = (ListView) viewGroups.findViewById(R.id.lv_grouplist);
		empty = (TextView) viewGroups.findViewById(R.id.empty);
		empty.setVisibility(View.GONE);
		btn_create_group = (Button) viewGroups
				.findViewById(R.id.btn_create_group);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		arr_gdetail.clear();
		alist.clear();
		new GrouplistContentDisplay().execute("");

	}

	private class GrouplistContentDisplay extends AsyncTask<String, Void, Void> {
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

			String url = StaticData.SERVER_URL + "displaygroup.php";

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
					GroupDetail gd = new GroupDetail();
					gd.setId(c.getString("group_id"));
					gd.setName(c.getString("group_name"));
					gd.setAdmin(c.getString("group_admin"));
					gd.setImage(StaticData.SERVER_IMAGE_URL
							+ c.getString("group_image"));

					arr_gdetail.add(gd);

				}
				fillist();

			} catch (NullPointerException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			// fillist();
			empty.setText("No groups to show");
			lv_grouplist.setEmptyView(empty);

		}

	}

	private void fillist() {
		// TODO Auto-generated method stub.
		alist.clear();
		for (int i = 0; i < arr_gdetail.size(); i++) {
			HashMap<String, String> tempName = new HashMap<String, String>();
			// tempName.put("group_admin", arr_gdetail.get(i).getAdmin());
			tempName.put("group_name", arr_gdetail.get(i).getName());
			// tempName.put("group_id", arr_gdetail.get(i).getId());
			alist.add(tempName);
		}
		String[] from = { "group_name" };

		int[] to = { R.id.txt_friendname };

		SpecialAdapter adapter = new SpecialAdapter(getActivity(), alist,
				R.layout.friends_row_cell_display, from, to);

		lv_grouplist.setAdapter(adapter);
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
			txt_lastname = (TextView) view.findViewById(R.id.txt_lastname);
			txt_friendemail = (TextView) view
					.findViewById(R.id.txt_friendemail);
			txt_lastname.setVisibility(view.GONE);
			txt_friendemail.setVisibility(view.GONE);

			img_frienddp = (ImageView) view.findViewById(R.id.img_frienddp);
			if (arr_gdetail.get(position).getImage() != null) {
				Picasso.with(getActivity())
						.load(arr_gdetail.get(position).getImage())
						.transform(new RoundedTransformation(360, 0))
						.resize(165, 165).into(img_frienddp);
				Log.i("Image", "" + arr_gdetail.get(position).getImage());
			}
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
