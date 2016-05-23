package com.test.cc;

import android.app.ListFragment;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Callback;

import java.io.IOException;

public class PostListFragment extends ListFragment
implements AdapterView.OnItemClickListener {

    OkHttpClient mClient = new OkHttpClient();

    // empty constructor necessary for fragments
    public PostListFragment() {}

    @Override
    public void onResume() {
        super.onResume();

        refreshPosts();

        getListView().setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        PostsAdapter adapter = (PostsAdapter)getListAdapter();
        Post post = adapter.getItem(position);

        Intent browseIntent = new Intent();
        browseIntent.setAction(Intent.ACTION_VIEW);
        browseIntent.setData(post.getUri());

        getActivity().startActivity(browseIntent);

    }

    protected void updatePosts(JSONArray posts) {

        setListAdapter(new PostsAdapter(posts));

    }

    public void refreshPosts() {

        Request request = new Request.Builder()
            .url("https://public-api.wordpress.com/rest/v1/freshly-pressed?number=30")
            .build();

        Response response = mClient.newCall(request).execute();

        JSONObject json = new JSONObject(response.body().string());
        JSONArray postsJson = json.getJSONArray("posts");
        updatePosts(postsJson);
        
    }

    private class PostsAdapter extends BaseAdapter {

        private final JSONArray mPostData;

        public PostsAdapter(JSONArray postData) {

            if (postData == null) {
                throw new IllegalArgumentException("postData must not be null");
            }

            mPostData = postData;

        }

        @Override
        public Post getItem(int position) {
            return Post.build(mPostData.optJSONObject(position));
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            View itemView = getActivity().getLayoutInflater()
                .inflate(R.layout.post_list_fragment_item, parent, false);

            TextView title = (TextView) itemView.findViewById(R.id.title);
            TextView summary = (TextView) itemView.findViewById(R.id.summary);

            Post post = getItem(position);

            title.setText(post.getTitle());
            summary.setText(Html.fromHtml(post.getExcerpt().toString()));

            return itemView;
        }

        @Override
        public long getItemId(int position) {
            return -1L;
        }

        @Override
        public int getCount() {
            return mPostData.length();
        }

    }

}