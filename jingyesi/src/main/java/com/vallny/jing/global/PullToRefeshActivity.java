package com.vallny.jing.global;

import android.widget.AbsListView.OnScrollListener;

import com.handmark.pulltorefresh.library.ImageLoaderScrollListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PullToRefeshActivity extends BaseActivity {
	
	protected PullToRefreshListView mPullRefreshListView;
	
	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;
	
	protected int pageNo;
	protected Boolean isOnline;
	

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	private void applyScrollListener() {
		mPullRefreshListView.setOnImageLoaderScrollListener(new _ImageLoaderScrollListener());
	}

	class _ImageLoaderScrollListener implements ImageLoaderScrollListener {
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
}
