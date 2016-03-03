package com.myapp.mehul.login.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.mehul.login.R;
import com.pkmmte.view.CircularImageView;

/**
 * Created by mehul on 2/17/16.
 */
public class ProfilePage extends Activity {
    private TextView name;
    private TextView nameValue;
    private TextView gender;
    private TextView genderValue;
    private TextView age;
    private TextView ageValue;
    private TextView nation;
    private TextView nationValue;
    private TextView level;
    private TextView levelValue;
    private FloatingActionButton upload;
    private Bitmap bitmap;
    private ProgressDialog dialog;
    private FloatingActionButton fab;
    private int cameraData = 0;
    private Bitmap image;
    private ImageView imageView;
    private CircularImageView circularImageView ;




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        MediaPlayer mp = new MediaPlayer();
        upload = (FloatingActionButton)findViewById(R.id.fab);


        name = (TextView)findViewById(R.id.name);
        nameValue = (TextView)findViewById(R.id.nameValue);
        age = (TextView)findViewById(R.id.age);
        ageValue = (TextView)findViewById(R.id.ageValue);
        gender = (TextView)findViewById(R.id.gender);
        genderValue = (TextView)findViewById(R.id.genderValue);
        level = (TextView)findViewById(R.id.level);
        levelValue = (TextView)findViewById(R.id.levelValue);
        nation = (TextView)findViewById(R.id.nationality);
        nationValue = (TextView)findViewById(R.id.nationValue);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        imageView = (ImageView)findViewById(R.id.profilepic);
        circularImageView = (CircularImageView)findViewById(R.id.circle_profile_pic);

        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, cameraData);
            }
        });

        /*Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/thinbold.ttf");
        name.setTypeface(myTypeFace);
        nameValue.setTypeface(myTypeFace);
        ageValue.setTypeface(myTypeFace);
        age.setTypeface(myTypeFace);
        gender.setTypeface(myTypeFace);
        genderValue.setTypeface(myTypeFace);
        levelValue.setTypeface(myTypeFace);
        level.setTypeface(myTypeFace);
        nation.setTypeface(myTypeFace);
        nationValue.setTypeface(myTypeFace);*/



        /*upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                public void onClick(View v) {
                    if (bitmap == null) {
                        Toast.makeText(getApplicationContext(),
                                "Please select image", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog = ProgressDialog.show(ImageUpload.this, "Uploading",
                                "Please wait...", true);
                        new ImageUploadTask().execute();
                    }
                }

            }
        });*/

        //Information about layout -
        //it all depends on when you actually put the view in the code....
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            image = (Bitmap)extras.get("data");
            imageView.setImageBitmap(image);
            circularImageView.setImageBitmap(image);
        }
    }

    public void onBackPressed(){
        finish();
    }

}
