package com.example.cashmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.cashmanager.Adapters.TransactionAdapter;
import com.example.cashmanager.Authentication.LogInActivity;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Dialogs.AddTransactionDialog;
import com.example.cashmanager.Models.Shopping;
import com.example.cashmanager.Models.Transaction;
import com.example.cashmanager.Models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private TextView tv_welcome, tv_amount;
    private RecyclerView transactionRecyclerView;
    private BarChart barChartView;
    private LineChart lineChartView;
    private FloatingActionButton fabAddTransaction;
    private Toolbar toolbar;

    private DataBaseHelper dataBaseHelper;
    private GetAccountAmount getAccountAmount;

    private Utils utils;

    private Button logout;

    private TransactionAdapter transactionAdapter;

    //list of global async task objects
    private GetTransactions getTransactions;
    private GetProfit getProfit;
    private GetSpending getSpending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkActiveLoginUser();

        dataBaseHelper = new DataBaseHelper(this);

        initViews();

        initBottomNavView();

        setSupportActionBar(toolbar);

        //getting remaining amount from database
        setupAmount();

        //setting on click listener to tv_welcome and floatingActionButton
        setOnClickListener();



        logout = findViewById(R.id.logout);
        logout.setOnClickListener(View->{
            logoutUser();
        });

        //initializing recycle view adapter to show all the transaction
        //getTransaction(); it also calls this method that implement a AsyncTask to get transaction data from database
        //GetTransactions AsyncTask to get data
        initTransactionRecVIew();

        //settingUp line chart
        initLineChart();

        //settingUp Bar chart
        initBarChart();

        //check if the back ground work scheduled or not
        Log.d(TAG, "onCreate: work" + WorkManager.getInstance(this).getWorkInfosByTag("profit"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAmount();
        getTransaction();
        initLineChart();
        initBarChart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupAmount();
        getTransaction();
        initLineChart();
        initBarChart();
    }

    public void checkActiveLoginUser(){
        utils = new Utils(getApplicationContext());
        User user = utils.getSharedPreferencesLogInInfo();

        if (null!=user){
            Toast.makeText(this,"User: "+user.get_id()+" is logged in",Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void logoutUser(){
        Utils utils = new Utils(getApplicationContext());
        utils.signOutUser();
    }

    @SuppressLint("Range")
    private void testDatabase(){
        SQLiteDatabase sqDB = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqDB.query("items",null, null,null,null,null,null);
        if (null!=cursor){
            if(cursor.moveToFirst()){
                Log.d(TAG, "testDatabase-------------------: "+cursor.getString(cursor.getColumnIndex("name")));
            }
        }
    }

    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //current selected item
        bottomNavigationView.setSelectedItemId(R.id.menu_items_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_items_stats:
                        break;

                    case R.id.menu_items_transaction:
                        break;

                    case R.id.menu_items_home:
                        break;

                    case R.id.menu_items_lone:
                        break;

                    case R.id.menu_items_investment:
                        Intent intent = new Intent(getApplicationContext(),InvestmentActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return false;
            }
        } );
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");

        tv_welcome = findViewById(R.id.tv_welcome_mainActivity);
        tv_amount =findViewById(R.id.tv_amount_mainActivity);

        barChartView = findViewById(R.id.barChart_dailySpent_mainActivity);
        lineChartView = findViewById(R.id.lineChart_profitChart_mainActivity);

        transactionRecyclerView = findViewById(R.id.transactionRecyclerView_mainActivity);

        fabAddTransaction = findViewById(R.id.fab_addTransaction_MainActivity);

        toolbar =findViewById(R.id.toolbar_mainActivity);
        bottomNavigationView = findViewById(R.id.bottomNavigationView_investmentActivity);

    }


    private void setupAmount() {
        Log.d(TAG, "setupAmount: started");

        User user = utils.getSharedPreferencesLogInInfo();

        if (user!=null){
            getAccountAmount = new GetAccountAmount();
            getAccountAmount.execute(user.get_id());
        }
    }

    private class GetAccountAmount extends AsyncTask<Integer, Void, Double>{

        @Override
        protected Double doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(integers[0])},null,null,null);
                if (null!=cursor){
                    if(cursor.moveToFirst()){
                        @SuppressLint("Range")
                        double amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        cursor.close();
                        db.close();
                        return amount;
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
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);

            if (null!=aDouble){
                tv_amount.setText(aDouble +" €");
            }else {
                tv_amount.setText("0.0 €");
            }
        }
    }

    private void setOnClickListener(){
        Log.d(TAG,  "setOnClickListeners: started") ;

        //setting click listener for text view
        tv_welcome.setOnClickListener(View->{
            AlertDialog.Builder builder =  new AlertDialog.Builder ( MainActivity.this)
                    .setTitle ("Cash Manager" )
                    .setMessage ("Created and Developed By sakib")
                    . setNegativeButton( "Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setPositiveButton("Visit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent ( MainActivity. this, WebsiteActivity.class) ;
                            startActivity(intent);
                        }
                    });
            builder.show();
        });

        //for floating action button
        fabAddTransaction.setOnClickListener(View->{
            AddTransactionDialog addTransactionDialog = new AddTransactionDialog();
            addTransactionDialog.show(getSupportFragmentManager(),"add transaction dialog");
        });


    }



    private void initTransactionRecVIew(){
        Log.d(TAG, "initTransactionRecVIew: called");

        transactionAdapter = new TransactionAdapter();
        transactionRecyclerView.setAdapter(transactionAdapter);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //
        getTransaction();
    }

    //separating reusable code which are also needed in onResume and onStart method
    private void getTransaction(){
        Log.d(TAG, "getTransaction: started");
        //create a async task to get all the transaction data from the database
        getTransactions = new GetTransactions();
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            getTransactions.execute(user.get_id());
        }
    }

    private class GetTransactions extends AsyncTask <Integer, Void, ArrayList<Transaction>>{

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
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is not null");
                Log.d(TAG, "GetTransactions->onPostExecute: "+transactions.get(0).toString());
                transactionAdapter.setTransactions(transactions);
            } else {
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is null");
                transactionAdapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }

    private void initLineChart() {
        Log.d(TAG, "initLineChart: started");

        getProfit = new GetProfit();
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            getProfit.execute(user.get_id());
        }

    }

    private class GetProfit extends AsyncTask<Integer, Void, ArrayList<Transaction>>{

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("transactions", null, "user_id=? AND type=?",
                        new String[] {String.valueOf(integers[0]),"profit"},null,null,null);
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
                Log.d(TAG, "GetProfit->onPostExecute: transaction data is not null. the size is -> "+transactions.size());

                ArrayList<Entry> entries = new ArrayList<>();

                for (Transaction t: transactions){
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(t.getDate());
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR); //keeping the current year
                        calendar.setTime(date); //setting the calender to a past date
                        int month = calendar.get(Calendar.MONTH)+1;
                        Log.d(TAG, "onPostExecute: month : " + month);
                        Log.d(TAG, "onPostExecute: year : " + year);

                        //if the transaction happened in current year
                        if (calendar.get(Calendar.YEAR) == year) {

                            boolean doesMonthExist =false;

                            for (Entry e: entries) {
                                if (e.getX()==month) {
                                    doesMonthExist = true;
                                }else {
                                    doesMonthExist = false;
                                }
                            }

                            if (!doesMonthExist) {
                                entries.add(new Entry(month, (float) t.getAmount()));
                            }else {
                                for (Entry e: entries) {
                                    if (e.getX() == month) {
                                        e.setY(e.getY() + (float) t.getAmount());
                                    }
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                for (Entry e: entries){
                    Log.d(TAG, "GetProfit->onPostExecute->printing entry x: "+e.getX()+" y: "+e.getY());
                }
                Log.d(TAG, "GetProfit->onPostExecute: after the thing i want -> "+entries.size());

                LineDataSet dataSet = new LineDataSet(entries, "Profit chart");
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.GREEN);
                LineData data = new LineData(dataSet);
                XAxis xAxis = lineChartView.getXAxis();
                xAxis.setSpaceMin(1);
                xAxis.setSpaceMax(1);
                xAxis.setAxisMaximum(12);
                xAxis.setEnabled(false);
                YAxis yAxis = lineChartView.getAxisRight();
                yAxis.setEnabled(false);
                YAxis leftAxis = lineChartView.getAxisLeft();
                leftAxis.setAxisMaximum(100);
                leftAxis.setAxisMinimum(10);
                leftAxis.setDrawGridLines(false);

                //Description description = new Description();
                //description.setText("Description");
                lineChartView.setDescription(null);
                lineChartView.animateY(2000);
                lineChartView.setData(data); //connecting with the line chart view
                lineChartView.invalidate();
            } else {
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is null");
            }
        }
    }


    private void initBarChart(){
        Log.d(TAG, "initBarChart: started");
        getSpending =   new GetSpending();
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            getSpending.execute(user.get_id());
        }
    }

    private class GetSpending extends AsyncTask <Integer, Void, ArrayList<Shopping>>{

        @SuppressLint("Range")
        @Override
        protected ArrayList<Shopping> doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("shopping", new String[] {"date","price"}, "user_id=?",
                        new String[] {String.valueOf(integers[0])},null,null,null);
                if (null!=cursor){
                    if (cursor.moveToNext()) {
                        ArrayList<Shopping> shoppings = new ArrayList<>();
                        for (int i=0; i<cursor.getCount(); i++) {
                            Shopping shopping = new Shopping();
                            shopping.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            shopping.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                            shoppings.add(shopping);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return shoppings;
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
        protected void onPostExecute(ArrayList<Shopping> shoppings) {
            super.onPostExecute(shoppings);
            Log.d(TAG, "GetSpending->onPostExecute: shoppings size : "+shoppings.size());
            if (null!=shoppings){
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (Shopping s: shoppings) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(s.getDate());
                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH)+1; //current month
                        Log.d(TAG, "shoppings->onPostExecute: month : "+month);
                        calendar.setTime(date);
                        int day = calendar.get(Calendar.DAY_OF_MONTH)+1;

                        if (calendar.get(Calendar.MONTH)+1 == month) {
                            boolean doesDayExist = false;
                            for (BarEntry e: entries) {
                                if (e.getX() == day) {
                                    doesDayExist = true;
                                }else {
                                    doesDayExist = false;
                                }
                            }

                            if (!doesDayExist) {
                                entries.add(new BarEntry(day, (float) s.getPrice()));
                            }else {
                                for (BarEntry e: entries) {
                                    if (e.getX() == day) {
                                        e.setY(e.getY() + (float) + s.getPrice());
                                    }
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "GetSpending->onPostExecute: size : "+entries.size());
                for (BarEntry e: entries) {
                    Log.d(TAG, "GetSpending->onPostExecute: x: " + e.getX() + " y: " + e.getY());
                }

                BarDataSet dataSet = new BarDataSet(entries, "Shopping chart");
                dataSet.setColor(Color.RED);
                BarData data = new BarData(dataSet);

                barChartView.getAxisRight().setEnabled(false);
                XAxis xAxis = barChartView.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = barChartView.getAxisLeft();
                yAxis.setAxisMaximum(30);
                yAxis.setAxisMinimum(10);
                yAxis.setDrawGridLines(false);
                barChartView.setData(data);
                barChartView.setDescription(null);
                barChartView.invalidate();

            } else {
                Log.d(TAG, "GetSpending->onPostExecute: shoppings is null");
            }

        }
    }


    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_toolbar_menue,menu);

        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    //to identify which menu is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_code_mainActivity:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("MeiCode")
                        .setMessage("Developed by sakib")
                        .setNegativeButton("Visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MainActivity.this, WebsiteActivity.class);
                                startActivity(intent);
                            }
                        }).setPositiveButton("Invite friends", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String message = "Hey, How is everything?\nCheckout this new awesome app. it helps me manage my money stuff" +
                                        "\nhttps:meiCode.org";

                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                intent.setType("text/plain");
                                Intent chooserIntent = Intent.createChooser(intent, "Send Message via:");
                                startActivity(chooserIntent);
                            }
                        });
                builder.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != getAccountAmount){
            if (!getAccountAmount.isCancelled()){
                getAccountAmount.cancel(true);
            }
        }
        if (null != getTransactions){
            if (!getTransactions.isCancelled()){
                getTransactions.cancel(true);
            }
        }

        if (null != getProfit){
            if (!getProfit.isCancelled()){
                getProfit.cancel(true);
            }
        }

        if (null != getSpending){
            if (!getSpending.isCancelled()){
                getSpending.cancel(true);
            }
        }

    }
}