package com.vallny.jing.util.global;

import com.vallny.jing.global.GlobalContext;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class NotificationUtility {

	public static void show(Notification notification, int id) {
		NotificationManager notificationManager = (NotificationManager) GlobalContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}

	public static void cancel(int id) {
		NotificationManager notificationManager = (NotificationManager) GlobalContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}

}
