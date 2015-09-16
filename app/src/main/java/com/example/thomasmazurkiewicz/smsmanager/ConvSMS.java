package com.example.thomasmazurkiewicz.smsmanager;

/**
 * Created by thomasmazurkiewicz on 12/08/15.
 */
public class ConvSMS {
    private String id_thread;
    private int nbSMS;
    private int nbMMS;
    private String contact;
    private String number;
    private int nbMessage;

    public ConvSMS(String _contact,String _number, String _id_thread,int _nbMessage, int _nbSMS, int _nbMMS) {
        contact = _contact;
        id_thread = _id_thread;
        nbSMS = _nbSMS;
        number = _number;
        nbMMS = _nbMMS;
        nbMessage = _nbMessage;
    }

    public String getIdThread() {
        return id_thread;
    }

    public int getNbSMS() {
        return nbSMS;
    }

    public int getNbMMS() {
        return nbMMS;
    }

    public String toString() {
        String res = contact + "(" + number + ")" + "\nTotal: " + nbMessage + " SMS: " + nbSMS + " MMS: " + nbMMS;
        return res;
    }
}
