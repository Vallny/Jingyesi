package com.vallny.jing.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.vallny.jing.R;
import com.vallny.jing.activity.WriteWeiboActivity;
import com.vallny.jing.dao.StatusNewMsgDao;
import com.vallny.jing.util.global.NotificationUtility;
import com.vallny.jing.util.http.FileUploaderHttpHelper;
import com.vallny.jing.util.image.ImageUtility;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

public class SendWeiboService extends Service {

	private Notification notification;

	private String token;
	private String content;
	private String picPath;

	private long size;
	private boolean result;

	private Handler handler = new Handler();
	private Map<WeiboSendTask, Boolean> tasksResult = new HashMap<WeiboSendTask, Boolean>();
	private Map<WeiboSendTask, Integer> tasksNotifications = new HashMap<WeiboSendTask, Integer>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		token = intent.getStringExtra("token");
		content = intent.getStringExtra("content");
		picPath = intent.getStringExtra("picPath");

		WeiboSendTask task = new WeiboSendTask();
		tasksResult.put(task, false);
		task.execute();

		return START_REDELIVER_INTENT;
	}

	/**
	 * 可以关闭Service
	 * 
	 * @param currentTask
	 */
	public void stopServiceIfTasksAreEnd(WeiboSendTask currentTask) {

		tasksResult.put(currentTask, true);

		boolean isAllTaskEnd = true;
		Set<WeiboSendTask> taskSet = tasksResult.keySet();
		for (WeiboSendTask task : taskSet) {
			if (!tasksResult.get(task)) {
				isAllTaskEnd = false;
				break;
			}
		}
		if (isAllTaskEnd) {
			stopForeground(true);
			stopSelf();
		}
	}

	/**
	 * 微博发送Task
	 * @author SIA
	 *
	 */
	class WeiboSendTask extends AsyncTask<Intent, Void, Void> {

		private Intent intent;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			Notification.Builder builder = new Notification.Builder(SendWeiboService.this).setTicker(getString(R.string.sending)).setContentTitle(getString(R.string.sending)).setContentText(content)
					.setOnlyAlertOnce(true).setOngoing(true).setSmallIcon(R.drawable.upload_white);
			// 设置没有进度的进度条
			builder.setProgress(0, 0, true);
			notification = builder.build();

			int notificationId = new Random().nextInt(Integer.MAX_VALUE);
			NotificationUtility.show(notification, notificationId);
			tasksNotifications.put(WeiboSendTask.this, notificationId);

		}

		/**
		 * 发送文本
		 * @return
		 */
		private boolean sendText() {
			return new StatusNewMsgDao(token).sendNewMsg(content, null);
		}

		/**
		 * 发送图片
		 * @param uploadPicPath
		 * @return
		 */
		private boolean sendPic(String uploadPicPath) {
			return new StatusNewMsgDao(token).setPic(uploadPicPath).sendNewMsg(content, new UploadProgressListener());
		}

		@Override
		protected Void doInBackground(Intent... params) {
			if (!TextUtils.isEmpty(picPath)) {
				String uploadPicPath = ImageUtility.compressPic(SendWeiboService.this, picPath);
				size = new File(uploadPicPath).length();
				result = sendPic(uploadPicPath);
			} else {
				result = sendText();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void svoid) {
			super.onPostExecute(svoid);
			if (result) {
				showSuccessfulNotification(this);
			} else {
				showFailedNotification(this);
			}
		}

		class UploadProgressListener implements FileUploaderHttpHelper.ProgressListener {
			private double lastStatus = -1d;
			private long lastMillis = -1L;
		
			@Override
			public void transferred(long data) {
		
				if (data != -1) {
					double r = data / (double) size;
		
					if (Math.abs(r - lastStatus) < 0.01d) {
						return;
					}
		
					if (System.currentTimeMillis() - lastMillis < 200L) {
						return;
					}
		
					lastStatus = r;
		
					lastMillis = System.currentTimeMillis();
		
					Notification.Builder builder = new Notification.Builder(SendWeiboService.this).setTicker(getString(R.string.send_photo)).setContentTitle(getString(R.string.send_photo))
							.setNumber((int) (r * 100)).setContentText(content).setProgress((int) size, (int) data, false).setOnlyAlertOnce(true).setOngoing(true)
							.setSmallIcon(R.drawable.upload_white);
					notification = builder.build();
				} else {
					Notification.Builder builder = new Notification.Builder(SendWeiboService.this).setTicker(getString(R.string.send_photo)).setContentTitle(getString(R.string.send_successfully))
							.setContentText(content).setNumber(100).setProgress(100, 100, false).setOnlyAlertOnce(true).setOngoing(true).setSmallIcon(R.drawable.upload_white);
					notification = builder.build();
				}
				NotificationUtility.show(notification, tasksNotifications.get(WeiboSendTask.this));
			}
		
			@Override
			public void waitServerResponse() {
			}
		
			@Override
			public void completed() {
			}
		
		}

		/**
		 * 发送失败
		 * 
		 * @param task
		 */
		private void showFailedNotification(final WeiboSendTask task) {
			Notification.Builder builder = new Notification.Builder(SendWeiboService.this).setTicker(getString(R.string.send_failed)).setContentTitle(getString(R.string.send_faile_click_to_open))
					.setContentText(content).setOnlyAlertOnce(true).setAutoCancel(true).setSmallIcon(R.drawable.send_failed).setOngoing(false);

			Intent notifyIntent = WriteWeiboActivity.startBecauseSendFailed(SendWeiboService.this, token, content,picPath);

			PendingIntent pendingIntent = PendingIntent.getActivity(SendWeiboService.this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			builder.setContentIntent(pendingIntent);

			// Notification notification;
			//
			// Notification.BigTextStyle bigTextStyle = new
			// Notification.BigTextStyle(builder);
			// bigTextStyle.setBigContentTitle(getString(R.string.send_faile_click_to_open));
			// bigTextStyle.bigText(content);
			// bigTextStyle.setSummaryText(account.getUsernick());
			// builder.setStyle(bigTextStyle);
			//
			// Intent intent = new Intent(SendCommentService.this,
			// SendCommentService.class);
			// intent.putExtra("oriMsg", oriMsg);
			// intent.putExtra("content", content);
			// intent.putExtra("comment_ori", comment_ori);
			// intent.putExtra("token", token);
			// intent.putExtra("account", account);
			//
			// intent.putExtra("lastNotificationId",
			// tasksNotifications.get(task));
			//
			// PendingIntent retrySendIntent =
			// PendingIntent.getService(SendCommentService.this, 0, intent,
			// PendingIntent.FLAG_UPDATE_CURRENT);
			// builder.addAction(R.drawable.send_light,
			// getString(R.string.retry_send), retrySendIntent);
			notification = builder.build();

			final int id = tasksNotifications.get(task);
			NotificationUtility.show(notification, id);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopServiceIfTasksAreEnd(task);
				}
			}, 3000);
		}

		/**
		 * 发送成功
		 * 
		 * @param task
		 */
		private void showSuccessfulNotification(final WeiboSendTask task) {
			Notification.Builder builder = new Notification.Builder(SendWeiboService.this).setTicker(getString(R.string.send_successfully)).setContentTitle(getString(R.string.send_successfully))
					.setOnlyAlertOnce(true).setAutoCancel(true).setSmallIcon(R.drawable.send_successfully).setOngoing(false);
			Notification notification = builder.build();
			final int id = tasksNotifications.get(task);

			NotificationUtility.show(notification, id);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					NotificationUtility.cancel(id);
					stopServiceIfTasksAreEnd(task);
				}
			}, 3000);
		}
	}

}
