package com.test.cc;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PostListFragment extends ListFragment
implements AdapterView.OnItemClickListener {

    private TextView mEmptyList;
    private View mBtnRefresh;
    private View mProgressContainer;

    private OkHttpClient mClient = new OkHttpClient();
    private PostsAdapter mAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View reply = inflater.inflate(R.layout.fragment_postlist, null);

        mEmptyList = (TextView) reply.findViewById(android.R.id.empty);

        mProgressContainer = reply.findViewById(R.id.progressContainer);

        mBtnRefresh = reply.findViewById(R.id.btn_download);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPosts(true);
            }
        });

        return reply;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshPosts(false);
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

    /**
     * List view adapter setting.
     * @param posts data
     */
    protected void updatePosts(final JSONArray posts) {
        mAdapter = new PostsAdapter(posts);
        getListView().post(new Runnable() {
            @Override
            public void run() {
                if(mAdapter.getCount() == 0)
                    mEmptyList.setText(getString(R.string.err_no_items));
                setListAdapter(mAdapter);
            }
        });

    }

    /**
     * HTTP request.
     * @param force true if should make HTTP Request anyway, false if should use in memory cache.
     */
    public void refreshPosts(boolean force) {
        if(mAdapter != null && !force) return;

        Request request = new Request.Builder()
            .url(getString(R.string.urlPosts))
            .build();

        hideList();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mEmptyList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyList.setText(getString(R.string.err_connection_ko));
                        showList();
                    }
                }, 1000);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray postsJson = json.getJSONArray("posts");
                    updatePosts(postsJson);
                    showList();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void hideList(){
        GUIUtils.alphaSwitch(null, mBtnRefresh, View.INVISIBLE);
        GUIUtils.alphaSwitch(mProgressContainer, getListView(), View.INVISIBLE);
    }

    private void showList(){
        GUIUtils.alphaSwitch(getListView(), mProgressContainer, View.INVISIBLE);
        GUIUtils.alphaSwitch(mBtnRefresh, null, View.INVISIBLE);
    }

    /**
     * Custom adapter
     */
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

            if(view == null){
                view = getActivity().getLayoutInflater()
                        .inflate(R.layout.post_list_fragment_item, parent, false);
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView summary = (TextView) view.findViewById(R.id.summary);
            ImageView image = (ImageView) view.findViewById(R.id.image);

            Post post = getItem(position);

            title.setText(Html.fromHtml(post.getTitle().toString()));
            summary.setText(Html.fromHtml(post.getExcerpt().toString()));

            try {
                Uri uri = Uri.parse(post.getFeaturedImage());
                Picasso
                        .with(view.getContext())
                        .load(uri)
                        .resize(parent.getWidth(), (int)getResources().getDimension(R.dimen.img_height))
                        .centerInside()
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(image);
            }catch (NullPointerException e){
                Picasso.with(view.getContext()).cancelRequest(image);
                image.setImageBitmap(null);
            }

            return view;
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