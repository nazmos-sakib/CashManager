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

import com.example.cashmanager.Models.Investment;
import com.example.cashmanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder>{
    private static final String TAG = "InvestmentAdapter->";

    private ArrayList<Investment> investmentArrayList = new ArrayList<>();

    private Context context;

    public InvestmentAdapter(Context context) {
        this.context = context;
    }

    public InvestmentAdapter() {
    }

    @NonNull
    @Override
    public InvestmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_investment,parent,false);

        return  new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");

        holder.name.setText(investmentArrayList.get(position).getName());
        holder.initDate.setText(investmentArrayList.get(position).getInit_date());
        holder.monthlyROI.setText(String.valueOf( investmentArrayList.get(position).getMonthly_roi()));
        holder.amount.setText(String.valueOf(investmentArrayList.get(position).getAmount()));
        holder.finishDate.setText(investmentArrayList.get(position).getFinish_date());

        holder.profit.setText(String.valueOf(getTotalProfit(investmentArrayList.get(position))));

        if (position%2==0){
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        } else{
            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
        }
    }

    private double getTotalProfit(Investment investment) {
        Log.d(TAG, "getTotalProfit: calculation total profit for " + investment.toString());

        double profit = 0.0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            calendar.setTime(simpleDateFormat.parse(investment.getInit_date()));
            int intMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);
            calendar.setTime(simpleDateFormat.parse(investment.getFinish_date()));
            int finishMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

            int months = finishMonths - intMonths;

            for (int i=0;i<months;i++){
                profit += investment.getAmount() * investment.getMonthly_roi()/100;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }

        return profit;
    }


    public void setInvestmentArrayList(ArrayList<Investment> investmentArrayList) {
        this.investmentArrayList = investmentArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return investmentArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView name,initDate, monthlyROI,amount,profit,finishDate;

        private ConstraintLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parentLayout_listItemInvestment);

            name = itemView.findViewById(R.id.tv_name2_listItemInvestment);
            initDate = itemView.findViewById(R.id.tv_initDate2_listItemInvestment);
            monthlyROI = itemView.findViewById(R.id.tv_roi2_listItemInvestment);
            amount = itemView.findViewById(R.id.tv_amount2_listItemInvestment);
            profit = itemView.findViewById(R.id.tv_profit2_listItemInvestment);
            finishDate = itemView.findViewById(R.id.tv_finishDate2_listItemInvestment);
        }
    }
}
