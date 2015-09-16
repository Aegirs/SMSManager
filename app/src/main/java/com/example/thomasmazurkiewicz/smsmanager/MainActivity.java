package com.example.thomasmazurkiewicz.smsmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listConv;
    private SwipeRefreshLayout swipeLayout;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefaultApp();

        listConv = (ListView)findViewById(R.id.listConv);
        listConv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listConv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final ConvSMS itemSelected = (ConvSMS) listConv.getAdapter().getItem(position);
                createDialogDelete(itemSelected);
            }
        });

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListConv();
                // Notify swipeRefreshLayout that the refresh has finished
                swipeLayout.setRefreshing(false);
            }
        });

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getListConv();

        /*ProgressDialog progressTmp = (ProgressDialog)getIntent().getBundleExtra("refresh").get("progress");
        if( progressTmp != null ) {
            if( !progressTmp.isShowing() ) {
                progressTmp.show();
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if ( id == R.id.action_set_default ) {
            setDefaultApp();
            return true;
        }
        else if ( id == R.id.action_open_thread ) {
            Intent intent =
                    new Intent(this,ListProcess.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setDefaultApp() {
        if(Build.VERSION.SDK_INT >= 19 ){
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                // App is not default.
                Intent intent =
                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        myPackageName);
                startActivity(intent);
            } else {
                // App is the default.
            }
        }
    }

    public void getListConv() {
        Uri allConv = Uri.parse("content://mms-sms/conversations?simple=true");
        ContentResolver cr = getContentResolver();
        Cursor curConv = cr.query(allConv, null, null, null, null);
        Cursor curIds;

        List<ConvSMS> listTmp = new ArrayList<ConvSMS>();
        String stringConv, nameContact;
        int nbSMS, nbMMS, nbMessage;
        String recipientId, address;

        if (curConv != null) {
            System.out.println("Nb conv: " + curConv.getCount());
            while( curConv.moveToNext() ) {

                recipientId = curConv.getString(curConv.getColumnIndexOrThrow("recipient_ids"));
                curIds =  getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id = " + recipientId, null, null);

                if(curIds.moveToFirst())
                {
                    address = curIds.getString(curIds.getColumnIndexOrThrow("address")).toString();
                    curIds.close();

                    nameContact = getContactName(getApplicationContext(), address);
                    if( nameContact == null) {
                        stringConv = address;
                    }
                    else {
                        stringConv = nameContact;
                    }

                    nbMessage = curConv.getInt(curConv.getColumnIndexOrThrow("message_count"));
                    nbSMS = countTable("content://sms", "thread_id = ?", new String[]{recipientId});
                    nbMMS = countTable("content://mms","thread_id = ?",new String[]{recipientId});

                    listTmp.add( new ConvSMS(stringConv,address,recipientId,nbMessage,nbSMS,nbMMS));
                }
            }

            listConv.setAdapter(new ArrayAdapter<ConvSMS>(this, android.R.layout.simple_list_item_1, listTmp));
            //close the cursor
            curConv.close();
        }
        else {
            listConv.setAdapter(null);
        }
    }

    public int countTable(String table, String selection,String[] argsSelection) {
        Uri allMessage = Uri.parse(table);
        ContentResolver cr = getContentResolver();
        int cmpt = 0;

        Cursor cursor = cr.query(allMessage, null, selection, argsSelection, null);

        if( cursor.moveToFirst() ) {
            cmpt = cursor.getCount();
            cursor.close();
        }

        return cmpt;
    }

    public void getTable(String pathTable,String type, String selection, String[] argsSelection) {
        Uri data = Uri.parse(pathTable);
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(data, null, selection, argsSelection, null);

        if (cursor != null) {
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            while( cursor.moveToNext() ) {
                String[] columns = cursor.getColumnNames();
                String contentColumn;

                stringBuilder.append("Start " + type + ":\n");
                for( String column_name: columns){
                    contentColumn = cursor.getString(cursor.getColumnIndex(column_name));
                    if( column_name.equals("address") ) {
                        stringBuilder.append("person: " + getContactName(getApplicationContext(),contentColumn) + "\n");
                    }

                    stringBuilder.append("column " + column_name + " = " + contentColumn + "\n");
                }
                stringBuilder.append("End " + type + "\n\n");
            }

            System.out.println(stringBuilder.toString());
            //close the cursor
            cursor.close();
        }
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    private void createDialogDelete(final ConvSMS itemSelected) {

        final int maxSMS = itemSelected.getNbSMS();
        final int maxMMS = itemSelected.getNbMMS();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputSaveSMS = new EditText(this);
        inputSaveSMS.setHint("Nb. SMS");
        inputSaveSMS.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputSaveSMS.setActivated(true);

        InputFilter limitFilter = new MinMaxInputFilter(0, maxSMS);
        inputSaveSMS.setFilters(new InputFilter[]{limitFilter});

        layout.addView(inputSaveSMS);

        final EditText inputSaveMMS = new EditText(this);
        inputSaveMMS.setHint("Nb. MMS");
        inputSaveMMS.setInputType(InputType.TYPE_CLASS_NUMBER);

        limitFilter = new MinMaxInputFilter(0, maxMMS);
        inputSaveMMS.setFilters(new InputFilter[]{limitFilter});

        layout.addView(inputSaveMMS);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(null).setTitle("Choose number to save:").setView(layout).setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int whichButton) {

                        // remove message here
                        String inputSMS = inputSaveSMS.getText().toString();
                        String inputMMS = inputSaveMMS.getText().toString();

                        int nbSaveSMS = 0, nbSaveMMS = 0;

                        if (!inputSMS.isEmpty()) nbSaveSMS = Integer.parseInt(inputSMS);
                        if (!inputMMS.isEmpty()) nbSaveMMS = Integer.parseInt(inputMMS);


                        System.out.println("NbSave SMS: " + nbSaveSMS);
                        System.out.println("NbSave MMS: " + nbSaveMMS);

                        int[] nbDelete = {maxSMS - nbSaveSMS,maxMMS - nbSaveMMS};

                        System.out.println("NbDelete SMS: " + nbDelete[0]);
                        System.out.println("NbDelete MMS: " + nbDelete[1]);
                        deleteMsg(nbDelete,itemSelected);
                    }

                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int whichButton) {
                    }
                });
        alert.show();
    }

    public void deleteMsg(int[] nbDelete,ConvSMS itemSelected) {
        try {
            Method functionRefresh = MainActivity.class.getMethod("getListConv", null);
            new ProgressDialogRefresh(this,itemSelected,"Message Deleted: ",nbDelete,functionRefresh).execute("content://sms/", "content://mms/");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}