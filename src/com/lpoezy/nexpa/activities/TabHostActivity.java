package com.lpoezy.nexpa.activities;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.ChatMessagesService;
import com.lpoezy.nexpa.chatservice.LocalBinder;
import com.lpoezy.nexpa.chatservice.RegistrationIntentService;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.NewMessage;
import com.lpoezy.nexpa.objects.OfUser;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.L;

public class TabHostActivity extends TabActivity {
	private SessionManager session;

	protected boolean mBind;

	//protected ChatMessagesService mService;
	
	public static boolean isRunning = false;
	
	private ProgressDialog pDialog;

	private FrameLayout mViewMsgCount;

	private boolean mBounded;
	private XMPPService mService;

	public  XMPPService getService(){
		return mService;
	}

	public  boolean isBounded(){
		return mBounded;
	}

	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBounded = false;
			mService = null;


		}

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBounded = true;

			mService = ((LocalBinder<XMPPService>) service).getService();

			XMPPService.OnServiceConnectedListener onServiceConnectedListener = (XMPPService.OnServiceConnectedListener) getCurrentActivity();
			onServiceConnectedListener.OnServiceConnected(mService);



		}
	};

	@Override
	protected void onResume() {
		
		super.onResume();
		L.debug("TabHost, onResume");

//		registerReceiver(mUpdateMsgCount, new IntentFilter(AppConfig.ACTION_RECEIVED_MSG));
//		isRunning = true;

		Intent service = new Intent(this, XMPPService.class);
		startService(service);
		bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);

	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
//		unregisterReceiver(mUpdateMsgCount);
		isRunning = false;
		
		L.debug("TabHost, onPause");

		if (mServiceConn != null) {
			unbindService(mServiceConn);
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_host);
		//*/
		final Resources res = getResources();
		TabHost tabHost = getTabHost();
		
		Intent intent;
		
		session = new SessionManager(getApplicationContext());
		SQLiteHandler db = new SQLiteHandler(this);
		db.openToWrite();
		db.updateBroadcasting(0);
		db.updateBroadcastTicker(0);
	
		if (!session.isLoggedIn()) {
			
			intent = new Intent(TabHostActivity.this, MainSignInActivity.class);
			startActivity(intent);
			finish();
		} else {

			// XMPPLogic.getInstance().getConnection();
			intent = new Intent().setClass(TabHostActivity.this, AroundMeActivity.class);

			TabHost.TabSpec spec = null;
			// "", res.getDrawable(R.drawable.ic_tab_people)
			spec = tabHost.newTabSpec("home")
					.setIndicator(getTabIndicator(this, res.getDrawable(R.drawable.ic_tab_people))).setContent(intent);
			//.setIndicator("Home1", getResources().getDrawable(R.drawable.ic_tab_people)).setContent(intent);

						//spec.setContent(intent);
			          // spec.setIndicator("Home");
			tabHost.addTab(spec);
			// "", res.getDrawable(R.drawable.ic_tab_chat)
			intent = new Intent().setClass(TabHostActivity.this, ChatHistoryActivity.class);
			spec = tabHost.newTabSpec("home3")
					.setIndicator(getTabIndicator(this, res.getDrawable(R.drawable.ic_tab_chat))).setContent(intent);
			tabHost.addTab(spec);

			// "", res.getDrawable(R.drawable.ic_tab_group)
			intent = new Intent().setClass(TabHostActivity.this, GroupChatHomeActivity.class);
			spec = tabHost.newTabSpec("home1")
					.setIndicator(getTabIndicator(this, res.getDrawable(R.drawable.ic_tab_group))).setContent(intent);
			tabHost.addTab(spec);

			// "", res.getDrawable(R.drawable.ic_tab_profile)
			intent = new Intent().setClass(TabHostActivity.this, UserProfileActivity.class);
			spec = tabHost.newTabSpec("home2")
					.setIndicator(getTabIndicator(this, res.getDrawable(R.drawable.ic_tab_profile))).setContent(intent);
			tabHost.addTab(spec);

			tabHost.setCurrentTab(0);
			tabHost.setup();
			int heightValue = 50;

			for (int i = 0; i < tabHost.getTabWidget().getTabCount(); i++) {
				tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int)(heightValue * res.getDisplayMetrics().density);
				tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#D2D7D3"));
				tabHost.getTabWidget().setDividerDrawable(null);

			}

			FrameLayout tabIcContainer = (FrameLayout) tabHost.getTabWidget().getChildAt(1)
					.findViewById(R.id.tab_ic_container);

			mViewMsgCount = (FrameLayout) LayoutInflater.from(TabHostActivity.this).inflate(R.layout.message_count,
					null);

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.RIGHT;

			mViewMsgCount.setLayoutParams(params);
			tabIcContainer.addView(mViewMsgCount);
			mViewMsgCount.setVisibility(View.GONE);

			tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

				@Override
				public void onTabChanged(String tabId) {

					//L.debug("tabId: " + tabId);
					if (tabId.equalsIgnoreCase("home3")) {
						if (mViewMsgCount.getVisibility() == View.VISIBLE)
							mViewMsgCount.setVisibility(View.GONE);
					}
				}
			});



		}
		//*/
	}
	
	// receiving chtMessages will be handle by receivedMessage
	// in ChatMessagesService
	private BroadcastReceiver mUpdateMsgCount = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			// don't show the msg count,
			// if current screen is ChatHistoryActivity or ChatActivity
			if (ChatHistoryActivity.isRunning || ChatActivity.isRunning)
				return;
			
			int count = NewMessage.getUnReadMsgCountOffline(TabHostActivity.this);
			L.debug("TabhostActivity, mUpdateMsgCount " + count);
			if (count > 0) {
				if (mViewMsgCount.getVisibility() == View.GONE)
					mViewMsgCount.setVisibility(View.VISIBLE);
				((TextView) mViewMsgCount.findViewById(R.id.tv_msg_count)).setText(Integer.toString(count));
			}

		}

	};
	
	private View getTabIndicator(Context context, Drawable background) {
		
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        FrameLayout container = (FrameLayout)view.findViewById(R.id.tab_ic_container);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_tab_ic);
		//iv.setImageResource(icon);
        iv.setBackgroundDrawable(background);
       
        return view;
    }
	
	public interface OnTabHostActivtyReadyListener{
		public void onTabHostActivtyReady();
	}
	
	
}