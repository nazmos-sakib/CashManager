package com.example.cashmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddLoanActivity extends AppCompatActivity {
    private static final String TAG = "AddLoanActivity->";

    //view variables
    TextView tv_warning;
    EditText edTxt_name,edTxt_initAmount,edTxt_roi,edTxt_monthlyPayment,edTxt_initDate,edTxt_finishDate;
    Button btn_pickInintDate, btn_pickFinishedDate, btn_addLoan;


    //setting up date picker listener
    //its a variable
    private Calendar calendar = Calendar.getInstance();
    //for initial date
    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            //converting calender object to simple readable string date format
            edTxt_initDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    //for finish date
    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            //converting calender object to simple readable string date format
            edTxt_finishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    private DataBaseHelper dataBaseHelper;
    private Utils utils;

    //async
    private  AddTransactionTask addTransactionTask;
    private AddLoanTask addLoanTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);
        dataBaseHelper = new DataBaseHelper(this);
        utils = new Utils(this);

        //initialize all views
        initViews();

        setOnclickListeners();
    }


    private boolean validateData() {
        Log.d(TAG, "validateData: started");
        if (edTxt_name.getText().toString().equals("")) {
            return false;
        }

        if (edTxt_initAmount.getText().toString().equals("")) {
            return false;
        }
        if (edTxt_roi.getText().toString().equals("")) {
            return false;
        }
        if (edTxt_monthlyPayment.getText().toString().equals("")) {
            return false;
        }

        if (edTxt_initDate.getText().toString().equals("")) {
            return false;
        }

        if (edTxt_finishDate.getText().toString().equals("")) {
            return false;
        }

        return true;
    }


    private void initAddToTransaction() {
        Log.d(TAG, "initAddToLoan: started");
        Utils utils = new Utils(this);
        User user = utils.getSharedPreferencesLogInInfo();

        if (null != user) {
            //
            addTransactionTask = new AddTransactionTask();
            addTransactionTask.execute(user.get_id());
        }
    }

    //async task
    private class AddTransactionTask extends AsyncTask<Integer, Void, Integer> {
        private double amount;
        private String name, date;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            amount = Double.parseDouble(edTxt_initAmount.getText().toString());
            date = edTxt_initDate.getText().toString();
            name = edTxt_name.getText().toString();

        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();


                ContentValues values = new ContentValues();
                values.put("amount", - amount);
                values.put("date", date);
                values.put("type", "loan");
                values.put("user_id", integers[0]);
                values.put("recipient", name);
                values.put("description", "received amount from "+name+" Loan");


                long transactionId = db.insert("transactions", null, values);
                Log.d(TAG, "doInBackground------insertingIntoDatabase: transactionId- " + transactionId);

                db.close();
                return (int)transactionId;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null!=integer){
                //T
                addLoanTask = new AddLoanTask();
                addLoanTask.execute(integer);
            }
        }
    }

    private class AddLoanTask extends AsyncTask<Integer, Void, Integer> {
        private int user_id;
        private String name, initDate, finishDate;
        private double roi, monthlyPayment, initAmount;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "AddLoanTask->onPreExecute: started");
            super.onPreExecute();

            name = edTxt_name.getText().toString();
            initDate = edTxt_initDate.getText().toString();
            finishDate = edTxt_finishDate.getText().toString();

            roi = Double.parseDouble(edTxt_roi.getText().toString());
            monthlyPayment = Double.parseDouble(edTxt_monthlyPayment.getText().toString());
            initAmount = Double.parseDouble(edTxt_initAmount.getText().toString());

            User user = utils.getSharedPreferencesLogInInfo();

            if (null!=user){
                user_id=user.get_id();
            } else user_id = -1;

        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            Log.d(TAG, "AddLoanTask->doInBackground: started");
            if (user_id!=-1){
                try {
                    SQLiteDatabase db = dataBaseHelper.getReadableDatabase();


                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("init_date", initDate);
                    values.put("finish_date", finishDate);
                    values.put("user_id", user_id);
                    values.put("init_amount", initAmount);
                    values.put("remained_amount", initAmount);
                    values.put("monthly_payment", monthlyPayment);
                    values.put("monthly_roi", roi);
                    values.put("transaction_id", integers[0]);
                    long loanId = db.insert("loans", null, values);
                    Log.d(TAG, "AddLoanTask->doInBackground------insertingIntoDatabase: loanId- " + loanId);

                    if (loanId != -1) {
                        Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                                new String[]{String.valueOf(user_id)}, null, null, null);
                        if (null != cursor) {
                            if (cursor.moveToFirst()) {
                                @SuppressLint("Range")
                                double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                //update the value
                                ContentValues amountValues = new ContentValues();
                                amountValues.put("remained_amount", remainedAmount - initAmount);
                                int affectedRows = db.update("users", amountValues, "_id=?",
                                        new String[]{String.valueOf(user_id)});
                                Log.d(TAG, "AddTransaction->doInBackground: affected row: " + affectedRows);
                                db.close();
                                cursor.close();
                                return (int)loanId;
                            } else {
                                cursor.close();
                                db.close();
                                return null;
                            }
                        }else {
                            db.close();
                            return null;
                        }
                    } else {
                        db.close();
                        return null;
                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            //creating a background work to automatically deduct from the account
            if (null!=integer ){

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date initDate = sdf.parse(this.initDate);
                    calendar.setTime(initDate);
                    int intMonth = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

                    Date finishDate = sdf.parse(this.finishDate);
                    calendar.setTime(finishDate);
                    int finishMonth = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

                    int month = finishMonth - intMonth;

                    int days =0;
                    for (int i=0;i<month;i++){
                        days+=30;
                        Data data = new Data.Builder()
                                .putInt("loan_id",integer)
                                .putInt("user_id",user_id)
                                .putDouble("monthly_payment" ,monthlyPayment)
                                .putString("name",name)
                                .build();

                        Constraints constraints = new Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build();

                        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LoanWorker.class)
                                .setInputData(data)
                                .setConstraints(constraints)
                                .setInitialDelay(days, TimeUnit.DAYS)
                                .addTag("lonePayment")
                                .build();

                        WorkManager.getInstance(AddLoanActivity.this).enqueue(request);

                        //when everything finish return to home
                        Intent intent = new Intent(AddLoanActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }  catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }



        private void setOnclickListeners() {
        btn_pickInintDate.setOnClickListener(View -> {
            new DatePickerDialog(AddLoanActivity.this, initDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btn_pickFinishedDate.setOnClickListener(View -> {
            new DatePickerDialog(AddLoanActivity.this, finishDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btn_addLoan.setOnClickListener(View -> {

            if (validateData()) {
                tv_warning.setVisibility(android.view.View.GONE);
                initAddToTransaction();
            } else {
                tv_warning.setVisibility(android.view.View.VISIBLE);
                tv_warning.setText("Please fill all the field");
            }
        });

    }

    private void initViews() {
        tv_warning = findViewById(R.id.tv_warning_loanActivity);

        edTxt_name = findViewById(R.id.edTxt_name_loanActivity);
        edTxt_initAmount = findViewById(R.id.edTxt_initialAmount_loanActivity);
        edTxt_roi = findViewById(R.id.edTxt_roi_loanActivity);
        edTxt_monthlyPayment = findViewById(R.id.edTxt_monthlyPayment_loanActivity);
        edTxt_initDate = findViewById(R.id.edTxt_initDate_transferActivity);
        edTxt_finishDate = findViewById(R.id.edTxt_finishDate_transferActivity);

        btn_pickInintDate = findViewById(R.id.btn_picInitDate_transferActivity);
        btn_pickFinishedDate = findViewById(R.id.btn_picFinishDate_transferActivity);
        btn_addLoan = findViewById(R.id.btn_addLoan_loanActivity);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != addTransactionTask) {
            if (!addTransactionTask.isCancelled()) {
                addTransactionTask.cancel(true);
            }
        }

        //to avoid memory leak destroy thread objects
        if (null != addLoanTask) {
            if (!addLoanTask.isCancelled()) {
                addLoanTask.cancel(true);
            }
        }

    }
}