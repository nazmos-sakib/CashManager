package com.example.cashmanager.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmanager.Models.Loan;
import com.example.cashmanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder>{
    private static final String TAG = "LoanAdapter->";

    private ArrayList<Loan> loanArrayList = new ArrayList<>();
    private Context context;

    public LoanAdapter(Context context) {
        this.context = context;
    }

    public LoanAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loan,parent,false);

        return  new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");

        holder.name.setText(loanArrayList.get(position).getName());
        holder.initDate.setText(loanArrayList.get(position).getInit_date());
        holder.monthlyROI.setText(String.valueOf(loanArrayList.get(position).getMonthly_roi()));
        holder.monthlyPayment.setText(String.valueOf(loanArrayList.get(position).getMonthly_payment()));
        holder.loanAmount.setText(String.valueOf(loanArrayList.get(position).getInit_amount()));
        holder.remainedAmount.setText(String.valueOf(loanArrayList.get(position).getRemained_amount()));
        holder.totalExpectedLoss.setText(String.valueOf(getTotalLoss(loanArrayList.get(position))));
        holder.finishDate.setText(loanArrayList.get(position).getFinish_date());


        if (position%2==0){
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        } else{
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
        }
    }

    private double getTotalLoss(Loan loan) {
        Log.d(TAG, "getTotalLoss: started for "+loan.toString());
        double loss = 0.0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            calendar.setTime(simpleDateFormat.parse(loan.getInit_date()));
            int intMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);
            calendar.setTime(simpleDateFormat.parse(loan.getFinish_date()));
            int finishMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

            int months = finishMonths - intMonths;

            for (int i=0;i<months;i++){
                loss += loan.getInit_amount() * loan.getMonthly_roi()/100;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }

        return loss;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLoanArrayList(ArrayList<Loan> loanArrayList) {
        this.loanArrayList = loanArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return loanArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView name,initDate, monthlyROI,monthlyPayment,loanAmount,remainedAmount,totalExpectedLoss,finishDate;

        private ConstraintLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parentLayout_listItemLoan);

            name = itemView.findViewById(R.id.tv_name2_listItemLoan);
            initDate = itemView.findViewById(R.id.tv_initDate2_listItemLoan);
            monthlyROI = itemView.findViewById(R.id.tv_roi2_listItemLoan);
            monthlyPayment = itemView.findViewById(R.id.tv_monthlyPayment2_listItemLoan);
            loanAmount = itemView.findViewById(R.id.tv_loanAmount2_listItemLoan);
            remainedAmount = itemView.findViewById(R.id.tv_remainedAmount2_listItemLoan);
            totalExpectedLoss = itemView.findViewById(R.id.tv_totalExpectedLoss2_listItemLoan);
            finishDate = itemView.findViewById(R.id.tv_finishDate2_listItemLoan);
        }
    }
}
