package com.vallny.jing.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ImageLoaderScrollListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.vallny.jing.R;
import com.vallny.jing.adapter.CommentTimeLineAdapter;
import com.vallny.jing.bean.CommentBean;
import com.vallny.jing.bean.CommentListBean;
import com.vallny.jing.bean.DBUserBean;
import com.vallny.jing.bean.ItemBean;
import com.vallny.jing.bean.MessageBean;
import com.vallny.jing.bean.RepostListBean;
import com.vallny.jing.fragment.FriendsTimeLineFragment;
import com.vallny.jing.global.BaseActivity;
import com.vallny.jing.util.http.ConnectException;
import com.vallny.jing.util.http.URLHelper;

public class DetailsTimeLineActivity extends BaseActivity implements OnRefreshListener2<ListView>, OnItemClickListener, OnItemLongClickListener, ImageLoaderScrollListener {

	public static final int STATUSCOUNT = 50;

	private PullToRefreshListView mPullRefreshListView;
	private LinearLayout timeline_indicator;
	private TextView repost_timeline;
	private TextView comment_timeline;
	// private ProgressBar progressBar;

	private int comment_scroll_top;
	private int comment_scroll_pos;
	private int repost_scroll_top;
	private int repost_scroll_pos;
	private boolean firstClick;

	private DBUserBean user;
	private MessageBean msgBean;
	private CommentTimeLineAdapter commentAdapter;

	private int commentPageNo;
	private int repostPageNo;

	private int firstVisibleItem;

	private ArrayList<ItemBean> msgList = new ArrayList<ItemBean>();

	private ArrayList<CommentBean> newCommentList = new ArrayList<CommentBean>();
	private ArrayList<MessageBean> newRepostList = new ArrayList<MessageBean>();

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	private boolean isOnline;

	private boolean showComment = true;

	public void setShowComment(boolean showComment) {
		this.showComment = showComment;
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
		setContentView(R.layout.weibo_content_list);
		Intent intent = getIntent();
		msgBean = intent.getParcelableExtra(FriendsTimeLineFragment.MESSAGE_BEAN);
		user = intent.getParcelableExtra(LoginActivity.INTENT_USER);
		msgList.add(msgBean);
		msgList.add(null);

		// IndicatorClickListener clickListener = new IndicatorClickListener();
		timeline_indicator = (LinearLayout) findViewById(R.id.timeline_indicator);
		repost_timeline = (TextView) findViewById(R.id.repost_timeline);
		comment_timeline = (TextView) findViewById(R.id.comment_timeline);
		// repost_timeline.setOnClickListener(clickListener);
		// comment_timeline.setOnClickListener(clickListener);
		// progressBar = (ProgressBar) findViewById(R.id.progressBar);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		String str = DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

		mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.refreshinglabel));
		mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel(getString(R.string.pulllabel_down));
		mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel(getString(R.string.pulllabel_up));
		mPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel(getString(R.string.releaselabel));
		mPullRefreshListView.getLoadingLayoutProxy(true, true).setLastUpdatedLabel(getString(R.string.lastupdatedlabel) + str);

		commentAdapter = new CommentTimeLineAdapter(this, imageLoader, msgList);
		commentAdapter.setLoading(true);
		mPullRefreshListView.setAdapter(commentAdapter);
		// mPullRefreshListView.getRefreshableView().setSelection(1);
		mPullRefreshListView.setOnRefreshListener(this);
		mPullRefreshListView.getRefreshableView().setOnItemClickListener(this);
		mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(this);

		commentPageNo = 1;
		new CommentTimeLineTask(commentPageNo, false).execute(user);

		mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// scrollPos记录当前可见的List顶端的一行的位置
					if (showComment) {
						comment_scroll_pos = mPullRefreshListView.getRefreshableView().getFirstVisiblePosition();
					} else {
						repost_scroll_pos = mPullRefreshListView.getRefreshableView().getFirstVisiblePosition();
					}
				}
				if (msgList != null) {

					View v = mPullRefreshListView.getRefreshableView().getChildAt(0);
					if (showComment) {
						comment_scroll_top = (v == null) ? 0 : v.getTop();
					} else {
						repost_scroll_top = (v == null) ? 0 : v.getTop();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				DetailsTimeLineActivity.this.firstVisibleItem = firstVisibleItem;
				if (firstVisibleItem > 1) {
					timeline_indicator.setVisibility(View.VISIBLE);
				} else {
					timeline_indicator.setVisibility(View.INVISIBLE);
				}
			}
		});

	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

		commentPageNo = 1;
		new CommentTimeLineTask(commentPageNo, true).execute(user);

	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		commentPageNo++;
		new CommentTimeLineTask(commentPageNo, false).execute(user);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, position + "onItemLongClick", Toast.LENGTH_SHORT).show();
		return true;
	}

	// public void setIndicatorLoading(boolean isLoading) {
	// if (isLoading) {
	// progressBar.setVisibility(View.VISIBLE);
	// } else {
	// progressBar.setVisibility(View.GONE);
	// }
	// }

	public class CommentTimeLineTask extends AsyncTask<DBUserBean, Object, Object> {

		private int pageNo;
		private boolean isNew;

		public CommentTimeLineTask(int pageNo, boolean isNew) {
			this.pageNo = pageNo;
			this.isNew = isNew;
		}

		private String getUrl(DBUserBean... params) {
			return URLHelper.COMMENTS_TIMELINE_BY_MSGID + "?" + "id=" + msgBean.getId() + "&access_token=" + params[0].getToken() + "&source=" + URLHelper.APP_KEY + "&page=" + this.pageNo + "&count="
					+ STATUSCOUNT;
		}

		@Override
		protected Object doInBackground(DBUserBean... params) {
			isOnline = true;
			String json0;
			try {
				json0 = URLHelper.queryStringForGet(getUrl(params));

				Gson gson = new Gson();
				CommentListBean commentListBean = gson.fromJson(json0, CommentListBean.class);
				if (commentListBean != null) {
					if (isNew) {
						msgList.clear();
						msgList.add(msgBean);
						msgList.add(null);
						newCommentList.clear();
					}
					newCommentList.addAll((ArrayList<CommentBean>) commentListBean.getItemList());
					msgList.addAll(newCommentList);
				} else {
					DetailsTimeLineActivity.this.commentPageNo--;
				}
			} catch (ConnectException e) {
				isOnline = false;
				if (DetailsTimeLineActivity.this.commentPageNo != 1) {
					DetailsTimeLineActivity.this.commentPageNo--;
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (isOnline) {
				// commentAdapter.setMessageList(msgList);
				commentAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(DetailsTimeLineActivity.this, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			commentAdapter.setLoading(false);
			commentAdapter.setShowComment(true);
			// setIndicatorLoading(false);
			mPullRefreshListView.onRefreshComplete();
		}
	}

	public class RepostTimeLineTask extends AsyncTask<DBUserBean, Object, Object> {

		private int pageNo;
		private boolean isNew;

		public RepostTimeLineTask(int pageNo, boolean isNew) {
			this.pageNo = pageNo;
			this.isNew = isNew;
		}

		private String getUrl(DBUserBean... params) {
			return URLHelper.REPOSTS_TIMELINE_BY_MSGID + "?" + "id=" + msgBean.getId() + "&access_token=" + params[0].getToken() + "&source=" + URLHelper.APP_KEY + "&page=" + this.pageNo + "&count="
					+ STATUSCOUNT;
		}

		@Override
		protected Object doInBackground(DBUserBean... params) {
			isOnline = true;
			String json0;
			try {
				json0 = URLHelper.queryStringForGet(getUrl(params));

				Gson gson = new Gson();
				RepostListBean repostListBean = gson.fromJson(json0, RepostListBean.class);
				if (repostListBean != null) {
					if (isNew) {
						msgList.clear();
						msgList.add(msgBean);
						msgList.add(null);
						newRepostList.clear();
					}
					newRepostList.addAll((ArrayList<MessageBean>) repostListBean.getItemList());
					msgList.addAll(newRepostList);
				} else {
					DetailsTimeLineActivity.this.repostPageNo--;
				}
			} catch (ConnectException e) {
				isOnline = false;
				if (DetailsTimeLineActivity.this.repostPageNo != 1) {
					DetailsTimeLineActivity.this.repostPageNo--;
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (isOnline) {
				// commentAdapter.setMessageList(msgList);
				commentAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(DetailsTimeLineActivity.this, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			commentAdapter.setLoading(false);
			commentAdapter.setShowBackground(false);
			commentAdapter.setShowComment(false);
			// setIndicatorLoading(false);
			if (firstVisibleItem > 1) {
				mPullRefreshListView.getRefreshableView().setSelection(2);
			}
			mPullRefreshListView.onRefreshComplete();
		}
	}

	public void indicatorClick(View v) {
		msgList.clear();
		msgList.add(msgBean);
		msgList.add(null);

		commentAdapter.setLoading(false);
		commentAdapter.setShowBackground(false);
		switch (v.getId()) {
		case R.id.comment_timeline:
			commentAdapter.setShowComment(true);
			setShowComment(true);
			msgList.addAll(newCommentList);
			commentAdapter.notifyDataSetChanged();
			// mPullRefreshListView.getRefreshableView().setSelection(4);
			if (firstVisibleItem > 1) {
				if (repost_scroll_pos >= 1 && comment_scroll_pos <= 1) {
					mPullRefreshListView.getRefreshableView().setSelection(2);
				} else {
					mPullRefreshListView.getRefreshableView().setSelectionFromTop(comment_scroll_pos, comment_scroll_top);
				}
			}
			break;
		case R.id.repost_timeline:
			commentAdapter.setShowComment(false);
			setShowComment(false);
			if (newRepostList.size() != 0) {
				firstClick = false;
				msgList.addAll(newRepostList);
			} else {
				firstClick = true;

			}
			if (firstClick) {
				commentAdapter.setLoading(true);
				if (firstVisibleItem > 1) {
					commentAdapter.setShowBackground(true);
				}
				commentAdapter.notifyDataSetChanged();
				if (firstVisibleItem > 1) {
					mPullRefreshListView.getRefreshableView().setSelection(2);
				}
				DetailsTimeLineActivity.this.repostPageNo = 1;
				new RepostTimeLineTask(DetailsTimeLineActivity.this.repostPageNo, false).execute(user);
			} else {
				commentAdapter.notifyDataSetChanged();
				if (firstVisibleItem > 1) {
					if (repost_scroll_pos <= 1 && comment_scroll_pos >= 1) {
						mPullRefreshListView.getRefreshableView().setSelection(2);
					}else{
						mPullRefreshListView.getRefreshableView().setSelectionFromTop(repost_scroll_pos, repost_scroll_top);
					}
				}
			}
			break;

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
