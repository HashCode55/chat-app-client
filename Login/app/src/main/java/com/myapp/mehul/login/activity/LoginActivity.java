package com.myapp.mehul.login.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.myapp.mehul.login.R;
import com.myapp.mehul.login.app.AppConfig;
import com.myapp.mehul.login.app.AppController;
import com.myapp.mehul.login.helper.SQLiteHandler;
import com.myapp.mehul.login.helper.SessionManager;


/**
 * Created by mehul on 1/17/16.
 */
public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText inputEmail;
    private EditText inputPassword;
    private Button btnLogin;
    private Button btnLinkToRegister;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private TextView header;


    private JSONObject jObj;
    @Override
    public void onCreate(Bundle savedInstanceState) {



        //  Initialize SharedPreferences
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        //  Create a new boolean and preference and set it to true
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
        //  If the activity has never started before..
        if (isFirstStart) {
            //  Launch app intro
            Intent i = new Intent(LoginActivity.this, MyAppIntro.class);
            startActivity(i);

            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();

            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("firstStart", false);

            //  Apply changes
            e.apply();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        header = (TextView)findViewById(R.id.textView);

        /*ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);

        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText("My Own Title");

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);*/





        //changing the font
        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/thin.ttf");
        header.setTypeface(myTypeFace);
        btnLogin.setTypeface(myTypeFace);
        btnLinkToRegister.setTypeface(myTypeFace);
        inputEmail.setTypeface(myTypeFace);
        inputPassword.setTypeface(myTypeFace);







        //Progress dialog... this is used to show the progress
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //SqlLite database Handler
        db = new SQLiteHandler(getApplicationContext());

        //create a new session
        session = new SessionManager(getApplicationContext());
        //check if the user is already logged in or not, if yes take him/her to the main Activity
        if (session.isLoggedIn()) {
            //just call the chat activity

            String usr = db.getUserDetails().get("username").toString();
            //mSocket.emit("add user", usr);

            Log.d(TAG, "already logged in");
            Intent intent = new Intent(LoginActivity.this, MainMenu.class);

            startActivity(intent);

            finish();
        }


        inputEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                inputEmail.requestFocus();
                return true;
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //check for empty data in the form

                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {

                    checkLogin(email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter the credentials!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

                finish();
            }
        });

    }






    //function to check if the credentials are right or not

    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");

        //have to implement an another clss to show the dialog box

        JSONObject params = new JSONObject();

        try {
            params.put("username", email);
            params.put("password", password);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                AppConfig.URL_LOGIN , params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        Log.d(TAG, response.toString());
                        try {
                        if(response.getBoolean("auth")){

                                session.setLogin(true);
                                String username = response.getString("username");
                                db.addUser(username);

                                Intent i = new Intent(LoginActivity.this, MainMenu.class);
                                Log.d("LogininActivity", "oh yeah, I fked ya");
                                startActivity(i);
                                finish();

                            }
                        else{
                            Toast.makeText(getApplicationContext(), "Check the username and password entered", Toast.LENGTH_LONG).show();
                        }
                        }catch(Exception e){}






                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("shjdbc", "sdbc");
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
}

