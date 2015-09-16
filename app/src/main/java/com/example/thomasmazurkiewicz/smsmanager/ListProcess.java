package com.example.thomasmazurkiewicz.smsmanager;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListProcess extends AppCompatActivity {

    private ServiceListProcess listProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_process);

        listProcess = ServiceListProcess.getInstance();
        if( listProcess != null ) {
            ListView listViewProcess = (ListView)findViewById(R.id.listProcess);

            if( listProcess.getList() != null ) {
                DeleteArrayAdapter adapter = new DeleteArrayAdapter(this,listProcess.getList());
                System.out.println("ici");
                listProcess.saveAdapter(adapter);
                listViewProcess.setAdapter(adapter);

                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_process, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
