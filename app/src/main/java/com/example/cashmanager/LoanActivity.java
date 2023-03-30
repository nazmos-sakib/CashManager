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

import com.example.cashmanager.Adapters.LoanAdapter;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.Loan;
import com.example.cashmanager.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class LoanActivity extends AppCompatActivity {
    private static final String TAG = "LoanActivity->";

    //
    private RecyclerView loanRecView;
    private BottomNavigationView bottomNavigationView;

    private Utils utils;
    private DataBaseHelper dataBaseHelper;
    //adapter
    private LoanAdapter loanAdapter;

    //task
    private GetLoansTask getLoansTask;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        //init all the view component
        initView();

        //init recyclerview adapter
        initLoanRecView();
    }

    void initView(){
        loanRecView = findViewById(R.id.recVIew_loanActivity);
        bottomNavigationView = findViewById(R.id.bottomNavigationView_loanActivity);
        //
        initBottomNavView();
    }

    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //changing the selected item
        bottomNavigationView.setSelectedItemId(R.id.menu_items_lone);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu_items_stats:
                        break;

                    case R.id.menu_items_transaction:
                        break;

                    case R.id.menu_items_home:
                        intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.menu_items_lone:
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

    void initLoanRecView(){
        Log.d(TAG, "initInvestmentRecView: called");
        loanAdapter = new LoanAdapter(this);
        loanRecView.setAdapter(loanAdapter);
        loanRecView.setLayoutManager(new LinearLayoutManager(this));

        utils  = new Utils(getApplicationContext());
        dataBaseHelper = new DataBaseHelper(this);
        //
        getLoans();
    }

    private void getLoans() {
        Log.d(TAG, "getTransaction: started");
        //create a async task to get all the transaction data from the database
        getLoansTask = new GetLoansTask();
        User user = utils.getSharedPreferencesLogInInfo();
        if (null!=user){
            getLoansTask.execute(user.get_id());
        }
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
                Log.d(TAG, "GetTransactions->onPostExecute: "+loans.get(0).toString());
                loanAdapter.setLoanArrayList(loans);
            } else {
                Log.d(TAG, "GetTransactions->onPostExecute: transaction data is null");
                loanAdapter.setLoanArrayList(new ArrayList<Loan>());
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != getLoansTask) {
            if (!getLoansTask.isCancelled()) {
                getLoansTask.cancel(true);
            }
        }
    }


}