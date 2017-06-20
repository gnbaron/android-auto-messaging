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

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static auto.app.messaging.MessagingService.*;
/**
 * A receiver that gets called when a reply is sent to a given conversationId.
 */
public class MessageReplyReceiver extends BroadcastReceiver {

    private static final String TAG = MessageReplyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        CharSequence reply = getMessageText(intent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if(MessagingService.REPLY_WELCOME_ACTION.equals(intent.getAction())){
            Log.d(TAG, "Got reply ("+reply+") for welcome action");
            if(reply.toString().toLowerCase().equals("sim")){
                ArticleList articles = (ArticleList) intent.getSerializableExtra("news");

            } else if(reply.toString().toLowerCase().equals("não")){
                notificationManager.cancel(intent.getIntExtra(MessagingService.CONVERSATION_ID, -1));
            }
        } else if (MessagingService.REPLY_ACTION.equals(intent.getAction())) {
            /*
            if(reply.toString().equals("read")){

            } else if(reply.toString().equals("next")){

            }
            */

            //StringBuilder messageForNotification = new StringBuilder();
            //messageForNotification.append(article.getTitle());
            //messageForNotification.append(EOL);
            //messageForNotification.append("Responda com \"LER\" para ouvir o artigo completo, \"PRÓXIMA\" ou \"SAIR\".");

            // Update the notification to stop the progress spinner.

            Notification repliedNotification = new NotificationCompat.Builder(context)
                    .setSmallIcon(auto.app.messaging.R.drawable.notification_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), auto.app.messaging.R.drawable.android_contact))
                    .setContentText(context.getString(auto.app.messaging.R.string.replied))
                    .build();

            notificationManager.notify(intent.getIntExtra(MessagingService.CONVERSATION_ID, -1), repliedNotification);
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(
                    MessagingService.EXTRA_REMOTE_REPLY);
        }
        return null;
    }
}
