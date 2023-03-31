package com.example.cashmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.Loan;
import com.example.cashmanager.Models.Transaction;
import com.example.cashmanager.Models.User;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "StatsActivity->";
    //views
    private BarChart barChartView;
    private PieChart pieChartView;
    private BottomNavigationView bottomNavigationView;

    private DataBaseHelper dataBaseHelper;
    private Utils utils;

    //tasks
    private GetTransactionsTask getTransactionsTask;
    private GetLoansTask getLoansTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        dataBaseHelper = new DataBaseHelper(this);
        utils = new Utils(this);

        initViews();
        initBottomNavView();

        //show chart
        showCharts();


    }
    private void initViews() {
        Log.d(TAG, "initViews: started");
        barChartView = findViewById(R.id.barChart_statsActivity);
        pieChartView = findViewById(R.id.pieChart_statsActivity);
        bottomNavigationView = findViewById(R.id.bottomNavigationView_statsActivity);

    }
    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //current selected item
        bottomNavigationView.setSelectedItemId(R.id.menu_items_stats);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu_items_stats:
                        break;

                    case R.id.menu_items_transaction:
                        intent = new Intent(getApplicationContext(),TransactionsActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.menu_items_home:
                        intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.menu_items_lone:
                        intent = new Intent(getApplicationContext(),LoanActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.menu_items_investment:
                        intent = new Intent(getApplicationContext(),InvestmentActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return false;
            }
        } );
    }

    private void showCharts(){
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            //Barchart
            getTransaction(user.get_id());
            //PieChart
            getLoans(user.get_id());

        }
    }

    //separating reusable code which are also needed in onResume and onStart method
    private void getTransaction(int user_id){
        Log.d(TAG, "getTransaction: started");
        //create a async task to get all the transaction data from the database
        getTransactionsTask = new GetTransactionsTask();
        getTransactionsTask.execute(user_id);
    }


    //show data in barChart
    private class GetTransactionsTask extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            Log.d(TAG, "GetTransactions-> doInBackground: started");

            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("transactions", null, "user_id=?",
                        new String[] {String.valueOf(integers[0])},null,null,"date DESC");
                if (null!=cursor){
                    if(cursor.moveToFirst()){
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for (int i=0;i<cursor.getCount();i++){
                            Transaction  transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                            transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));

                            transactions.add(transaction);

                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return transactions;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    db.close();
                    return null;
                }

            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);
            if (null!=transactions){
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (Transaction t:transactions){
                    try {
                        Date date = sdf.parse(t.getDate());
                        calendar.setTime(date);
                        int year = calendar.get(Calendar.YEAR); //current month
                        int month = calendar.get(Calendar.MONTH); //current month
                        int day = calendar.get(Calendar.DAY_OF_MONTH)+1; //current month


                        if (currentMonth == month && year==currentYear) {
                            boolean doesDayExist = false;
                            for (BarEntry e: entries) {
                                if (e.getX() == day) {
                                    doesDayExist = true;
                                }else {
                                    doesDayExist = false;
                                }
                            }

                            if (!doesDayExist) {
                                entries.add(new BarEntry(day, (float) t.getAmount()));
                            }else {
                                for (BarEntry e: entries) {
                                    if (e.getX() == day) {
                                        e.setY(e.getY() + (float) + t.getAmount());
                                    }
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                for (BarEntry e: entries) {
                    Log.d(TAG, "GetSpending->onPostExecute: x: " + e.getX() + " y: " + e.getY());
                }

                BarDataSet dataSet = new BarDataSet(entries, "Account Activity");
                dataSet.setColor(Color.GREEN);
                BarData data = new BarData(dataSet);

                barChartView.getAxisRight().setEnabled(false);
                XAxis xAxis = barChartView.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = barChartView.getAxisLeft();
                yAxis.setAxisMinimum(10);
                yAxis.setDrawGridLines(false);
                barChartView.setData(data);
                Description description = new Description();
                description.setText("All of the account Transaction");
                description.setTextSize(12f);
                barChartView.setDescription(description);
                barChartView.invalidate();


            }

        }
    }

    //show data in pieChart


    private void getLoans(int user_id) {
        Log.d(TAG, "getTransaction: started");
        //create a async task to get all the transaction data from the database
        getLoansTask = new GetLoansTask();
        getLoansTask.execute(user_id);
    }

    private class GetLoansTask extends AsyncTask<Integer, Void, ArrayList<Loan>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            Log.d(TAG, "GetLoansTask-> doInBackground: started");
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("loans", null, "user_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, "init_date DESC");
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Loan> loans = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            Loan loan = new Loan();
                            loan.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            loan.setName(cursor.getString(cursor.getColumnIndex("name")));
                            loan.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            loan.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                            loan.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                            loan.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                            loan.setInit_amount(cursor.getDouble(cursor.getColumnIndex("init_amount")));
                            loan.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));
                            loan.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            loan.setMonthly_payment(cursor.getDouble(cursor.getColumnIndex("monthly_payment")));

                            loans.add(loan);

                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return loans;
                    } else {
                        cursor.close();
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

        @Override
        protected void onPostExecute(ArrayList<Loan> loans) {
            super.onPostExecute(loans);
            if (null!=loans){
                ArrayList<PieEntry> entries = new ArrayList<>();

                double totalLoanAmount = 0.0;
                double totalRemainingAmount = 0.0;

                for(Loan l:loans){
                    totalLoanAmount += l.getInit_amount();
                    totalRemainingAmount += l.getRemained_amount();
                }

                entries.add(new PieEntry((float) totalLoanAmount,"Total loans"));
                entries.add(new PieEntry((float) totalRemainingAmount,"Remaining loans"));

                PieDataSet dataSet = new PieDataSet(entries,"");
                dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                dataSet.setSliceSpace(5f);
                PieData pieData = new PieData(dataSet);
                pieChartView.setDrawHoleEnabled(false);
                pieChartView.animateY(2000, Easing.EaseInCubic);
                pieChartView.setData(pieData);
                pieChartView.invalidate();


            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != getTransactionsTask) {
            if (!getTransactionsTask.isCancelled()) {
                getTransactionsTask.cancel(true);
            }
        }
        //to avoid memory leak destroy thread objects
        if (null != getLoansTask) {
            if (!getLoansTask.isCancelled()) {
                getLoansTask.cancel(true);
            }
        }
    }

}