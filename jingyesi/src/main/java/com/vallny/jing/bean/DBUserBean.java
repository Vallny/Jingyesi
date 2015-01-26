package com.vallny.jing.bean;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class DBUserBean implements Parcelable {
	public static final String ID = "_id";
	public static final String USERID = "uid";
	public static final String TOKEN = "token";
	public static final String USERNAME = "userName";
	public static final String USERICON = "userIcon";

	private String id;
	private String uid;
	private String token;
	private String userName;
	private Bitmap userIcon;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Bitmap getUserIcon() {
		return userIcon;
	}

	public void setUserIcon(Bitmap userIcon) {
		this.userIcon = userIcon;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(uid);
		dest.writeString(token);
		dest.writeString(userName);
		dest.writeParcelable(userIcon, flags);
		
	}
	
	 public static final Parcelable.Creator<DBUserBean> CREATOR =
             new Parcelable.Creator<DBUserBean>() {
                 public DBUserBean createFromParcel(Parcel in) {
                	 DBUserBean user = new DBUserBean();
                	 user.id = in.readString();
                	 user.uid = in.readString();
                	 user.token = in.readString();
                	 user.userName = in.readString();
                	 user.userIcon = in.readParcelable(Bitmap.class.getClassLoader());
                	 
                	 
					return user;
                   
                 }

                 public DBUserBean[] newArray(int size) {
                     return new DBUserBean[size];
                 }
             };
}
