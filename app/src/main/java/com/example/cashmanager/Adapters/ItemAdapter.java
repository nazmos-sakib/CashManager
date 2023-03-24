package com.example.cashmanager.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cashmanager.Models.Item;
import com.example.cashmanager.R;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{
    private static final String TAG = "ItemAdapter->";

    //
    public interface GetItem{
        void onGettingItemResult(Item item);
    }
    private GetItem getItem;

    private ArrayList<Item> itemArrayList = new ArrayList<>();
    private Context context;
    private DialogFragment dialogFragment;

    public ItemAdapter(Context context, DialogFragment dialogFragment) {
        this.context = context;
        this.dialogFragment = dialogFragment;
    }

    public ItemAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_item,parent,false);

        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");

        holder.name.setText(itemArrayList.get(position).getName());
        Glide.with(context)
                .asBitmap()
                .load(itemArrayList.get(position).getImage_url())
                .into(holder.imageView);

        holder.parent.setOnClickListener(View->{

            try {
                getItem = (GetItem) dialogFragment;
                getItem.onGettingItemResult(itemArrayList.get(position));

            } catch (ClassCastException c){
                c.printStackTrace();
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    public void setItemArrayList(ArrayList<Item> itemArrayList) {
        this.itemArrayList = itemArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView name;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.itemImage_listitem_item);
            name = itemView.findViewById(R.id.itemName_listitem_item);
            parent = itemView.findViewById(R.id.parent_listitem_item);
        }
    }
}
