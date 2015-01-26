package com.vallny.jing.global;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.vallny.jing.R;
import com.vallny.jing.bean.UserBean;
import com.vallny.jing.util.emotion.SmileyMap;
import com.vallny.jing.util.global.Utility;

public final class GlobalContext extends Application {

	// singleton
	private static GlobalContext globalContext = null;

	// image size
	private Activity activity = null;
	private Activity currentRunningActivity = null;
	private DisplayMetrics displayMetrics = null;

	// image memory cache
	private LruCache<String, Bitmap> avatarCache = null;

	// current account info
	// private AccountBean accountBean = null;

	public boolean startedApp = false;

	private LinkedHashMap<Integer, LinkedHashMap<String, Bitmap>> emotionsPic = new LinkedHashMap<Integer, LinkedHashMap<String, Bitmap>>();

	// private GroupListBean group = null;

	// private MusicInfo musicInfo = new MusicInfo();

	private Handler handler = new Handler();

	public boolean tokenExpiredDialogIsShowing = false;

	@Override
	public void onCreate() {
		super.onCreate();
		globalContext = this;
		buildCache();

		if (Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		}
		initImageLoader(getApplicationContext());

		// CrashManagerConstants.loadFromContext(this);
		// CrashManager.registerHandler();
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs().build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	public static GlobalContext getInstance() {
		return globalContext;
	}

	public Handler getHandler() {
		return handler;
	}

	// public GroupListBean getGroup() {
	// if (group == null) {
	// group =
	// GroupDBTask.get(GlobalContext.getInstance().getCurrentAccountId());
	// }
	// return group;
	// }

	// public void setGroup(GroupListBean group) {
	// this.group = group;
	// }

	public DisplayMetrics getDisplayMetrics() {
		if (displayMetrics != null) {
			return displayMetrics;
		} else {
			Activity a = getActivity();
			if (a != null) {
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				DisplayMetrics metrics = new DisplayMetrics();
				display.getMetrics(metrics);
				this.displayMetrics = metrics;
				return metrics;
			} else {
				// default screen is 800x480
				DisplayMetrics metrics = new DisplayMetrics();
				metrics.widthPixels = 480;
				metrics.heightPixels = 800;
				return metrics;
			}
		}
	}

	// public void setAccountBean(final AccountBean accountBean) {
	// this.accountBean = accountBean;
	// }
	//
	// public void updateUserInfo(final UserBean userBean) {
	// this.accountBean.setInfo(userBean);
	// handler.post(new Runnable() {
	// @Override
	// public void run() {
	// for (MyProfileInfoChangeListener listener : profileListenerSet) {
	// listener.onChange(userBean);
	// }
	// }
	// });
	// }
	//
	// public AccountBean getAccountBean() {
	// if (accountBean == null) {
	// String id = SettingUtility.getDefaultAccountId();
	// if (!TextUtils.isEmpty(id)) {
	// accountBean = AccountDBTask.getAccount(id);
	// } else {
	// List<AccountBean> accountList = AccountDBTask.getAccountList();
	// if (accountList != null && accountList.size() > 0) {
	// accountBean = accountList.get(0);
	// }
	// }
	// }
	//
	// return accountBean;
	// }

	private Set<MyProfileInfoChangeListener> profileListenerSet = new HashSet<MyProfileInfoChangeListener>();

	public void registerForAccountChangeListener(MyProfileInfoChangeListener listener) {
		if (listener != null)
			profileListenerSet.add(listener);
	}

	public void unRegisterForAccountChangeListener(MyProfileInfoChangeListener listener) {
		profileListenerSet.remove(listener);
	}

	public static interface MyProfileInfoChangeListener {
		public void onChange(UserBean newUserBean);
	}

	// public String getCurrentAccountId() {
	// return getAccountBean().getUid();
	// }
	//
	//
	// public String getCurrentAccountName() {
	//
	// return getAccountBean().getUsernick();
	// }

	public synchronized LruCache<String, Bitmap> getAvatarCache() {
		if (avatarCache == null) {
			buildCache();
		}
		return avatarCache;
	}

	// public String getSpecialToken() {
	// if (getAccountBean() != null)
	// return getAccountBean().getAccess_token();
	// else
	// return "";
	// }

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Activity getCurrentRunningActivity() {
		return currentRunningActivity;
	}

	public void setCurrentRunningActivity(Activity currentRunningActivity) {
		this.currentRunningActivity = currentRunningActivity;
	}

	private void buildCache() {
		int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

		int cacheSize = Math.max(1024 * 1024 * 8, 1024 * 1024 * memClass / 5);

		avatarCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {

				return bitmap.getByteCount();
			}
		};
	}

	public synchronized Map<String, Bitmap> getEmotionsPics() {
		if (emotionsPic != null && emotionsPic.size() > 0) {
			return emotionsPic.get(SmileyMap.GENERAL_EMOTION_POSITION);
		} else {
			getEmotionsTask();
			return emotionsPic.get(SmileyMap.GENERAL_EMOTION_POSITION);
		}
	}

	public synchronized Map<String, Bitmap> getAllEmotionsPics() {
		Map<String, Bitmap> allEmotions = new HashMap<String, Bitmap>();
		if (emotionsPic != null && emotionsPic.size() > 0) {
			allEmotions.putAll(emotionsPic.get(SmileyMap.GENERAL_EMOTION_POSITION));
			allEmotions.putAll(emotionsPic.get(SmileyMap.HUAHUA_EMOTION_POSITION));
		} else {
			getEmotionsTask();
			allEmotions.putAll(emotionsPic.get(SmileyMap.GENERAL_EMOTION_POSITION));
			allEmotions.putAll(emotionsPic.get(SmileyMap.HUAHUA_EMOTION_POSITION));
		}
		return allEmotions;

	}

	public synchronized Map<String, Bitmap> getHuahuaPics() {
		if (emotionsPic != null && emotionsPic.size() > 0) {
			return emotionsPic.get(SmileyMap.HUAHUA_EMOTION_POSITION);
		} else {
			getEmotionsTask();
			return emotionsPic.get(SmileyMap.HUAHUA_EMOTION_POSITION);
		}
	}

	private void getEmotionsTask() {
		Map<String, String> general = SmileyMap.getInstance().getGeneral();
		emotionsPic.put(SmileyMap.GENERAL_EMOTION_POSITION, getEmotionsTask(general));
		Map<String, String> huahua = SmileyMap.getInstance().getHuahua();
		emotionsPic.put(SmileyMap.HUAHUA_EMOTION_POSITION, getEmotionsTask(huahua));
	}

	private LinkedHashMap<String, Bitmap> getEmotionsTask(Map<String, String> emotionMap) {

		List<String> index = new ArrayList<String>();
		index.addAll(emotionMap.keySet());
		LinkedHashMap<String, Bitmap> bitmapMap = new LinkedHashMap<String, Bitmap>();
		for (String str : index) {
			String name = emotionMap.get(str);
			AssetManager assetManager = GlobalContext.getInstance().getAssets();
			InputStream inputStream;
			try {
				inputStream = assetManager.open(name);
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				if (bitmap != null) {
					Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Utility.dip2px(getResources().getInteger(R.integer.emotion_size)),
							Utility.dip2px(getResources().getInteger(R.integer.emotion_size)), true);
					if (bitmap != scaledBitmap) {
						bitmap.recycle();
						bitmap = scaledBitmap;
					}
					bitmapMap.put(str, bitmap);
				}
			} catch (IOException e) {

			}
		}

		return bitmapMap;
	}

	// public void updateMusicInfo(MusicInfo musicInfo) {
	// this.musicInfo = musicInfo;
	// }
	//
	// public MusicInfo getMusicInfo() {
	// return musicInfo;
	// }
	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}
}
