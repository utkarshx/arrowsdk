package com.btpltech.bankingbot.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.btpltech.bankingbot.Adapter.AdapterChatList;
import com.btpltech.bankingbot.Adapter.SideMenuAdaper;
import com.btpltech.bankingbot.Adapter.TopMenuAdapter;
import com.btpltech.bankingbot.Model.AppConfiguration;
import com.btpltech.bankingbot.Model.IntitialResponse;
import com.btpltech.bankingbot.Model.NavItem;
import com.btpltech.bankingbot.Model.RequestParams;
import com.btpltech.bankingbot.Model.TopMenu;
import com.btpltech.bankingbot.Model.botInitial;
import com.btpltech.bankingbot.Model.buttonPayload;
import com.btpltech.bankingbot.Model.message;
import com.btpltech.bankingbot.Model.messageData;
import com.btpltech.bankingbot.Model.postback;
import com.btpltech.bankingbot.Model.sender;
import com.btpltech.bankingbot.R;
import com.btpltech.bankingbot.SQliteData.ChatListData;
import com.btpltech.bankingbot.SQliteData.SQLiteAdapter;
import com.btpltech.bankingbot.util.AppController;
import com.btpltech.bankingbot.util.CommonUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.Context;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import pl.droidsonroids.gif.GifImageView;

public class ChatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String email, mobile;
    private ValueEventListener mConnectedListener;
    private AdapterChatList mChatListAdapter;
    private ScrollView scroll;
    private String appId;
    private JSONArray bots;
    private JSONArray sideMenu;
    String input;
    Chat chat;
    List<Chat> chatList;
    ChatListData chatListData;
    private SQLiteAdapter sqLiteAdapter;
    ListView listViewChat;
    EditText inputText;
    String botName;
    String title;
    String openType;
    String text;
    String endExistingFlow;
    GifImageView imgLoading;
    String initialGreeting;
    TopMenu menu;
    List<TopMenu> menuArrayList;
    TopMenuAdapter topMenuAdapter;
    GridView topMenueGrid;
    private String botId;
    private String botImage;
    private String getBotName;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    ArrayList<NavItem> menuItem;
    NavItem sideMenuItem;
    private SideMenuAdaper mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    //firebase implimantion
    private String mActivityTitle;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private String userId;
    private JSONObject defaultbBot;
    private String mUsername;
    private Firebase mFirebaseRef;
    String bot;
    String sideMenus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieHandler.setDefault(new CookieManager());
        setContentView(R.layout.activity_chat);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FrameLayout f = (FrameLayout) findViewById(R.id.frameLayout);
                        f.setVisibility(View.VISIBLE);
                    }
                }
        );
        AppConfiguration appConfiguration = new AppConfiguration();

        menuItem = new ArrayList<>();
        topMenueGrid = (GridView) findViewById(R.id.gridTop);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setIcon(R.drawable.home);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        listViewChat = (ListView) findViewById(R.id.list);
        setupUsername();
        sqLiteAdapter = new SQLiteAdapter(this);
        chatListData = new ChatListData(sqLiteAdapter);
        chatList = new ArrayList<Chat>();
        menuArrayList = new ArrayList<TopMenu>();
        imgLoading = (GifImageView) findViewById(R.id.imgLoading);
        checkNetwork();
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        setupDrawer();
        try {
            bots = new JSONArray(bot);
            sideMenu = new JSONArray(sideMenus);
        } catch (Exception e) {

        }
        BindTopMenu();
        addDrawerItems();

        Random r = new Random();
        Intent intent = getIntent();
        botName = "Bhaiya Ji";

        getSupportActionBar().setTitle("Bhaiya Ji");
        //bindList();
        inputText = (EditText) findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String message = inputText.getText().toString();
                    sendMessage(message, false, null);
                    inputText.setText("");
                }
                return true;
            }
        });
        findViewById(R.id.sendButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = inputText.getText().toString();
                        sendMessage(message, false, null);
                        inputText.setText("");
                    }
                });

        if (chatList.size() <= 0) {
            //getBotInitials();
        }
        mChatListAdapter = new AdapterChatList(ChatActivity.this, chatList, mUsername);
        listViewChat.setAdapter(mChatListAdapter);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("outGoingMessage/" + appId + "/users/" + userId + "/messages");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                String str = s;
                JSONObject responseObj = new JSONObject();
                JSONObject messageData = new JSONObject();
                JSONObject message = new JSONObject();
                dataSnapshot.getChildren();
                // RequestParams response = dataSnapshot.getValue(RequestParams.class);
                Gson gson = new Gson();
                String responseJson = gson.toJson(dataSnapshot.getValue());
                try {
                    responseObj = new JSONObject(responseJson);
                    Log.d("My App", responseObj.toString());
                } catch (Throwable t) {
                }
                if (responseObj.has("message")) {
                    processResponse(responseJson);
                } else if (responseObj.has("messageData")) {
                    try {
                        messageData = responseObj.getJSONObject("messageData");
                        String from = "";
                        String chatText = "";
                        Chat chat = null;
                        from = mUsername;
                        if (messageData.has("message")) {
                            message = messageData.getJSONObject("message");
                            chatText = message.getString("text");
                            if (chatText != null && !chatText.trim().equals("")) {
                                chat = new Chat(mUsername, botId, chatText, from, getTime(), null, "");
                                chatList.add(chat);
                            }
                        }
                    } catch (Throwable t) {
                    }
                } else {
                    if (dataSnapshot.hasChild("start")) {
                        String startVal = dataSnapshot.child("start").getValue().toString();
                        if (startVal == "1") {
                            return;
                        } else {
                            IntitialResponse res = dataSnapshot.getValue(IntitialResponse.class);
                            Gson gso = new Gson();
                            String resJson = gso.toJson(res);
                            bindInitialGreatings(resJson);
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
                setFocusLastElem();
            }

            @Override
            public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(String chatText, final boolean botResend, JSONObject payloadParam) {
        if (chatText.equals("") && !botResend) {
            return;
        }
        try {

            if (payloadParam != null) {
                String variable = "", value = "";
                buttonPayload payload = new buttonPayload();
                postback postback = new postback();
                message m = new message();
                variable = payloadParam.getString("variable");
                value = payloadParam.getString("value");
                payload.setVariable(variable);
                payload.setValue(value);
                postback.setPayload(payload);
                m.setPostback(postback);
                sender s = new sender(userId);
                messageData mdata = new messageData(m, s);
                RequestParams req = new RequestParams(botId, appId, null, mdata, null, s, false, true, Long.valueOf(getTime()));
                myRef = database.getReference("inComingMessage");
                myRef.push().setValue(req);

            } else {
                message m = new message();
                m.setText(chatText);
                sender s = new sender(userId);
                messageData mdata = new messageData(m, s);
                RequestParams req = new RequestParams(botId, appId, null, mdata, null, s, false, true, Long.valueOf(getTime()));
                myRef = database.getReference("inComingMessage");
                myRef.push().setValue(req);
            }
        } catch (Exception e) {

        }


    }

    void bindInitialGreatings(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);
            JSONObject data = responseJson.getJSONObject("start");
            String suggestionArray = null;
            if (data.has("suggestions")) {
                suggestionArray = data.getString("suggestions");

            }
            if (data.has("initialGreeting")) {
                initialGreeting = data.getString("initialGreeting");
            }
            if (initialGreeting != null && initialGreeting.length() > 0) {
                chat = new Chat(mUsername, botId, initialGreeting, botName, getTime(), null, "");
                chatList.add(chat);
                chat = new Chat(mUsername, botId, suggestionArray, botName, getTime(), null, "list");
                chatList.add(chat);
            }
            mChatListAdapter = new AdapterChatList(ChatActivity.this, chatList, mUsername);
            listViewChat.setAdapter(mChatListAdapter);
            mChatListAdapter.notifyDataSetChanged();
            imgLoading.setVisibility(View.GONE);
            setFocusLastElem();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void processResponse(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);
            String callAgain = "", text = "", noResponse = "";
            attachmentJson = null;
            if (responseJson.has("message")) {
                JSONObject messageJson = responseJson.getJSONObject("message");
                if (messageJson.has("callAgain")) {
                    callAgain = messageJson.getString("callAgain");
                }
                if (messageJson.has("noResponse")) {
                    noResponse = messageJson.getString("noResponse");
                }
                if (messageJson.has("text")) {
                    text = messageJson.getString("text");
                }
                if (messageJson.has("attachment")) {
                    attachmentJson = new JSONObject();
                    attachmentJson = messageJson.getJSONObject("attachment");
                }
                if (!noResponse.toLowerCase().equals("true")) {
                    chat = new Chat(mUsername, botId, text, botName, getTime(), attachmentJson, "");
                    chatList.add(chat);
                }
                if (callAgain != null && callAgain.toLowerCase().equals("true")) {
                    // sendMessage("", true, null);
                } else {
                    RelativeLayout lytSuggestion = (RelativeLayout) findViewById(R.id.lytSuggestion);
                    if (messageJson.has("suggestion")) {

                        String suggestion = messageJson.getString("suggestion");
                        lytSuggestion.setVisibility(View.VISIBLE);
                        TextView textSuggestionMessage = (TextView) findViewById(R.id.textSuggestionMessage);
                        textSuggestionMessage.setText(suggestion);
                    } else {
                        lytSuggestion.setVisibility(View.GONE);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getBotInitials() {
        sender s = new sender(userId);
        botInitial star = new botInitial(appId, botId, 1, s, false, true, Long.valueOf(getTime()));
        myRef = database.getReference("inComingMessage");
        myRef.push().setValue(star);
    }


    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void addDrawerItems() {
       /* menuItem
          mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuItems);
  		mDrawerList.setAdapter(mAdapter);
  		*/
        try {

            if (sideMenu.get(1).equals(null)) {
                return;
            } else {
                for (int i = 0; i < sideMenu.length(); i++) {
                    try {
                        JSONObject jsonObject = (JSONObject) sideMenu.get(i);
                        botId = jsonObject.getString("bot_id");
                        title = jsonObject.getString("title");
                        if (jsonObject.has("text")) {
                            text = jsonObject.getString("text");
                        }
                        openType = jsonObject.getString("open_type");
                        endExistingFlow = jsonObject.getString("endExistingFlow");
                        sideMenuItem = new NavItem(title, openType, botId, text, endExistingFlow);
                        menuItem.add(sideMenuItem);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {

        }
        mAdapter = new SideMenuAdaper(ChatActivity.this, menuItem);
        mDrawerList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(mDrawerList);
                String bot_id = menuItem.get(position).getBot_id();
                title = menuItem.get(position).getTitle();
                text = menuItem.get(position).getText();
                openType = menuItem.get(position).getOpen_type();
                endExistingFlow = menuItem.get(position).getEndExistingFlow();
                botId = bot_id;
                if (openType.toLowerCase() == "bot") {
                    getBotInitials();
                    return;
                }
                sendMessage(text, false, null);
            }
        });
    }

    public void saveSharedPref(String name) {
        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", name);
        editor.commit();
    }

    MenuItem menuItems;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuItems = menu.findItem(R.id.action_arrowup);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            FrameLayout f = (FrameLayout) findViewById(R.id.frameLayout);
            f.setVisibility(View.VISIBLE);
            listViewChat.setEmptyView(new View(ChatActivity.this));
        }
        if (id == R.id.action_logout) {
            saveSharedPref("");
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_arrowup) {
            FrameLayout f = (FrameLayout) findViewById(R.id.frameLayout);
            if (f.getVisibility() == View.VISIBLE) {
                f.setVisibility(View.GONE);
                menuItems.setIcon(R.drawable.arrowup);
            } else {
                f.setVisibility(View.VISIBLE);
                menuItems.setIcon(R.drawable.arrowdown);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private void checkNetwork() {
        RelativeLayout noconnection = (RelativeLayout) findViewById(R.id.noconnection);
        if (!CommonUtil.CheckInternet(getApplicationContext())) {

            noconnection.setVisibility(View.VISIBLE);
        } else {
            noconnection.setVisibility(View.GONE);
        }
    }

    private void setupUsername() {
        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
        mUsername = prefs.getString("username", null);
        email = prefs.getString("email", null);
        mobile = prefs.getString("mobile", null);
        userId = prefs.getString("userId", null);
        bot = prefs.getString("bots", null);
        sideMenus = prefs.getString("sideMenu", null);
        appId = prefs.getString("appId", null);
        if (mUsername == null) {
            Random r = new Random();
            prefs.edit().putString("username", mUsername).commit();
        }
    }



    private void bindList() {
//    chatList.clear();
        sqLiteAdapter.openToRead();
        Cursor cursor = chatListData.getAllChatListData();
        if (cursor == null) {
            return;
        }
        String chatbot;
        String chatuser;
        String text;
        String from;
        String time;
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                chatbot = cursor.getString(cursor.getColumnIndex("chatbot"));
                chatuser = cursor.getString(cursor.getColumnIndex("chatuser"));
                if (chatbot != null && chatbot.toLowerCase().equals(botId.toLowerCase()) && chatuser.toLowerCase().equals(mUsername.toLowerCase())) {
                    text = cursor.getString(cursor.getColumnIndex("text"));
                    from = cursor.getString(cursor.getColumnIndex("fromUser"));
                    time = cursor.getString(cursor.getColumnIndex("time"));
                    chat = new Chat(chatuser, chatbot, text, from, time, null, "");
                    chatList.add(chat);
                    mChatListAdapter = new AdapterChatList(getApplicationContext(), chatList, mUsername);
                    listViewChat.setAdapter(mChatListAdapter);
                }
                cursor.moveToNext();
            }
        }
        // mChatListAdapter = new AdapterChatList(null, ChatActivity.this, R.layout.activity_chat,chat,mUsername);
        cursor.close();
        sqLiteAdapter.close();
        setFocusLastElem();
    }

    private void setFocusLastElem() {
        listViewChat.setSelection(chatList.size() - 1);

    }

    Chat suggestionChat;
    JSONObject payload;
    JSONObject attachmentJson;

    public void showCardDiscriopion(String cardName, String cardDescription) {

        if (chatList != null && chatList.size() > 0) {
            chat = chatList.get(chatList.size() - 1);
        }

        if (chat != null && chat.getType().equals("card_detail")) {
            chatList.remove(chatList.size() - 1);
        }

        chat = new Chat(cardName, cardDescription, "", "", "", null, "card_detail");
        chatList.add(chat);

        mChatListAdapter = new AdapterChatList(ChatActivity.this, chatList, mUsername);
        listViewChat.setAdapter(mChatListAdapter);

        setFocusLastElem();
    }

    public void BindTopMenu() {

        for (int i = 0; i < bots.length(); i++) {
            try {
                JSONObject jsonObject = (JSONObject) bots.get(i);
                botId = jsonObject.getString("bot_id");
                //botImage = jsonObject.getString("image");
                title = jsonObject.getString("bot_text");
                menu = new TopMenu(botId, botImage, title);
                menuArrayList.add(menu);
            } catch (Exception e) {
            }
        }
        topMenuAdapter = new TopMenuAdapter(ChatActivity.this, menuArrayList);
        topMenueGrid.setAdapter(topMenuAdapter);
        topMenuAdapter.notifyDataSetChanged();

    }

    private String getTime() {
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        return ts;
    }

    private String getStringFromArray(JSONArray array) {
        String s = "";
        try {
            for (int i = 0; i < array.length(); i++) {
                s += String.valueOf(array.get(i) + "/n");
            }
        } catch (JSONException e) {
            e.getStackTrace();
        }
        return s;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.accountManagement) {
            AccountManagement();

        } else if (id == R.id.transferMoney) {
            RechargeAndPayments();

        } else if (id == R.id.search_card) {
            searchCard();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(mDrawerList);
        return false;
    }

    public void setBotId(String botsId) {
        botId = botsId;
        bindWidget("");
    }

    public void AccountManagement() {
        botId = "574c4e563447c94e0c8b4568";
        bindWidget("Basic Account Management");
    }

    void RechargeAndPayments() {
        botId = "574e85ae3447c9561c8b4567";
        bindWidget("Recharge and Payment");
    }

    void searchCard() {
        //appId = "574c4ba63447c94e0c8b4567";
        botId = "5750230f3447c9e6168b4567";
        bindWidget("Search Card");
    }

    void support() {
        botId = "575530df3447c9810d8b4567";
        bindWidget("Search Card");
    }


    public void bindWidget(String type) {
       getBotInitials();
    }

    public int getTotalHeightofListView(ListView listView) {

        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();
            Log.w("HEIGHT" + i, String.valueOf(totalHeight));

        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
        return totalHeight;

    }

}