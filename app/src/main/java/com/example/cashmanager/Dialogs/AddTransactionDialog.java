package com.example.cashmanager.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.cashmanager.AddInvestmentActivity;
import com.example.cashmanager.AddLoanActivity;
import com.example.cashmanager.R;
import com.example.cashmanager.ShoppingActivity;
import com.example.cashmanager.TransferActivity;

public class AddTransactionDialog extends DialogFragment  {
    private static final String TAG = "AddTransactionDialog";
    private RelativeLayout shopping,investment,loan,transaction;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transaction,null);

        shopping = view.findViewById(R.id.shoppingRelLayout);
        investment = view.findViewById(R.id.investmentRelLayout);
        loan = view.findViewById(R.id.loanRelLayout);
        transaction = view.findViewById(R.id.transactionRelLayout);

        shopping.setOnClickListener(View->{
            //
            Intent intent = new Intent(getActivity(), ShoppingActivity.class);
            startActivity(intent);
        });

        investment.setOnClickListener(View->{
            //
            Intent intent = new Intent(getActivity(), AddInvestmentActivity.class);
            startActivity(intent);
        });
        loan.setOnClickListener(View->{
            //
            Intent intent = new Intent(getActivity(), AddLoanActivity.class);
            startActivity(intent);

        });
        transaction.setOnClickListener(View->{
            //
            Intent intent = new Intent(getActivity(), TransferActivity.class);
            startActivity(intent);

        });

        AlertDialog.Builder builder =  new AlertDialog.Builder ( getActivity())
                .setTitle ("Add Transaction" )
                . setNegativeButton( "Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setView(view);

        //return super.onCreateDialog(savedInstanceState);

        return builder.create();
    }
}
