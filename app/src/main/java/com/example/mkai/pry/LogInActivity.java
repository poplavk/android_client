package com.example.mkai.pry;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.mkai.pry.aleksey2093.GiveMeSettings;
import com.example.mkai.pry.aleksey2093.ListenResultFromServer;
import com.example.mkai.pry.aleksey2093.ShowDialogInfo;
import com.example.mkai.pry.subscription.SubscriptionActivity;

public class LogInActivity extends Activity implements OnClickListener {
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        GiveMeSettings giveMeSettings = new GiveMeSettings(this, getSharedPreferences("pry_settings", Context.MODE_PRIVATE));

        String tmp = giveMeSettings.getLpkString_Commit(true);
        ((EditText) this.findViewById(R.id.input_login)).setText(tmp);
        tmp = giveMeSettings.getLpkString_Commit(false);
        ((EditText) this.findViewById(R.id.input_password)).setText(tmp);

        ShowDialogInfo.context = this;
        ShowDialogInfo.activity = this;
        if (!GiveMeSettings.isOnline())
            ShowDialogInfo.showMsgBox("Нет интернета","Отсутствует подключение к интернету. " +
                    "Функции приложения требующие подключние к интернту не будут работать!");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                saveLPK();
                Intent intent = new Intent(LogInActivity.this, SubscriptionActivity.class);
                startActivity(intent);
            default:
                break;
        }
    }

    /**
     * Сохранение логина и пароля
     */
    private void saveLPK() {
        final String login = ((EditText) this.findViewById(R.id.input_login)).getText().toString();
        final String password = ((EditText) this.findViewById(R.id.input_password)).getText().toString();
        CheckBox checkBox = (CheckBox) this.findViewById(R.id.checkBox);
        GiveMeSettings giveMeSettings = new GiveMeSettings();
        if (checkBox.isChecked())
            giveMeSettings.setLpkString_Commit(login,password);
        giveMeSettings.setLpkString(login,password);
    }
}
