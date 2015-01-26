package com.vallny.jing.widget;



import com.vallny.jing.util.emotion.SmileyPickerUtility;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;

public class SmileyPicker extends GridView {
	
	public static final int EMOTION_COLUMN_NUM = 7;
	
//	private GridView gridView;
	private final LayoutTransition transitioner = new LayoutTransition();
	private int mPickerHeight;
	private Activity activity;
	private EditText mEditText;

	public SmileyPicker(Context context) {
		super(context);
	}

	public SmileyPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	public SmileyPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	
	
	private void setupAnimations(LayoutTransition transition) {

		ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY", SmileyPickerUtility.getScreenHeight(this.activity), mPickerHeight).setDuration(
				transition.getDuration(LayoutTransition.APPEARING));
		transition.setAnimator(LayoutTransition.APPEARING, animIn);

		ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", mPickerHeight, SmileyPickerUtility.getScreenHeight(this.activity)).setDuration(
				transition.getDuration(LayoutTransition.DISAPPEARING));
		transition.setAnimator(LayoutTransition.DISAPPEARING, animOut);

	}

	public void setEditText(Activity activity, ViewGroup rootLayout, EditText paramEditText) {
		this.mEditText = paramEditText;
		this.activity = activity;
		rootLayout.setLayoutTransition(transitioner);
		setupAnimations(transitioner);

	}

	public void show(Activity paramActivity, boolean showAnimation,int content_height) {
		if (showAnimation) {
			transitioner.setDuration(200);
		} else {
			transitioner.setDuration(0);
		}
		this.mPickerHeight = SmileyPickerUtility.getKeyboardHeight(paramActivity,showAnimation,content_height);
		SmileyPickerUtility.hideSoftInput(this.mEditText);
		getLayoutParams().height = this.mPickerHeight;
		setVisibility(View.VISIBLE);
		// open smilepicker, press home, press app switcher to return to write
		// weibo interface,
		// softkeyboard will be opened by android system when smilepicker is
		// showing,
		// this method is used to fix this issue
		paramActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

	public void hide(Activity paramActivity) {
		setVisibility(View.GONE);
		paramActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

	}
//
//	public GridView getGridView() {
//		return gridView;
//	}
}
