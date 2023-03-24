package com.example.cashmanager.Adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmanager.Models.Transaction;
import com.example.cashmanager.R;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private static final String  TAG = "TransactionAdapter";

    ArrayList<Transaction> transactions = new ArrayList<>();

    public TransactionAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: started");
        //View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transaction,null);
        //this class is not an activity class. that is why we cannot use getActivity function

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_transaction,parent,false);
        
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "------onBindViewHolder: called");

        holder.txtDate.setText(transactions.get(position).getDate());
        holder.txtDesc.setText(transactions.get(position).getDescription());
        holder.txtTransactionId.setText("Transaction Id: "+String.valueOf(transactions.get(position).get_id()));
        holder.txtSender.setText(transactions.get(position).getRecipient() );

        double amount = transactions.get(position).getAmount();
        if (amount>0){
            holder.txtAmount.setText("+ "+amount);
            holder.txtAmount.setTextColor(Color.GREEN);
        }else {
            holder.txtAmount.setText("- "+amount);
            holder.txtAmount.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }


    public void setTransactions(ArrayList<Transaction> transactions) {
        Log.d(TAG, "TransactionAdapter-> setTransactions: ");
        this.transactions = transactions;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtAmount, txtDesc, txtDate, txtSender, txtTransactionId;
        private final CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtSender = (TextView) itemView.findViewById(R.id.txtSender);
            txtTransactionId = (TextView) itemView.findViewById(R.id.txtTransaction);
            parent = (CardView) itemView.findViewById(R.id.parent_listitem_item);
        }
    }
}
