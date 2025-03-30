//package com.example.myapplication.Adapters;
//
//public class FeatListModelAdapter2 {
//}


package com.example.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.FeatListModel2;
import com.example.myapplication.ProcessingActivity;
import com.example.myapplication.R;

import java.util.ArrayList;

public class FeatListModelAdapter2 extends RecyclerView.Adapter<FeatListModelAdapter2.viewHolder> {
    ArrayList<FeatListModel2> list;
    Context context;

    public FeatListModelAdapter2(ArrayList<FeatListModel2> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        FeatListModel2 model = list.get(position);
        holder.imageView.setImageResource(model.getImg());
        holder.textView.setText(model.getFeat_name());

//        expriment doing

        DisplayMetrics displayMetrics = holder.itemView.getContext().getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = screenWidth / 2; // Set each item to half of screen width
        holder.itemView.setLayoutParams(layoutParams);


//        tilll here

        holder.itemView.setOnClickListener((view) -> {
            Intent intent = new Intent(view.getContext(), ProcessingActivity.class);
            String text = holder.textView.getText().toString();
            intent.putExtra("text_key", text);
            view.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.feat_img);
            textView = itemView.findViewById(R.id.feat_title);
        }
    }
}

