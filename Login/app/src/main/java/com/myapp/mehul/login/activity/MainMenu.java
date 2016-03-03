package com.myapp.mehul.login.activity;

/**
 * Created by mehul on 1/29/16.
 */

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import com.myapp.mehul.login.R;
import com.myapp.mehul.login.app.AppConfig;
import com.myapp.mehul.login.app.AppController;
import com.myapp.mehul.login.helper.SQLiteHandler;
import com.myapp.mehul.login.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainMenu extends AppCompatActivity {
    private static final String TAG = MainMenu.class.getSimpleName();

    private TextView txtName;
    private TextView txtEmail;
    private TextView btnLogout;
    private TextView btnChat;
    private TextView settings;
    private TextView profile;
    private String opponentId;
    private String you;
    private boolean stroke;
    private String username;
    private SQLiteHandler db;
    private SessionManager session;
    private ImageView image;
    private TextView welcome;
    boolean doubleBackToExitPressedOnce = false;



    //private Socket mSocket;

    /*{
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {

            throw new RuntimeException(e);
        }
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        welcome = (TextView)findViewById(R.id.welcome);
        txtName = (TextView)findViewById(R.id.name);
        btnLogout = (TextView)findViewById(R.id.btnLogout);
        profile = (TextView)findViewById(R.id.profile);
        settings = (TextView)findViewById(R.id.settings);
        btnChat = (TextView)findViewById(R.id.btnchat);
        stroke = true;
        image = (ImageView)findViewById(R.id.logo);
        image.setImageResource(R.drawable.logo);

        //changing the fonts
        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/thinbold.ttf");
        welcome.setTypeface(myTypeFace);
        txtName.setTypeface(myTypeFace);
        btnLogout.setTypeface(myTypeFace);
        btnChat.setTypeface(myTypeFace);
        profile.setTypeface(myTypeFace);
        settings.setTypeface(myTypeFace);

        // Add a listener to observe the motion of the spring.




        Log.d(TAG, "painted again");
        db = new SQLiteHandler(getApplicationContext());

        session = new SessionManager(getApplicationContext());

        if(!session.isLoggedIn()){
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");
        //Log.d("the username is", username);
        //String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(username);
        //txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        //if this button is clicked go to the chat activity
        btnChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(stroke) {
                    stroke = false;
                    goToChat();
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent profile = new Intent(MainMenu.this, ProfilePage.class);
                startActivity(profile);
            }
        });



        JSONObject params = new JSONObject();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                AppConfig.URL_ROOT , params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        Log.d(TAG, response.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error in response");
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }






    private void goToChat(){


        Log.d("go to chat", "was called");

        Intent intent = new Intent(MainMenu.this, LoadingScreen.class);
        //check if the activity is opening only once, prevent multiple instances of the same
        //activity
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }


    private void logoutUser() {
        session.setLogin(false);

        /*JSONObject discon = new JSONObject();
        try {
            discon.put("opponent", opponentId);
            discon.put("you", you);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSocket.emit("discon", discon);
        mSocket.disconnect();*/
        db.deleteUsers();

        JSONObject params = new JSONObject();

        try {
            params.put("logout" , true);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                AppConfig.URL_LOGOUT , params,
                new Response.Listener<JSONObject>() {
                    //at this point the request has been sent, waiting for the response.
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            //if the response comes out to be true
                            //continue on the login screen.
                            if(response.getBoolean("logout")) {
                                Intent intent = new Intent(
                                        MainMenu.this,
                                        LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Log-Out Failed!!!!!!!", Toast.LENGTH_LONG).show();
                            }
                        }
                        catch(Exception e){}

                        // Launch login activity

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "sdbc");
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(jsonObjReq);
        // Launching the login activity
        //Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //startActivity(intent);
        //finish();
    }


    //check if user presses two times back button, if yes exit the app

    @Override
    public void onBackPressed(){
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Please press back again to exit the app", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }





}
