package com.example.cashmanager.Dialogs;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmanager.Adapters.ItemAdapter;
import com.example.cashmanager.DataBase.DataBaseHelper;
import com.example.cashmanager.Models.Item;
import com.example.cashmanager.R;

import java.util.ArrayList;

public class SelectItemDialog extends DialogFragment implements ItemAdapter.GetItem {
    private static final String TAG = "SelectItemDialog";

    private ItemAdapter itemAdapter;

    private EditText edtTxtItemName;
    private RecyclerView recyclerView;

    private DataBaseHelper dataBaseHelper;

    //for async task
    private GettingAllItems gettingAllItems;
    private SearchForItem searchForItem;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_item,null);
        dataBaseHelper = new DataBaseHelper(getActivity());
        edtTxtItemName = view.findViewById(R.id.edtTxtItemName_dialogSelectItem);
        recyclerView = view.findViewById(R.id.itemsRecView_dialogSelectItem);

        itemAdapter = new ItemAdapter(getActivity(),this);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        edtTxtItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //need to connect to the database and fetch data when text in edittext change
                //second task instantiate
                searchForItem = new SearchForItem();
                searchForItem.execute(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //first async task instantiate
        //getting all the items
        gettingAllItems = new GettingAllItems();
        gettingAllItems.execute();

        AlertDialog.Builder builder =  new AlertDialog.Builder ( getActivity())
                .setView(view)
                .setTitle ("Select an Item" );


        return builder.create();
    }

    //ItemAdapter.GetItem implementation
    private ItemAdapter.GetItem getItem; //global variable
    @Override
    public void onGettingItemResult(Item item) {
        Log.d(TAG, "onGettingItemResult: item: "+item.toString());

        try {
            getItem = (ItemAdapter.GetItem) getActivity();
            getItem.onGettingItemResult(item);
            dismiss();
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }


    //creating async task to fetch data.
    // two task. one: for getting oll the data. second: according to search text

    //async task for getting all the items
    private class GettingAllItems extends AsyncTask<Void, Void, ArrayList<Item>>{

        @SuppressLint("Range")
        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("items", null, null,
                        null,null,null,null);
                if (null!=cursor){
                    if (cursor.moveToNext()) {
                        ArrayList<Item> itemArrayList = new ArrayList<>();
                        for (int i=0; i<cursor.getCount(); i++) {

                            Item item = new Item();
                            item.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            item.setName(cursor.getString(cursor.getColumnIndex("name")));
                            item.setImage_url(cursor.getString(cursor.getColumnIndex("image_url")));
                            item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            itemArrayList.add(item);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return itemArrayList;
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
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (null!=items){
                itemAdapter.setItemArrayList(items);
            } else {
                itemAdapter.setItemArrayList(new ArrayList<Item>());
            }
        }
    }

    //second task for getting specific result
    private class SearchForItem extends AsyncTask<String, Void, ArrayList<Item>>{

        @SuppressLint("Range")
        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            try {
                SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

                Cursor cursor = db.query("items", null, " name LIKE ?",
                        new String[] {strings[0]},null,null,null);
                if (null!=cursor){
                    if (cursor.moveToNext()) {
                        ArrayList<Item> itemArrayList = new ArrayList<>();
                        for (int i=0; i<cursor.getCount(); i++) {

                            Item item = new Item();
                            item.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            item.setName(cursor.getString(cursor.getColumnIndex("name")));
                            item.setImage_url(cursor.getString(cursor.getColumnIndex("image_url")));
                            item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            itemArrayList.add(item);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return itemArrayList;
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
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (null!=items){
                itemAdapter.setItemArrayList(items);
            } else {
                itemAdapter.setItemArrayList(new ArrayList<Item>());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //to avoid memory leak destroy thread objects
        if (null != gettingAllItems) {
            if (!gettingAllItems.isCancelled()) {
                gettingAllItems.cancel(true);
            }
        }

        //to avoid memory leak destroy thread objects
        if (null != searchForItem) {
            if (!searchForItem.isCancelled()) {
                searchForItem.cancel(true);
            }
        }
    }

}
