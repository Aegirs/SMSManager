package com.example.thomasmazurkiewicz.smsmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by thomasmazurkiewicz on 16/08/15.
 */
public class ProgressDialogRefresh extends AsyncTask<String, Integer, Boolean> {
    private ProgressDialog progressDialog;
    private Context context;
    private String msg;
    private String currentMsg;
    private ConvSMS convSMS;
    private boolean breakAll;
    private String remainingString;
    private long cmpt;
    private int[] max;
    private int currentMax;
    private PowerManager.WakeLock wl;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Method functionRefresh;
    private ServiceListProcess listProcess;
    private int iDdeleteItem;

    public ProgressDialogRefresh(Context ctx,ConvSMS _convSMS,String _msg, int[] _max, Method _fctRefresh) {
        context = ctx;
        msg = _msg;
        max = _max;
        convSMS = _convSMS;
        functionRefresh = _fctRefresh;
        listProcess = ServiceListProcess.getInstance();

        PowerManager pm = (PowerManager) context.getSystemService(ctx.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SMS Manager");


          //  Solution: manage thread process in over view
            Intent resultIntent = new Intent(context, ListProcess.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack
            stackBuilder.addParentStack(ListProcess.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        mNotifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentIntent(resultPendingIntent)
                .setContentTitle("SMSManager")
                .setContentText("Deleting in progress")
                .setSmallIcon(R.drawable.notification_template_icon_bg);

        wl.acquire();
    }

    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        currentMsg = msg  + " SMS";
        progressDialog.setMessage(currentMsg);
        progressDialog.setProgress(0);
        progressDialog.setMax(max[0]);
        currentMax = max[0];

        progressDialog.setCanceledOnTouchOutside(false);
        breakAll = false;
        progressDialog.setButton("Stop", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Use either finish() or return() to either close the activity or just the dialog
                stopThread();
                return;
            }
        });
        progressDialog.show();

        DeleteItem deleteItem = new DeleteItem("Conv",0,max[0],this);
        iDdeleteItem = listProcess.saveDeleteItem(deleteItem);
    }

    public void stopThread() {
        breakAll = true;
    }

    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() */
    protected Boolean doInBackground(String... table) {
        int progress = 0;
        long id;

        Uri tableUri;
        String limitClause;
        String thread_id;
        ContentResolver cr = context.getContentResolver();

        long start = SystemClock.elapsedRealtime();
        long time,timeRemaining;
        cmpt = 0;

        mBuilder.setContentText(currentMsg + " 0/" + currentMax);
        mBuilder.setProgress(currentMax, 0, false);
        mNotifyManager.notify(0, mBuilder.build());

        for(int i = 0 ; i < table.length ; i++ ) {
            progress = 0;
            tableUri = Uri.parse(table[i]);
            thread_id = convSMS.getIdThread();
            limitClause = "LIMIT " + max[i];
            Cursor cursor = cr.query(tableUri, null, "thread_id = ?", new String[]{thread_id}, "date ASC " + limitClause);

            while( cursor.moveToNext() ) {
                if( breakAll ) return false;

                id = cursor.getLong(cursor.getColumnIndex("_id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));

                Uri deleteMessage = Uri.parse(table[i] + id);
                context.getContentResolver().delete(deleteMessage, "date = ?", new String[]{date});

                progress++;
                cmpt++;

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }

                if( cmpt%5 == 1 ) {
                    time = SystemClock.elapsedRealtime() - start;
                    timeRemaining = (time/cmpt)*(max[0]+max[1] - cmpt);
                    timeRemaining /= 1000;

                    remainingString = "";
                    if( (timeRemaining / 3600) > 0 ) {
                        remainingString += (timeRemaining / 3600) + "h ";
                    }
                    if( (timeRemaining%3600)/60 > 0 ) {
                        remainingString += (timeRemaining%3600) / 60 + "m ";
                    }

                    if( (timeRemaining%60) > 0 ) {
                        remainingString += (timeRemaining%60) + "s";
                    }

                    publishProgress(-2);
                }
                publishProgress(progress);
            }
            publishProgress(-1);
            cursor.close();
        }

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        return true;
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(Boolean successed) {
        // When the loop is finished, updates the notification
        if( successed ) {
            Toast.makeText(context, "All Message deleted", Toast.LENGTH_SHORT).show();
            mBuilder.setContentText("Deleting complete");
        }
        else {
            Toast.makeText(context,"Error process interrupted",Toast.LENGTH_SHORT).show();
            mBuilder.setContentText("Deleting has been stop");
        }

        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(0, mBuilder.build());

        listProcess.getList().remove(iDdeleteItem);
        listProcess.refresh();

        try {
            functionRefresh.invoke(context,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        wl.release();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // Update the ProgressBar
        if( progress[0] == -1 ) {
            currentMsg = msg  + " MMS";
            progressDialog.setMessage(currentMsg + "\nTime remaining: " + remainingString);
            progressDialog.setProgress(0);
            progressDialog.setMax(max[1]);

            currentMax = max[1];
            mBuilder.setProgress(currentMax, 0, false);
        }
        else if( progress[0] == -2) {
            progressDialog.setMessage(currentMsg + "\nTime remaining: " + remainingString);
        }
        else {
            progressDialog.setProgress(progress[0]);

            if( !listProcess.getList().isEmpty() ) {
                listProcess.getList().get(iDdeleteItem).progress = progress[0];
                listProcess.refresh();
            }

            // state
            mBuilder.setContentText(currentMsg + " " + progress[0] + "/" + currentMax);
            mBuilder.setProgress(currentMax, progress[0], false);
            // Displays the progress bar for the first time.
            mNotifyManager.notify(0, mBuilder.build());
        }
    }
}
