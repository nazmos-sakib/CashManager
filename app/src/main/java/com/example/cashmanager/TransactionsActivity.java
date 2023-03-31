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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.cashmanager.Adapters.TransactionAdapter;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.Transaction;
import com.example.cashmanager.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class TransactionsActivity extends AppCompatActivity {
    private static final String TAG = "TransactionsActivity";

    //views
    private BottomNavigationView bottomNavigationView;
    private RadioGroup rdgV_transactionType;
    private RadioButton rBtn_all,rBtn_shopping,
            rBtn_investment,rBtn_profit,rBtn_loan,
            rBtn_loanPayment,rBtn_send,rBtn_receive;
    private EditText edTxt_search;
    private Button btn_search;
    private RecyclerView recView_transaction;
    private TextView tv_noTransaction;

    //
    private DataBaseHelper dataBaseHelper;
    private Utils utils;

    private TransactionAdapter transactionAdapter;

    //Async Task
    private GetTransactions getTransactions;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        dataBaseHelper = new DataBaseHelper(this);
        utils = new Utils(this);

        //
        initViews();
        initBottomNavView();

        initTransactionRecVIew();

        //on button click fetch data from database
        setSearchClickListener();

        //on radio button change filter and
        // fetch data from database
        rdgV_transactionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                getTransaction();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        //getTransaction();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getTransaction();
    }



    private void initBottomNavView(){
        Log.d(TAG, "initBottomNavView: started");

        //current selected item
        bottomNavigationView.setSelectedItemId(R.id.menu_items_transaction);

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

    private void initViews(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView_transactionActivity);

        rdgV_transactionType = findViewById(R.id.rdoGroup_transactionActivity);
        rBtn_all = findViewById(R.id.rBtn_all_transactionActivity);
        rBtn_shopping = findViewById(R.id.rBtn_shopping_transactionActivity);
        rBtn_investment = findViewById(R.id.rBtn_investment_transactionActivity);
        rBtn_profit = findViewById(R.id.rBtn_profit_transactionActivity);
        rBtn_loan = findViewById(R.id.rBtn_loan_transactionActivity);
        rBtn_loanPayment = findViewById(R.id.rBtn_loanPayment_transactionActivity);
        rBtn_send = findViewById(R.id.rBtn_send_transactionActivity);
        rBtn_receive = findViewById(R.id.rBtn_receive_transactionActivity);

        edTxt_search = findViewById(R.id.edTxt_search_investmentActivity);
        btn_search = findViewById(R.id.btn_search_investmentActivity);
        recView_transaction = findViewById(R.id.recView_allTransaction_transactionActivity);

        tv_noTransaction = findViewById(R.id.tv_noTransaction_transactionActivity);

    }




    private void initTransactionRecVIew(){
        Log.d(TAG, "initTransactionRecVIew: called");

        transactionAdapter = new TransactionAdapter();
        recView_transaction.setAdapter(transactionAdapter);
        recView_transaction.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setSearchClickListener() {
        btn_search.setOnClickListener(View->{
            //
            getTransaction();
        });
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

    private class GetTransactions extends AsyncTask<Integer, Void, ArrayList<Transaction>> {
        private String type = "all";
        private double min = 0.0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!edTxt_search.getText().toString().isEmpty()){
                this.min = Double.parseDouble(edTxt_search.getText().toString());
            }
            switch (rdgV_transactionType.getCheckedRadioButtonId()){
                case R.id.rBtn_shopping_transactionActivity:
                    type = "shopping";
                    break;
                case R.id.rBtn_investment_transactionActivity:
                    type = "investment";
                    break;
                case R.id.rBtn_profit_transactionActivity:
                    type = "profit";
                    break;
                case R.id.rBtn_loan_transactionActivity:
                    type = "loan";
                    break;
                case R.id.rBtn_loanPayment_transactionActivity:
                    type = "oan_payment";
                    break;
                case R.id.rBtn_send_transactionActivity:
                    type = "send";
                    break;
                case R.id.rBtn_receive_transactionActivity:
                    type = "receive";
                    break;
                default:
                    type="all";
                    break;

            }
        }

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            Log.d(TAG, "GetTransactions-> doInBackground: started");

            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor;

                if (type.equals("all")){
                    cursor = db.query("transactions", null, "user_id=?",
                            new String[] {String.valueOf(integers[0])},null,null,"date DESC");
                } else {
                    cursor = db.query("transactions", null, "user_id=? AND type=?",
                            new String[] {String.valueOf(integers[0]),type},null,null,"date DESC");

                }

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

                            double absoluteAmount = transaction.getAmount();
                            if (absoluteAmount<0){
                                absoluteAmount = -absoluteAmount;
                            }
                            if (absoluteAmount>min){
                                transactions.add(transaction);
                            }

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


}