package com.lpoezy.nexpa.chatservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.lpoezy.nexpa.R;
import com.lpoezy.nexpa.activities.ChatActivity;
import com.lpoezy.nexpa.configuration.AppConfig;
import com.lpoezy.nexpa.objects.Correspondent;
import com.lpoezy.nexpa.objects.NewMessage;
import com.lpoezy.nexpa.utility.L;

//started in TabHostActivity class onResume
public class ChatMessagesService extends Service {

	private IBinder mBinder = new LocalBinder();
	public static boolean isRunning;

	public class LocalBinder extends Binder {
		public ChatMessagesService getService() {
			return ChatMessagesService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		isRunning = false;

		L.debug("ChatMessagesService onDestroy");
	}

	@Override
	public void onCreate() {
		L.debug("ChatMessagesService, onCreate");
		super.onCreate();

		isRunning = true;

		onReceiveChatMessages();

	}

	// create a connection with xmpp,
	// and listen to any incoming chtMessages
	private void onReceiveChatMessages() {

//		L.debug("onReceiveChatMessages");
//
//		// called from NetworkChangeReceiver whenever the user is logged in
//		// will always be called whenever there is a change in network state,
//		// will always check if the app is connected to server,
//		XMPPConnection connection = XMPPLogic.getInstance().getConnection();
//
//		if (connection == null || !connection.isConnected()) {
//			SQLiteHandler db = new SQLiteHandler(getApplicationContext());
//			db.openToWrite();
//
//			// db.updateBroadcasting(0);
//			// db.updateBroadcastTicker(0);
//
//			Account ac = new Account();
//			ac.LogInChatAccount(db.getUsername(), db.getEncryptedPassword(), db.getEmail(), new OnXMPPConnectedListener() {
//
//				@Override
//				public void onXMPPConnected(XMPPConnection con) {
//					
//					//addPacketListener(con);
//				}
//
//			});
//
//			db.close();
//		} else {
//
//			//addPacketListener(connection);
//
//		}
	}
	
	// will handle all the received chtMessages,
	// and send chtMessages to all waiting activities via BroadcastReceiver
	
	
//	private void addPacketListener(XMPPConnection connection) {
//		
//		PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
//
//		connection.addPacketListener(new PacketListener() {
//			@Override
//			public void processPacket(Packet packet) {
//				final Message message = (Message) packet;
//				
//				if (message.getBody() != null) {
//					
//					//saveVCard correspondent
//					final String fromName = StringUtils.parseBareAddress(message.getFrom());
//					L.debug("received msg from "+fromName);
//					new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							
//							String senderName = fromName.split("@")[0];
//							Correspondent correspondent = new Correspondent(senderName);
//							correspondent.saveOffline(getApplicationContext());
//							correspondent.downloadCorrespondentIdOffline(getApplicationContext());
//							//saveVCard msg
//							boolean isLeft = true;
//							boolean isSuccessful = true;
//							String body = message.getBody();
//							long date = System.currentTimeMillis();
//							String receiverName = "";
//							boolean isUnread = true;//ChatActivity.isRunning ? false: true;
//							boolean isSyncedOnline = false;
//							NewMessage msg = new NewMessage(senderName, receiverName, body, isLeft, isSuccessful, isUnread, isSyncedOnline, date);
//							msg.saveMyReceivedMsgOffline(getApplicationContext());
//							
//							// check if there is an available activity
//							// to,
//							// receive the brodacast
//							if (!TabHostActivity.isRunning && !ChatHistoryActivity.isRunning
//									&& !ChatActivity.isRunning) {
//
//								L.debug("sending nottification!");
//
//								// send notification
//								sendNotification(correspondent);
//
//							} else {
//
//								// send broadcast
//								Intent broadcast = new Intent(AppConfig.ACTION_RECEIVED_MSG);
//								broadcast.putExtra("userid", correspondent.getId());
//								broadcast.putExtra("username", correspondent.getUsername());
//								broadcast.putExtra("msg", msg.getBody());
//								broadcast.putExtra("date", msg.getDate());
//								L.debug("sending broadcast!");
//								sendBroadcast(broadcast);
//
//							}
//							
//							
//							
//							
//						}
//					}).start();
//					
//					
//					
///*/
//					new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							SQLiteHandler db = new SQLiteHandler(getApplicationContext());
//							db.openToRead();
//
//							HashMap<String, String> postDataParams = new HashMap<String, String>();
//							postDataParams.put("tag", "download_msg_by_user_name_and_correspondent_id");
//							postDataParams.put("username", fromName.split("@")[0]);
//							postDataParams.put("correspondent_id", db.getLoggedInID());
//
//							final String spec = AppConfig.URL_MSG;s
//							String webPage = HttpUtilz.makeRequest(spec, postDataParams);
//							L.debug("ChatMessageService, webPage: " + webPage);
//
//							db.close();
//
//							try {
//								JSONObject jObj = new JSONObject(webPage);
//								boolean error = jObj.getBoolean("error");
//								L.debug("msg? " + message.getBody());
//								if (!error) {
//									
//									JSONArray msgs = jObj.getJSONArray("chtMessages");
//									JSONObject msg = msgs.getJSONObject(0);
//									
//									String userId = msg.getString("user_id");//from, sender id
//									String email = msg.getString("email");
//									String name = msg.getString("firstname");
//									
//									String correspondent_id = msg.getString("correspondent_id");//to
//									String newMsg = msg.getString("message");
//									String isLeft = msg.getString("is_left");
//									String isSuccess = msg.getString("is_success");
//									String dateCreated = msg.getString("date_created");
//									
//									L.debug("getting sender's credential " + userId);
//
//									final Correspondent correspondent = new Correspondent();
//									correspondent.setId(Long.parseLong(userId));
//									correspondent.setUsername(name);
//									
//
//									OneComment comment = new OneComment(false, newMsg, true, dateCreated, true);
//									long now = System.currentTimeMillis();
//									String dateReceived = DateUtils.millisToSimpleDate(now, DateFormatz.DATE_FORMAT_5);
//									comment.dateReceived = dateReceived;
//									
//									// comment.isUnread = true;
//									correspondent.addMessage(comment);
//									//marking chtMessages received online is moved,
//									//to the sync button in the settings screen
//									//comment.markAsReceivedOnline(getApplicationContext(), Long.parseLong(userId));
//									boolean success = true;//by pass mark as received
//									
//									
//									long senderId= Long.parseLong(userId);
//									long receiverId = Long.parseLong(correspondent_id);
//									if(success)correspondent.saveOffline(getApplicationContext(), senderId, receiverId, false);
//									// check if there is an available activity
//									// to,
//									// receive the brodacast
//									if (!TabHostActivity.isRunning && !ChatHistoryActivity.isRunning
//											&& !ChatActivity.isRunning) {
//										if (success) {
//											L.debug("sending nottification!");
//
//											// send notification
//											sendNotification(correspondent);
//										}
//
//									} else {
//										if (success) {
//											// send broadcast
//											Intent broadcast = new Intent(AppConfig.ACTION_RECEIVED_MSG);
//											broadcast.putExtra("userid", correspondent.getId());
//											broadcast.putExtra("username", correspondent.getUsername());
//											broadcast.putExtra("msg", message.getBody());
//											L.debug("sending broadcast!");
//											sendBroadcast(broadcast);
//										}
//
//									}
//
//								} else {
//									String errorMsg = jObj.getString("error_msg");
//									Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
//								}
//							} catch (JSONException e) {
//								e.printStackTrace();
//							}
//
//						}
//					}).start();
////*/
//					// StringRequest strReq = new StringRequest(Method.POST,
//					// AppConfig.URL_MSG,
//					// new Response.Listener<String>() {
//					//
//					// @Override
//					// public void onResponse(String response) {
//					// L.debug("response: " + response.toString());
//					//
//					//
//					// try {
//					// JSONObject jObj = new JSONObject(response);
//					// boolean error = jObj.getBoolean("error");
//					// L.debug("msg? " + message.getBody());
//					// if (!error) {
//					//
//					// String uid = jObj.getString("uid");
//					// JSONObject user = jObj.getJSONObject("user");
//					// String name = user.getString("name");
//					// String email = user.getString("email");
//					// L.debug("getting sender's credential " + uid);
//					//
//					// final Correspondent correspondent = new Correspondent();
//					// correspondent.setId(Long.parseLong(uid));
//					// correspondent.setUsername(name);
//					// correspondent.setEmail(email);
//					//
//					// OneComment comment = new OneComment(false,
//					// message.getBody(), true);
//					// //comment.isUnread = true;
//					// correspondent.addMessage(comment);
//					//
//					// new Thread(new Runnable() {
//					//
//					// @Override
//					// public void run() {
//					// // saveVCard mesage offline
//					// // if ChatHistoryActivity is running
//					//
//					// boolean success = true;
//					// //if (!ChatActivity.isRunning) {
//					// //success = correspondent.saveVCard(getApplicationContext(),
//					// false);
//					// correspondent.saveOffline(getApplicationContext(),
//					// false);
//					// //}
//					//
//					// // check if there is an available activity
//					// // to,
//					// // receive the brodacast
//					// if (!TabHostActivity.isRunning &&
//					// !ChatHistoryActivity.isRunning
//					// && !ChatActivity.isRunning) {
//					// if(success){
//					// L.debug("sending nottification!");
//					//
//					// // send notification
//					// sendNotification(correspondent);
//					// }
//					//
//					//
//					// } else {
//					// if(success){
//					// // send broadcast
//					// Intent broadcast = new
//					// Intent(AppConfig.ACTION_RECEIVED_MSG);
//					// broadcast.putExtra("userid", correspondent.getId());
//					// broadcast.putExtra("email", correspondent.getEmail());
//					// broadcast.putExtra("username",
//					// correspondent.getUsername());
//					// broadcast.putExtra("fname", correspondent.getFname());
//					// broadcast.putExtra("msg", message.getBody());
//					// L.debug("sending broadcast!");
//					// sendBroadcast(broadcast);
//					// }
//					//
//					// }
//					//
//					// }
//					// }).start();
//					//
//					// } else {
//					// String errorMsg = jObj.getString("error_msg");
//					// Toast.makeText(getApplicationContext(), errorMsg,
//					// Toast.LENGTH_LONG).show();
//					// }
//					// } catch (JSONException e) {
//					// e.printStackTrace();
//					// }
//					//
//					//
//					// }
//					//
//					// }, new Response.ErrorListener() {
//					// @Override
//					// public void onErrorResponse(VolleyError error) {
//					// L.error("error: " + error.getBody());
//					// Toast.makeText(getApplicationContext(),
//					// error.getBody(), Toast.LENGTH_LONG).show();
//					//
//					// }
//					// }) {
//					// @Override
//					// protected Map<String, String> getParams() {
//					//
//					// SQLiteHandler db = new
//					// SQLiteHandler(getApplicationContext());
//					// db.openToRead();
//					//
//					// Map<String, String> params = new HashMap<String,
//					// String>();
//					// params.put("tag", tag_string_req);
//					// params.put("username", fromName.split("@")[0]);
//					// params.put("correspondent_id", db.getLoggedInID());
//					//
//					// db.close();
//					// return params;
//					// }
//					// };
//					//
//					// AppController.getInstance().addToRequestQueue(strReq,
//					// tag_string_req);
//					// OneComment comment = new OneComment(false,
//					// message.getBody(), true);
//
//				}
//			}
//		}, filter);
//
//	}

	// private void saveMesageOffline(final Correspondent correspondent) {
	//
	// //saveVCard only the message,
	// //if sender is already exist in db
	// if(correspondent.isExisting(getApplicationContext())){
	// correspondent.getConversation().get(0).saveOnline(getApplicationContext(),correspondent.getId());
	// correspondent.getConversation().get(0).saveOffline(getApplicationContext(),correspondent.getId());
	//
	// //saveVCard both the sender and the message,
	// //if sender doesn't exist in db
	// }else{
	// correspondent.saveOffline(getApplicationContext(), false);
	// }
	//
	// }

	private PendingIntent getNotificationPendingIntent(Correspondent correspondent) {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ChatActivity.class);
		resultIntent.putExtra("userid", correspondent.getId());
		resultIntent.putExtra("username", correspondent.getUsername());
		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ChatActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);

		return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

	}

	private void sendNotification(Correspondent correspondent) {

		int msgCount = NewMessage.getUnReadMsgCountOffline(getApplicationContext());

		String title = (msgCount > 1) ? " new chtMessages" : " new message";

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(msgCount + title).setAutoCancel(true).setContentText(correspondent.getUsername());

		mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;

		PendingIntent resultPendingIntent = getNotificationPendingIntent(correspondent);

		// if (Build.VERSION.SDK_INT == 19) {
		// resultPendingIntent.cancel();
		// }

		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(AppConfig.MSG_NOTIFICATION_ID, mBuilder.build());

	}

}
