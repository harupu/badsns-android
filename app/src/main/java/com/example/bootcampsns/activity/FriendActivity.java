package com.example.bootcampsns.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bootcampsns.R;
import com.example.bootcampsns.adapter.FriendAdapter;
import com.example.bootcampsns.adapter.FriendInfo;
import com.example.bootcampsns.util.AsyncHttpRequest;
import com.example.bootcampsns.util.FriendsIconManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class FriendActivity extends BaseActivity {

    /**
     * HTTPリクエストのタイプ
     */
    private enum REQ_TYPE {
        LIST(0),            // 友達一覧取得
        ADD(1),             // 友達追加
        ICON(1000000000);   // 友達のアイコン取得
        private final int id;
        private REQ_TYPE(final int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
    };

    /**
     * Feedを表示する部分のリスト
     */
    private FriendAdapter adapter = null;
    private ArrayList<FriendInfo> friendList = new ArrayList<>();
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        /** 登録ボタン押下時のアクション設定 */
        final EditText edit = (EditText) findViewById(R.id.friend_add);
        Button registerButton = (Button) findViewById(R.id.friend_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncHttpRequest httpRequest = new AsyncHttpRequest(FriendActivity.this, REQ_TYPE.ADD.getId());
                String login_id = edit.getText().toString();
                httpRequest.addParam("login_id", login_id);
                httpRequest.execute("/friends");
            }
        });

        /** キャンセルボタン押下時のアクション設定 */
        Button cancelButton = (Button) findViewById(R.id.friend_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendActivity.this.finish();
            }
        });

        // 友達一覧の表示場所の設定
        listView = (ListView) findViewById(R.id.friend_list);
        adapter = new FriendAdapter(this);
        adapter.setFriendList(friendList);
        listView.setAdapter(adapter);

        // 友達一覧の取得開始
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(FriendActivity.this, REQ_TYPE.LIST.getId(),
                AsyncHttpRequest.HTTP_METHOD.GET);
        httpRequest.execute("/friends");
    }

    @Override
    public void asyncHttpCallback(JSONObject result, int requestId) {
        try {
            if (requestId == REQ_TYPE.LIST.getId()) {
                // 友達一覧
                HashMap<Integer, Boolean> friendMap = new HashMap<Integer, Boolean>();
                JSONArray friends = result.getJSONArray("friends");
                for (int i = 0; i < friends.length(); ++i) {
                    FriendInfo item = new FriendInfo();
                    JSONObject friend = (JSONObject) friends.get(i);
                    item.setUserId(friend.getInt("id"));
                    item.setUserName(friend.getString("name"));
                    friendMap.put(friend.getInt("id"), true);
                    friendList.add(item);
                }
                // 取得していない友達のアイコンを取得する
                for (Iterator<Integer> ite = friendMap.keySet().iterator(); ite.hasNext(); ) {
                    int user_id = ite.next();
                    if (FriendsIconManager.getInstance().getIcon(user_id) == null) {
                        AsyncHttpRequest request = new AsyncHttpRequest(this, REQ_TYPE.ICON.getId() + user_id, AsyncHttpRequest.HTTP_METHOD.GET, AsyncHttpRequest.RESPONSE_TYPE.IMAGE);
                        request.execute("/users/" + user_id + "/icon");
                    }
                }
                refreshListViewIcons();
            } else if (requestId == REQ_TYPE.ADD.getId()) {
                // 友達追加
                if(result.has("errors")) {
                    Toast.makeText(this, result.getJSONArray("errors").getString(0), Toast.LENGTH_LONG).show();
                }else {// エラーが発生していなければ追加成功。Activityを終了
                    FriendActivity.this.finish();
                }
            } else if (requestId > REQ_TYPE.ICON.getId()) {
                // 友達のアイコンの取得
                FriendsIconManager.getInstance().addIcon(requestId - REQ_TYPE.ICON.getId(), result.getString("data"));
                refreshListViewIcons();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** 友達一覧のViewを描画し直す */
    private void refreshListViewIcons() {
        synchronized (friendList) {
            FriendsIconManager iconManager = FriendsIconManager.getInstance();
            for (Iterator<FriendInfo> ite = friendList.iterator(); ite.hasNext(); ) {
                FriendInfo friend = ite.next();
                friend.setIconBitmap(iconManager.getIcon(friend.getUserId()));
            }
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
        }
    }
}
