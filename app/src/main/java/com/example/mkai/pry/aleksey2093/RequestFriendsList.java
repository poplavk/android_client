package com.example.mkai.pry.aleksey2093;

import android.app.AlertDialog;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;


public class RequestFriendsList {

    private ArrayList<String> listfrends = new ArrayList<String>();

    private static Context context;
    public RequestFriendsList() { }

    public RequestFriendsList(Context context)
    {
        RequestFriendsList.context = context;
    }
    /**
     * Получеие списка подписок
     * @return
     */
    public ArrayList<String> getListFriends() {
        boolean res = downloadListFriends();
        if (res) {
            return listfrends;
        } else {
            return null;
        }
    }

    /**
     * Основной метод загрузки управляющий общим процессом загрузки
     * @return true в случае успеха
     */
    private boolean downloadListFriends() {
        GiveMeSettings giveMeSettings = new GiveMeSettings();
        Socket socket = getSocket(giveMeSettings);
        if (socket == null) {
            errSocket = true;
            return false;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            if (sendMsgToServer(giveMeSettings, outputStream))
                if (readMsgFromServer(giveMeSettings, inputStream))
                    return true;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean errAuth = false;

    private boolean errSocket = false;

    /**
     * Возвращает информацию об ошибке авторизации
     * @return ошибка(true), нет ошибки false
     */
    public boolean isErrAuth() {
        return errAuth;
    }

    /**
     * Возвращает информацию об ошибке сокета
     * @return ошибка(true), нет ошибки false
     */
    public boolean isErrSocket() {
        return errSocket;
    }

    /**
     * Чтение сообщения полученного с сервера
     * @param giveMeSettings указатель на класс настроек
     * @param inputStream входящий поток
     * @return true в случае успеха
     */
    private boolean readMsgFromServer(GiveMeSettings giveMeSettings, DataInputStream inputStream) {
        int len = 0, err = 0;
        byte[] msg = new byte[1];
        try {
            System.out.println("Ожидание ответа на запрос списка подписок");
            while (len == 0) {
                msg = new byte[inputStream.available()];
                len = inputStream.read(msg);
                if (err > 10)
                    if (len == 0) {
                        ShowDialogInfo.showToast("Список подписок не получен. Превышено время ожидания.",
                                false, Gravity.CENTER);
                        return false;
                    } else
                        break;
                else
                    try {
                        err++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            System.out.println("Получен ответ сервера на запрос списка подписок");
            msg = giveMeSettings.getDecryptMsg(msg); //дешифруем
            if (msg[0] == (byte) -1) {
                System.out.println("Сообщение дешифровано неверно");
                return false;
            } else if (msg[1] == (byte) 101) {
                System.out.println("Список подписок. Ошибка авторизации. Неправильный логин или пароль");
                ShowDialogInfo.showMsgBox("Ошибка авторизации", "Неправильный логин или пароль");
                errAuth = true;
                return false;
            } else if (msg[1] != (byte) 1) {
                System.out.println("Получили неправильный ответ с сервера. Тип: " + msg[1]);
                return false;
            }
            System.out.println("Получен ответ. Начинается обработка данных.");
            if (!formationListFriends(msg, len))
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Формирование списка логинов подписок
     * @param msg сообщение полученное с сервера
     * @param len длинна сообщения
     * @return true в случае успеха
     */
    private boolean formationListFriends(byte[] msg, int len) {
        int j = 2;
        ArrayList<String> stringArrayList = new ArrayList<>();
        if (len <= j) {
            System.out.println("Подписчиков нет");
            ShowDialogInfo.showToast("Подписчиков нет",false,Gravity.CENTER);
            listfrends = stringArrayList;
            return true;
        }
        while (j < len) {
            /* ключ пока нигде не используется, поэтому просто будем собирать, но не хранить
            * как вариант можно выводить его в списке подписок и по клику отправлять его
            * на сервер, а не строковый логин */
            int key = java.nio.ByteBuffer.wrap(msg, j, 4).getInt();
            j += 4;
            int len_login = msg[j];
            j++;
            try {
                String tess = new String(msg, j, len_login, "UTF-8");
                stringArrayList.add(tess);
            } catch (UnsupportedEncodingException e) {
                System.out.println("Ошибка обработки имени подписчика");
                e.printStackTrace();
            }
            j += len_login;
        }
        listfrends = stringArrayList;
        return listfrends.size() != 0;
    }

    /**
     * Отправка сообщения на сервер
     * @param giveMeSettings указатель на класс настроек
     * @param outputStream выходной поток
     * @return true в случае успеха
     */
    private boolean sendMsgToServer(GiveMeSettings giveMeSettings, DataOutputStream outputStream) {
        byte[] login = giveMeSettings.getLpk(true);
        byte[] pass = giveMeSettings.getLpk(false);
        byte[] message_byte = new byte[1 + 1 + 1 + login.length + 1 + pass.length];
        int j = 0;
        try {
            message_byte[j] = (byte) giveMeSettings.getEncryption();
            j++;
            message_byte[j] = 1;
            j++;
            message_byte[j] = (byte) login.length;
            j++;
            for (int i = 0; i < login.length; i++, j++) {
                message_byte[j] = login[i];
            }
            message_byte[j] = (byte) pass.length;
            j++;
            for (int i = 0; i < pass.length; i++, j++) {
                message_byte[j] = pass[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            message_byte = giveMeSettings.getDecryptMsg(message_byte);
            outputStream.write(message_byte);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        System.out.println("Отправлен запрос списка подписок");
        return true;
    }

    /**
     * Создание сокета
     * @param giveMeSettings указатель на файл настроек
     * @return возвращает сокет в случае успеха, или null, если неудача
     */
    private Socket getSocket(GiveMeSettings giveMeSettings) {
        int err = 0;
        while (true) {
            try {
                if (!giveMeSettings.isOnline()) {
                    ShowDialogInfo.showToast("Отсутствуют подключение к интернету. " +
                            "Список подписок загрузить невозможно",false,Gravity.CENTER);
                    if (err > 5)
                        return null;
                    err++;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String server = giveMeSettings.getServerName(2);
                int port = giveMeSettings.getServerPort(2);
                Socket socket = null;
                socket = new Socket(server,port);
                return socket;
            } catch (Exception ex) {
                err++;
                if (err > 9) {
                    ShowDialogInfo.showToast("Ошибка подключения. Проверьте подключение к интернету " +
                            "и повторите попытку", false, Gravity.CENTER);
                    return null;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.err.println("Ошибка подключения " + ex.getMessage() + "\n" + ex.toString());
            }
        }
    }
}
