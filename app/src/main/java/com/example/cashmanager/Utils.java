package com.example.cashmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.cashmanager.Authentication.LogInActivity;
import com.example.cashmanager.Authentication.RegisterActivity;
import com.example.cashmanager.Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Utils {
    private static final String TAG = "Utils";
    private Context context;
    public Utils (Context context){
        this.context = context;

    }

    public void addUserToSharedPreferences (User user) {
        Log.d(TAG, "addUserToSharedPreferences: adding:" + user.toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged in user", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson(); //to convert java type to json
        editor.putString("user",gson.toJson(user));
        editor.apply();
    }

    public User getSharedPreferencesLogInInfo(){
        Log.d(TAG, "getSharedPreferencesLogInInfo: started");
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged in user", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        Type type = new TypeToken<User>(){}.getType();  //to convert json to java type we need type
        return gson.fromJson(sharedPreferences.getString("user",null),type);

    }

    public void signOutUser(){
        Log.d(TAG, "signOutUser: started");
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged in user", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user");
        editor.apply();

        Intent intent = new Intent(context, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity (intent);

    }
}

