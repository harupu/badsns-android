package com.example.bootcampsns.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.bootcampsns.R;
import com.example.bootcampsns.adapter.FeedAdapter;
import com.example.bootcampsns.adapter.FeedInfo;
import com.example.bootcampsns.util.AsyncHttpRequest;
import com.example.bootcampsns.util.FriendsIconManager;
import com.example.bootcampsns.util.UserSessionInfo;
import com.example.bootcampsns.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class TimelineActivity extends BaseActivity {

    /**
     * HTTPリクエストのタイプ
     */
    private enum REQ_TYPE {
        FEED(0),
        POST(1),
        FIND_NEW(2),
        FIND_NEW_FETCH(3),
        FIND_OLD(4),
        FIND_OLD_FETCH(5),

        FRIEND_ICON(1000000000),
        POST_IMAGE(2000000000);
        private final int id;

        private REQ_TYPE(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    ;

    /**
     * Activityを立ち上げた際のResultを取ってくるためのID
     */
    private enum ACTIVITY_REQ {
        IMAGE_UPLOAD(1),
        FRIEND_ADD(2);
        private final int id;

        private ACTIVITY_REQ(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * FEEDを取ってくるときにListViewの前後どちらにたすかを決める数値
     */
    private enum FEED_DIRECTION {
        NORMAL,
        REVERSE
    }

    /**
     * 各変数定義
     */
    // 文字の高さの指定
    private final int baseLineHeight = 40;

    // 新しいfeedを取ってくる間隔
    private final int findInterval = 5000;

    // 最新の投稿のFeedId
    private int lastFeedId = 0;
    private int oldFeedId = 999999999;

    /**
     * Feedを表示する部分のリスト
     */
    private FeedAdapter adapter = null;
    private ArrayList<FeedInfo> feedList = new ArrayList<>();
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar((Toolbar) findViewById(R.id.toolbar));
        setContentView(R.layout.activity_timeline);

        UserSessionInfo userInfo = UserSessionInfo.getInstance();

        /** 上部のツールバー、メニューの設定 */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(userInfo.getIconBitmap(),
                        Utils.dpToPixel(this, baseLineHeight),
                        Utils.dpToPixel(this, baseLineHeight), false)));
        toolbar.setTitle(userInfo.getUserName());
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /** 各メニュー項目 */
                if (item.getItemId() == R.id.menu_server_settings) {
                    // Server設定：設定Activityを立ち上げる
                    Intent intent = new Intent(TimelineActivity.this, ServerSettingsActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.menu_friend) {
                    // Server設定：設定Activityを立ち上げる
                    Intent intent = new Intent(TimelineActivity.this, FriendActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.menu_upload) {
                    // 画像を投稿：画像アップロード用Activityを立ち上げる
                    Intent intent = new Intent(TimelineActivity.this, UploadActivity.class);
                    startActivityForResult(intent, ACTIVITY_REQ.IMAGE_UPLOAD.getId());
                } else if (item.getItemId() == R.id.menu_logout) {
                    // ログアウト
                    logout();
                }
                return true;
            }
        });

        // 投稿用のEditView
        final EditText post = (EditText) findViewById(R.id.timeline_post);
        post.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //エンターキーで送信
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.POST.getId());
                    String comment = post.getText().toString();
                    if (comment.equals("")) {// コメントが空だったら何もしない
                        return false;
                    }
                    httpRequest.addParam("feed_type", "text");
                    httpRequest.addParam("comment", comment);
                    httpRequest.execute("/feeds");
                    post.setText("");
                    return true;
                }
                return false;
            }
        });

        // Feedの新着欄のイベント設定
        TextView feedNew = (TextView) findViewById(R.id.feed_new);
        feedNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewFeed();
            }
        });

        // Feedの表示場所の設定
        listView = (ListView) findViewById(R.id.feed_list);
        adapter = new FeedAdapter(this);
        adapter.setFeedList(feedList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /** FeedItemがクリックされて、画像タイプだったらViewを表示する */
                ListView listView = (ListView) parent;
                FeedInfo item = (FeedInfo) listView.getItemAtPosition(position);
                if (item.getComment() != null) {
                    if(item.getFeedId() == -1) {
                        feedList.remove(position);
                        getOldFeed();

                    }
                    return;
                }
                // Intentで渡す形式だと重すぎて落ちるので直接Viewを追加する。
                final View layout = TimelineActivity.this.getLayoutInflater()
                        .inflate(R.layout.activity_image, null);
                TimelineActivity.this.addContentView(layout,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                ImageView imageView = (ImageView) findViewById(R.id.post_image_view);
                imageView.setImageBitmap(item.getImageBitmap());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // クリックされたら自壊する
                        ((ViewGroup) layout.getParent()).removeView(layout);
                    }
                });
            }
        });

        // Feedの取得開始
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.FEED.getId(),
                AsyncHttpRequest.HTTP_METHOD.GET);
        httpRequest.execute("/feeds");
    }

    /**
     * 新しいFeedを取得する
     */
    private void sendFindNew() {
        if (this.isDestroyed()) {
            // Activityが終了していたら停止する
            return;
        }
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.FIND_NEW.getId(),
                AsyncHttpRequest.HTTP_METHOD.GET);
        httpRequest.execute("/feeds/" + lastFeedId + "/find_new");
    }

    /**
     * HTTP通信の戻りを処理する
     */
    @Override
    public void asyncHttpCallback(JSONObject result, int requestId) {
        try {
            if (requestId == REQ_TYPE.FEED.getId()) {
                // Feed全体をとる処理の場合、FeedをViewに全件追加する
                addFeeds(result, FEED_DIRECTION.NORMAL);
                JSONArray feeds = result.getJSONArray("feeds");
                if(feeds.length() == 30) {// 30件の場合、取得限界まで取ってきている。本来はfind_oldするべきだが、影響は少ないので直接「もっと読む」を表示する
                    FeedInfo item = new FeedInfo();
                    item.setFeedId(-1);
                    item.setComment("もっと読む");
                    feedList.add(item);
                }
                sendFindNew();// 新着監視を開始する
            } else if (requestId == REQ_TYPE.POST.getId()) {
                // 投稿が終わったら自分の投稿結果をサーバに取りに行く
                AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.FIND_NEW_FETCH.getId(),
                        AsyncHttpRequest.HTTP_METHOD.GET);
                // 新しいFeedを取得する
                httpRequest.execute("/feeds/" + lastFeedId + "/find_new?include_items=1");
            } else if (requestId == REQ_TYPE.FIND_NEW.getId()) {
                // 新しいFeedがあったら取りに行く
                if (result.has("count")) {
                    int count = result.getInt("count");
                    if (count > 0) {
                        TextView feedNew = (TextView) findViewById(R.id.feed_new);
                        feedNew.setText(count + "件の新着投稿");
                        feedNew.getLayoutParams().height = Utils.dpToPixel(this, baseLineHeight);
                        feedNew.requestLayout();
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendFindNew();
                    }
                }, findInterval);
            } else if (requestId == REQ_TYPE.FIND_NEW_FETCH.getId()) {
                // 新しいFeedを追加する
                addFeeds(result, FEED_DIRECTION.REVERSE);
                TextView feedNew = (TextView) findViewById(R.id.feed_new);
                feedNew.setText("");
                feedNew.getLayoutParams().height = 0;
                feedNew.requestLayout();
            } else if (requestId == REQ_TYPE.FIND_OLD_FETCH.getId()) {
                // 古いFeedを追加する
                addFeeds(result, FEED_DIRECTION.NORMAL);
                JSONArray feeds = result.getJSONArray("feeds");
                if(feeds.length() == 30) {
                    FeedInfo item = new FeedInfo();
                    item.setFeedId(-1);
                    item.setComment("もっと読む");
                    feedList.add(item);
                }
            } else if (requestId > REQ_TYPE.POST_IMAGE.getId()) {
                // 投稿画像の取得
                byte[] imageByte = Base64.decode(result.getString("data").getBytes(), Base64.DEFAULT);
                refreshListViewImage(requestId - REQ_TYPE.POST_IMAGE.getId(),
                        BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length));
            } else if (requestId > REQ_TYPE.FRIEND_ICON.getId()) {
                // 友達のアイコンの取得
                FriendsIconManager.getInstance().addIcon(requestId - REQ_TYPE.FRIEND_ICON.getId(), result.getString("data"));
                refreshListViewIcons();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * FeedのListViewのユーザアイコンを更新する
     */
    private void refreshListViewIcons() {
        synchronized (feedList) {
            FriendsIconManager iconManager = FriendsIconManager.getInstance();
            for (Iterator<FeedInfo> ite = feedList.iterator(); ite.hasNext(); ) {
                FeedInfo feed = ite.next();
                feed.setIconBitmap(iconManager.getIcon(feed.getUserId()));
            }
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
        }
    }

    /**
     * FeedのListViewの投稿画像を更新する
     */
    private void refreshListViewImage(int feedId, Bitmap image) {
        synchronized (feedList) {
            for (Iterator<FeedInfo> ite = feedList.iterator(); ite.hasNext(); ) {
                FeedInfo feed = ite.next();
                if (feedId == feed.getFeedId()) {
                    feed.setImageBitmap(image);
                    break;
                }
            }
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
        }
    }

    /**
     * FeedのListViewにFeedを追加する
     */
    private void addFeeds(JSONObject result, FEED_DIRECTION direction) {
        try {
            HashMap<Integer, Boolean> friendList = new HashMap<Integer, Boolean>();
            JSONArray feeds = result.getJSONArray("feeds");
            for (int i = 0; i < feeds.length(); ++i) {
                FeedInfo item = new FeedInfo();
                JSONObject feed = (JSONObject) feeds.get(i);
                item.setUserName(feed.getString("name"));
                item.setDate(feed.getString("created_at").replace("T", " ").replaceAll("\\..*$", ""));
                if (feed.getString("feed_type").equals("image")) {
                    AsyncHttpRequest request = new AsyncHttpRequest(this,
                            REQ_TYPE.POST_IMAGE.getId() + feed.getInt("id"),
                            AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                    request.execute("/images/" + feed.getString("image_file_name"));
                } else {
                    item.setComment(feed.getString("comment"));
                }
                item.setFeedId(feed.getInt("id"));
                item.setUserId(feed.getInt("user_id"));
                friendList.put(feed.getInt("user_id"), true);
                if (direction == FEED_DIRECTION.NORMAL) {
                    feedList.add(item);
                } else {
                    feedList.add(0, item);
                }
                if (lastFeedId < item.getFeedId()) {
                    lastFeedId = item.getFeedId();
                }
                if (oldFeedId > item.getFeedId()) {
                    oldFeedId = item.getFeedId();
                }
            }
            for (Iterator<Integer> ite = friendList.keySet().iterator(); ite.hasNext(); ) {
                int user_id = ite.next();
                if (FriendsIconManager.getInstance().getIcon(user_id) == null) {
                    AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.FRIEND_ICON.getId() + user_id, AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                    request.execute("/users/" + user_id + "/icon");
                }
            }

            ListView listView = (ListView) findViewById(R.id.feed_list);

            int height = 0;
            for (int i = 0; i < adapter.getCount(); i++) {
                View item = adapter.getView(i, null, listView);
                item.measure(0, 0);
                height += item.getMeasuredHeight();
            }
            ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
            layoutParams.height = height + (listView.getDividerHeight() * (adapter.getCount() - 1));
            listView.setLayoutParams(layoutParams);

            refreshListViewIcons();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * メニューを作成する
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Activityの戻りを処理する
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQ.IMAGE_UPLOAD.getId()) {
            getNewFeed();
        }
    }

    /**
     * 新しいFeedを取りに行く
     */
    private void getNewFeed() {
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.FIND_NEW_FETCH.getId(),
                AsyncHttpRequest.HTTP_METHOD.GET);
        httpRequest.execute("/feeds/" + lastFeedId + "/find_new?include_items=1");
    }

    /**
     * 古いFeedを取りに行く
     */
    private void getOldFeed() {
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(TimelineActivity.this, REQ_TYPE.FIND_OLD_FETCH.getId(),
                AsyncHttpRequest.HTTP_METHOD.GET);
        httpRequest.execute("/feeds/" + oldFeedId + "/find_old?include_items=1");
    }

    /**
     * ログアウトのためにローカルのデータを削除してActivityを終了する
     */
    private void logout() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(TimelineActivity.this);
        SharedPreferences.Editor editor = preference.edit();
        UserSessionInfo.getInstance().clear();

        editor.remove("name");
        editor.remove("icon_path");
        editor.remove("token");
        editor.remove("icon_data");
        editor.apply();

        Intent intent = new Intent(TimelineActivity.this, LoginActivity.class);
        startActivity(intent);
        TimelineActivity.this.finish();
    }
}
