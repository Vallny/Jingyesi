package com.vallny.jing.global;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.vallny.jing.util.global.AnimateFirstDisplayListener;

import android.widget.BaseAdapter;

public abstract class _BaseAdapter extends BaseAdapter {
	protected ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
}
