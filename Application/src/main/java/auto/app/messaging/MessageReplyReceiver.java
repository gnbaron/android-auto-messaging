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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

/**
 * A receiver that gets called when a reply is sent to a given conversationId.
 */
public class MessageReplyReceiver extends BroadcastReceiver {

    private static final String TAG = MessageReplyReceiver.class.getSimpleName();
    private static final String YES = "sim";
    private static final String NO = "não";
    private static final String READ = "ler";
    private static final String NEXT = "próxima";
    private static final String EXIT = "sair";

    @Override
    public void onReceive(Context context, Intent intent) {

        String reply = getMessageText(intent).toString().toLowerCase();
        Log.d(TAG, "Got reply ("+reply+") on Receiver");

        if(MessagingService.REPLY_WELCOME_ACTION.equals(intent.getAction())) {

            if(reply.equals(NO))
                clear(context, intent.getIntExtra(MessagingService.CONVERSATION_ID, -1));
            else if(reply.equals(YES)) {
                ArticleList articles = (ArticleList) intent.getSerializableExtra("news");
                sendNextArticle(context, articles);
            } else
                Log.e(TAG, "Unrecognized command.");

        } else if (MessagingService.REPLY_ACTION.equals(intent.getAction())) {

            if(reply.equals(READ)) {
                Article article = (Article) intent.getSerializableExtra("article");
                ArticleList news = (ArticleList) intent.getSerializableExtra("news");
                readFullArticle(context, article, news);
            } else if(reply.equals(NEXT)) {
                ArticleList articles = (ArticleList) intent.getSerializableExtra("news");
                sendNextArticle(context, articles);
            } else if(reply.equals(EXIT)) {
                clear(context, intent.getIntExtra(MessagingService.CONVERSATION_ID, -1));
            } else
                Log.e(TAG, "Unrecognized command.");
        }
    }

    private void sendNextArticle(Context context, ArticleList articles) {

        Log.d(TAG, "Número de artigos "+articles.data.size());
        Article next = articles.getNext();
        Log.d(TAG, "Número de artigos depois "+articles.data.size());

        if(next != null) {
            Intent replyContent = MessagingService.getReplyIntent(MessagingService.REPLY_ACTION)
                .putExtra("news", articles)
                .putExtra("article", next);

            MessagingService.sendNotification(
                context,
                MessagingService.getPendindIntent(context, replyContent),
                next.getTitle(),
                "Responda com \"LER\" para ouvir o artigo completo, \"PRÓXIMA\" ou \"SAIR\"."
            );
        } else
            sendEmptyDataNotification(context);
    }

    private void readFullArticle(Context context, Article article, ArticleList news) {
        if(article != null) {
            Intent replyContent = MessagingService.getReplyIntent(MessagingService.REPLY_ACTION)
                .putExtra("news", news);

            MessagingService.sendNotification(
                context,
                MessagingService.getPendindIntent(context, replyContent),
                "Notícia publicada por " + article.getAuthor() + ".",
                article.getDescription(),
                "Fim.",
                "Responda com \"PRÓXIMA\" para ouvir as próximas notícias ou \"SAIR\"."
            );
        } else
            sendNextArticle(context, news);
    }

    private void sendEmptyDataNotification(Context context) {
        MessagingService.sendNotification(
            context,
            MessagingService.getPendindIntent(context, MessagingService.getReplyIntent(MessagingService.REPLY_ACTION)),
            "Não há mais notícias.",
            "Até amanhã!"
        );
    }

    private void clear(Context context, int id) {
        Log.d(TAG, "Clear all notifications");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(id);
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(MessagingService.EXTRA_REMOTE_REPLY);
        }
        return null;
    }
}
