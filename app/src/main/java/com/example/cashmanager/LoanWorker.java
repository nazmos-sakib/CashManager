package com.example.cashmanager;

import android.annotation.SuppressLint;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoanWorker extends Worker {
    private static final String TAG = "LoanWorker->";

    private DataBaseHelper dataBaseHelper;


    public LoanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        dataBaseHelper = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();
        int loanId = data.getInt("loan_id",-1);
        int userId = data.getInt("user_id",-1);
        double monthlyPayment = data.getDouble("monthly_payment",0.0);
        String name = data.getString("name");

        if (loanId == -1 || userId==-1 || monthlyPayment==0.0){
            return Result.failure();
        }

        try {
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("amount", - monthlyPayment);
            values.put("user_id", userId);
            values.put("type", "loan");
            values.put("description", "received amount from "+name+" Loan");
            values.put("recipient", name);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(calendar.getTime());
            values.put("date", date);

            long transactionId = db.insert("transactions", null, values);

            if (transactionId==-1){
                return Result.failure();
            } else {
                //updating user table
                Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[]{String.valueOf(userId)}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range")
                        double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        //update the value
                        ContentValues amountValues = new ContentValues();
                        amountValues.put("remained_amount", remainedAmount - monthlyPayment);
                        int affectedRows = db.update("users", amountValues, "_id=?",
                                new String[]{String.valueOf(userId)});
                        Log.d(TAG, "AddTransaction->doInBackground: affected row: " + affectedRows);
                        cursor.close();

                        //updating loan table
                        Cursor secondCursor = db.query("loans", new String[]{"remained_amount"}, "_id=?",
                                new String[]{String.valueOf(loanId)}, null, null, null);

                        if (null != secondCursor) {
                            if (cursor.moveToFirst()) {
                                @SuppressLint("Range")
                                double currentLoanAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                ContentValues secondValues = new ContentValues();
                                secondValues.put("remained_amount",currentLoanAmount-monthlyPayment);
                                db.update("loans",secondValues,"_id=?",
                                        new String[]{String.valueOf(loanId)});

                                secondCursor.close();
                                db.close();
                                return Result.success();

                            } else {
                                secondCursor.close();
                                db.close();
                                return Result.failure();
                            }
                        } else {
                            db.close();
                            return Result.failure();
                        }

                    }else {
                        cursor.close();
                        db.close();
                        return Result.failure();
                    }
                }else {
                    db.close();
                    return Result.failure();
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
