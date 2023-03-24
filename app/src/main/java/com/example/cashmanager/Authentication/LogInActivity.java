package com.example.cashmanager.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.MainActivity;
import com.example.cashmanager.Models.User;
import com.example.cashmanager.R;
import com.example.cashmanager.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
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

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText ev_email, ev_password;
    private TextView tv_warning, tv_register, tv_credit;
    private Button btn_login;

    private DataBaseHelper dataBaseHelper;
    DoUserExist doUserExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        dataBaseHelper = new DataBaseHelper(this);

        //setting click listener to the root view so that
        // upon clicking in the page the virtual keyboard can be hide
        setOnTouchClickListenerToRootView();


        //initializing all the resource view in one function
        initView();

        btn_login.setOnClickListener(View->{

            String email = ev_email.getText().toString();
            String password = ev_password.getText().toString();

            if (isLoginCredentialsAuthentic(email, password)) {
                //check for existent user with the same email and password.
                //if found add to shared preference and move to main activity
                doUserExist = new DoUserExist();
                doUserExist.execute(email,password);
            }
        });


    }

    //creating a async task to find out does a user exist or not
    private class DoUserExist extends AsyncTask<String, Void, Boolean> {
        Cursor cursor;
        SQLiteDatabase db;

        @SuppressLint("Range")
        @Override
        protected Boolean doInBackground(String... strings) {


            try {
                db = dataBaseHelper.getReadableDatabase();

                cursor = db.query("users", null,
                        "email=? AND password=?",
                        new String[]{strings[0],strings[1]},null,null,null);

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        if (cursor.getString(cursor.getColumnIndex("email")).equals(strings[0]) && cursor.getString(cursor.getColumnIndex("password")).equals(strings[1])){

                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    Log.d(TAG, "-------doInBackground: user and password does not match");

                    return false;
                }
            } catch (SQLException e){
                e.printStackTrace();
                return false;
            }
        }

        @SuppressLint("Range")
        @Override
        protected void onPostExecute (Boolean aBoolean){
            Log.d(TAG, "checking user onPostExecute----: started");
            super.onPostExecute(aBoolean);
            if (!aBoolean){  //user or password does not match
                            // or problem happen connecting to DB
                tv_warning.setVisibility(View.VISIBLE);
                tv_warning.setText("user and password does not matched");
            } else {
                //being conformed that the user email and password is matched ,
                if (cursor.moveToFirst()){
                    //cursor have data retrieve from database
                    // mapping cursor values to User class
                    User user = new User();

                    user.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                    user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                    user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                    user.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
                    user.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
                    user.setImg_url(cursor.getString(cursor.getColumnIndex("image_url")));
                    user.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    user.setRemainingAmount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));


                    Toast.makeText(getApplicationContext(),"user "+user.getEmail() +" logged in successfully", Toast.LENGTH_LONG).show();

                    //add the user to shared preference
                    Utils utils = new Utils(getApplicationContext());
                    utils.addUserToSharedPreferences(user);

                    //redirect page to main activity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    //prevent from going back to register activity using back button and so on.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
            cursor.close();
            db.close();
        }
    }

    //business logic for form field validation
    private boolean isLoginCredentialsAuthentic(String email, String password){
        Log.d(TAG, "onRegBtnClicked: register button is clicked");


        //logic for authentic email and password.
        if (email.equals("") || password.equals("")) {
            tv_warning.setVisibility(View.VISIBLE);
            tv_warning.setText("Please enter the password and Email");
            return false;
        } else { //email and password are valid
            tv_warning.setVisibility(View.GONE);
            return true;
        }

    }

    private void initView(){
        Log.d(TAG, "initView: started");
        ev_email = findViewById(R.id.ev_email_loginActivity);
        ev_password = findViewById(R.id.ev_password_loginActivity);


        btn_login = findViewById(R.id.btn_login_loginActivity);


        //register text view
        tv_register = findViewById(R.id.tv_register_loginActivity);
        //setting click listener and making it fancy;
        setRegisterTextViewFancyModification();

        tv_warning = findViewById(R.id.tv_warning_loginActivity);

        tv_credit = findViewById(R.id.tv_credit_loginActivity);
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


    //adding extra feature in text view login.
    // making last only clickable.
    // making it italic and underlined
    private void setRegisterTextViewFancyModification(){
        SpannableString spannableString = new SpannableString("Don't have an account? Register from here");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
        int start = 37; // Start index of the word "clickable"
        int end = 18;   // End index of the word "clickable"
        spannableString.setSpan(clickableSpan, start,  spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //italic text
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //underline text
        spannableString.setSpan(new UnderlineSpan(),  start, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //so that the color of the text remain the same
        spannableString.setSpan(new ForegroundColorSpan(tv_register.getCurrentTextColor()), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_register.setText(spannableString);
        tv_register.setMovementMethod(LinkMovementMethod.getInstance());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != doUserExist){
            if (!doUserExist.isCancelled()){
                doUserExist.cancel(true);
            }
        }

    }


}