package com.vallny.jing.util.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.vallny.jing.bean.DBUserBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DataHelper {


	private static String DB_NAME = "jingyesi.db";

	private static int DB_VERSION = 1;

	private SQLiteDatabase db;

	private SqliteHelper dbHelper;

	public DataHelper(Context context) {

		dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);

		db = dbHelper.getWritableDatabase();

	}

	public void close()

	{

		db.close();

		dbHelper.close();

	}


	public List<DBUserBean> getUserList(Boolean isSimple)

	{

		List<DBUserBean> userList = new ArrayList<DBUserBean>();

		Cursor cursor = db.query(SqliteHelper.TB_NAME, null, null, null, null, null, DBUserBean.ID + " DESC");

		cursor.moveToFirst();

		while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {

			DBUserBean user = new DBUserBean();

			user.setId(cursor.getString(0));

			user.setUid(cursor.getString(1));

			user.setToken(cursor.getString(2));

			if (!isSimple) {
				user.setUserName(cursor.getString(3));
				ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(4));
				Bitmap icon = BitmapFactory.decodeStream(stream);
				user.setUserIcon(icon);
			}

			userList.add(user);

			cursor.moveToNext();

		}

		cursor.close();

		return userList;

	}


	public Boolean haveUserInfo(String uid)

	{

		Boolean b = false;

		Cursor cursor = db.query(SqliteHelper.TB_NAME, null, DBUserBean.USERID + "=" + uid, null, null, null, null);

		b = cursor.moveToFirst();

		Log.e("HaveUserInfo", b.toString());

		cursor.close();

		return b;

	}


	public int updateUserInfo(String userName, Bitmap userIcon, String UserId)

	{

		ContentValues values = new ContentValues();

		values.put(DBUserBean.USERNAME, userName);


		final ByteArrayOutputStream os = new ByteArrayOutputStream();


		userIcon.compress(Bitmap.CompressFormat.PNG, 100, os);


		values.put(DBUserBean.USERICON, os.toByteArray());

		int id = db.update(SqliteHelper.TB_NAME, values, DBUserBean.USERID + "=" + UserId, null);

		Log.e("UpdateUserInfo2", id + "");

		return id;

	}


	public int updateUserInfo(DBUserBean user)

	{

		ContentValues values = new ContentValues();

		values.put(DBUserBean.USERID, user.getUid());

		values.put(DBUserBean.TOKEN, user.getToken());

		int id = db.update(SqliteHelper.TB_NAME, values, DBUserBean.USERID + "=" + user.getUid(), null);

		Log.e("UpdateUserInfo", id + "");

		return id;

	}


	public Long saveUserInfo(DBUserBean user)

	{

		ContentValues values = new ContentValues();

		values.put(DBUserBean.USERID, user.getUid());
		values.put(DBUserBean.TOKEN, user.getToken());
		values.put(DBUserBean.USERNAME, user.getUserName());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		user.getUserIcon().compress(Bitmap.CompressFormat.PNG, 100, os);
		values.put(DBUserBean.USERICON, os.toByteArray());
		Long uid = db.insert(SqliteHelper.TB_NAME, DBUserBean.ID, values);

		return uid;

	}


	public int delUserInfo(String uid) {

		int id = db.delete(SqliteHelper.TB_NAME, DBUserBean.USERID + "=" + uid, null);

		Log.e("DelUserInfo", id + "");

		return id;

	}

}