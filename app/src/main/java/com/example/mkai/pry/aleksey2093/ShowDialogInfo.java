package com.example.mkai.pry.aleksey2093;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

/**
 * Класс отвечающий за отображение уведомлений
 */
public class ShowDialogInfo {
    /**
     * Контекст приложения. Присвается при первом запуске во время создания окна авторизации
     */
    public static Context context;
    /**
     * Счетчик уведомлений строки состояния
     */
    private static int notification_count = 0;

    /**
     * Отображение уведомлений в строке состояния
     *
     * @param ticker отображаемый текст в строке состояния
     * @param title  заголовое уведомления
     * @param text   текст уведомления
     */
    public static void showNotification(final String ticker, final String title, final String text) {
        Intent notificationIntent = new Intent(context, Activity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text); // Текст уведомления
        final Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            notification = builder.build();
        else
            notification = builder.getNotification();
        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notificationManager.notify(notification_count, notification);
                notification_count++;
            }
        });
    }


    public static Activity activity;

    /**
     * Вывод всплывающих уведомлений
     *
     * @param text          текст уведомления
     * @param short_or_long короткое или долгое отображение
     * @param gravity       где разместить (Gravity.*)
     */
    public static void showToast(final String text, final boolean short_or_long, final int gravity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast toast;
                    if (short_or_long)
                        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                    else
                        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.setGravity(gravity, 0, 0);
                    toast.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * MessageBox
     *
     * @param title заголовок
     * @param text  текст диалогового окна
     */
    public static void showMsgBox(final String title, final String text) {
        showMsgBox(title,text,activity);
    }

    /**
     * MessageBox
     * @param title заголовок
     * @param text текст диалогового окна
     * @param activity активити окна, из которого происходит вызов
     */
    public static void showMsgBox(final String title, final String text, Activity activity)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
                dlgAlert.setTitle(title);
                dlgAlert.setMessage(text);
                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //finish();
                    }
                });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });
    }
}
