package com.test.cc;

import android.net.Uri;

import org.json.JSONObject;

public class Post {

    private final JSONObject mJson;

    public static Post build(JSONObject json) {
        return new Post(json);
    }

    public Post(JSONObject json) {
        mJson = json;
    }

    public CharSequence getTitle() {
        return mJson.optString("title");
    }

    public CharSequence getExcerpt() {
        return mJson.optString("excerpt");
    }

    public Uri getUri() {
        return Uri.parse(mJson.optString("URL"));
    }

}