package com.vallny.jing.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ImageLoaderScrollListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vallny.jing.R;
import com.vallny.jing.adapter.FriendsTimeLineAdapter;
import com.vallny.jing.bean.DBUserBean;
import com.vallny.jing.bean.MessageBean;
import com.vallny.jing.bean.MessageListBean;
import com.vallny.jing.util.http.ConnectException;
import com.vallny.jing.util.http.URLHelper;

@SuppressLint("ValidFragment")
public class PublicTimeLineFragment extends SherlockFragment implements OnRefreshListener2<ListView>, OnItemClickListener, OnItemLongClickListener, ImageLoaderScrollListener {

	public static final int STATUSCOUNT = 50;

	private View view;
	private PullToRefreshListView mPullRefreshListView;

	private DBUserBean user;
	private Context context;
	private FriendsTimeLineAdapter friendsAdapter;

	private int pageNo;

	private ArrayList<MessageBean> msgList = new ArrayList<MessageBean>();

	private ImageLoader imageLoader;
	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	private Boolean isOnline;

	public PublicTimeLineFragment(){}
	
	public PublicTimeLineFragment(DBUserBean user, Context context, ImageLoader imageLoader) {
		this.user = user;
		this.context = context;
		this.imageLoader = imageLoader;
	}

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	private void applyScrollListener() {
		mPullRefreshListView.setOnImageLoaderScrollListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.weibo_content_list, container, false);

		mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
		String str = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

		mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.refreshinglabel));
		mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel(getString(R.string.pulllabel_down));
		mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel(getString(R.string.pulllabel_up));
		mPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel(getString(R.string.releaselabel));
		mPullRefreshListView.getLoadingLayoutProxy(true, true).setLastUpdatedLabel(getString(R.string.lastupdatedlabel) + str);

		friendsAdapter = new FriendsTimeLineAdapter(context, imageLoader,user);
		mPullRefreshListView.setAdapter(friendsAdapter);
		mPullRefreshListView.setRefreshing();

		mPullRefreshListView.setOnRefreshListener(this);
		mPullRefreshListView.getRefreshableView().setOnItemClickListener(this);
		mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(this);

		pageNo = 1;
		new FriendsTimeLineTask(pageNo, false).execute(user);

		return view;

	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

		pageNo = 1;
		new FriendsTimeLineTask(pageNo, true).execute(user);

	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		pageNo++;
		new FriendsTimeLineTask(pageNo, false).execute(user);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(context, "3", 0).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(context, position + "onItemLongClick", 0).show();
		return true;
	}

	public class FriendsTimeLineTask extends AsyncTask<DBUserBean, Object, Object> {

		private ArrayList<MessageBean> newMsgList;
		private int pageNo;
		private boolean isNew;

		public FriendsTimeLineTask(int pageNo, boolean isNew) {
			this.pageNo = pageNo;
			this.isNew = isNew;
		}

		private String getUrl(DBUserBean... params) {
			return URLHelper.PUBLIC_TIMELINE + "?" + "access_token=" + params[0].getToken() + "&source=" + URLHelper.APP_KEY + "&page=" + this.pageNo + "&count=" + STATUSCOUNT;
		}

		@Override
		protected Object doInBackground(DBUserBean... params) {
			isOnline = true;
			String json0;
			try {
				json0 = URLHelper.queryStringForGet(getUrl(params));

				Gson gson = new Gson();
				MessageListBean messageListBean = gson.fromJson(json0, MessageListBean.class);
				newMsgList = (ArrayList<MessageBean>) messageListBean.getItemList();
				if (isNew) {
					msgList.clear();
				}
				msgList.addAll(newMsgList);
			} catch (ConnectException e) {
				isOnline = false;
				if(PublicTimeLineFragment.this.pageNo!=1){
					PublicTimeLineFragment.this.pageNo--;
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (isOnline) {
				friendsAdapter.setMessageList(msgList);
				friendsAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(context, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			mPullRefreshListView.onRefreshComplete();
		}
	}

	@Override
	public void onScrollStateChanged(int state) {
		switch (state) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			imageLoader.resume();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			if (pauseOnScroll) {
				imageLoader.pause();
			}
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			if (pauseOnFling) {
				imageLoader.pause();
			}
			break;
		}

	}

}
