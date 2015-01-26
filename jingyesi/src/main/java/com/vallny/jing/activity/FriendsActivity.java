package com.vallny.jing.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vallny.jing.R;
import com.vallny.jing.adapter.FriendsAdapter;
import com.vallny.jing.bean.UserBean;
import com.vallny.jing.bean.UserListBean;
import com.vallny.jing.global.PullToRefeshActivity;
import com.vallny.jing.util.http.ConnectException;
import com.vallny.jing.util.http.URLHelper;

public class FriendsActivity extends PullToRefeshActivity {

	public static final int FRIENDSCOUNT = 50;

	private Context context;
	private FriendsAdapter friendsAdapter;

	private String token;
	private String uid;
	private long next_cursor;

	private ArrayList<UserBean> itemList = new ArrayList<UserBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list);

		Intent intent = getIntent();

		token = intent.getStringExtra("token");
		uid = intent.getStringExtra("uid");

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		friendsAdapter = new FriendsAdapter(this, imageLoader);
		mPullRefreshListView.setAdapter(friendsAdapter);
		mPullRefreshListView.setRefreshing();
		mPullRefreshListView.setOnRefreshListener(new FriendsOnRefreshListener());
		mPullRefreshListView.getRefreshableView().setOnItemClickListener(new FriendsOnItemClickListener());

		new AsyncFriendsTask(0, false).execute(token);
	}

	class AsyncFriendsTask extends AsyncTask<String, Void, Void> {

		private long pageNo;
		private boolean isNew;
		private ArrayList<UserBean> newList;

		public AsyncFriendsTask(long pageNo, boolean isNew) {
			this.pageNo = pageNo;
			this.isNew = isNew;
		}

		private String getUrl(String... params) {
			return URLHelper.FRIENDS_LIST_BYID + "?" + "access_token=" + params[0] + "&source=" + URLHelper.APP_KEY + "&cursor=" + this.pageNo + "&count=" + FRIENDSCOUNT + "&uid=" + uid;
		}

		@Override
		protected Void doInBackground(String... params) {

			try {
				String json0 = URLHelper.queryStringForGet(getUrl(params));
				Gson gson = new Gson();
				UserListBean userListBean = gson.fromJson(json0, UserListBean.class);
				newList = (ArrayList<UserBean>) userListBean.getItemList();
				next_cursor = userListBean.getNext_cursor();
				if (isNew) {
					itemList.clear();
				}
				itemList.addAll(newList);
				isOnline = true;
			} catch (ConnectException e) {
				isOnline = false;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isOnline) {
				friendsAdapter.setUserList(itemList);
				friendsAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(context, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			mPullRefreshListView.onRefreshComplete();

		}

	}

	class FriendsOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ImageView iv = (ImageView) view.findViewById(R.id.checkbox);
			if (iv.getVisibility() == View.VISIBLE) {
				iv.setVisibility(View.INVISIBLE);
			} else {
				iv.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 下拉刷新
	 * 
	 * @author SIA
	 * 
	 */
	class FriendsOnRefreshListener implements OnRefreshListener2<ListView> {

		public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

			new AsyncFriendsTask(0, true).execute(token);
		}

		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

			new AsyncFriendsTask(next_cursor, false).execute(token);
		}

	}

}
