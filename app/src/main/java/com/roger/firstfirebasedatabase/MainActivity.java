package com.roger.firstfirebasedatabase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ChildEventListener {
    private static final String DATA = "data";
    private static final String USERS = "users";
    private static final String ARTICLES = "articles";
    private static final String FRIENDS = "friends";
    private static final String TAG = "Fire base";
    private static final String MY_EMAIL = "rogertsai917@gmail.com";
    private static final String IS_FRIEND = "朋友";
    private static final String RECEIVED_FRIEND_REQUEST = "待確認";
    private static final String SEND_FRIEND_REQUEST = "待邀請";

    private EditText mSearchUserEditText;
    private TextView mSearchUserTextView;
    private TextView mFriendStateTextView;
    private Button mSearchUserButton;
    private Button mFriendAcceptButton;
    private Button mFriendRequestButton;
    private Button mFriendCancelButton;
    private Spinner mArticlesTagSpinner;
    private EditText mArticleTitleEditText;
    private EditText mArticleContentEditText;
    private Button mPostArticleButton;
    private ArrayAdapter<String> mTagListAdapter;
    private Spinner mSearchArticlesTagSpinner;
    private Spinner mSearchArticlesFriendSpinner;
    private Button mSearchArticleButton;
    private ArrayAdapter<String> mSearchTagListAdapter;
    private ArrayAdapter<String> mSearchFriendListAdapter;

    private String mCurrentUser;
    private String[] mArticleTags = {"八卦", "表特", "就可", "生活"};
    private String[] mSearchArticleTags = {"全部", "八卦", "表特", "就可", "生活"};
    private ArrayList<String> mSearchArticleFriends = new ArrayList<>();
    private String mCurrentPostArticleTag = mArticleTags[0];
    private String mCurrentSearchArticleTag = mSearchArticleTags[0];
    private String mCurrentSearchArticleFriend = "無";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchUserEditText = findViewById(R.id.editText_search_user);
        mSearchUserButton = findViewById(R.id.button_search_user);
        mSearchUserTextView = findViewById(R.id.textView_search_user);
        mFriendStateTextView = findViewById(R.id.textView_friend_state);
        mFriendAcceptButton = findViewById(R.id.button_friend_accept);
        mFriendRequestButton = findViewById(R.id.button_friend_request);
        mFriendCancelButton = findViewById(R.id.button_friend_cancel);
        mArticlesTagSpinner = findViewById(R.id.spinner_article_tag);
        mArticleTitleEditText = findViewById(R.id.editText_article_title);
        mArticleContentEditText = findViewById(R.id.editText_article_content);
        mPostArticleButton = findViewById(R.id.button_post_article);
        mSearchArticlesTagSpinner = findViewById(R.id.spinner_search_tag);
        mSearchArticlesFriendSpinner = findViewById(R.id.spinner_search_friend);
        mSearchArticleButton = findViewById(R.id.button_search_articles);
        mSearchArticleFriends.add("無");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFriendReference = mFirebaseDatabase.getReference(DATA).child(USERS).child(changeEmailToId(MY_EMAIL)).child(FRIENDS);
        mFriendReference.addChildEventListener(this);

        mSearchUserButton.setOnClickListener(this);
        mFriendAcceptButton.setOnClickListener(this);
        mFriendRequestButton.setOnClickListener(this);
        mFriendCancelButton.setOnClickListener(this);
        mPostArticleButton.setOnClickListener(this);
        mSearchArticleButton.setOnClickListener(this);

        mTagListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mArticleTags);
        mArticlesTagSpinner.setAdapter(mTagListAdapter);
        mArticlesTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentPostArticleTag = mArticleTags[position];
                Log.d(TAG, "onPostArticlesTagSelected: " + mCurrentPostArticleTag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSearchTagListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSearchArticleTags);
        mSearchArticlesTagSpinner.setAdapter(mSearchTagListAdapter);
        mSearchArticlesTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentSearchArticleTag = mSearchArticleTags[position];
                Log.d(TAG, "SearchTagSelected: " + mCurrentSearchArticleTag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mSearchFriendListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSearchArticleFriends);
        mSearchArticlesFriendSpinner.setAdapter(mSearchFriendListAdapter);
        mSearchArticlesFriendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentSearchArticleFriend = mSearchArticleFriends.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search_user:
                String email = mSearchUserEditText.getText().toString();
                searchUser(email);
                break;
            case R.id.button_friend_accept:
                acceptFriend();
                break;
            case R.id.button_friend_request:
                requestFriend();
                break;
            case R.id.button_friend_cancel:
                cancelFriend();
                break;
            case R.id.button_post_article:
                postArticle();
                break;
            case R.id.button_search_articles:
                searchArticles();
                break;
//            case R.id.button_get_user_data:
//                User user = new User("fff" + mCount, "fff@gmail.com");
//                mReference.child(USERS).child("fff").setValue(user);
//                mCount++;
//                break;
//            case R.id.button_post_article:
//                Author author = new Author("rogertsai917_gmail_com", "Roger", "rogertsai917@gmail.com");
//                Article article = new Article("哈囉", "哈囉哈囉!", "生活", author, "123");
//                String key = mReference.child(ARTICLES).push().getKey();
//                Log.d(TAG, "KEY: " + key);
//                mReference.child(ARTICLES).child(key).setValue(article);
//                mReference.child(USERS).child(MYEMAILNAME).child("articles").child(key).setValue(true);
//                break;
            default:
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String value = dataSnapshot.getKey();
        String state = (String) dataSnapshot.getValue();
        Log.d(TAG, "onChildAdded Key is: " + value);
        Log.d(TAG, "onChildAdded Value is: " + state);
        if (value != null && !value.equals("null")) {
            if (state != null && state.equals(IS_FRIEND)) {
                mSearchArticleFriends.add(value);
            }
            mCurrentUser = value;
            searchUser(mCurrentUser);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String value = dataSnapshot.getKey();
        String state = (String) dataSnapshot.getValue();
        mCurrentUser = value;
        Log.d(TAG, "onChildChanged Key is: " + value);
        searchUser(mCurrentUser);
        if (state != null && state.equals(IS_FRIEND)) {
            mSearchArticleFriends.add(value);
        } else {
            mSearchArticleFriends.remove(value);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String value = dataSnapshot.getKey();
        Log.d(TAG, "Removed Value is: " + value);
        searchUser(value);
        mSearchArticleFriends.remove(value);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public void searchUser(String id) {

        String searchId = changeEmailToId(id);

        DatabaseReference reference = mFirebaseDatabase.getReference(DATA).child(USERS);
        final String finalEmail = searchId;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = (String) dataSnapshot.child(finalEmail).child("name").getValue();
                if (userName == null) {
                    mSearchUserTextView.setText("找不到此用戶");
                    notFound();

                } else {
                    String userEmail = (String) dataSnapshot.child(finalEmail).child("email").getValue();
                    mCurrentUser = userEmail;
                    String result = userName + "  " + userEmail;
                    mSearchUserTextView.setText(result);
                    FriendState(finalEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addListenerForSingleValueEvent(valueEventListener);
    }

    public void FriendState(final String id) {
        if (id.equals(changeEmailToId(MY_EMAIL))) {
            myself();
        } else {
            DatabaseReference reference = mFirebaseDatabase.getReference(DATA).child(USERS).child(changeEmailToId(MY_EMAIL));
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = String.valueOf(dataSnapshot.child(FRIENDS).child(id).getValue());
                    Log.d(TAG, "value: " + value);
                    if (value == null || value.equals("null")) {
                        notFriend();
                    } else {
                        switch (value) {
                            case IS_FRIEND:
                                isFriend();
                                break;
                            case RECEIVED_FRIEND_REQUEST:
                                receivedFriendRequest();
                                break;
                            case SEND_FRIEND_REQUEST:
                                sendFriendRequest();
                                break;
                            default:
                                notFriend();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            reference.addListenerForSingleValueEvent(valueEventListener);
        }
    }

    private void acceptFriend() {
        DatabaseReference reference = mFirebaseDatabase.getReference(DATA).child(USERS);
        reference.child(changeEmailToId(mCurrentUser)).child(FRIENDS).child(changeEmailToId(MY_EMAIL)).setValue(IS_FRIEND);
        reference.child(changeEmailToId(MY_EMAIL)).child(FRIENDS).child(changeEmailToId(mCurrentUser)).setValue(IS_FRIEND);
        searchUser(mCurrentUser);
    }

    private void requestFriend() {
        DatabaseReference reference = mFirebaseDatabase.getReference(DATA).child(USERS);
        reference.child(changeEmailToId(mCurrentUser)).child(FRIENDS).child(changeEmailToId(MY_EMAIL)).setValue(RECEIVED_FRIEND_REQUEST);
        reference.child(changeEmailToId(MY_EMAIL)).child(FRIENDS).child(changeEmailToId(mCurrentUser)).setValue(SEND_FRIEND_REQUEST);
        searchUser(mCurrentUser);
    }

    private void cancelFriend() {
        DatabaseReference reference = mFirebaseDatabase.getReference(DATA).child(USERS);
        reference.child(changeEmailToId(mCurrentUser)).child(FRIENDS).child(changeEmailToId(MY_EMAIL)).removeValue();
        reference.child(changeEmailToId(MY_EMAIL)).child(FRIENDS).child(changeEmailToId(mCurrentUser)).removeValue();
        searchUser(mCurrentUser);
    }

    public void notFound() {
        mFriendStateTextView.setText("");
        mFriendAcceptButton.setVisibility(View.INVISIBLE);
        mFriendRequestButton.setVisibility(View.INVISIBLE);
        mFriendCancelButton.setVisibility(View.INVISIBLE);
    }

    public void myself() {
        mFriendStateTextView.setText("本人");
        mFriendAcceptButton.setVisibility(View.INVISIBLE);
        mFriendRequestButton.setVisibility(View.INVISIBLE);
        mFriendCancelButton.setVisibility(View.INVISIBLE);
    }

    public void notFriend() {
        mFriendStateTextView.setText("不是朋友");
        mFriendAcceptButton.setVisibility(View.INVISIBLE);
        mFriendRequestButton.setVisibility(View.VISIBLE);
        mFriendCancelButton.setVisibility(View.INVISIBLE);
    }

    public void sendFriendRequest() {
        mFriendStateTextView.setText("已發送好友邀請");
        mFriendAcceptButton.setVisibility(View.INVISIBLE);
        mFriendRequestButton.setVisibility(View.INVISIBLE);
        mFriendCancelButton.setVisibility(View.VISIBLE);
    }

    public void receivedFriendRequest() {
        mFriendStateTextView.setText("已收到好友邀請");
        mFriendAcceptButton.setVisibility(View.VISIBLE);
        mFriendRequestButton.setVisibility(View.INVISIBLE);
        mFriendCancelButton.setVisibility(View.VISIBLE);
    }

    public void isFriend() {
        mFriendStateTextView.setText("朋友");
        mFriendAcceptButton.setVisibility(View.INVISIBLE);
        mFriendRequestButton.setVisibility(View.INVISIBLE);
        mFriendCancelButton.setVisibility(View.VISIBLE);
    }

    public String changeEmailToId(String email) {
        return email.replace('@', '_').replace('.', '_');
    }

    public static String getRemoveEmail(String input) {
        input = input.replace(" ", "").replace("{","").replace("}","");
        int equalIndex = input.indexOf(',');
        return input.substring(0, equalIndex-1);
    }

    private void postArticle() {
        String title = mArticleTitleEditText.getText().toString();
        String content = mArticleContentEditText.getText().toString();
        if (!title.equals("") && !content.equals("")) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DATA);
            Author author = new Author(changeEmailToId(MY_EMAIL), "Roger", MY_EMAIL);
            Long currentTime = System.currentTimeMillis() / 1000;
            Article article = new Article(title, content, mCurrentPostArticleTag, author, String.valueOf(currentTime));
            String key = reference.child(ARTICLES).push().getKey();
            Log.d(TAG, "KEY: " + key);
            reference.child(ARTICLES).child(key).setValue(article);
            reference.child(USERS).child(changeEmailToId(MY_EMAIL)).child("articles").child(key).setValue(true);
        } else {
            Toast.makeText(this, "title or content can not be empty", Toast.LENGTH_LONG).show();
        }
        mArticleTitleEditText.setText("");
        mArticleContentEditText.setText("");
    }

    private void searchArticles() {
        if (mCurrentSearchArticleTag.equals(mSearchArticleTags[0]) && mCurrentSearchArticleFriend.equals("無")) {
            Log.d(TAG, "search type : searchAllArticles");
            searchAllArticles();
        } else if(!mCurrentSearchArticleTag.equals(mSearchArticleTags[0]) && mCurrentSearchArticleFriend.equals("無")) {
            Log.d(TAG, "search type : searchArticlesByTag");
            searchArticlesByTag();
        } else if (mCurrentSearchArticleTag.equals(mSearchArticleTags[0]) && !mCurrentSearchArticleFriend.equals("無")) {
            Log.d(TAG, "search type : searchArticlesByFriend");
            searchArticlesByFriend();
        } else {
            Log.d(TAG, "search type : searchArticlesByTagAndFriend");
            searchArticlesByTagAndFriend();
        }
    }

    private void searchAllArticles() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DATA).child(ARTICLES);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String title = (String) singleSnapshot.child("article_title").getValue();
                    Log.d(TAG, "articles titles are: " + title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    private void searchArticlesByTag() {
        Query query = FirebaseDatabase.getInstance().getReference(DATA).child(ARTICLES).orderByChild("article_tag").equalTo(mCurrentSearchArticleTag);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String title = (String) singleSnapshot.child("article_title").getValue();
                    Log.d(TAG, "articles titles are: " + title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private void searchArticlesByFriend() {
        Query query = FirebaseDatabase.getInstance().getReference(DATA).child(ARTICLES).orderByChild("author/id").equalTo(mCurrentSearchArticleFriend);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String title = (String) singleSnapshot.child("article_title").getValue();
                    Log.d(TAG, "articles titles are: " + title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private void searchArticlesByTagAndFriend() {
        Query query = FirebaseDatabase.getInstance().getReference(DATA).child(ARTICLES).orderByChild("author/id").equalTo(mCurrentSearchArticleFriend);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String tag = (String) singleSnapshot.child("article_tag").getValue();
                    if (tag.equals(mCurrentSearchArticleTag)) {
                        String title = (String) singleSnapshot.child("article_title").getValue();
                        Log.d(TAG, "articles titles are: " + title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }
}
