package com.example.emergencyalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.contact.ContactDetail;

public class DBhelper<RetrieveData> extends SQLiteOpenHelper {
	public static String Dbname = "help_contacts.db";
	public static String Tbname = "contacts_tb";
	public String asd;

	public DBhelper(Context context) {
		super(context, Dbname, null, 1);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		db.execSQL("create table if not exists " + Tbname
				+ "(email text,contacts text)");
		Log.i("onCreate",
				"Table is Created.................................$$$$$$$");

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public void insertRecord(ContactDetail cd) {
		try {

			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put("email", cd.getEmail()); // Contact Name
			values.put("contacts", cd.getContact()); // Contact Phone
			// Number

			// Inserting Row
			db.insert(Tbname, null, values);
			db.close();
			Log.i("insertRecord", "Record Inserted..........$$$$");

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("insertRecord", "Record not inserted.");
		}

	}

	public String selectrecord(String email) {
		try {

			SQLiteDatabase db = this.getReadableDatabase();

			String selectQuery = "SELECT contacts FROM contacts_tb where email ="
					+ "'" + email + "'";
			Cursor c = db.rawQuery(selectQuery, null);
			if (c.moveToFirst()) {
				asd = c.getString(c.getColumnIndex("contacts"));

			}

			c.close();
			Log.e("selectRecord", "Record  selected." + asd);
		} catch (Exception e) {
			Log.e("selectRecord", "Record not selected.");
		}
		return asd;

	}

}
