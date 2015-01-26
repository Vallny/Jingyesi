package com.vallny.jing.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.vallny.jing.R;
import com.vallny.jing.bean.DBUserBean;
import com.vallny.jing.fragment.FriendsTimeLineFragment;
import com.vallny.jing.fragment.PublicTimeLineFragment;
import com.vallny.jing.global.BaseFragmentActivity;

public class WeiboContentActivity extends BaseFragmentActivity {

	private DBUserBean user;
	private FragmentManager fm;
	private FriendsTimeLineFragment friendsTimeLineFragment;
	private PublicTimeLineFragment publicTimeLineFragment;

	private SlidingMenu menu;
	private TextView friends_time_line;
	private TextView public_time_line;
	private View selected_text_view;
	private Fragment showing_fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_content);

		Intent intent = getIntent();
		user = intent.getParcelableExtra(LoginActivity.INTENT_USER);

		initSlidingMenu();
		MenuClickListener mClickListener = new MenuClickListener();
		friends_time_line = (TextView) findViewById(R.id.friends_time_line);
		public_time_line = (TextView) findViewById(R.id.public_time_line);
		friends_time_line.setOnClickListener(mClickListener);
		public_time_line.setOnClickListener(mClickListener);

		// URLHelper.setToken(user.getToken());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	private void initSlidingMenu() {
		initFragments();
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.setBehindScrollScale(0);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(R.layout.menu_frame);
	}

	private void initFragments() {
		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		friendsTimeLineFragment = getFriendsTimeLineFragment(ft);
		publicTimeLineFragment = getPublicTimeLineFragment(ft);

		// ft.setCustomAnimations(R.anim.in_from_left, R.anim.static_anim,
		// R.anim.static_anim, R.anim.out_to_right);
		ft.add(R.id.weibo_container, friendsTimeLineFragment);
		ft.add(R.id.weibo_container, publicTimeLineFragment);
		ft.show(friendsTimeLineFragment);
		ft.commit();
	}

	private FriendsTimeLineFragment getFriendsTimeLineFragment(FragmentTransaction ft) {
		if (friendsTimeLineFragment == null) {
			friendsTimeLineFragment = new FriendsTimeLineFragment(user, this, imageLoader);
			ft.hide(friendsTimeLineFragment);
		}
		return friendsTimeLineFragment;
	}

	private PublicTimeLineFragment getPublicTimeLineFragment(FragmentTransaction ft) {
		if (publicTimeLineFragment == null) {
			publicTimeLineFragment = new PublicTimeLineFragment(user, this, imageLoader);
			ft.hide(publicTimeLineFragment);
		}
		return publicTimeLineFragment;
	}

	class MenuClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			v.setBackgroundResource(R.color.sliding_menu_background_selected);
			FragmentTransaction ft = fm.beginTransaction();
			if (showing_fragment != null) {
				ft.hide(showing_fragment);
			} else {
				ft.hide(friendsTimeLineFragment);
			}
			if (selected_text_view == null) {
				friends_time_line.setBackgroundResource(R.drawable.slidingmenu_selector);
			} else {
				selected_text_view.setBackgroundResource(R.drawable.slidingmenu_selector);
			}
			switch (v.getId()) {
			case R.id.friends_time_line:
				showing_fragment = friendsTimeLineFragment;
				ft.show(friendsTimeLineFragment);
				break;
			case R.id.public_time_line:
				showing_fragment = publicTimeLineFragment;
				ft.show(publicTimeLineFragment);
				break;
			}

			selected_text_view = v;
			menu.showContent();
			ft.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(R.string.write_weibo).setIcon(R.drawable.edit_light).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent(WeiboContentActivity.this, WriteWeiboActivity.class);
			intent.putExtra("token", user.getToken());
			intent.putExtra("uid", user.getUid());

			startActivity(intent);
			break;

		case android.R.id.home:
			menu.toggle();
			break;
		}
		// FragmentTransaction ft = fm.beginTransaction();
		// ft.setCustomAnimations(R.anim.in_from_left, R.anim.static_anim,
		// R.anim.static_anim, R.anim.out_to_right);
		// // ft.replace(R.id.weibo_container, arg1);
		// ft.commit();

		return true;
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.toggle();
		} else {
			super.onBackPressed();
		}
	}

}
