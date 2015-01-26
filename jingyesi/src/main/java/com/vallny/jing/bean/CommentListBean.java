package com.vallny.jing.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CommentListBean extends ListBean<CommentBean, CommentListBean> implements Parcelable {

	private List<CommentBean> comments = new ArrayList<CommentBean>();

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeInt(total_number);
		dest.writeLong(previous_cursor);
		dest.writeLong(next_cursor);

		dest.writeTypedList(comments);
	}

	public static final Parcelable.Creator<CommentListBean> CREATOR = new Parcelable.Creator<CommentListBean>() {
		public CommentListBean createFromParcel(Parcel in) {
			CommentListBean commentListBean = new CommentListBean();

			commentListBean.total_number = in.readInt();
			commentListBean.previous_cursor = in.readLong();
			commentListBean.next_cursor = in.readLong();

			commentListBean.comments = new ArrayList<CommentBean>();
			in.readTypedList(commentListBean.comments, CommentBean.CREATOR);

			return commentListBean;
		}

		public CommentListBean[] newArray(int size) {
			return new CommentListBean[size];
		}
	};

	private List<CommentBean> getComments() {
		return comments;
	}

	public void setComments(List<CommentBean> comments) {
		this.comments = comments;
	}

	@Override
	public CommentBean getItem(int position) {
		return getComments().get(position);
	}

	@Override
	public List<CommentBean> getItemList() {
		return getComments();
	}

	@Override
	public int getSize() {
		return comments.size();
	}

	public void replaceAll(CommentListBean newValue) {
		if (newValue != null && newValue.getSize() > 0) {
			setTotal_number(newValue.getTotal_number());
			getItemList().clear();
			getItemList().addAll(newValue.getItemList());
		}
	}

	public void clear() {
		setTotal_number(0);
		getItemList().clear();
	}

	@Override
	public void addNewData(CommentListBean newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOldData(CommentListBean oldValue) {
		// TODO Auto-generated method stub

	}

}
