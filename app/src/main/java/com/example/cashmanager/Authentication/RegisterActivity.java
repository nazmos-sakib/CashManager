package com.example.cashmanager.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.MainActivity;
import com.example.cashmanager.Models.User;
import com.example.cashmanager.R;
import com.example.cashmanager.Utils;
import com.example.cashmanager.WebsiteActivity;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText ev_email, ev_password, ev_name, ev_address;
    private TextView tv_warning, tv_login, tv_credit;
    private ImageView img_avatar_male_1,img_avatar_male_2,img_avatar_male_3,img_avatar_female_1,img_avatar_female_2,img_avatar_female_3;
    private Button btn_register;

    private DataBaseHelper dataBaseHelper;

    private String image_url;

    private DoUserExist doUserExist;
    private RegisterUser registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerctivity);

        //setting click listener to the root view so that
        // upon clicking in the page the virtual keyboard can be hide
        setOnTouchClickListenerToRootView();


        dataBaseHelper = new DataBaseHelper(this);

        //initializing all the resource view in one function
        initView();

        //functionality for handling image url
        image_url = "img_avatar_male_1";
        handleImageUrl();

        //setting up clickListener to register button
        btn_register.setOnClickListener(View->{
            onRegBtnClick();
        });

        //credit text click listener.
        tv_credit.setOnClickListener(View->{
            Intent intent = new Intent(getApplicationContext(), WebsiteActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


    }

    private void handleImageUrl() {
        Log.d(TAG, "handleImageUrl: tarted");

        img_avatar_male_1.setOnClickListener(View->{
            image_url = "img_avatar_male_1";
        });
        img_avatar_male_2.setOnClickListener(View->{
            image_url = "img_avatar_male_2";
        });
        img_avatar_male_3.setOnClickListener(View->{
            image_url = "img_avatar_male_3";
        });
        img_avatar_female_1.setOnClickListener(View->{
            image_url = "img_avatar_female_1";
        });
        img_avatar_female_2.setOnClickListener(View->{
            image_url = "img_avatar_female_2";
        });
        img_avatar_female_3.setOnClickListener(View->{
            image_url = "img_avatar_female_3";
        });
    }

    private void onRegBtnClick() {
        Log.d(TAG, "onRegBtnClicked: register button is clicked");

        String email = ev_email.getText().toString();
        String password = ev_password.getText().toString();
        String name = ev_name.getText().toString();
        String address = ev_address.getText().toString();

        //logic for authentic email and password.
        if (email.equals("") || password.equals("")) {
            tv_warning.setVisibility(View.VISIBLE);
            tv_warning.setText("Please enter the password and Email");
        } else { //email and password are valid
            tv_warning.setVisibility(View.GONE);

            //check for existent user with the same email
            doUserExist = new DoUserExist();
            doUserExist.execute(email);
        }

    }

    //creating a async task to find out does a user exist or not
    private class DoUserExist extends AsyncTask<String, Void, Boolean>{

        @SuppressLint("Range")
        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
                Cursor cursor = db.query("users", new String[]{" _id", "email"}, "email=?",
                        new String[]{strings[0]}, null, null, null);

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        if (cursor.getString(cursor.getColumnIndex("email")).equals(strings[0])){
                            cursor.close();
                            db.close();
                            return true;
                        } else {
                            cursor.close();
                            db.close();
                            return false;
                        }
                    } else {
                        cursor.close();
                        db.close();
                        return false;
                    }
                } else {
                    cursor.close();
                    db.close();
                    return true;
                }
            } catch (SQLException e){
                e.printStackTrace();
                return true;
            }
        }

        @Override
        protected void onPostExecute (Boolean aBoolean){
            Log.d(TAG, "checking user onPostExecute----: started");
            super.onPostExecute(aBoolean);
            if (aBoolean){  //checking if an email or user exist or not
                tv_warning.setVisibility(View.VISIBLE);
                tv_warning.setText("There is email with this email try another email");
            } else { //no user found with this email. time to add the data to database
                tv_warning.setVisibility(View.GONE);

                //inserting indo data base creating a async task
                registerUser = new RegisterUser();
                registerUser.execute();

            }
        }
    }

    //creating a async task to add user to the DataBase.
    private class RegisterUser extends AsyncTask<Void, Void, User>{
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String address;

        //reading editView values and preprocessing them
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: started");
            super.onPreExecute();

            String email = ev_email.getText().toString();
            String password = ev_password.getText().toString();
            String address = ev_address.getText().toString();
            String name = ev_name.getText().toString();

            this.email = email;
            this.password = password;
            this.address = address;

            String[] names = name.split(" ");

            if (names.length>1){
                this.lastName = names[names.length-1];

                for (int i=0;i<names.length-1;i++){
                    if (i>1){
                        this.firstName += " " + names[i];
                    } else {
                        this.firstName = names[i];
                    }
                }
            } else {
                this.firstName = names[0];
            }

            Log.d(TAG, "onPreExecute: "+firstName+" "+lastName);

        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if (null != user){
                Toast.makeText(RegisterActivity.this,"user "+user.getEmail() +" Registered successfully", Toast.LENGTH_LONG).show();

                //being conformed that the user is added to the database ,
                //add the user to shared preference
                Utils utils = new Utils(RegisterActivity.this);
                utils.addUserToSharedPreferences(user);

                //redirect page to main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //prevent from going back to register activity using back button and so on.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(RegisterActivity.this,"was not able to register. please try again letter", Toast.LENGTH_LONG).show();
            }

        }

        //inserting in to database
        @SuppressLint("Range")
        @Override
        protected User doInBackground(Void... voids) {
            Log.d(TAG, "inserting in to database --doInBackground: started");
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                ContentValues values = new ContentValues();
                values.put("email",this.email);
                values.put("password",this.password);
                values.put("first_name",this.firstName);
                values.put("last_name",this.lastName);
                values.put("address",this.address);
                values.put("remained_amount",image_url);
                values.put("remained_amount",0.0);

                long userId = db.insert("users", null, values);

                Log.d(TAG, "doInBackground------insertingIntoDatabase: userID- "+userId);

                Cursor cursor = db.query("users", null, "_id=?",
                        new String[] { String.valueOf(userId)},null,null,null);

                //after inserting into database again retrieving user info to add them in shared preference
                // so that the user wont have to login again when open the app again.
                //
                if (null != cursor){
                    if (cursor.moveToFirst()) {
                        User user = new User();

                        user.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                        user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                        user.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
                        user.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
                        user.setImg_url(cursor.getString(cursor.getColumnIndex("image_url")));
                        user.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                        user.setRemainingAmount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));

                        cursor.close();
                        db.close();
                        return user;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    Log.d(TAG, "doInBackground: from cursor id not found");
                    db.close();
                    return null;
                }

            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }
    }


    private void initView(){
        Log.d(TAG, "initView: started");
        ev_email = findViewById(R.id.ev_email);
        ev_password = findViewById(R.id.ev_password);
        ev_name = findViewById(R.id.ev_name);
        ev_address = findViewById(R.id.ev_address);


        img_avatar_male_1 = findViewById(R.id.avater_male_1);
        img_avatar_male_2 = findViewById(R.id.avater_male_2);
        img_avatar_male_3 = findViewById(R.id.avater_male_3);
        img_avatar_female_1 = findViewById(R.id.avater_female_1);
        img_avatar_female_2 = findViewById(R.id.avater_female_2);
        img_avatar_female_3 = findViewById(R.id.avater_female_3);

        btn_register = findViewById(R.id.btn_register);


        //login text view
        tv_login = findViewById(R.id.tv_login);
        //setting click listener and making it fancy;
        setLoginTextViewFancyModification();

        tv_warning = findViewById(R.id.tv_warning);

        tv_credit = findViewById(R.id.tv_credit);
    }

    //adding extra feature in text view login.
    // making last only clickable.
    // making it italic and underlined
    private void setLoginTextViewFancyModification(){
        SpannableString spannableString = new SpannableString("Have an account? Login from here");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
        int start = 27; // Start index of the word "clickable"
        int end = 18;   // End index of the word "clickable"
        spannableString.setSpan(clickableSpan, start,  spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //italic text
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //underline text
        spannableString.setSpan(new UnderlineSpan(),  start, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //so that the color of the text remain the same
        spannableString.setSpan(new ForegroundColorSpan(tv_login.getCurrentTextColor()), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_login.setText(spannableString);
        tv_login.setMovementMethod(LinkMovementMethod.getInstance());
    }

    //set keyboard hiding functionality
    //it calls another function: hideKeyboard();
    private void setOnTouchClickListenerToRootView(){
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    //implements the functionality of hiding keyboard upon clicking anywhere in the page
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != doUserExist){
            if (!doUserExist.isCancelled()){
                doUserExist.cancel(true);
            }
        }

        if (null != registerUser){
            if (!registerUser.isCancelled()){
                registerUser.cancel(true);
            }
        }
    }
}