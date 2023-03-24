package com.example.cashmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.cashmanager.DataBase.DataBaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InvestmentWorker extends Worker {
    private static final String TAG = "InvestmentWorker";

    private DataBaseHelper dataBaseHelper;

    public InvestmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        dataBaseHelper = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: started");
        Data data = getInputData();

        double amount = data.getDouble("amount",0.0);
        String recipient = data.getString("recipient");
        String description = data.getString("description");
        int user_id = data.getInt("user_id",-1);
        String type = "profit";

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(calendar.getTime());


        //step 1- add into transaction table
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("amount", amount);
        transactionValues.put("date", date);
        transactionValues.put("user_id", user_id);
        transactionValues.put("description",description);
        transactionValues.put("type", type );
        transactionValues.put("date", date );

        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            long newTransactionId = db.insert("transactions", null, transactionValues);
            if (newTransactionId!=-1){
                Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(user_id)},null,null,null);
                if (null!=cursor){
                    if(cursor.moveToFirst()){
                        double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        //update the value
                        ContentValues amountValues = new ContentValues();
                        amountValues.put("remained_amount",remainedAmount-amount);
                        int affectedRows = db.update("users",amountValues,"_id=?",
                                new String[] {String.valueOf(user_id)});
                        Log.d(TAG, "AddShopping->doInBackground: affected row: "+affectedRows);
                    }
                    cursor.close();
                } else {
                    cursor.close();
                }
            }
            db.close();
        } catch (SQLException e){
            e.printStackTrace();
            return Result.failure();
        }


        return null;
    }
}
