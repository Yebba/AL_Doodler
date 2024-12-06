package com.example.al_doodler.viewHolder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.al_doodler.Interface.ViewOnClick;
import com.example.al_doodler.R;

public class FileViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    private ViewOnClick viewOnClick;

    public void setViewOnClick(ViewOnClick viewOnClick) {
        this.viewOnClick = viewOnClick;
    }

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.image);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                viewOnClick.onClick(getAdapterPosition());
            }
        });
    }
}
