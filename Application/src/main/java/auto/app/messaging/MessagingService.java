/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package auto.app.messaging;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class MessagingService extends Service {
    private static final String TAG = MessagingService.class.getSimpleName();
    private static final String READ_ACTION = "auto.app.messaging.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION = "auto.app.messaging.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_REMOTE_REPLY = "extra_remote_reply";

    private static final String EOL = "\n";
    public static final int MSG_SEND_NOTIFICATION = 1;

    private NotificationManagerCompat mNotificationManager;

    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    // Creates an intent that will be triggered when a message is marked as read.
    private Intent getMessageReadIntent(int id) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(READ_ACTION)
                .putExtra(CONVERSATION_ID, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(REPLY_ACTION)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void sendNotification(News news) {
        int conversationId = 1;
        String conversationName = "News Bot";
        long conversationTimestamp = System.currentTimeMillis();

        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversationId,
                getMessageReadIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a RemoteInput for receiving voice input in a Car Notification or text input on
        // devices that support text input (like devices on Android N and above).
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REMOTE_REPLY)
                .setLabel(getString(auto.app.messaging.R.string.reply))
                .build();

        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversationId,
                getMessageReplyIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build an Android N compatible Remote Input enabled action.
        NotificationCompat.Action actionReplyByRemoteInput = new NotificationCompat.Action.Builder(
                auto.app.messaging.R.drawable.notification_icon, getString(auto.app.messaging.R.string.reply), replyIntent)
                .addRemoteInput(remoteInput)
                .build();

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        UnreadConversation.Builder unreadConvBuilder =
                new UnreadConversation.Builder(conversationName)
                .setLatestTimestamp(conversationTimestamp)
                .setReadPendingIntent(readPendingIntent)
                .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder
        StringBuilder messageForNotification = new StringBuilder();
        messageForNotification.append(news.getTitle());
        messageForNotification.append(EOL);
        messageForNotification.append("Responda com \"LER\" para ouvir o artigo completo, \"PRÓXIMA\" ou \"SAIR\".");

        String message = messageForNotification.toString();
        unreadConvBuilder.addMessage(message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(auto.app.messaging.R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), auto.app.messaging.R.drawable.android_contact))
                .setContentText(message)
                .setWhen(conversationTimestamp)
                .setContentTitle(conversationName)
                .setContentIntent(readPendingIntent)
                .extend(new CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(getApplicationContext().getResources()
                                .getColor(auto.app.messaging.R.color.default_color_light)))
                .addAction(actionReplyByRemoteInput);

        mNotificationManager.notify(conversationId, builder.build());
    }

    /**
     * Handler for incoming messages from clients.
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<MessagingService> mReference;

        IncomingHandler(MessagingService service) {
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MessagingService service = mReference.get();
            switch (msg.what) {
                case MSG_SEND_NOTIFICATION:
                    if (service != null) {
                        service.sendNotification((News) msg.obj);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
