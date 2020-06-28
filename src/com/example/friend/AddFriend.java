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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.emergencyalert.R;
import com.example.emergencyalert.SessionManager;
import com.example.emergencyalert.StaticData;
import com.example.friend.LoadMoreListView.OnLoadMoreListener;
import com.example.parser.FriendDetail;
import com.example.parser.RestClient;
import com.example.parser.RoundedTransformation;
import com.squareup.picasso.Picasso;

public class AddFriend extends Activity {

	ListView list;
	EditText edt_find_friend;
	ImageButton img_search;
	ImageView img_frienddp;
	String name;
	SpecialAdapter adapter;
	String[] separated;
	int i = 0, a = 0;

	// Variable for Background process
	JSONObject jobj;
	String Response_code;
	public String login_result;

	String[] fnamearray, emailarray, imagearray;
	String strImage;

	// Profile picture upload
	ArrayList<FriendDetail> arr_fdetail = new ArrayList<FriendDetail>();
	List<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();

	// Variable for get Current user Email
	SessionManager session;
	String useremail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_add_friend);

		if (StaticData.isNetworkAvailable(getApplicationContext())) {

			session = new SessionManager(getApplicationContext());
			HashMap<String, String> user = session.getUserDetails();
			useremail = user.get(SessionManager.KEY_EMAIL);

			setContent();
			name = edt_find_friend.getText().toString();

			separated = name.split(" ");
			((LoadMoreListView) list)
					.setOnLoadMoreListener(new OnLoadMoreListener() {
						public void onLoadMore() {
							// Do the work to load more items at the end of list
							// here
							new LoadDataTask().execute();
						}
					});
			i = separated.length;

			fillFriendlist();
			img_search.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					a = 0;
					name = edt_find_friend.getText().toString();

					separated = name.split(" ");
					i = separated.length;

					fillFriendlist();

				}
			});
			edt_find_friend.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub
					if (edt_find_friend.getText().toString().length() == 0) {
						a = 0;
						name = edt_find_friend.getText().toString();

						separated = name.split(" ");
						i = separated.length;
						fillFriendlist();
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub

				}
			});

			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					HashMap<String, String> send = new HashMap<String, String>();
					send.put("fname", arr_fdetail.get(arg2).getFname());
					send.put("lname", arr_fdetail.get(arg2).getLname());
					send.put("email", arr_fdetail.get(arg2).getEmail());

					String dp_path = arr_fdetail.get(arg2).getImage();
					send.put("image", dp_path);

					Intent pass = new Intent(AddFriend.this,
							AddFriendProfile.class);
					pass.putExtra("from_addfriend", send);
					startActivity(pass);
					finish();
				}
			});

		} else {
			Toast.makeText(getApplicationContext(), "Internet not connected",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void setContent() {
		// TODO Auto-generated method stub

		list = (ListView) findViewById(R.id.list);
		edt_find_friend = (EditText) findViewById(R.id.edt_find_friend);
		img_search = (ImageButton) findViewById(R.id.img_search);

	}

	private void fillFriendlist() {
		// TODO Auto-generated method stub
		if (StaticData.isNetworkAvailable(getApplicationContext())) {

			new FriendlistContentDisplay().execute("");

		} else {
			Toast.makeText(getApplicationContext(), "Internet not connected",
					Toast.LENGTH_SHORT).show();
		}
	}

	private class FriendlistContentDisplay extends
			AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(AddFriend.this);

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
				// new LoadImage().execute(picture_path);

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
			json.put("sfname", separated[0]);

			if (i == 1) {
				json.put("slname", JSONObject.NULL);
			} else {
				json.put("slname", separated[1]);
			}

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000000);
			HttpConnectionParams.setSoTimeout(httpParams, 1000000);
			HttpClient client = new DefaultHttpClient(httpParams);

			String url = StaticData.SERVER_URL + "add_friendrequest.php";

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
			arr_fdetail.clear();

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
				e.printStackTrace();
			}

		} else {
			Toast.makeText(getApplicationContext(), "Result not Found",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void fillist() {
		// TODO Auto-generated method stub.
		alist.clear();

		for (int i = 0; i < 10; i++, a++) {
			if (arr_fdetail.size() == a) {
				break;
			}
			HashMap<String, String> tempName = new HashMap<String, String>();
			tempName.put("fname", arr_fdetail.get(a).getFname());
			tempName.put("lname", arr_fdetail.get(a).getLname());
			tempName.put("email", arr_fdetail.get(a).getEmail());
			alist.add(tempName);
			// adapter.notifyDataSetChanged();

		}
		String[] from = { "fname", "lname", "email" };

		int[] to = { R.id.txt_friendname, R.id.txt_lastname,
				R.id.txt_friendemail };

		adapter = new SpecialAdapter(this, alist, R.layout.friends_row_cell_display, from, to);

		list.setAdapter(adapter);

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
				Picasso.with(AddFriend.this)
						.load(arr_fdetail.get(position).getImage())
						.resize(165, 165)
						.transform(new RoundedTransformation(360, 0))
						.into(img_frienddp);
			}

			return view;

		}
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.d("Load more", e + "");
			}
			// alist.clear();
			for (int i = 0; i < 10; i++, a++) {
				if (arr_fdetail.size() == a) {
					break;
				}
				HashMap<String, String> tempName = new HashMap<String, String>();
				tempName.put("fname", arr_fdetail.get(a).getFname());
				tempName.put("lname", arr_fdetail.get(a).getLname());
				tempName.put("email", arr_fdetail.get(a).getEmail());
				alist.add(tempName);
				// adapter.notifyDataSetChanged();

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			// We need notify the adapter that the data have been changed
			adapter.notifyDataSetChanged();

			// Call onLoadMoreComplete when the LoadMore task, has finished
			((LoadMoreListView) list).onLoadMoreComplete();

			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			((LoadMoreListView) list).onLoadMoreComplete();
		}
	}
}