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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //searchNews();
        searchMockedNews();
    }

    private void searchNews() {
        String url = "https://Articleapi.org/v1/articles?source=bbc-Article&sortBy=top&apiKey=ad13989aa3694667a102596ba285e15c";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<Article> news = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray articles = jsonObject.getJSONArray("articles");
                            for(int i = 0; i < articles.length(); i ++) {
                                JSONObject json = articles.getJSONObject(i);
                                Article article = new Article();
                                article.setAuthor(json.getString("author"));
                                article.setTitle(json.getString("title"));
                                article.setDescription(json.getString("description"));
                                news.add(article);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        read(news);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error sending a message", error);
                    }
                }
        );
        RequestSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void searchMockedNews() {
        List<Article> news = new ArrayList<>();
        Article n1 = new Article();
        n1.setAuthor("G 1 Tecnologia");
        n1.setTitle("Trio é preso com drone para enviar drogas e celulares a presídio.");
        n1.setDescription("Três jovens foram presos e um menor apreendido tentando lançar drogas, celulares e serras para dentro da Casa " +
                "de Prisão Provisória de Palmas. O detalhe é que eles estavam usando um drone para entregar os objetos aos presos, no pátio da cadeia. " +
                "A ação deles foi frustrada na noite deste sábado (17).");
        news.add(n1);
        Article n2 = new Article();
        n2.setAuthor("G 1 Tecnologia");
        n2.setTitle("Google endurece medidas para remover conteúdo extremista de YouTube.");
        n2.setDescription("O Google, empresa da Alphabet, vai adotar mais medidas para identificar e remover conteúdo terrorista ou de " +
                "violência extremista de sua plataforma de vídeos YouTube, informou a companhia neste domingo (19).");
        news.add(n2);
        Article n3 = new Article();
        n3.setAuthor("G 1 Tecnologia");
        n3.setTitle("8 tecnologias que prometem mudar a forma como você paga contas.");
        n3.setDescription("Caixas eletrônicos que mais parecem smartphones, com espessura fina, tela sensível ao toque e que liberem saques pelo celular. " +
                "O cenário pode parecer um tanto futurista, mas esses terminais já foram desenvolvidos e estão prontos para chegar ao mercado.");
        news.add(n3);
        read(news);
    }

    private void read(List<Article> news) {
        Intent serviceIntent = new Intent(this, MessagingService.class);
        serviceIntent.putExtra("news", new ArticleList(news));
        this.startService(serviceIntent);
        this.finish();
    }
}
