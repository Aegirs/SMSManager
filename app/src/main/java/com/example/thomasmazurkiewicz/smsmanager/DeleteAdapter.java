package com.example.thomasmazurkiewicz.smsmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by thomasmazurkiewicz on 17/08/15.
 */

class DeleteItem {
    public String name;
    public Integer progress;  // How much is delete
    public Integer max;    // max delete
    public ProgressDialogRefresh threadCtx;

    public DeleteItem(String name, Integer progress, Integer max,ProgressDialogRefresh threadCtx) {
        this.name = name;
        this.progress = progress;
        this.max = max;
        this.threadCtx = threadCtx;
    }
}

class DeleteArrayAdapter extends BaseAdapter {
    private ArrayList<DeleteItem> listProcess;
    private Context ctx;

    public DeleteArrayAdapter(Context ctx, ArrayList<DeleteItem> listProcess) {
        this.ctx = ctx;
        this.listProcess = listProcess;
    }

    @Override
    public int getCount() {
        if( listProcess != null) {
            return listProcess.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if( listProcess != null) {
            return listProcess.get(position);
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if(row == null) {
            // Inflate
            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row, parent, false);
        }

        if( position < listProcess.size() ) {
            final DeleteItem item = listProcess.get(position);
            ProgressBar downloadProgressBar = (ProgressBar) row.findViewById(R.id.status_delete);
            TextView textProgress = (TextView) row.findViewById(R.id.textProgress);
            TextView textPerCent = (TextView) row.findViewById(R.id.textPerCent);
            TextView nameDelete = (TextView) row.findViewById(R.id.nameDelete);

            textProgress.setText(item.progress + "/" + item.max);
            textPerCent.setText(100*item.progress/item.max + " %");
            nameDelete.setText(item.name);

            final Button stopButton = (Button) row.findViewById(R.id.button_stop);

            stopButton.setTag(item);
            downloadProgressBar.setMax(item.max);
            downloadProgressBar.setProgress(item.progress);

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.threadCtx.stopThread();
                    listProcess.remove(item);
                    notifyDataSetChanged();
                }
            });
        }


        return row;
    }
}
