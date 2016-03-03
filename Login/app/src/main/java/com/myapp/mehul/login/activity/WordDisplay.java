package com.myapp.mehul.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

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
 * Created by mehul on 2/11/16.
 */
public class WordDisplay extends Activity {

    private static final String TAG = WordDisplay.class.getSimpleName();
    private String word;
    private String definition;
    private String you;
    private String opponent;
    private String username;
    private TextView textWord;
    private TextView textdefinition;
    private String opponentWord;
    private String opponentName;
    private Socket mSocket;
    //initialise the sockets
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {

            throw new RuntimeException(e);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.worddisplay);

        word = getIntent().getExtras().getString("word");
        definition = getIntent().getExtras().getString("definition");
        you = getIntent().getExtras().getString("you");
        opponent = getIntent().getExtras().getString("opponentId");
        username = getIntent().getExtras().getString("username");
        opponentWord = getIntent().getExtras().getString("opponentWord");
        opponentName = getIntent().getExtras().getString("opponentName");
        textWord = (TextView)findViewById(R.id.word);

        textdefinition = (TextView)findViewById(R.id.definition);

        textWord.setText(word);
        textdefinition.setText(definition);


        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.drumroll);
        mp.start();
        mp.setLooping(true);

        final JSONObject params = new JSONObject();

        try {
            params.put("you", you);
            params.put("opponent", opponent);
            //params.put("word", word);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSocket.emit("start chat", params);
        //run this code after 8 seconds
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {


            @Override
            public void run() {
                Log.d(TAG, " emitting JSON data");


                Log.d(TAG, opponentWord + " " + opponent);
                mp.stop();
                Intent i = new Intent(WordDisplay.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("opponentWord", opponentWord);
                i.putExtra("word", word);
                i.putExtra("you", you);
                i.putExtra("opponentId", opponent);
                i.putExtra("username", username);
                i.putExtra("opponentName", opponentName);
                Log.d(TAG, "moving to chat");
                startActivity(i);
                finish();

                //start your activity here
            }

        }, 6000L);


    }

    public void onDestroy(){
        super.onDestroy();
        //mSocket.disconnect();
    }


}
