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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity {
    private static final String TAG = "TransferActivity->";

    //view variables
    private EditText edTxt_amount, edTxt_recipient, edTxt_date, edTxt_description;
    private Button btn_picDate, btn_addTransfer;
    private RadioGroup radioBtn_transferType;
    private TextView tv_warning;

    //setting up date picker listener
    //its a variable
    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener transferDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) { //i-year, i1-month, i2-day

            //converting user input to calender object
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            //converting calender object to simple readable string date format
            edTxt_date.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    private DataBaseHelper dataBaseHelper;

    //ASYNC
    private AddTransaction addTransactionTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        dataBaseHelper = new DataBaseHelper(this);

        initViews();

        setOnclickListeners();
    }

    private void setOnclickListeners() {
        btn_picDate.setOnClickListener(View -> {
            new DatePickerDialog(TransferActivity.this, transferDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();

        });

        btn_addTransfer.setOnClickListener(View -> {

            if (validateData()) {
                tv_warning.setVisibility(android.view.View.GONE);
                initAddToTransaction();
            } else {
                tv_warning.setVisibility(android.view.View.VISIBLE);
                tv_warning.setText("Please fill all the field");
            }
        });

    }

    private void initAddToTransaction() {
        Log.d(TAG, "initAddToTransaction: started");
        Utils utils = new Utils(this);
        User user = utils.getSharedPreferencesLogInInfo();

        if (null != user) {
            //TODO
            addTransactionTask = new AddTransaction();
            addTransactionTask.execute(user.get_id());
        }

    }

    //async task
    private class AddTransaction extends AsyncTask<Integer, Void, Void> {

        private double amount;
        private String recipient, date, description, type;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            amount = Double.parseDouble(edTxt_amount.getText().toString());
            recipient = edTxt_recipient.getText().toString();
            date = edTxt_date.getText().toString();
            description = edTxt_description.getText().toString();
            type = edTxt_recipient.getText().toString();

            /*radioBtn_transferType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.radio_receive_transferActivity:
                            type = "receive";
                            break;
                        case R.id.radio_send_transferActivity:
                            type = "send";
                            amount = -amount;
                            break;
                        default:
                            break;
                    }
                }
            });*/

            int rbId = radioBtn_transferType.getCheckedRadioButtonId();
            switch (rbId) {
                case R.id.radio_receive_transferActivity:
                    type = "receive";
                    break;
                case R.id.radio_send_transferActivity:
                    type = "send";
                    amount = -amount;
                    break;
                default:
                    break;
            }
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();


                ContentValues values = new ContentValues();
                values.put("amount", amount);
                values.put("date", date);
                values.put("type", type);
                values.put("user_id", integers[0]);
                values.put("recipient", recipient);
                values.put("description", description);


                long transactionId = db.insert("transactions", null, values);
                Log.d(TAG, "doInBackground------insertingIntoDatabase: transactionId- " + transactionId);

                if (transactionId != -1) {
                    Cursor cursor = db.query("users", new String[]{"remained_amount"}, "_id=?",
                            new String[]{String.valueOf(integers[0])}, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            //update the value
                            ContentValues amountValues = new ContentValues();
                            amountValues.put("remained_amount", remainedAmount + amount);
                            int affectedRows = db.update("users", amountValues, "_id=?",
                                    new String[]{String.valueOf(integers[0])});
                            Log.d(TAG, "AddTransaction->doInBackground: affected row: " + affectedRows);
                            db.close();
                        }
                        cursor.close();
                    }
                }
                db.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent intent = new Intent(TransferActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private boolean validateData() {
        Log.d(TAG, "validateData: started");
        if (edTxt_amount.getText().toString().equals("")) {
            return false;
        }

        if (edTxt_recipient.getText().toString().equals("")) {
            return false;
        }
        if (edTxt_date.getText().toString().equals("")) {
            return false;
        }
        if (edTxt_description.getText().toString().equals("")) {
            return false;
        }

        int selectedId = radioBtn_transferType.getCheckedRadioButtonId(); // get the selected radio button's resource ID

        Log.d(TAG, "validateData: radiobutton--->" + selectedId);
        if (selectedId == -1) {
            // no radio button is selected
            tv_warning.setVisibility(android.view.View.VISIBLE);
            tv_warning.setText("select transfer type");
            return false;
        }

        return true;
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");

        edTxt_amount = findViewById(R.id.ed_amount_transferActivity);
        edTxt_recipient = findViewById(R.id.ed_recipient_transferActivity);
        edTxt_date = findViewById(R.id.ed_date_transferActivity);
        edTxt_description = findViewById(R.id.ed_description_transferActivity);

        btn_picDate = findViewById(R.id.btn_picDate_transferActivity);
        btn_addTransfer = findViewById(R.id.btn_add_transferActivity);

        radioBtn_transferType = findViewById(R.id.radioGroup_transferActivity);

        tv_warning = findViewById(R.id.tv_warning_transferActivity);

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

    }
}