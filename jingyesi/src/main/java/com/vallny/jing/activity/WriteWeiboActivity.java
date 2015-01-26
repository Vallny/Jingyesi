package com.vallny.jing.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.vallny.jing.R;
import com.vallny.jing.global.BaseActivity;
import com.vallny.jing.global.GlobalContext;
import com.vallny.jing.service.SendWeiboService;
import com.vallny.jing.util.emotion.SmileyPickerUtility;
import com.vallny.jing.util.global.Utility;
import com.vallny.jing.util.http.URLHelper;
import com.vallny.jing.util.image.ImageUtility;
import com.vallny.jing.util.link.TimeLineUtility;
import com.vallny.jing.widget.SmileyPicker;

public class WriteWeiboActivity extends BaseActivity {

	private static final int CAMERA_RESULT = 0;
	private static final int PIC_RESULT = 1;
	private static final int REMOVE_PIC_RESULT = 2;

	private static final int ADD_EMOTION = 3;

	public static final String ACTION_SEND_FAILED = "com.vallny.jing.SEND_FAILED";

	private int content_height;

	private EditText content;
	private ImageButton menu_add_pic;
	private ImageButton menu_at;
	private ImageButton menu_emoticon;
	private Button menu_send;
	// private ScrollView smiley_picker_container;
	// private GridLayout smiley_picker;
	private LinearLayout root_layout;
	private SmileyPicker smiley_picker;

	private String picPath = "";
	private Uri imageFileUri = null;
	private String token;
	private String uid;
	private String action;

	@Override
	public void onBackPressed() {
		if (smiley_picker.isShown()) {
			hideSmileyPicker(true);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CAMERA_RESULT:
				if (TextUtils.isEmpty(content.getText().toString())) {
					content.setText(getString(R.string.share_pic));
					content.setSelection(content.getText().toString().length());
				}

				picPath = Utility.getPicPathFromUri(imageFileUri, this);
				enablePicture(picPath);
				break;
			case PIC_RESULT:
				if (TextUtils.isEmpty(content.getText().toString())) {
					content.setText(getString(R.string.share_pic));
					content.setSelection(content.getText().toString().length());
				}

				Uri imageFileUri = intent.getData();
				picPath = Utility.getPicPathFromUri(imageFileUri, this);
				enablePicture(picPath);
				break;
			case REMOVE_PIC_RESULT:
				picPath = null;
				menu_add_pic.setImageResource(R.drawable.camera_light);
				if (getResources().getString(R.string.share_pic).equals(content.getText().toString())) {
					content.setText(null);
				}
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_weibo);
		findAndSetListener();
		getParametersFromIntent();
	}

	private void getParametersFromIntent() {
		Intent intent = getIntent();
		token = intent.getStringExtra("token");
		uid = intent.getStringExtra("uid");
		action = intent.getAction();
		if (!TextUtils.isEmpty(action)) {
			if (action.equals(ACTION_SEND_FAILED)) {
				reSend(intent);
			}
		}
	}

	private void findAndSetListener() {
		content = (EditText) findViewById(R.id.status_new_content);
		menu_add_pic = (ImageButton) findViewById(R.id.menu_add_pic);
		menu_at = (ImageButton) findViewById(R.id.menu_at);
		menu_emoticon = (ImageButton) findViewById(R.id.menu_emoticon);
		menu_send = (Button) findViewById(R.id.menu_send);
		root_layout = (LinearLayout) findViewById(R.id.root_layout);

		content.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideSmileyPicker(false);
			}
		});

		final ViewTreeObserver vto = content.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			boolean flag = true;

			@Override
			public void onGlobalLayout() {
				if (content_height == 0) {
					content_height = content.getMeasuredHeight();
				} else {
					int height = content.getMeasuredHeight();

					if (height < content_height && flag) {
						flag = false;
						content_height = height;
					}
				}
			}
		});

		// smiley_picker_container = (ScrollView)
		// findViewById(R.id.smiley_picker_container);
		// smiley_picker = (GridLayout) findViewById(R.id.smiley_picker);

		content.addTextChangedListener(new ContentTextWatcher());
		BottomButtonClickListener clickListener = new BottomButtonClickListener();
		menu_add_pic.setOnClickListener(clickListener);
		menu_at.setOnClickListener(clickListener);
		menu_emoticon.setOnClickListener(clickListener);
		menu_send.setOnClickListener(clickListener);
		initEmotions();

	}

	/**
	 * 加载表情
	 */
	private void initEmotions() {
		new Thread() {
			public void run() {
				smiley_picker = (SmileyPicker) findViewById(R.id.smiley_picker);
				smiley_picker.setEditText(WriteWeiboActivity.this, root_layout, content);
				Map<String, Bitmap> pics = GlobalContext.getInstance().getAllEmotionsPics();
				Message msg = handler.obtainMessage(ADD_EMOTION, pics);
				msg.sendToTarget();
			}
		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ADD_EMOTION:
				SmileyAdapter s_adapter = new SmileyAdapter((Map<String, Bitmap>) msg.obj, WriteWeiboActivity.this);
				smiley_picker.setAdapter(s_adapter);
				break;
			}
		};
	};

	private class SmileyAdapter extends BaseAdapter {
		// private Map<String, Bitmap> pics;
		private List<Entry<String, Bitmap>> pics;
		private Context context;

		public SmileyAdapter(Map<String, Bitmap> pics, Context context) {
			this.pics = new ArrayList<Entry<String, Bitmap>>(pics.entrySet());
			this.context = context;
		}

		@Override
		public int getCount() {
			return pics.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ImageView iv = new ImageView(context);
			iv.setPadding(5, 5, 5, 5);
			final Entry<String, Bitmap> pic = pics.get(position);
			iv.setImageBitmap(pic.getValue());
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String text = content.getText().toString();
					text = text + pic.getKey();
					content.setText(TimeLineUtility.addEmotions(new SpannableString(text)));
					content.setSelection(text.length());
				}
			});
			return iv;
		}
	}

	/**
	 * 重新发送
	 * 
	 * @param intent
	 */
	private void reSend(Intent intent) {
		String content_text = intent.getStringExtra("content");
		picPath = intent.getStringExtra("picPath");
		content.setText(content_text);
		content.setSelection(content_text.length());
		enablePicture(picPath);
	}

	/**
	 * 发送微博
	 */
	private void sendWeibo() {

		String content_text = content.getText().toString();
		if (canSend()) {
			sendWeibo(content_text);
		}
	}

	/**
	 * 发送微博
	 * 
	 * @param content
	 */
	private void sendWeibo(String content) {

		Intent intent = new Intent(WriteWeiboActivity.this, SendWeiboService.class);
		intent.putExtra("token", token);
		intent.putExtra("content", content);
		intent.putExtra("picPath", picPath);

		// intent.putExtra("picPath", picPath);
		// intent.putExtra("account", accountBean);
		// intent.putExtra("content", contentString);
		// intent.putExtra("geo", geoBean);
		// intent.putExtra("draft", statusDraftBean);
		startService(intent);
		finish();

	}

	/**
	 * 是否可以发送
	 * 
	 * @return
	 */
	private boolean canSend() {

		boolean haveContent = !TextUtils.isEmpty(content.getText().toString());
		// boolean haveToken = !TextUtils.isEmpty(token);

		if (haveContent) {
			return true;
		} else {
			Toast.makeText(this, getString(R.string.content_cant_be_empty), Toast.LENGTH_SHORT).show();

			// if (!haveContent && !haveToken) {
			// Toast.makeText(this,
			// getString(R.string.content_cant_be_empty_and_dont_have_account),
			// Toast.LENGTH_SHORT).show();
			// } else if (!haveContent) {
			// content.setError(getString(R.string.content_cant_be_empty));
			// } else if (!haveToken) {
			// Toast.makeText(this, getString(R.string.dont_have_account),
			// Toast.LENGTH_SHORT).show();
			// }
			//
		}
		return false;
	}

	// private class BottomButtonClickListener implements View.OnClickListener {
	// @Override
	// public void onClick(View v) {
	// switch (v.getId()) {
	// case R.id.menu_send:
	// sendWeibo();
	// break;
	// case R.id.menu_add_pic:
	// if (TextUtils.isEmpty(picPath))
	// addPic();
	// else
	// showPic();
	// break;
	// case R.id.menu_emoticon:
	// if (smiley_picker.isShown()) {
	// hideSmileyPicker(true);
	// } else {
	// showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(WriteWeiboActivity.this));
	// }
	// break;
	// case R.id.menu_at:
	// // showFriends();
	//
	// break;
	//
	// }
	// }

	/**
	 * 转到
	 */
	private void showFriends() {
		// Intent intent = new
		// Intent(WriteWeiboActivity.this,FriendsActivity.class);
		// intent.putExtra("token", token);
		// intent.putExtra("uid", uid);
		// startActivity(intent);

		// }
	}

	/**
	 * 隐藏表情框
	 * 
	 * @param showKeyBoard
	 */
	public void hideSmileyPicker(boolean showKeyBoard) {
		if (this.smiley_picker.isShown()) {
			if (showKeyBoard) {
				// this time softkeyboard is hidden
				LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.content.getLayoutParams();
				localLayoutParams.height = content_height;
				localLayoutParams.weight = 0.0F;
				this.smiley_picker.hide(WriteWeiboActivity.this);

				SmileyPickerUtility.showKeyBoard(content);
				content.postDelayed(new Runnable() {
					@Override
					public void run() {
						unlockContainerHeightDelayed();
					}
				}, 200L);
			} else {
				this.smiley_picker.hide(WriteWeiboActivity.this);
				smiley_picker.postDelayed(new Runnable() {
					@Override
					public void run() {
						unlockContainerHeightDelayed();
					}
				}, 200L);
			}
		}

	}

	public void unlockContainerHeightDelayed() {
		((LinearLayout.LayoutParams) WriteWeiboActivity.this.content.getLayoutParams()).weight = 1.0F;
	}

	/**
	 * 显示表情选择栏
	 * 
	 * @param showAnimation
	 */
	private void showSmileyPicker(boolean showAnimation) {
		this.smiley_picker.show(WriteWeiboActivity.this, showAnimation, content_height);
		lockContainerHeight(content_height, showAnimation);
	}

	/**
	 * 保持编辑框高度
	 * 
	 * @param paramInt
	 */
	private void lockContainerHeight(int paramInt, boolean showAnimation) {
		LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.content.getLayoutParams();
		localLayoutParams.weight = 0.0F;
		if (showAnimation) {
			localLayoutParams.height = paramInt;
		} else {
			localLayoutParams.height = content_height;
		}

	}

	/**
	 * 显示图片
	 */
	private void showPic() {
		Intent intent = new Intent(WriteWeiboActivity.this, ShowSinglePicActivity.class);
		intent.putExtra("path", picPath);
		startActivityForResult(intent, REMOVE_PIC_RESULT);
	}

	/**
	 * 弹出增加图片
	 */
	private void addPic() {
		new AlertDialog.Builder(this).setTitle(R.string.check_pic).setItems(R.array.dialog_add_pic, new DialogItemClickListener()).show();
	}

	/**
	 * 选择图片监听器
	 * 
	 * @author SIA
	 * 
	 */
	private class DialogItemClickListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
				getLastPic();
				break;
			case 1:
				getPicFromCamera();
				break;
			case 2:
				getPicFromGallery();
				break;
			}

		}

		/**
		 * 获取最后一张照片
		 */
		private void getLastPic() {
			String[] projection = new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
					MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.MIME_TYPE };
			final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
			if (cursor.moveToFirst()) {
				String path = cursor.getString(1);
				if (!TextUtils.isEmpty(path)) {
					picPath = path;
					enablePicture(picPath);
					if (TextUtils.isEmpty(content.getText().toString())) {
						content.setText(getString(R.string.share_pic));
						content.setSelection(content.getText().toString().length());
					}
				}
			} else {
				Toast.makeText(WriteWeiboActivity.this, getString(R.string.dont_have_the_last_picture), Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * 从相册获取照片
		 */
		private void getPicFromGallery() {
			Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(choosePictureIntent, PIC_RESULT);
		}

		/**
		 * 拍照获取照片
		 */
		private void getPicFromCamera() {
			imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
			if (imageFileUri != null) {
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
				if (Utility.isIntentSafe(WriteWeiboActivity.this, i)) {
					startActivityForResult(i, CAMERA_RESULT);
				} else {
					Toast.makeText(WriteWeiboActivity.this, getString(R.string.dont_have_camera_app), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(WriteWeiboActivity.this, getString(R.string.cant_insert_album), Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 底部选择按钮
	 * 
	 * @author SIA
	 * 
	 */
	private class BottomButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.menu_send:
				sendWeibo();
				break;
			case R.id.menu_add_pic:
				if (TextUtils.isEmpty(picPath))
					addPic();
				else
					showPic();
				break;
			case R.id.menu_emoticon:
				if (smiley_picker.isShown()) {
					hideSmileyPicker(true);
				} else {
					showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(WriteWeiboActivity.this));
				}
				break;
			case R.id.menu_at:
				// showFriends();

				break;

			}
		}

		/**
		 * 转到
		 */
		private void showFriends() {
			Intent intent = new Intent(WriteWeiboActivity.this, FriendsActivity.class);
			intent.putExtra("token", token);
			intent.putExtra("uid", uid);
			startActivity(intent);

		}
	}

	/**
	 * 产生照片缩略图
	 */
	private void enablePicture(String picPath) {
		Bitmap bitmap = ImageUtility.getWriteWeiboPictureThumblr(picPath);
		if (bitmap != null) {
			menu_add_pic.setImageBitmap(bitmap);
		}
	}

	/**
	 * 重新发送Intent
	 * 
	 * @param context
	 * @param access_token
	 * @param content
	 * @param picPath
	 * @return
	 */
	public static Intent startBecauseSendFailed(Context context, String token, String content, String picPath) {
		Intent intent = new Intent(context, WriteWeiboActivity.class);
		intent.setAction(WriteWeiboActivity.ACTION_SEND_FAILED);
		intent.putExtra("token", token);
		intent.putExtra("content", content);
		intent.putExtra("picPath", picPath);
		return intent;
	}

	/**
	 * 文本监控
	 * 
	 * @author SIA
	 * 
	 */
	private class ContentTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

	}
}
