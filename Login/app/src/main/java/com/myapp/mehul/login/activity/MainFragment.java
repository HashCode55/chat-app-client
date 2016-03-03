package com.myapp.mehul.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.mehul.login.MainActivity;
import com.myapp.mehul.login.R;
import com.myapp.mehul.login.app.Constants;

import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * A chat fragment containing messages view and input form.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private static final int REQUEST_LOGIN = 0;

    private static final int TYPING_TIMER_LENGTH = 600;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private String opponentId;
    private String you;
    private String word;
    private String opponentWord;
    private Socket mSocket;
    private CountDownTimer countDown;
    private CountDownTimer countDownDisplay;
    private CountDownTimer countDownDisplay2;
    private long seconds;
    private long youWordDeployed = -1;
    private long opponentWordDeployed = -1;
    private long youWordGuessed = -1;
    private long opponentWordGuessed = -1;
    private String opponentName;
    private String finResult;
    private TextView timerText;
    private int tries = 5;
    private boolean flag = true;
    private long diff;
    private boolean oppoguessed = true;

    public MainFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(activity, mMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        {
            try {
                mSocket = IO.socket(Constants.CHAT_SERVER_URL);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /*countDown = new CountDownTimer(180000, 1000) {

            public void onTick(long millisUntilFinished)
            {
                seconds = millisUntilFinished;
            }

            public void onFinish() {
                //Intent i = new Intent(getActivity().getApplicationContext(), GameOver.class);
                //startActivity(i);
                //getActivity().finish();
            }
        }.start();*/

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("word deployed", onWordDeployed);
        mSocket.on("word guessed", onWordGuessed);
        mSocket.on("result", result);
        mSocket.on("deploy word first", deployWordFirst);
        mSocket.on("no more tries", noMoreTries);
        //mSocket.on("opponent", onOpponent);
        mSocket.connect();

        //startSignIn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JSONObject discon = new JSONObject();
        try {
            discon.put("opponent", opponentId);
            discon.put("you", you);
            discon.put("where", "MainFragment onDestroy");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSocket.emit("discon", discon);
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("word deployed", onWordDeployed);
        mSocket.off("word guessed", onWordGuessed);
        mSocket.off("result", result);
        mSocket.off("deploy word first", deployWordFirst);
        mSocket.off("no more tries", noMoreTries);

    }
    //public void onResume(){
     //   Intent intent = new Intent()
    //}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);
        mUsername  = getActivity().getIntent().getExtras().getString("username");
        opponentId = getActivity().getIntent().getExtras().getString("opponentId");
        word = getActivity().getIntent().getExtras().getString("word");
        opponentName = getActivity().getIntent().getExtras().getString("opponentName");
        opponentWord = getActivity().getIntent().getExtras().getString("opponentWord");
        Log.d(TAG, opponentWord + " " + opponentId);
        you = getActivity().getIntent().getExtras().getString("you");
        addLog(getResources().getString(R.string.message_welcome));

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    JSONObject typin = new JSONObject();
                    try {
                        typin.put("opponent", opponentId);
                        typin.put("you", you);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mSocket.emit("typing", typin);
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivity", "Result");
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            Log.d("result failed!", "YES IT FAILED");

            getActivity().finish();
            return;
        }

        mUsername = data.getStringExtra("username");

        addLog(getResources().getString(R.string.message_welcome));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.timer, menu);
        MenuItem timerItem = menu.findItem(R.id.break_timer);
        timerText = (TextView) MenuItemCompat.getActionView(timerItem);

        timerText.setPadding(10, 0, 10, 0);
        startTimer();
        //timerText.setText("" + (180 - seconds / 1000));

    }


    public void startTimer(){
        countDownDisplay = new CountDownTimer( 60*60 * 1000, 1000) {

            public void onTick(long millisUntilFinished)
            {
                seconds = millisUntilFinished;
                //timerText.setText("" + (180 - seconds / 1000));
            }

            public void onFinish() {
                //now if 3 minutes get over, game over will be emitted and hence we'll get to the result screen.
                JSONObject times = new JSONObject();
                try{
                    times.put("you", you);
                    times.put("opponent", opponentId);
                    times.put("youWordDeployed" ,youWordDeployed);
                    times.put("opponentWordDeployed" , opponentWordDeployed);
                    times.put("youWordGuessed", youWordGuessed);
                    times.put("opponentWordGuessed", opponentWordGuessed);

                }
                catch(JSONException e){
                    Log.d(TAG, "opponent - Exception in onwordguessed");
                    return;
                }
                Log.d(TAG, "opponent emitting 'times' JSON...");
                mSocket.emit("game over", times);

            }
        }.start();
    }

    public void startTimer2(){
        Log.d(TAG, "2nd timer started");
        countDownDisplay2 = new CountDownTimer( diff, 1000) {

            public void onTick(long millisUntilFinished)
            {
                timerText.setText("" + millisUntilFinished/1000);
            }

            public void onFinish() {
                //now if 3 minutes get over, game over will be emitted and hence we'll get to the result screen.
                JSONObject times = new JSONObject();
                try{
                    times.put("you", you);
                    times.put("opponent", opponentId);
                    times.put("youWordDeployed" ,youWordDeployed);
                    times.put("opponentWordDeployed" , opponentWordDeployed);
                    times.put("youWordGuessed", youWordGuessed);
                    times.put("opponentWordGuessed", opponentWordGuessed);

                }
                catch(JSONException e){
                    Log.d(TAG, "opponent - Exception in onwordguessed");
                    return;
                }
                Log.d(TAG, "opponent emitting 'times' JSON...");
                mSocket.emit("game over", times);

            }
        }.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //check later
        //if (id == R.id.action_leave) {
        //    leave();
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }


    private void addMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addOpponentMessage(String username, String message) {
        username = opponentName;
        mMessages.add(new Message.Builder(Message.TYPE_OPPONENT_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }



    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }
        JSONObject jsonobj = new JSONObject();

        try {
                jsonobj.put("you", you);
                jsonobj.put("message", message);
                jsonobj.put("guessFlag", flag);
                jsonobj.put("youWordDeployed", youWordDeployed);
                jsonobj.put("opponent", opponentId);
                jsonobj.put("word", word);
                jsonobj.put("opponentWord", opponentWord);


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        mInputMessageView.setText("");
        addMessage(mUsername, message);

        // perform the sending message attempt.
        mSocket.emit("new message", jsonobj);

    }





    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("opponent"); //data.getString("opponent");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);
                    addOpponentMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("opponent");
                        //numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                }
            });
        }
    };


    private Emitter.Listener onUserLeft = new Emitter.Listener() {


        @Override
        public void call(final Object... args) {

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Intent i = new Intent(getActivity() ,MainMenu.class);
                    startActivity(i);
                    getActivity().finish();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("opponent");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(opponentName);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("opponent");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(opponentName);
                }
            });
        }
    };


    ////////////////////////////////////////////////////////////////////
    ////////////////////////ONLY LISTENERS AHEAD////////////////////////


    private Emitter.Listener onWordDeployed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    Log.d(TAG, data.toString() + " onworddeployed listener working....");
                    try {
                        System.out.println("the value is" + data.getString("who").equals(you));
                        if (data.getBoolean("wordDeployed")) {
                            if(data.getString("who").equals(you)){
                                youWordDeployed = seconds;
                                Log.d(TAG, "the seconds are " + youWordDeployed);
                                Toast.makeText(getActivity().getApplicationContext(), "Word successfully deployed, PROTECT your word", Toast.LENGTH_LONG).show();
                            }
                            else{
                                opponentWordDeployed = seconds;
                                Log.d(TAG, "the seconds are " + opponentWordDeployed);
                            }
                        }
                    } catch(JSONException e){
                        Log.d(TAG, "Json exception");
                        return;
                    }
                }
            });
        }
    };



    private Emitter.Listener onWordGuessed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    Log.d(TAG, "resp received in wordguessed " + data.toString());
                    try {
                        if (data.getBoolean("wordGuessed")) {
                            if(data.getString("who").equals(you)){
                                Toast.makeText(getActivity().getApplicationContext(), "word successfully guessed!", Toast.LENGTH_LONG).show();
                                youWordGuessed = seconds;
                                System.out.println("the time at which word was guessed is " + youWordGuessed);
                                    Log.d(TAG, "its not -1");
                                if(youWordDeployed > opponentWordDeployed) {
                                    Log.d(TAG, "you word deployed greater than opponent");
                                    JSONObject times = new JSONObject();
                                    try {
                                        times.put("you", you);
                                        times.put("opponent", opponentId);
                                        times.put("youWordDeployed", youWordDeployed);
                                        times.put("opponentWordDeployed", opponentWordDeployed);
                                        times.put("youWordGuessed", youWordGuessed);
                                        times.put("opponentWordGuessed", opponentWordGuessed);
                                        Log.d(TAG, times.toString());

                                    } catch (JSONException e) {
                                        Log.d(TAG, "you - Exception in onwordguessed");
                                        return;
                                    }
                                    Log.d(TAG, "you emitting 'times' JSON...");
                                    mSocket.emit("game over", times);
                                }
                                else{
                                    Log.d(TAG, "start the timer");
                                    diff = opponentWordDeployed - youWordDeployed;
                                    startTimer2();
                                    oppoguessed = false;
                                }
                            }
                            else{
                                Log.d(TAG, "opponent guessed the word");
                                if(!oppoguessed){
                                    JSONObject times = new JSONObject();
                                    try {
                                        times.put("you", you);
                                        times.put("opponent", opponentId);
                                        times.put("youWordDeployed", youWordDeployed);
                                        times.put("opponentWordDeployed", opponentWordDeployed);
                                        times.put("youWordGuessed", youWordGuessed);
                                        times.put("opponentWordGuessed", opponentWordGuessed);
                                        Log.d(TAG, times.toString());

                                    } catch (JSONException e) {
                                        Log.d(TAG, "you - Exception in onwordguessed");
                                        return;
                                    }
                                    Log.d(TAG, "you emitting 'times' JSON...");
                                    mSocket.emit("game over", times);
                                }
                            }
                        }

                        else{
                            if(data.getString("who").equals(you)){
                                if(tries==0){
                                    flag = false;
                                    Toast.makeText(getActivity().getApplicationContext(), "You cannot guess the word now", Toast.LENGTH_LONG).show();
                                }
                                tries--;
                                Toast.makeText(getActivity().getApplicationContext(), "Word wrongly guessed, satan's unhappy, " + tries + "tries left.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch(JSONException e){
                        Log.d(TAG, "Json exception");
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener result = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    try {
                        finResult = data.getString("string");

                        Log.d(TAG, data.toString() + "FINAL RESULT RECEIVED");
                        Intent go = new Intent(getActivity(), GameOver.class);
                        go.putExtra("opponentWord", opponentWord);
                        go.putExtra("result", finResult);
                        startActivity(go);
                        getActivity().finish();
                    } catch(JSONException e){
                        Log.d(TAG, "Json exception");
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener deployWordFirst = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "Deploy the fucking word first. CHEATER!", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener noMoreTries = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "You cannot guess the word now.", Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;
            JSONObject stoptypin = new JSONObject();
            try {
                stoptypin.put("opponent", opponentId);
                stoptypin.put("you", you);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mTyping = false;
            mSocket.emit("stop typing", stoptypin);
        }
    };


}

