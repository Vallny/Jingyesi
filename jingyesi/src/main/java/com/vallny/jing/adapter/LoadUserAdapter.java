package com.vallny.jing.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vallny.jing.R;
import com.vallny.jing.bean.DBUserBean;

public class LoadUserAdapter extends BaseAdapter {

	private List<DBUserBean> userList;
	private LayoutInflater mInflater;

	public LoadUserAdapter(List<DBUserBean> userList, Context context) {
		this.userList = userList;
		mInflater = LayoutInflater.from(context);
	}

	public void setUserList(List<DBUserBean> userList) {
		this.userList = userList;

	}

	@Override
	public int getCount() {
		return userList.size();
	}

	@Override
	public Object getItem(int position) {
		return userList.get(position);
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
			convertView = mInflater.inflate(R.layout.login_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.user_name);
			holder.imageView = (ImageView) convertView.findViewById(R.id.user_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.textView.setText(userList.get(position).getUserName());
		holder.imageView.setImageBitmap(userList.get(position).getUserIcon());

		return convertView;
	}

	private class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}
}
