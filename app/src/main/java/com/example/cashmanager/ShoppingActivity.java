package com.example.cashmanager;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cashmanager.Adapters.ItemAdapter;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Dialogs.SelectItemDialog;
import com.example.cashmanager.Models.Item;
import com.example.cashmanager.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.BlockingDeque;

public class ShoppingActivity extends AppCompatActivity implements ItemAdapter.GetItem {
    private static final String TAG = "AddShoppingActivity";

    private Calendar calendar = Calendar.getInstance();
    private DataBaseHelper dataBaseHelper;

    //setting up date picker listener
    //its a variable
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            calendar.set(Calendar.YEAR,i);
            calendar.set(Calendar.MONTH,i1);
            calendar.set(Calendar.DAY_OF_MONTH,i2);
            //converting calender object to simple readable string date format
            edtTxtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    private EditText edtTxtDate,edtTxtDesc,edtTxtItemPrice,edtTxtStore;
    private Button btnPickItem, btnPickDate, btnAdd;
    private TextView txtWarning,txtItemName;
    private ImageView itemImage;
    private RelativeLayout itemRelativeLayout;
    private Item selectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        initViews();

        //setting on clicker listener to the date pic button
        btnPickDate.setOnClickListener(View->{
            new DatePickerDialog(ShoppingActivity.this,dateSetListener,calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        //
        btnPickItem.setOnClickListener(View->{
            SelectItemDialog selectItemDialog = new SelectItemDialog();
            selectItemDialog.show(getSupportFragmentManager(),"select item dialog");
        });

        //validate data
        //add the data to the database
        btnAdd.setOnClickListener(View->{
            initAddButtonFunction();
        });
    }


    private void initViews() {
        Log.d(TAG, "initViews: started");

        edtTxtDate = findViewById(R.id.edtTxtDate_shoppingActivity);
        edtTxtDesc = findViewById(R.id.edtTxtDesc_shoppingActivity);
        edtTxtItemPrice = findViewById(R.id.edtTxtPrice_shoppingActivity);
        edtTxtStore = findViewById(R.id.edtTxtStore_shoppingActivity);


        txtWarning = findViewById(R.id.txtWarning_shoppingActivity);
        txtItemName = findViewById(R.id.txtItemName_shoppingActivity);

        btnPickItem = findViewById(R.id.btnPick_shoppingActivity);
        btnPickDate = findViewById(R.id.btnPickDate_shoppingActivity);
        btnAdd = findViewById(R.id.btnAdd_shoppingActivity);

        itemImage = findViewById(R.id.itemImg_shoppingActivity);

        itemRelativeLayout = findViewById(R.id.invisibleItemRelLayout_shoppingActivity);
    }

    //implements ItemAdapter.GetItem
    @Override
    public void onGettingItemResult(Item item) {

        Log.d(TAG, "ItemAdapter.GetItem->onGettingItemResult: "+item.toString());
        selectedItem = item;
        itemRelativeLayout.setVisibility(View.VISIBLE);
        Glide.with(this)
                .asBitmap()
                .load(item.getImage_url())
                .into(itemImage);
        txtItemName.setText(item.getName());
        edtTxtDesc.setText(item.getDescription());

    }

    // validating the data
    private void initAddButtonFunction(){
        Log.d(TAG, "initAddButtonFunction: started");
        if (null!=selectedItem){
            if (!edtTxtItemPrice.getText().toString().equals("")){
                if (!edtTxtDate.getText().toString().equals("")){
                    //after validating add the data to the database
                    //another async task to add into the data base
                    //
                    addShopping = new AddShopping();
                    addShopping.execute();
                } else  {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("please select a date");
                }

            } else  {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Add a price");
            }

        } else  {
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("please select an Item");
        }
    }

    private AddShopping addShopping;

    //async task to add into database
    //step 1- add to the transaction
    //step 2- getting the transaction id add to the shopping table
    //step 3- change the remained amount in user account
    private class AddShopping extends AsyncTask<Void, Void, Void> {
        private User loggedInUser;
        private String date;
        private double price;
        private String store;
        private String description;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Utils utils = new Utils(ShoppingActivity.this);
            loggedInUser = utils.getSharedPreferencesLogInInfo();
            date = edtTxtDate.getText().toString();
            price = Double.parseDouble(edtTxtItemPrice.getText().toString());
            store = edtTxtStore.getText().toString();
            description = edtTxtDesc.getText().toString();

            dataBaseHelper = new DataBaseHelper(ShoppingActivity.this);

        }

        @SuppressLint("Range")
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "GetTransactions-> doInBackground: started");

            try {
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

                //step 1- add into transaction table
                ContentValues transactionValues = new ContentValues();
                transactionValues.put("amount", price);
                transactionValues.put("date", date);
                transactionValues.put("type", "shopping");
                transactionValues.put("user_id", loggedInUser.get_id());
                transactionValues.put("description", description);
                transactionValues.put("recipient", store);
                long newTransactionId = db.insert("transactions", null, transactionValues);


                //step 2- add to the shopping table
                ContentValues shoppingValues = new ContentValues();
                shoppingValues.put("item_id", selectedItem.get_id());
                shoppingValues.put("transaction_id", newTransactionId);
                shoppingValues.put("user_id", loggedInUser.get_id());
                shoppingValues.put("price", price);
                shoppingValues.put("description", description);
                shoppingValues.put("date", date);
                long shoppingId = db.insert("shopping", null, shoppingValues);
                Log.d(TAG, "AddShopping->doInBackground: shoppingId"+shoppingId);

                //step 3- change the remained amount
                Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(loggedInUser.get_id())},null,null,null);
                if (null!=cursor){
                    if(cursor.moveToFirst()){
                        double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        //update the value
                        ContentValues amountValues = new ContentValues();
                        amountValues.put("remained_amount",remainedAmount-price);
                        int affectedRows = db.update("users",amountValues,"_id=?",
                                new String[] {String.valueOf(loggedInUser.get_id())});
                        Log.d(TAG, "AddShopping->doInBackground: affected row: "+affectedRows);
                    }
                    cursor.close();
                }
                db.close();
                return null;

            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(ShoppingActivity.this,selectedItem.getName()+" is added successfully",Toast.LENGTH_SHORT);
            Intent intent = new Intent(ShoppingActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != addShopping){
            if (!addShopping.isCancelled()){
                addShopping.cancel(true);
            }
        }
    }

}