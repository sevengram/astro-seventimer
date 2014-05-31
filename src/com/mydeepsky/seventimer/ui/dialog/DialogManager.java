package com.mydeepsky.seventimer.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.mydeepsky.android.location.Locator;
import com.mydeepsky.seventimer.R;

public class DialogManager {
    public static Dialog createRefreshDialog(Context activity) {
        Dialog refreshDialog = new Dialog(activity, R.style.RefreshDialog);
        refreshDialog.setContentView(R.layout.refresh_view);
        refreshDialog.setCanceledOnTouchOutside(false);
        return refreshDialog;
    }

    public static Dialog createProgressDialog(Context activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(R.string.title_wait);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(activity.getText(R.string.text_processing));
        return progressDialog;
    }

    public static Dialog createUpdateDialog(Context context, String message,
            DialogInterface.OnClickListener positiveListener,
            DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(R.string.dialog_title_update);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_update_now, positiveListener);
        builder.setNegativeButton(R.string.btn_cancel, negativeListener);
        builder.setCancelable(false);
        return builder.create();
    }

    public static Dialog createNetworkDialog(final Context context,
            DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_network);
        builder.setMessage(R.string.dialog_message_service);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_wifi, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ((Activity) context).startActivityForResult(new Intent(
                        Settings.ACTION_WIFI_SETTINGS), Locator.LOCATION_SETTINGS_REQUEST_CODE);
            }
        });
        builder.setNeutralButton(R.string.btn_mobile, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ((Activity) context).startActivityForResult(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS), Locator.LOCATION_SETTINGS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, negativeListener);
        return builder.create();
    }

    public static Dialog createLocationDialog(final Context context,
            DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_location);
        builder.setMessage(R.string.dialog_message_service);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ((Activity) context).startActivityForResult(new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        Locator.LOCATION_SETTINGS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton(R.string.btn_no, negativeListener);
        return builder.create();
    }

}
