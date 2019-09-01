package com.plus.reader.utils;

import android.widget.Toast;

import com.linksure.api.ApiManager;

/**
 * Created by newbiechen on 17-5-11.
 */

public class ToastUtils {

    public static void show(String msg) {
        Toast.makeText(ApiManager.getInstance().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
