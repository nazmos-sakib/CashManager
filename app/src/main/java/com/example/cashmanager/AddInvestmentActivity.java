package com.example.cashmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

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
import android.view.View;
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

public class AddInvestmentActivity extends AppCompatActivity {

    private static final String TAG = "AddInvestmentActivity";

    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate;
    private Button btnPickInitDate, btnPickFinishDate, btnAddInvestment;
    private TextView txtWarning;

    private DataBaseHelper dataBaseHelper;
    private Utils utils;

    //member variable of the async task
    private AddTransactionTask addTransactionTask;
    private AddInvestmentTask addInvestmentTask;



    private Calendar initCalender = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

    //setting up date picker listener
    //its a variable
    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            initCalender.set(Calendar.YEAR,i);
            initCalender.set(Calendar.MONTH,i1);
            initCalender.set(Calendar.DAY_OF_MONTH,i2);
            //converting calender object to simple readable string date format
            edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initCalender.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            finishCalendar.set(Calendar.YEAR,i);
            finishCalendar.set(Calendar.MONTH,i1);
            finishCalendar.set(Calendar.DAY_OF_MONTH,i2);
            //converting calender object to simple readable string date format
            edtTxtFinishDate .setText(new SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.getTime()));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);
        dataBaseHelper = new DataBaseHelper(AddInvestmentActivity.this);
        utils = new Utils(AddInvestmentActivity.this);
        initViews();

        // setting on click listener for all the buttons
        setOnClickListener();
    }


    private void setOnClickListener(){
        //setting date picker for btnPickInitDate button
        btnPickInitDate.setOnClickListener(View->{
            new DatePickerDialog(AddInvestmentActivity.this,initDateSetListener,
                    initCalender.get(Calendar.YEAR),
                    initCalender.get(Calendar.MONTH),
                    initCalender.get(Calendar.DAY_OF_MONTH)).show();
        });

        //setting date picker for btnPickFinishDate button
        btnPickFinishDate.setOnClickListener(View->{
            new DatePickerDialog(AddInvestmentActivity.this,finishDateSetListener,
                    finishCalendar.get(Calendar.YEAR),
                    finishCalendar.get(Calendar.MONTH),
                    finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        //
        btnAddInvestment.setOnClickListener(View->{
            if(validateDataForAddInvestmentButton()){
                txtWarning.setVisibility(View.GONE);
                initAddingToDatabase();
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please fill all the blanks");
            }
        });
    }

    private boolean validateDataForAddInvestmentButton(){
        Log.d(TAG, "validateDataForAddInvestmentButton: started");
        if (edtTxtName.getText().toString().equals("")){
            return false;
        }
        if (edtTxtInitAmount.getText().toString().equals("")){
            return false;
        }
        if (edtTxtInitDate.getText().toString().equals("")){
            return false;
        }
        if (edtTxtFinishDate.getText().toString().equals("")){
            return false;
        }
        if (edtTxtROI.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    private void initAddingToDatabase() {
        Log.d(TAG, "initAddingToDatabase: started");

        User user = utils.getSharedPreferencesLogInInfo();

        if (null!=user){
            //to insert in to transaction table
            addTransactionTask = new AddTransactionTask();
            addTransactionTask.execute(user.get_id());
        }

    }


    private class AddTransactionTask extends AsyncTask<Integer, Void, Integer> {

        private String date,name;
        private double amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            date = edtTxtInitDate.getText().toString();
            name = edtTxtName.getText().toString();
            amount = Double.parseDouble(edtTxtInitAmount.getText().toString());

        }

        @SuppressLint("Range")
        @Override
        protected Integer doInBackground(Integer... integers) {
            Log.d(TAG, "GetTransactions-> doInBackground: started");

            try {
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

                //step 1- add into transaction table
                ContentValues transactionValues = new ContentValues();
                transactionValues.put("amount", amount);
                transactionValues.put("date", date);
                transactionValues.put("type", "investment");
                transactionValues.put("user_id", integers[0]);
                transactionValues.put("description", "initial amount for "+name+" investment");
                transactionValues.put("recipient", name );
                long newTransactionId = db.insert("transactions", null, transactionValues);

                db.close();
                return (int) newTransactionId;

            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Integer aInteger) {
            super.onPostExecute(aInteger);

            if (null!=aInteger){

                //to insert in to investment table
                addInvestmentTask = new AddInvestmentTask();
                addInvestmentTask.execute(aInteger);

            }
        }
    }

    //inserting in to the Investment table
    //creating back ground process
    private class AddInvestmentTask extends AsyncTask<Integer, Void, Void> {
        private int userId;
        private String initDate, finishDate, name;
        private double monthlyRIO, amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            initDate = edtTxtInitDate.getText().toString();
            finishDate = edtTxtFinishDate.getText().toString();
            name = edtTxtName.getText().toString();
            amount = Double.parseDouble(edtTxtInitAmount.getText().toString());
            monthlyRIO = Double.parseDouble(edtTxtROI.getText().toString());

            User user = utils.getSharedPreferencesLogInInfo();

            if (null!=user){
                userId = user.get_id();
            } else {
                userId = -1;
            }

            dataBaseHelper = new DataBaseHelper(AddInvestmentActivity.this);

        }

        @SuppressLint("Range")
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d(TAG, "GetTransactions-> doInBackground: started");

            if (userId!=-1){
                try {
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

                    //step 1- add into transaction table
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("init_date", initDate);
                    values.put("finish_date", finishDate);
                    values.put("amount", amount);
                    values.put("monthly_roi", monthlyRIO);
                    values.put("user_id", userId);
                    values.put("transaction_id", integers[0] );
                    long id = db.insert("investments", null, values);

                    //if -1 returns then some error occurred
                    //if it is add in the database successfully -> update user remaining balance
                    if (id!=-1){
                        Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                                new String[] {String.valueOf(userId)},null,null,null);
                        if (null!=cursor){
                            if(cursor.moveToFirst()){
                                double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                //update the value
                                ContentValues amountValues = new ContentValues();
                                amountValues.put("remained_amount",remainedAmount-amount);
                                int affectedRows = db.update("users",amountValues,"_id=?",
                                        new String[] {String.valueOf(userId)});
                                Log.d(TAG, "AddShopping->doInBackground: affected row: "+affectedRows);
                            }
                            cursor.close();
                        }
                    }
                    db.close();
                } catch (SQLException e){
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        //creating a back ground process so that it keeps updating the profit in database
        @Override
        protected void onPostExecute(Void aInteger) {
            super.onPostExecute(aInteger);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date initDate = simpleDateFormat.parse(edtTxtInitDate.getText().toString());
                calendar.setTime(initDate);
                int initMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);
                Date finishDate = simpleDateFormat.parse(edtTxtFinishDate.getText().toString());
                calendar.setTime(finishDate);
                int finishMonths = calendar.get(Calendar.MONTH);

                int differences = finishMonths-initMonths;
                //difference is the number of time we are going to schedule WORK

                int days = 0;
                for (int i=0;i<differences;i++){
                    days += 30;

                    Data data = new Data.Builder()
                            .putDouble("amount",amount*monthlyRIO/100)
                            .putString("description","profit for "+name)
                            .putInt("user_id",userId)
                            .putString("recipient",name)
                            .build();

                    Constraints constraints =  new Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build();

                    OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(InvestmentWorker.class)
                            .setInputData(data)
                            .setConstraints(constraints)
                            .setInitialDelay(days, TimeUnit.DAYS)
                            .addTag("profit")
                            .build();

                    WorkManager.getInstance(AddInvestmentActivity.this).enqueue(request);
                }


            } catch (ParseException e){
                e.printStackTrace();
            }

            Intent intent = new Intent(AddInvestmentActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
    }


    private void initViews() {
        Log.d(TAG, "initViews: started");

        edtTxtName = (EditText) findViewById(R.id.edtTxtName_AddInvestmentActivity);
        edtTxtInitAmount = (EditText) findViewById(R.id.edtTxtInitAmount_AddInvestmentActivity);
        edtTxtROI = (EditText) findViewById(R.id.edtTxtMonthlyROI_AddInvestmentActivity);
        edtTxtInitDate = (EditText) findViewById(R.id.edtTxtInitDate_AddInvestmentActivity);
        edtTxtFinishDate = (EditText) findViewById(R.id.edtTxtFinishDate_AddInvestmentActivity);

        btnPickInitDate = (Button) findViewById(R.id.btnPickInitDate_AddInvestmentActivity);
        btnPickFinishDate = (Button) findViewById(R.id.btnPickFinishDate_AddInvestmentActivity);
        btnAddInvestment = (Button) findViewById(R.id.btnAddInvestment_AddInvestmentActivity);

        txtWarning = (TextView) findViewById(R.id.txtWarning_AddInvestmentActivity);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != addTransactionTask){
            if (!addTransactionTask.isCancelled()){
                addTransactionTask.cancel(true);
            }
        }


        //to avoid memory leak destroy thread objects
        if (null != addInvestmentTask){
            if (!addInvestmentTask.isCancelled()){
                addInvestmentTask.cancel(true);
            }
        }
    }

}