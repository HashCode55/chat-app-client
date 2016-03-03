package com.myapp.mehul.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.myapp.mehul.login.R;

/**
 * Created by mehul on 2/17/16.
 */
public class GameOver extends Activity{
    private TextView result;
    private TextView gameOver;
    private String finResult;
    private String opponentWord;
    private TextView oppoWord;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gameover);

        result = (TextView)findViewById(R.id.result);
        gameOver = (TextView)findViewById(R.id.gameover);
        oppoWord = (TextView)findViewById(R.id.oppoWord);
        finResult = getIntent().getExtras().getString("result");
        opponentWord = getIntent().getExtras().getString("opponentWord");
        result.setText(finResult);
        oppoWord.setText(opponentWord);


        //changing the font.
        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/thin.ttf");
        result.setTypeface(myTypeFace);
        gameOver.setTypeface(myTypeFace);


    }

    public void onBackPressed(){
        Intent i = new Intent(GameOver.this, MainMenu.class);
        startActivity(i);
        finish();

    }
}
