package com.example.cashmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.cashmanager.Adapters.InvestmentAdapter;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.Investment;
import com.example.cashmanager.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class InvestmentActivity extends AppCompatActivity {

    private static final String TAG = "InvestmentActivity->";
    private RecyclerView investmentRecView;
    private BottomNavigationView bottomNavigationView;

    private InvestmentAdapter investmentAdapter;
    private Utils utils;
    private DataBaseHelper dataBaseHelper;


    //async task
    private GetInvestment getInvestment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        //init all the view component
        initView();

        //init recyclerview adapter
        initInvestmentRecView();
    }

    void initView(){
        investmentRecView = findViewById(R.id.recVIew_investmentActivity);
        bottomNavigationView = findViewById(R.id.bottomNavigationView_investmentActivity);
        //
        initBottomNavView();
    }

    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //changing the selected item
        bottomNavigationView.setSelectedItemId(R.id.menu_items_investment);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_items_stats:
                        break;

                    case R.id.menu_items_transaction:
                        break;

                    case R.id.menu_items_home:
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.menu_items_lone:
                        break;

                    case R.id.menu_items_investment:

                        break;
                    default:
                        break;
                }
                return false;
            }
        } );
    }

    void initInvestmentRecView(){
        Log.d(TAG, "initInvestmentRecView: called");
        investmentAdapter = new InvestmentAdapter(this);
        investmentRecView.setAdapter(investmentAdapter);
        investmentRecView.setLayoutManager(new LinearLayoutManager(this));

        utils  = new Utils(getApplicationContext());
        dataBaseHelper = new DataBaseHelper(this);
        //
        getInvestment();
    }

    private void getInvestment(){
        Log.d(TAG, "getTransaction: started");
        //create a async task to get all the transaction data from the database
        getInvestment = new GetInvestment();
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            getInvestment.execute(user.get_id());
        }
    }

    private class GetInvestment extends AsyncTask<Integer, Void, ArrayList<Investment>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Investment> doInBackground(Integer... integers) {
            Log.d(TAG, "GetInvestment-> doInBackground: started");

            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("investments", null, "user_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, "init_date DESC");
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Investment> transactions = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            Investment investment = new Investment();
                            investment.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            investment.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            investment.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                            investment.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                            //investment.setFinish_date("2023-03-22");
                            investment.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                            investment.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            investment.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            investment.setName(cursor.getString(cursor.getColumnIndex("name")));

                            transactions.add(investment);

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

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Investment> investments) {
            super.onPostExecute(investments);
            if (null!=investments){
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is not null");
                Log.d(TAG, "GetTransactions->onPostExecute: "+investments.get(0).toString());
                investmentAdapter.setInvestmentArrayList(investments);
            } else {
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is null");
                investmentAdapter.setInvestmentArrayList(new ArrayList<Investment>());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != getInvestment) {
            if (!getInvestment.isCancelled()) {
                getInvestment.cancel(true);
            }
        }
    }


}