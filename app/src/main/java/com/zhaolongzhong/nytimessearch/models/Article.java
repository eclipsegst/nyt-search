package com.zhaolongzhong.nytimessearch.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/*

{
    "response": {
        "meta": {
        "hits": 6944,
        "time": 20,
        "offset": 0
        },
        "docs": [
            {
            "web_url": "http://topics.nytimes.com/topic/subject/android-operating-system",
            "snippet": "News about Android (Operating System), including commentary and archival articles published in The New York Times.",
            "lead_paragraph": "News about Android (Operating System), including commentary and archival articles published in The New York Times.",
            "abstract": null,
            "print_page": null,
            "blog": [],
            "source": null,
            "multimedia": [],
            "headline": {
            "main": "Android"
            },
            "keywords": [],
            "pub_date": null,
            "document_type": "topic",
            "news_desk": null,
            "section_name": "Times Topics",
            "subsection_name": null,
            "byline": null,
            "type_of_material": "timestopic",
            "_id": "56e0ea3e38f0d80718d563b2",
            "word_count": "16",
            "slideshow_credits": null
            }
        ]
    }
}

 */

@RealmClass
public class Article implements RealmModel {
    private static final String TAG = Article.class.getSimpleName();

    // Property name
    private static final String HEADLINE = "headline";

    @PrimaryKey
    private String id;
    private String webUrl;
    private String headline;
    private String thumbnail;
    private String snippet;

    public String getId() {
        return id;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getSnippet() {
        return snippet;
    }

    /**
     * @param jsonObject
     * @throws JSONException
     *
     * Map a Article JSONObject to a Article object
     */
    public void mapData(JSONObject jsonObject) throws JSONException {
        try {
            this.id = jsonObject.getString("_id");
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");
            this.snippet = jsonObject.getString("snippet");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            if (multimedia.length() > 0) {
                JSONObject multimediaJson = multimedia.getJSONObject(0);
                this.thumbnail = "http://www.nytimes.com/" + multimediaJson.getString("url");
            } else {
                this.thumbnail = "";
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing article.", e);
        }
    }

    /**
     * @param jsonArray
     *
     * Convert Article JSONArray to Article object and save to realm
     */
    public static List<Article> fromJSONArray(JSONArray jsonArray) {
        RealmList<Article> results = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Article article = new Article();
                article.mapData(jsonArray.getJSONObject(i));
                results.add(article);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing article object.", e);
            }
        }

        realm.copyToRealmOrUpdate(results);
        realm.commitTransaction();
        realm.close();

        return results;
    }

    /**
     * @return all the articles
     */
    public static RealmResults<Article> getAllArticles() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Article> articles =  realm.where(Article.class).findAll();
        realm.close();
        return articles;
    }

    /**
     * @param headline
     * @return a list of articles by headline
     */
    public static RealmResults<Article> getArticlesByHeadline(String headline) {
        if (headline == null || headline.isEmpty()) {
            return getAllArticles();
        }

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Article> articles =  realm.where(Article.class).
                contains(HEADLINE, headline, Case.INSENSITIVE).findAll();
        realm.close();
        return articles;
    }
}