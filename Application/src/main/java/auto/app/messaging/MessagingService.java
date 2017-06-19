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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessagingService extends Service {
    private static final String TAG = MessagingService.class.getSimpleName();
    private static final String READ_ACTION = "auto.app.messaging.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION = "auto.app.messaging.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_REMOTE_REPLY = "extra_remote_reply";

    private static final String EOL = "\n";
    private static final String CONVERSATION_NAME = "News Bot";
    private static final int CONVERSATION_NUMBER_ID = 1;

    private NotificationManagerCompat mNotificationManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        ArticleList articles = (ArticleList) intent.getSerializableExtra("news");
        sendWelcome(articles);
        return Service.START_STICKY;
    }

    private void sendWelcome(ArticleList articles) {

        Intent replyContent = new Intent().addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(REPLY_ACTION)
                .putExtra("news", articles);

        PendingIntent replyIntent = PendingIntent.getBroadcast(
                getApplicationContext(), CONVERSATION_NUMBER_ID,
                replyContent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        List<String> messages = new ArrayList<>();
        messages.add("Olá! Sou seu aplicativo de notícias, deseja ouvir algumas notícias?");
        messages.add("Responsa com \"SIM\" ou \"Não\"");

        //StringBuilder messageForNotification = new StringBuilder();
        //messageForNotification.append(article.getTitle());
        //messageForNotification.append(EOL);
        //messageForNotification.append("Responda com \"LER\" para ouvir o artigo completo, \"PRÓXIMA\" ou \"SAIR\".");

        sendNotification(messages, replyIntent);
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

    private void sendNotification(List<String> messages, PendingIntent replyIntent) {

        long timestamp = System.currentTimeMillis();

        // TODO a princípio não vai ser usada
        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), CONVERSATION_NUMBER_ID,
                getMessageReadIntent(CONVERSATION_NUMBER_ID), PendingIntent.FLAG_UPDATE_CURRENT);


        // Build a RemoteInput for receiving voice input in a Car Notification or text input on
        // devices that support text input (like devices on Android N and above).


        /* TODO recebido como parãmetro
        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                CONVERSATION_NUMBER_ID,
                getMessageReplyIntent(CONVERSATION_NUMBER_ID),
                PendingIntent.FLAG_UPDATE_CURRENT);
        */

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REMOTE_REPLY).setLabel(getString(auto.app.messaging.R.string.reply)).build();
        // Build an Android N compatible Remote Input enabled action.
        NotificationCompat.Action actionReplyByRemoteInput = new NotificationCompat.Action.Builder(
                auto.app.messaging.R.drawable.notification_icon, getString(auto.app.messaging.R.string.reply), replyIntent)
                .addRemoteInput(remoteInput)
                .build();

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        UnreadConversation.Builder unreadConvBuilder =
                new UnreadConversation.Builder(CONVERSATION_NAME)
                .setLatestTimestamp(timestamp)
                .setReplyAction(replyIntent, remoteInput)
                .setReadPendingIntent(readPendingIntent); //TODO testar para ver se não é preciso realmente usar uma read intent

        StringBuilder message = new StringBuilder();
        for(String m : messages) {
            unreadConvBuilder.addMessage(m);
            message.append(m);
            message.append(EOL);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(auto.app.messaging.R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), auto.app.messaging.R.drawable.android_contact))
                .setContentText(message.toString())
                .setWhen(timestamp)
                .setContentTitle(CONVERSATION_NAME)
                .setContentIntent(readPendingIntent) //TODO não vai ter read intent eu acho
                .extend(new CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(getApplicationContext().getResources().getColor(auto.app.messaging.R.color.default_color_light)))
                .addAction(actionReplyByRemoteInput); // TODO ver se funciona a reply intent

        mNotificationManager.notify(CONVERSATION_NUMBER_ID, builder.build());
    }
}
