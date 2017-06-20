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
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

public class MessagingService extends Service {

    private static final String TAG = MessagingService.class.getSimpleName();

    private static final String CONVERSATION_NAME = "News Bot";
    private static final int CONVERSATION_NUMBER = 9999;
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_REMOTE_REPLY = "extra_remote_reply";

    private static final String EOL = "\n";

    public static final String REPLY_ACTION = "auto.app.messaging.REPLY_ACTION";
    public static final String REPLY_WELCOME_ACTION = "auto.app.messaging.REPLY_WELCOME_ACTION";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        ArticleList articles = (ArticleList) intent.getSerializableExtra("news");
        sendWelcome(articles);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendWelcome(ArticleList articles) {
        Intent replyContent = getReplyIntent(REPLY_WELCOME_ACTION)
                .putExtra("news", articles);

        sendNotification(
            getApplicationContext(),
            getPendindIntent(getApplicationContext(), replyContent),
            "Olá! Sou seu aplicativo de notícias, deseja ouvir algumas notícias?",
            "Responda com \"SIM\" ou \"Não\""
        );
    }

    public static Intent getReplyIntent(String action) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(action)
                .putExtra(CONVERSATION_ID, CONVERSATION_NUMBER);
    }

    public static PendingIntent getPendindIntent(Context context, Intent intent) {
        return PendingIntent.getBroadcast(
            context, CONVERSATION_NUMBER,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public static void sendNotification(Context context, PendingIntent replyIntent, String... messages) {
        long timestamp = System.currentTimeMillis();

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REMOTE_REPLY).setLabel(context.getString(auto.app.messaging.R.string.reply)).build();
        NotificationCompat.Action actionReplyByRemoteInput = new NotificationCompat.Action.Builder(
                auto.app.messaging.R.drawable.notification_icon, context.getString(auto.app.messaging.R.string.reply), replyIntent)
                .addRemoteInput(remoteInput)
                .build();

        UnreadConversation.Builder unreadConvBuilder =
                new UnreadConversation.Builder(CONVERSATION_NAME)
                .setLatestTimestamp(timestamp)
                .setReplyAction(replyIntent, remoteInput);

        StringBuilder message = new StringBuilder();
        for(String m : messages) {
            unreadConvBuilder.addMessage(m);
            message.append(m);
            message.append(EOL);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(auto.app.messaging.R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), auto.app.messaging.R.drawable.android_contact))
                .setContentText(message.toString())
                .setWhen(timestamp)
                .setContentTitle(CONVERSATION_NAME)
                .extend(new CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(context.getResources().getColor(auto.app.messaging.R.color.default_color_light)))
                .addAction(actionReplyByRemoteInput);

        NotificationManagerCompat.from(context).notify(CONVERSATION_NUMBER, builder.build());
    }
}
