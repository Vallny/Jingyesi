package com.vallny.jing.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vallny.jing.R;
import com.vallny.jing.adapter.LoadUserAdapter;
import com.vallny.jing.bean.DBUserBean;
import com.vallny.jing.global.BaseActivity;
import com.vallny.jing.util.db.DataHelper;
import com.vallny.jing.util.http.ConnectException;
import com.vallny.jing.util.http.URLHelper;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

	private Weibo mWeibo;
	private Oauth2AccessToken mAccessToken;
	private SsoHandler mSsoHandler;

	private DataHelper dbHelper;
	private ListView listView;
	private LoadUserAdapter userAdapter;

	private ActionMode mMode;
	private ProgressDialog dialog;

	public final static String INTENT_USER = "user";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_list);

		dbHelper = new DataHelper(this);
		List<DBUserBean> userList = dbHelper.getUserList(false);

		listView = (ListView) findViewById(R.id.listView);

		userAdapter = new LoadUserAdapter(userList, this);
		listView.setAdapter(userAdapter);
		listView.getAdapter();
		listView.setOnItemClickListener(this);
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(R.string.add).setIcon(R.drawable.add_user_light).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 0:
			new AlertDialog.Builder(LoginActivity.this).setItems(R.array.dialog_arr, this).show();
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Intent intent = new Intent(LoginActivity.this, WeiboContentActivity.class);

		intent.putExtra(INTENT_USER, (DBUserBean) parent.getItemAtPosition(position));
		startActivity(intent);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mMode = startActionMode(new AnActionModeOfEpicProportions());
		DBUserBean user = (DBUserBean) userAdapter.getItem(position);

		mMode.setTag(user.getUid());

		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		mWeibo = Weibo.getInstance(URLHelper.APP_KEY, URLHelper.REDIRECT_URL, URLHelper.SCOPE);

		switch (which) {
		case 0:

			mWeibo.anthorize(LoginActivity.this, new AuthDialogListener());

			break;

		default:

			mSsoHandler = new SsoHandler(LoginActivity.this, mWeibo);
			mSsoHandler.authorize(new AuthDialogListener(), null);

			break;
		}

	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {

			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			mAccessToken = new Oauth2AccessToken(token, expires_in);
			if (mAccessToken.isSessionValid()) {
				// String date = new
				// SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new
				// java.util.Date(mAccessToken.getExpiresTime()));
				// mText.setText("��֤�ɹ�: \r\n access_token: " + token + "\r\n"
				// +
				// "expires_in: " + expires_in + "\r\n��Ч�ڣ�" + date);

				// AccessTokenKeeper.keepAccessToken(WelcomeActivity.this,
				// mAccessToken);
				dialog = new ProgressDialog(LoginActivity.this);
				dialog.setMessage(getResources().getString(R.string.manage_dialog_message));
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialog.show();
				new LoadUserTask().execute(mAccessToken.getToken());

			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			Toast.makeText(getApplicationContext(), "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(), "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	class AnActionModeOfEpicProportions implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			mode.setTitle(R.string.manage);

			menu.add(R.string.delete).setIcon(R.drawable.user_remove_light).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			String uid = (String) mode.getTag();

			dbHelper.delUserInfo(uid);
			userAdapter.setUserList(dbHelper.getUserList(false));
			userAdapter.notifyDataSetChanged();

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}
	}

	class LoadUserTask extends AsyncTask<String, Object, Boolean> {
		private boolean isOnline;

		private String getUrl(String uid, String... params) {
			return URLHelper.USER_SHOW + "?" + "uid=" + uid + "&source=" + URLHelper.APP_KEY + "&access_token=" + params[0];
		}

		@Override
		protected Boolean doInBackground(String... params) {

			try {
				String json0 = URLHelper.queryStringForGet(URLHelper.UID + "?" + "access_token=" + params[0]);
				JSONObject object0 = new JSONObject(json0);
				String uid = object0.getString("uid");

				String json1 = URLHelper.queryStringForGet(getUrl(uid, params));

				JSONObject object1 = new JSONObject(json1);
				DBUserBean userInfo = new DBUserBean();
				userInfo.setToken(params[0]);
				userInfo.setUid(uid);
				userInfo.setUserName(object1.getString("screen_name"));

				InputStream is = new URL(object1.getString("profile_image_url")).openStream();
				Bitmap userIcon = BitmapFactory.decodeStream(is);

				userInfo.setUserIcon(userIcon);

				if (!dbHelper.haveUserInfo(uid)) {
					dbHelper.saveUserInfo(userInfo);
				} else {
					return false;
				}
				isOnline = true;
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				isOnline = false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (isOnline) {
				if (result) {
					userAdapter.setUserList(dbHelper.getUserList(false));
					userAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(LoginActivity.this, R.string.already, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(LoginActivity.this, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();

			}
		}

	}

}
