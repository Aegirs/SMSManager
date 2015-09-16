package com.example.thomasmazurkiewicz.smsmanager;

import java.util.ArrayList;

/**
 * Created by thomasmazurkiewicz on 16/08/15.
 */
public class ServiceListProcess {
    private static ServiceListProcess instance;

    private ServiceListProcess()
    {
        //Constructor content
        savedList = null;
    }

    public static ServiceListProcess getInstance()
    {
        if(instance == null)
        {
            instance = new ServiceListProcess();
        }
        return instance;
    }

    private ArrayList<DeleteItem> savedList;
    private DeleteArrayAdapter adapter;

    public ArrayList<DeleteItem> getList()
    {
        return savedList;
    }

    public int saveDeleteItem(DeleteItem item)
    {
        if( this.savedList != null ) {
            this.savedList.add(item);
            return this.savedList.size()-1;
        }
        else {
            this.savedList = new ArrayList<DeleteItem>();
            this.savedList.add(item);
            return 0;
        }
    }

    public void saveAdapter(DeleteArrayAdapter adapter) {
        this.adapter = adapter;
    }

    public void refresh(){
        if( this.adapter != null ) {
            adapter.notifyDataSetChanged();
        }
    }
}
