package com.vallny.jing.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vallny.jing.R;
import com.vallny.jing.bean.DBUserBean;
import com.vallny.jing.bean.MessageBean;
import com.vallny.jing.global._BaseAdapter;
import com.vallny.jing.util.link.ClickableTextViewMentionOnTouchListener;

public class FriendsTimeLineAdapter extends _BaseAdapter {

	public static final int GRIDLAYOUT_CHILDVIEW_COUNT = 9;
	public static final int MESSAGE_BEAN = R.id.tag_msg_bean;
	public static final int DBUSER_BEAN = R.id.tag_dbuser_bean;

	private DisplayImageOptions options;

	private ImageLoader imageLoader;
	private List<MessageBean> msgList;
	private DBUserBean user;
	private LayoutInflater mInflater;

	public FriendsTimeLineAdapter(Context context, ImageLoader imageLoader, DBUserBean user) {
		mInflater = LayoutInflater.from(context);
		msgList = new ArrayList<MessageBean>();
		this.user = user;
		this.imageLoader = imageLoader;
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.nga_bg).showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(0)).build();
	}

	public void setMessageList(List<MessageBean> msgList) {
		this.msgList = msgList;

	}

	public List<MessageBean> getMessageList() {

		return msgList;
	}

	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.weibo_content_item, null);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.screen_name = (TextView) convertView.findViewById(R.id.screen_name);
			holder.created_at = (TextView) convertView.findViewById(R.id.created_at);
			holder.source = (TextView) convertView.findViewById(R.id.source);
			holder.reposts_count = (TextView) convertView.findViewById(R.id.reposts_count);
			holder.comments_count = (TextView) convertView.findViewById(R.id.comments_count);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.single_imageview = (ImageView) convertView.findViewById(R.id.single_imageview);
			holder.gridLayout = (GridLayout) convertView.findViewById(R.id.grid_layout);

			holder.retweeted_status = (LinearLayout) convertView.findViewById(R.id.retweeted_status);
			holder.retweeted_icon = (ImageView) convertView.findViewById(R.id.retweeted_icon);
			holder.retweeted_screen_name = (TextView) convertView.findViewById(R.id.retweeted_screen_name);
			holder.retweeted_created_at = (TextView) convertView.findViewById(R.id.retweeted_created_at);
			holder.retweeted_source = (TextView) convertView.findViewById(R.id.retweeted_source);
			holder.retweeted_reposts_count = (TextView) convertView.findViewById(R.id.retweeted_reposts_count);
			holder.retweeted_comments_count = (TextView) convertView.findViewById(R.id.retweeted_comments_count);
			holder.retweeted_text = (TextView) convertView.findViewById(R.id.retweeted_text);
			holder.retweeted_single_imageview = (ImageView) convertView.findViewById(R.id.retweeted_single_imageview);
			holder.retweeted_gridLayout = (GridLayout) convertView.findViewById(R.id.retweeted_grid_layout);

			// holder.imageView = (ImageView)
			// convertView.findViewById(R.id.user_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MessageBean msgBean = msgList.get(position);
		convertView.setTag(MESSAGE_BEAN, msgBean);
		convertView.setTag(DBUSER_BEAN, user);
		imageLoader.displayImage(msgBean.getUser().getProfile_image_url(), holder.icon, options, animateFirstListener);
		setScreenName(holder.screen_name, msgBean);
		holder.created_at.setText(msgBean.getListviewItemShowTime());
		holder.source.setText(msgBean.getSourceString());
		holder.reposts_count.setText(msgBean.getReposts_count() + "");
		holder.comments_count.setText(msgBean.getComments_count() + "");

		holder.text.setText(msgBean.getListViewSpannableString());
		holder.text.setOnTouchListener(new ClickableTextViewMentionOnTouchListener());

		int count = msgBean.getPicCount();
		switch (count) {
		case 0:
			clearPics(holder, 0);
			break;
		case 1:
			clearPics(holder, 1);
			holder.single_imageview.setVisibility(View.VISIBLE);
			holder.single_imageview.setTag(msgBean.getOriginal_pic());
			imageLoader.displayImage(msgBean.getBmiddle_pic(), holder.single_imageview, options, animateFirstListener);

			break;

		default:
			clearPics(holder, 2);
			holder.gridLayout.setVisibility(View.VISIBLE);

			List<String> middle_picUrls = msgBean.getMiddlePicUrls();
			List<String> high_picUrls = msgBean.getHighPicUrls();
			for (int i = 0; i < count; i++) {
				ImageView imageview = (ImageView) holder.gridLayout.getChildAt(i);
				imageview.setVisibility(View.VISIBLE);
				imageview.setTag(high_picUrls.get(i));
				imageLoader.displayImage(middle_picUrls.get(i), imageview, options, animateFirstListener);
			}

			break;
		}

		if (msgBean.getRetweeted_status() != null) {
			holder.retweeted_status.setVisibility(View.VISIBLE);
			MessageBean retweeted_msgBean = msgBean.getRetweeted_status();
			imageLoader.displayImage(retweeted_msgBean.getUser().getProfile_image_url(), holder.retweeted_icon, options, animateFirstListener);
			setScreenName(holder.retweeted_screen_name, retweeted_msgBean);
			holder.retweeted_created_at.setText(retweeted_msgBean.getListviewItemShowTime());
			holder.retweeted_source.setText(retweeted_msgBean.getSourceString());
			holder.retweeted_reposts_count.setText(retweeted_msgBean.getReposts_count() + "");
			holder.retweeted_comments_count.setText(retweeted_msgBean.getComments_count() + "");

			holder.retweeted_text.setText(retweeted_msgBean.getListViewSpannableString());
			holder.retweeted_text.setOnTouchListener(new ClickableTextViewMentionOnTouchListener());

			int retweeted_count = retweeted_msgBean.getPicCount();
			switch (retweeted_count) {
			case 0:
				clearRetweetedPics(holder, 0);
				break;
			case 1:
				clearRetweetedPics(holder, 1);
				holder.retweeted_single_imageview.setVisibility(View.VISIBLE);
				holder.retweeted_single_imageview.setTag(retweeted_msgBean.getOriginal_pic());
				imageLoader.displayImage(retweeted_msgBean.getBmiddle_pic(), holder.retweeted_single_imageview, options, animateFirstListener);
				break;
			default:
				clearRetweetedPics(holder, 2);
				holder.retweeted_gridLayout.setVisibility(View.VISIBLE);

				List<String> middle_picUrls = retweeted_msgBean.getMiddlePicUrls();
				List<String> high_picUrls = retweeted_msgBean.getHighPicUrls();
				for (int i = 0; i < retweeted_count; i++) {
					ImageView imageview = (ImageView) holder.retweeted_gridLayout.getChildAt(i);
					imageview.setVisibility(View.VISIBLE);
					imageview.setTag(high_picUrls.get(i));
					imageLoader.displayImage(middle_picUrls.get(i), imageview, options, animateFirstListener);
				}
				break;
			}
		} else {
			clearRetweetedStatus(holder);
		}

		return convertView;
	}

	private void setScreenName(TextView screen_name_tv, MessageBean msgBean) {
		String screen_name = msgBean.getUser().getScreen_name();
		String remark = msgBean.getUser().getRemark();
		if (!"".equals(remark)) {
			screen_name = screen_name + "(" + remark + ")";
		}
		screen_name_tv.setText(screen_name);
	}

	private void clearRetweetedStatus(ViewHolder holder) {
		holder.retweeted_status.setVisibility(View.GONE);
		holder.retweeted_icon.setImageDrawable(null);
		holder.retweeted_screen_name.setText(null);
		holder.retweeted_created_at.setText(null);
		holder.retweeted_source.setText(null);
		holder.retweeted_reposts_count.setText(null);
		holder.retweeted_comments_count.setText(null);
		holder.retweeted_text.setText(null);
		clearRetweetedPics(holder, 0);
	}

	private void clearRetweetedPics(ViewHolder holder, int flag) {
		if (flag == 1 || flag == 0) {
			clearGridLayout(holder.retweeted_gridLayout);
			holder.retweeted_gridLayout.setVisibility(View.GONE);
		}
		if (flag == 2 || flag == 0) {
			holder.retweeted_single_imageview.setImageDrawable(null);
			holder.retweeted_single_imageview.setVisibility(View.GONE);
		}

	}

	private void clearPics(ViewHolder holder, int flag) {
		if (flag == 1 || flag == 0) {
			clearGridLayout(holder.gridLayout);
			holder.gridLayout.setVisibility(View.GONE);
		}
		if (flag == 2 || flag == 0) {
			holder.single_imageview.setImageDrawable(null);
			holder.single_imageview.setVisibility(View.GONE);
		}
	}

	private void clearGridLayout(GridLayout gridLayout) {
		if (gridLayout == null)
			return;
		for (int i = 0; i < GRIDLAYOUT_CHILDVIEW_COUNT; i++) {
			ImageView iv = (ImageView) gridLayout.getChildAt(i);
			if (iv != null) {
				iv.setImageDrawable(null);
				iv.setVisibility(View.GONE);
			}
		}
	}

	private class ViewHolder {
		private ImageView icon, retweeted_icon;
		private TextView screen_name, retweeted_screen_name;
		private TextView created_at, retweeted_created_at;
		private TextView source, retweeted_source;
		private TextView reposts_count, retweeted_reposts_count;
		private TextView comments_count, retweeted_comments_count;
		private TextView text, retweeted_text;
		private ImageView single_imageview, retweeted_single_imageview;
		private GridLayout gridLayout, retweeted_gridLayout;
		private LinearLayout retweeted_status;

	}

}
