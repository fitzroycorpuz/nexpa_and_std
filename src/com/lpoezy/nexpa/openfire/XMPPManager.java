package com.lpoezy.nexpa.openfire;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.chatservice.XMPPService;
import com.lpoezy.nexpa.objects.ChatMessage;
import com.lpoezy.nexpa.sqlite.SQLiteHandler;
import com.lpoezy.nexpa.sqlite.SessionManager;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMPPManager {

	public static boolean connected = false;
	public boolean loggedin = false;
	public static boolean isconnecting = false;
	public static boolean isToasted = true;
	private boolean chat_created = false;
	private String serverAddress;
	public static AbstractXMPPConnection connection;
	public static String loginUser;
	public static String passwordUser;
	Gson gson;
	XMPPService context;
	public static XMPPManager instance = null;
	public static boolean instanceCreated = false;

	public XMPPManager(XMPPService context, String serverAdress,
			String logiUser, String passwordser) {
		this.serverAddress = serverAdress;
		this.loginUser = logiUser;
		this.passwordUser = passwordser;
		this.context = context;
		init();

	}

	public static XMPPManager getInstance(XMPPService context, String server,
			String user, String pass) {

		if (instance == null) {
			instance = new XMPPManager(context, server, user, pass);
			instanceCreated = true;
		}
		return instance;

	}

	public org.jivesoftware.smack.chat.Chat Mychat;

	ChatManagerListenerImpl mChatManagerListener;
	MMessageListener mMessageListener;

	String text = "";
	String mMessage = "", mReceiver = "";

	static {
		try {
			Class.forName("org.jivesoftware.smack.ReconnectionManager");
		} catch (ClassNotFoundException ex) {
			// problem loading reconnection manager
		}
	}

	public void init() {
		gson = new Gson();
		mMessageListener = new MMessageListener(context);
		mChatManagerListener = new ChatManagerListenerImpl();
		initialiseConnection();

	}

	@SuppressWarnings("deprecation")
	private void initialiseConnection() {

		XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		config.setServiceName(serverAddress);
		config.setHost(serverAddress);
		config.setPort(5222);
		
		config.setDebuggerEnabled(true);
		XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
		XMPPTCPConnection.setUseStreamManagementDefault(true);
		
		connection = new XMPPTCPConnection(config.build());
		XMPPConnectionListener connectionListener = new XMPPConnectionListener();
		connection.addConnectionListener(connectionListener);
	}

	public void disconnect() throws NotConnectedException {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {


				L.debug("xmpp, Disconnected");
				connection.disconnect();



		//	}
//		}).start();
	}

	public void connect(final String caller) {

		AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected synchronized Boolean doInBackground(Void... arg0) {
				if (connection.isConnected())
					return false;
				isconnecting = true;
				if (isToasted)
					new Handler(Looper.getMainLooper()).post(new Runnable() {

						@Override
						public void run() {

							Toast.makeText(context,
									caller + "=>connecting....",
									Toast.LENGTH_LONG).show();
						}
					});
				L.debug("Connect() Function " + caller + "=>connecting....");

				try {
					connection.connect();
					DeliveryReceiptManager dm = DeliveryReceiptManager
							.getInstanceFor(connection);
					dm.setAutoReceiptMode(AutoReceiptMode.always);
					dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

						@Override
						public void onReceiptReceived(final String fromid,
								final String toid, final String msgid,
								final Stanza packet) {

						}
					});
					connected = true;

				} catch (IOException e) {
					if (isToasted)
						new Handler(Looper.getMainLooper())
								.post(new Runnable() {

									@Override
									public void run() {

										Toast.makeText(
												context,
												"(" + caller + ")"
														+ "IOException: ",
												Toast.LENGTH_SHORT).show();
									}
								});

					L.error("(" + caller + "), IOException: " + e.getMessage());
				} catch (SmackException e) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(context,
									"(" + caller + ")" + "SMACKException: ",
									Toast.LENGTH_SHORT).show();
						}
					});
					L.error("(" + caller + "), SMACKException: "
							+ e.getMessage());
				} catch (XMPPException e) {
					if (isToasted)

						new Handler(Looper.getMainLooper())
								.post(new Runnable() {

									@Override
									public void run() {

										Toast.makeText(
												context,
												"(" + caller + ")"
														+ "XMPPException: ",
												Toast.LENGTH_SHORT).show();
									}
								});
					Log.e("connect(" + caller + ")",
							"XMPPException: " + e.getMessage());

				}
				return isconnecting = false;
			}
		};
		connectionThread.execute();
	}

	public void register(String uname, String password, String email)
			throws NoResponseException, XMPPErrorException,
			NotConnectedException {

		AccountManager accountManager = AccountManager.getInstance(connection);
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", uname);
		map.put("name", uname);
		map.put("password", password);
		map.put("email", email);

		accountManager.createAccount(uname, password, map);

		L.debug("REGISTER, New user created successfully." + uname + ", "
				+ password + ", " + email);

	}

	public void login(String uname, String password)
			throws AlreadyLoggedInException, SmackException, XMPPException {

		try {

			connection.login(uname, password);

			L.debug("LOGIN, Yey! We're connected to the Xmpp server!");

		} catch (IOException e) {
			L.error("" + e);

		}

	}


	private class ChatManagerListenerImpl implements ChatManagerListener {
		@Override
		public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
				final boolean createdLocally) {
			if (!createdLocally)
				chat.addMessageListener(mMessageListener);

		}

	}

	public void sendMessage(ChatMessage chatMessage) {
		String body = gson.toJson(chatMessage);

		if (!chat_created) {
			Mychat = ChatManager.getInstanceFor(connection).createChat(
					chatMessage.receiver + "@"
							+ context.getString(R.string.server),
					mMessageListener);
			chat_created = true;
		}
		final Message message = new Message();
		message.setBody(body);
		message.setStanzaId(chatMessage.msgid);
		message.setType(Message.Type.chat);

		try {
			if (connection.isAuthenticated()) {

				Mychat.sendMessage(message);

			} else {

				String uname, password;
				SQLiteHandler db = new SQLiteHandler(context);
				db.openToRead();
				uname = db.getUsername();
				password = db.getPlainPassword();
				login(uname, password);
				db.close();
			}
		} catch (NotConnectedException e) {
			L.error("xmpp.SendMessage(), msg Not sent!-Not Connected!");

		} catch (Exception e) {
			L.error("xmpp.SendMessage()-Exception, msg Not sent!"
					+ e.getMessage());
		}

	}

	public class XMPPConnectionListener implements ConnectionListener {
		@Override
		public void connected(final XMPPConnection connection) {

			L.debug("xmpp, Connected!");
			connected = true;



		}

		@Override
		public void connectionClosed() {
			if (isToasted)

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						Toast.makeText(context, "ConnectionCLosed!",
								Toast.LENGTH_SHORT).show();

					}
				});
			Log.d("xmpp", "ConnectionCLosed!");
			connected = false;
			chat_created = false;
			loggedin = false;
		}

		@Override
		public void connectionClosedOnError(Exception arg0) {
			if (isToasted)

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(context, "ConnectionClosedOn Error!!",
								Toast.LENGTH_SHORT).show();

					}
				});
			Log.d("xmpp", "ConnectionClosedOn Error!");
			connected = false;

			chat_created = false;
			loggedin = false;
		}

		@Override
		public void reconnectingIn(int arg0) {

			L.debug("xmpp, Reconnectingin " + arg0);

			loggedin = false;
		}

		@Override
		public void reconnectionFailed(Exception arg0) {
			if (isToasted)

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {

						Toast.makeText(context, "ReconnectionFailed!",
								Toast.LENGTH_SHORT).show();

					}
				});
			L.error("xmpp, ReconnectionFailed!");
			connected = false;

			chat_created = false;
			loggedin = false;
		}

		@Override
		public void reconnectionSuccessful() {
			if (isToasted)

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						Toast.makeText(context, "REConnected!",
								Toast.LENGTH_SHORT).show();

					}
				});
			L.debug("xmpp, ReconnectionSuccessful");
			connected = true;

			chat_created = false;
			loggedin = false;
		}

		@Override
		public void authenticated(XMPPConnection arg0, boolean arg1) {
			L.debug("xmpp, Authenticated! "+connection.getUser());
			loggedin = true;

			ChatManager.getInstanceFor(connection).addChatListener(
					mChatManagerListener);

			chat_created = false;
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}).start();
			if (isToasted)

				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						Toast.makeText(context, "Connected!",
								Toast.LENGTH_SHORT).show();

					}
				});
		}
	}

	private class MMessageListener implements ChatMessageListener {

		public MMessageListener(Context contxt) {
		}

		@Override
		public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
				final Message message) {
			Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
					+ message);

			if (message.getType() == Message.Type.chat
					&& message.getBody() != null) {
				final ChatMessage chatMessage = gson.fromJson(
						message.getBody(), ChatMessage.class);

				processMessage(chatMessage);
			}
		}

		private void processMessage(final ChatMessage chatMessage) {
			/*
			 * / chatMessage.isMine = false; Chats.chatlist.add(chatMessage);
			 * new Handler(Looper.getMainLooper()).post(new Runnable() {
			 * 
			 * @Override public void run() {
			 * Chats.chatAdapter.notifyDataSetChanged();
			 * 
			 * } }); //
			 */
		}

	}

}
