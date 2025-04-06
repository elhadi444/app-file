package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onDownload(String filename);
        void onDelete(String filename);
    }

    private List<String> fileList;
    private OnItemActionListener listener;

    public FileAdapter(List<String> fileList, OnItemActionListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button btnDownload, btnDelete;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textFile);
            btnDownload = view.findViewById(R.id.btnDownload);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {
        String filename = fileList.get(position);
        holder.textView.setText(filename);
        holder.btnDownload.setOnClickListener(v -> listener.onDownload(filename));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(filename));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
}

