package com.statbike.valtan.statbike;

import android.graphics.Color;

public class colortab {

    /*
     * @double speed
     * given the speed, return the corresponded color, based on the percentage of the
     * maximum speed
     * */
    public static int speedToColor(double speed, double vmax) {

        if ((int)speed < 0.05*vmax) {
            return Color.parseColor("#00FF00");
        } else if ((int)speed < 0.10*vmax) {
            return Color.parseColor("#00FF99");
        } else if ((int)speed < 0.15*vmax) {
            return Color.parseColor("#00FFCC");
        } else if ((int)speed < 0.2*vmax) {
            return Color.parseColor("#00FFFF");
        } else if ((int)speed < 0.25*vmax) {
            return Color.parseColor("#33CCFF");
        } else if ((int)speed < 0.30*vmax) {
            return Color.parseColor("#3399FF");
        } else if ((int)speed < 0.35*vmax) {
            return Color.parseColor("#0099FF");
        } else if ((int)speed < 0.4*vmax) {
            return Color.parseColor("#0066FF");
        } else if ((int)speed < 0.45*vmax) {
            return Color.parseColor("#0000FF");
        } else if ((int)speed < 0.5*vmax) {
            return Color.parseColor("#3366ff");
        } else if ((int)speed < 0.55*vmax) {
            return Color.parseColor("#6666FF");
        } else if ((int)speed < 0.6*vmax) {
            return Color.parseColor("#6600FF");
        } else if ((int)speed < 0.66*vmax) {
            return Color.parseColor("#9933FF");
        } else if ((int)speed < 0.7*vmax) {
            return Color.parseColor("#CC00FF");
        } else if ((int)speed < 0.75*vmax) {
            return Color.parseColor("#FF00FF");
        } else if ((int)speed < 0.8*vmax) {
            return Color.parseColor("#FF33CC");
        } else if ((int)speed < 0.85*vmax) {
            return Color.parseColor("#FF3399");
        } else if ((int)speed < 0.9*vmax) {
            return Color.parseColor("#FF0066");
        } else if ((int)speed < 0.95*vmax) {
            return Color.parseColor("#FF5050");
        } else {
            return Color.parseColor("#FF0000");
        }
    }


}
