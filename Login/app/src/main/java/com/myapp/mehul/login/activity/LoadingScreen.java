package com.myapp.mehul.login.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.myapp.mehul.login.MainActivity;
import com.myapp.mehul.login.R;
import com.myapp.mehul.login.app.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by mehul on 2/6/16.
 */
public class LoadingScreen extends Activity {
    private static final String TAG = LoadingScreen.class.getSimpleName();

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    private Socket mSocket;
    private String you;
    private  String opponentId;
    private String username;
    private String word;
    private String definition;
    private String opponentWord;
    private String opponentName;

    public void onCreate(Bundle icicle) {

        {
            try {
                mSocket = IO.socket(Constants.CHAT_SERVER_URL);
            } catch (URISyntaxException e) {

                throw new RuntimeException(e);
            }
        }
        super.onCreate(icicle);
        setContentView(R.layout.loadingcreen);
        ImageView gif = (ImageView)findViewById(R.id.imageView);
        //using Ion Library to diplay the gif!
        Ion.with(gif).load("android.resource://com.myapp.mehul.login/" + R.drawable.loading);


        //web = (WebView) findViewById(R.id.webView);
        //web.setBackgroundColor(Color.TRANSPARENT); //for gif without background
        //web.loadUrl("file:///Users/mehul/AndroidStudioProjects/Tester2/app/src/main/assets/htmls/loading.html");
        //initialise the socket
        mSocket.connect();
        //call add user
        mSocket.emit("add user");
        //start a listener for opponent
        mSocket.on("opponent", onOpponent);

        mSocket.on("opponent word", onOpponentWord);
        //initialise the username
        username = getIntent().getExtras().getString("username");
        //listener for game start
        mSocket.on("game start", gameStart);


        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent mainIntent = new Intent(LoadingScreen.this,MainActivity.class);
                LoadingScreen.this.startActivity(mainIntent);
                LoadingScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);*/
    }



    public void onDestroy(){
        super.onDestroy();

        //mSocket.disconnect();
        mSocket.off("opponent", onOpponent);
        mSocket.off("game start", gameStart);
        mSocket.off("opponent word", onOpponentWord);

    }

    //public void onStop(){
    //    super.onStop();
    //    Intent i = new Intent(LoadingScreen.this, MainMenu.class);
    //    startActivity(i);
    //    finish();

    //}




    private Emitter.Listener onOpponent = new Emitter.Listener(){
        @Override
        public  void call(final Object... args){
            LoadingScreen.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    try {
                        you = data.getString("you");
                        opponentId = data.getString("opponent");
                        data.put("username", username);

                        //adding the name of the user and sending it
                        //to the server

                        Log.d(TAG, data.toString());

                        mSocket.emit("game start", data);
                        //mSocket.off("opponent", onOpponent);

                        //setResult(RESULT_OK, i);


                    } catch (JSONException e) {
                        return;
                    }


                }
            });
        }
    };

    private Emitter.Listener gameStart = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            LoadingScreen.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        word = data.getString("word");
                        definition = data.getString("definition");
                        opponentName = data.getString("opponentUsername");

                        //mSocket.off("game start", gameStart);
                        final JSONObject params = new JSONObject();

                        try {
                            params.put("you", you);
                            params.put("opponent", opponentId);
                            params.put("word", word);
                            //params.put("word", word);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mSocket.emit("opponent word", params);
                        if(opponentWord!=null){
                            Intent i = new Intent(LoadingScreen.this, WordDisplay.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            i.putExtra("opponentId", opponentId);
                            i.putExtra("you", you);
                            i.putExtra("username", username);
                            i.putExtra("opponentName", opponentName);
                            i.putExtra("word", word);
                            i.putExtra("definition", definition);
                            i.putExtra("opponentWord", opponentWord);
                            Log.d(TAG, "moving to word display");
                            startActivity(i);
                            finish();
                        }

                        Log.d(TAG, data.toString() + " word received");
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onOpponentWord = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            LoadingScreen.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, data.toString() + " opponent word received");
                    try {
                        opponentWord = data.getString("opponentWord");
                    } catch (JSONException e) {
                        return;
                    }
                    if (word != null) {
                        Intent i = new Intent(LoadingScreen.this, WordDisplay.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        i.putExtra("opponentId", opponentId);
                        i.putExtra("you", you);
                        i.putExtra("opponentName", opponentName);

                        i.putExtra("username", username);
                        i.putExtra("word", word);
                        i.putExtra("definition", definition);
                        i.putExtra("opponentWord", opponentWord);
                        Log.d(TAG, "moving to word display");
                        startActivity(i);
                        finish();
                    }
                }
            });
        }

    };





    @Override
    public void onBackPressed(){

        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
        builder.setMessage("I knew you didn't have BALLS.").setCancelable(
                false).setPositiveButton("I am a LOSER",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //send the logout information to the server
                        //JSONObject discon = new JSONObject();
                        //try {
                        //    discon.put("opponent", opponentId);
                        //    discon.put("you", you);
                        //} catch (JSONException e) {
                          //  // TODO Auto-generated catch block
                          //  e.printStackTrace();
                        //}
                        //mSocket.emit("discon", discon);
                        //mSocket.disconnect();
                        JSONObject discon = new JSONObject();
                        try {
                            discon.put("opponent", opponentId);
                            discon.put("you", you);
                            discon.put("where", "LoadingScreen onBackPressed");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mSocket.emit("discon", discon);
                        mSocket.disconnect();
                        //finish the current activity.
                        Intent intent = new Intent(LoadingScreen.this, MainMenu.class);
                        startActivity(intent);
                        LoadingScreen.this.finish();

                    }
                }).setNegativeButton("I'll fkin face it",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
