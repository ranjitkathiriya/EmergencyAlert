package com.example.emergencyalert;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {
	SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		session = new SessionManager(getApplicationContext());
		new SelectDataTask().execute();
	}

	private class SelectDataTask extends AsyncTask<String, Void, String> {
		// private final ProgressDialog dialog = new ProgressDialog(Main.this);

		// can use UI thread here
		protected void onPreExecute() {

		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(final String... args) {
			return null;
		}

		// can use UI thread here
		protected void onPostExecute(final String result) {

			Thread splashThread = new Thread() {
				public void run() {

					synchronized (this) {
						try {
							wait(2000);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}
					}

					mainpage();
				}
			};

			splashThread.start();

		}
	}

	protected void mainpage() {

		session.checkLogin();
		finish();
	}

}
