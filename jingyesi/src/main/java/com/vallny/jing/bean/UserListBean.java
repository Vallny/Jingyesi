package com.vallny.jing.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vallny.jing.util.global.ObjectToStringUtility;

public class UserListBean extends ListBean<UserBean, UserListBean> implements Parcelable {

	private List<UserBean> statuses = new ArrayList<UserBean>();
	private int removedCount = 0;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(total_number);
		dest.writeLong(previous_cursor);
		dest.writeLong(next_cursor);

		dest.writeTypedList(statuses);
		dest.writeInt(removedCount);
	}

	public static final Parcelable.Creator<UserListBean> CREATOR = new Parcelable.Creator<UserListBean>() {
		public UserListBean createFromParcel(Parcel in) {
			UserListBean userListBean = new UserListBean();

			userListBean.total_number = in.readInt();
			userListBean.previous_cursor = in.readLong();
			userListBean.next_cursor = in.readLong();

			userListBean.statuses = new ArrayList<UserBean>();
			in.readTypedList(userListBean.statuses, UserBean.CREATOR);

			userListBean.removedCount = in.readInt();

			return userListBean;
		}

		public UserListBean[] newArray(int size) {
			return new UserListBean[size];
		}
	};

	public List<UserBean> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<UserBean> statuses) {
		this.statuses = statuses;
	}

	@Override
	public int getSize() {
		return statuses.size();
	}

	@Override
	public UserBean getItem(int position) {
		return getStatuses().get(position);
	}

	@Override
	public List<UserBean> getItemList() {
		return getStatuses();
	}

	public int getReceivedCount() {
		return getSize() + removedCount;
	}

	public void removedCountPlus() {
		removedCount++;
	}

	@Override
	public void addNewData(UserListBean newValue) {

		if (newValue == null || newValue.getSize() == 0) {
			return;
		}

		// boolean receivedCountBelowRequestCount = newValue.getReceivedCount()
		// < Integer.valueOf(SettingUtility.getMsgCount());
		// boolean receivedCountEqualRequestCount = newValue.getReceivedCount()
		// >= Integer.valueOf(SettingUtility.getMsgCount());
		boolean receivedCountBelowRequestCount = newValue.getReceivedCount() < Integer.valueOf("25");
		boolean receivedCountEqualRequestCount = newValue.getReceivedCount() >= Integer.valueOf("25");
		if (receivedCountEqualRequestCount && this.getSize() > 0) {
			newValue.getItemList().add(null);
		}
		this.getItemList().addAll(0, newValue.getItemList());
		this.setTotal_number(newValue.getTotal_number());
	}

	@Override
	public void addOldData(UserListBean oldValue) {
		if (oldValue != null && oldValue.getSize() > 1) {
			getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
			setTotal_number(oldValue.getTotal_number());

		}
	}

	public void addMiddleData(int position, UserListBean middleValue, boolean towardsBottom) {
		if (middleValue == null)
			return;

		if (middleValue.getSize() == 0 || middleValue.getSize() == 1) {
			getItemList().remove(position);
			return;
		}

		List<UserBean> middleData = middleValue.getItemList().subList(1, middleValue.getSize());

		String beginId = getItem(position + 1).getId();
		String endId = getItem(position - 1).getId();
		Iterator<UserBean> iterator = middleData.iterator();
		while (iterator.hasNext()) {
			UserBean msg = iterator.next();
			boolean notNull = !TextUtils.isEmpty(msg.getId());
			if (notNull) {
				if (msg.getId().equals(beginId) || msg.getId().equals(endId)) {
					iterator.remove();
				}
			}
		}

		getItemList().addAll(position, middleData);

	}

	public void replaceData(UserListBean value) {
		if (value == null)
			return;
		getItemList().clear();
		getItemList().addAll(value.getItemList());
		setTotal_number(value.getTotal_number());
	}

	public UserListBean copy() {
		UserListBean object = new UserListBean();
		object.replaceData(UserListBean.this);
		return object;
	}

//	@Override
//	public String toString() {
//		return ObjectToStringUtility.toString(this);
//	}

}