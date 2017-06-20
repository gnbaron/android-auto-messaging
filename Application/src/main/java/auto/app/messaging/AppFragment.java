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

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AppFragment.class.getSimpleName();
    private Button startBtn;
    private ArticleList news;

    /*private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            startBtn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
            startBtn.setEnabled(false);
        }
    };*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(auto.app.messaging.R.layout.fragment_message_me, container, false);
        startBtn = (Button) rootView.findViewById(auto.app.messaging.R.id.start_btn);
        startBtn.setOnClickListener(this);
        startBtn.setEnabled(true);
        searchArticle();
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == startBtn && news != null && news.data.size() > 0) {
            Intent serviceIntent = new Intent(getActivity(), MessagingService.class);
            serviceIntent.putExtra("news", news);
            getActivity().startService(serviceIntent);
        }
    }

    private void searchArticle() {
        /*
        String url = "https://Articleapi.org/v1/articles?source=bbc-Article&sortBy=top&apiKey=ad13989aa3694667a102596ba285e15c";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Article = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray articles = jsonObject.getJSONArray("articles");
                            for(int i = 0; i < articles.length(); i ++) {
                                JSONObject article = articles.getJSONObject(i);
                                Article articleModel = new Article();
                                articleModel.setAuthor(article.getString("author"));
                                articleModel.setTitle(article.getString("title"));
                                articleModel.setDescription(article.getString("description"));
                                article.add(articleModel);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error sending a message", error);
                    }
                }
        );
        RequestSingleton.getInstance(this.getContext()).addToRequestQueue(stringRequest);
        */
        List<Article> articles = new ArrayList<>();
        Article n1 = new Article();
        n1.setAuthor("G1");
        n1.setTitle("Trio é preso com drone para enviar drogas e celulares a presídio.");
        n1.setDescription("Três jovens foram presos e um menor apreendido tentando lançar drogas, celulares e serras para dentro da Casa " +
                "de Prisão Provisória de Palmas. O detalhe é que eles estavam usando um drone para entregar os objetos aos presos, no pátio da cadeia. " +
                "A ação deles foi frustrada na noite deste sábado (17).");
        Article n2 = new Article();
        n2.setAuthor("G1");
        n2.setTitle("Google endurece medidas para remover conteúdo extremista de YouTube.");
        n2.setDescription("O Google, empresa da Alphabet, vai adotar mais medidas para identificar e remover conteúdo terrorista ou de " +
                "violência extremista de sua plataforma de vídeos YouTube, informou a companhia neste domingo (19).");
        articles.add(n2);
        Article n3 = new Article();
        n3.setAuthor("G1");
        n3.setTitle("8 tecnologias que prometem mudar a forma como você paga contas.");
        n3.setDescription("Caixas eletrônicos que mais parecem smartphones, com espessura fina, tela sensível ao toque e que liberem saques pelo celular. " +
                "O cenário pode parecer um tanto futurista, mas esses terminais já foram desenvolvidos e estão prontos para chegar ao mercado.");
        articles.add(n3);
        news = new ArticleList(articles);
    }
}
