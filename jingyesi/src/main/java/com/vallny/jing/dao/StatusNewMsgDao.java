package com.vallny.jing.dao;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.vallny.jing.util.http.FileUploaderHttpHelper;
import com.vallny.jing.util.http.HttpUtility;
import com.vallny.jing.util.http.URLHelper;
import com.vallny.jing.util.http.HttpUtility.HttpMethod;

public class StatusNewMsgDao {

	  private String access_token;

	    private String pic;

//	    private GeoBean geoBean;

//	    public StatusNewMsgDao setGeoBean(GeoBean geoBean) {
//	        this.geoBean = geoBean;
//	        return this;
//	    }

	    public StatusNewMsgDao setPic(String pic) {
	        this.pic = pic;
	        return this;
	    }

	    public StatusNewMsgDao(String access_token) {

	        this.access_token = access_token;
	    }

	    public boolean sendNewMsg(String str, FileUploaderHttpHelper.ProgressListener listener) {

	        if (!TextUtils.isEmpty(pic)) {
	            return sendNewMsgWithPic(str, listener);

	        }
	        String url = URLHelper.STATUSES_UPDATE;
	        Map<String, String> map = new HashMap<String, String>();
	        map.put("access_token", access_token);
	        map.put("status", str);
	        map.put("source", URLHelper.APP_KEY);
//	        if (geoBean != null) {
//	            map.put("lat", String.valueOf(geoBean.getLat()));
//	            map.put("long", String.valueOf(geoBean.getLon()));
//	        }

	        
	        return HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

	    }

	    private boolean sendNewMsgWithPic(String str, FileUploaderHttpHelper.ProgressListener listener){
	        String url = URLHelper.STATUSES_UPLOAD;
	        Map<String, String> map = new HashMap<String, String>();
	        map.put("access_token", access_token);
	        map.put("status", str);
	        map.put("source", URLHelper.APP_KEY);
//	        if (geoBean != null) {
//	            map.put("lat", String.valueOf(geoBean.getLat()));
//	            map.put("long", String.valueOf(geoBean.getLon()));
//	        }

	        return HttpUtility.getInstance().executeUploadTask(url, map, pic, "pic", listener);

	    }
	
}
