package com.vallny.jing.global;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vallny.jing.R;

public class BaseFragmentActivity extends SherlockFragmentActivity {
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.in_from_left, R.anim.static_anim);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.static_anim, R.anim.out_to_right);

	}
}
