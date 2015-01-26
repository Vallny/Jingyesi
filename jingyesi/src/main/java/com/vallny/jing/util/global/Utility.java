package com.vallny.jing.util.global;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vallny.jing.global.GlobalContext;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;

public class Utility {

	public static String encodeUrl(Map<String, String> param) {
		if (param == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		Set<String> keys = param.keySet();
		boolean first = true;

		for (String key : keys) {
			String value = param.get(key);
			// pain...EditMyProfileDao params' values can be empty
			if (!TextUtils.isEmpty(value) || key.equals("description") || key.equals("url")) {
				if (first)
					first = false;
				else
					sb.append("&");
				try {
					sb.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(param.get(key), "UTF-8"));
				} catch (UnsupportedEncodingException e) {

				}
			}
		}
		return sb.toString();
	}

	public static boolean isWeiboAccountIdLink(String url) {
		return !TextUtils.isEmpty(url) && url.startsWith("http://weibo.com/u/");
	}

	public static boolean isWeiboAccountDomainLink(String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		} else {
			boolean a = url.startsWith("http://weibo.com/");
			boolean b = !url.contains("?");

			String tmp = url;
			if (tmp.endsWith("/"))
				tmp = tmp.substring(0, tmp.lastIndexOf("/"));

			int count = 0;
			char[] value = tmp.toCharArray();
			for (char c : value) {
				if ("/".equalsIgnoreCase(String.valueOf(c))) {
					count++;
				}
			}
			return a && b && count == 3;
		}
	}

	public static int dip2px(int dipValue) {
		float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
		return (int) ((dipValue * reSize) + 0.5);
	}

	 public static void closeSilently(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
    }
	 
	 public static int getBitmapMaxWidthAndMaxHeight() {
	        return 2048;
	    }
	 public static int getScreenWidth() {
	        Activity activity = GlobalContext.getInstance().getActivity();
	        if (activity != null) {
	            Display display = activity.getWindowManager().getDefaultDisplay();
	            DisplayMetrics metrics = new DisplayMetrics();
	            display.getMetrics(metrics);
	            return metrics.widthPixels;
	        }

	        return 480;
	    }

	    public static int getScreenHeight() {
	        Activity activity = GlobalContext.getInstance().getActivity();
	        if (activity != null) {
	            Display display = activity.getWindowManager().getDefaultDisplay();
	            DisplayMetrics metrics = new DisplayMetrics();
	            display.getMetrics(metrics);
	            return metrics.heightPixels;
	        }
	        return 800;
	    }
	    
	    public static boolean isIntentSafe(Activity activity, Intent intent) {
	        PackageManager packageManager = activity.getPackageManager();
	        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
	        return activities.size() > 0;
	    }
	    
	    public static String getPicPathFromUri(Uri uri, Activity activity) {
	        String value = uri.getPath();

	        if (value.startsWith("/external")) {
	            String[] proj = {MediaStore.Images.Media.DATA};
	            Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	            cursor.moveToFirst();
	            return cursor.getString(column_index);
	        } else {
	            return value;
	        }
	    }

}
