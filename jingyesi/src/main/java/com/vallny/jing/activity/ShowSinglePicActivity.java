package com.vallny.jing.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vallny.jing.R;
import com.vallny.jing.util.image.ImageUtility;

public class ShowSinglePicActivity extends SherlockActivity implements DialogInterface.OnClickListener {

	public static final int M_HEIGHT = 2048;
	public static final int M_WIDTH = 2048;

	private String path;
	private ImageView mImageView;
	private Bitmap bitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_single_pic);
		getActionBar().setTitle(R.string.show_pic);

		mImageView = (ImageView) findViewById(R.id.iv_photo);
		path = getIntent().getStringExtra("path");

		if (!TextUtils.isEmpty(path)) {
			if (ImageUtility.isThisBitmapCanRead(path)) {
				int[] size = ImageUtility.getBitmapSize(path);
				getActionBar().setSubtitle(String.valueOf(size[0]) + "x" + String.valueOf(size[1]));
			}

			if (ImageUtility.isThisBitmapCanRead(path)) {
				if (!ImageUtility.isThisBitmapTooLargeToRead(path)) {
					bitmap = ImageUtility.decodeBitmapFromSDCard(path);
				} else {
					bitmap = ImageUtility.decodeBitmapFromSDCard(path, M_HEIGHT, M_WIDTH);
				}
				mImageView.setImageBitmap(bitmap);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.remove_pic).setIcon(R.drawable.remove_pic).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 0:
			new AlertDialog.Builder(ShowSinglePicActivity.this).setTitle(R.string.remove_pic_title).setMessage(R.string.confirm_remove_pic)
					.setPositiveButton(R.string.remove_pic_positive, this).setNegativeButton(R.string.remove_pic_negation, null).show();
			break;
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageUtility.recycleBitmap(bitmap);
		bitmap = null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		setResult(RESULT_OK);
		finish();
	}

}
