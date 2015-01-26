package com.vallny.jing.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vallny.jing.R;
import com.vallny.jing.bean.UserBean;
import com.vallny.jing.global._BaseAdapter;

public class FriendsAdapter extends _BaseAdapter {

	public static final int GRIDLAYOUT_CHILDVIEW_COUNT = 9;

	
	private DisplayImageOptions options;

	private ImageLoader imageLoader;
	private List<UserBean> msgList;
	private LayoutInflater mInflater;
	private Context context;

	public FriendsAdapter(Context context, ImageLoader imageLoader) {
		mInflater = LayoutInflater.from(context);
		msgList = new ArrayList<UserBean>();
		this.context = context;
		this.imageLoader = imageLoader;
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.nga_bg).showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(0)).build();
	}

	public void setUserList(List<UserBean> msgList) {
		this.msgList = msgList;

	}

	public List<UserBean> getUserList() {

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
			holder.user_icon = (ImageView) convertView.findViewById(R.id.user_icon);
			holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
			holder.checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		initView(holder);
		UserBean user = msgList.get(position);
		imageLoader.displayImage(user.getProfile_image_url(), holder.user_icon, options, animateFirstListener);
		setScreenName(holder.user_name,user);
		return convertView;
	}

	private void setScreenName(TextView screen_name_tv, UserBean userBean) {
		String screen_name = userBean.getScreen_name();
		String remark = userBean.getRemark();
		if(!"".equals(remark)){
			screen_name = screen_name+"("+remark+")";
		}
		screen_name_tv.setText(screen_name);
	}
	
	private void initView(ViewHolder holder) {
		holder.checkbox.setVisibility(View.INVISIBLE);
	}

	private class ViewHolder {

		private ImageView user_icon;
		private TextView user_name;
		private ImageView checkbox;

	}

	
}
