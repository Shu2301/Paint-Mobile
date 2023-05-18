package com.example.appmobileproject.viewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmobileproject.Interface.ViewOnClick;
import com.example.appmobileproject.R;
import com.example.appmobileproject.common.common;

public class FileViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    private ViewOnClick viewOnClick;

    public void setViewOnClick(ViewOnClick viewOnClick){
        this.viewOnClick = viewOnClick;
    }

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.image);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnClick.onClick(getAdapterPosition());
            }
        });

        itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                ContextMenu.add(0, 0, getAdapterPosition(), common.DELETE);
            }
        });
    }
}
