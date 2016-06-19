package com.example.mkai.pry.aleksey2093;


import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.example.mkai.pry.suh.GetSomePrivateData;
import com.example.mkai.pry.suh.PersonInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Класс загрузки последнего результата пользователя
 */
public class GetFriendsLastResult {

    private Context context;
    private ArrayList<PersonInfo> personInfoArrayListResult;

    public GetFriendsLastResult(Context context) {
        this.context = context;
    }

    /**
     * Возвращает результат по пописке в виде списка PersonInfo
     *
     * @param friend имя подписчика, чей результат загружаем
     * @return Результат в виде списка
     */
    public ArrayList<PersonInfo> getPersonInfoArrayListResult(String friend) {
        getLastResult(friend);
        if (personInfoArrayListResult != null)
            return personInfoArrayListResult;
        else
            return new ArrayList<>();
    }

    /**
     * Загрузка последнего результата подписчика
     *
     * @param friend имя подписчика
     */
    private void getLastResult(String friend) {
        GiveMeSettings giveMeSettings = new GiveMeSettings();
        Socket socket = getSocket(giveMeSettings);
        if (socket == null)
            return;
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            if (sendRequestToServer(giveMeSettings, outputStream, friend)) {
                waitServerMsg(giveMeSettings,inputStream, friend);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!socket.isClosed())
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Подклечения сокета сервера подписок
     *
     * @param giveMeSettings указатель на класс настроек
     * @return сокет сервера подписок
     */
    private Socket getSocket(GiveMeSettings giveMeSettings) {
        int err = 0;
        while (err < 3)
            try {
                return new Socket(giveMeSettings.getServerName(2), giveMeSettings.getServerPort(2));
            } catch (IOException e) {
                e.printStackTrace();
                err++;
            }
        showDialogInform(3, null);
        return null;
    }

    /**
     * Отправка запроса на сервер
     *
     * @param giveMeSettings указатель на класс настроек
     * @param outputStream   указатель на выходной поток
     * @param friend         имя подписчика
     * @return true в случае успеха, false - неудачи
     */
    private boolean sendRequestToServer(GiveMeSettings giveMeSettings, DataOutputStream outputStream, String friend) {
        try {
            byte[] login = giveMeSettings.getLpk(true);
            byte[] pass = giveMeSettings.getLpk(false);
            //байт шифр, байт тип, байт длинны логина, логин, байт длинны пароля, пароль, логин друга (чей результат будем смотреть)
            byte[] msg = new byte[1 + 1 + 1 + login.length + 1 + pass.length + 1 + friend.getBytes().length];
            msg[0] = (byte) giveMeSettings.getEncryption();
            msg[1] = 3;
            msg[2] = (byte) login.length;
            int j = 3;
            for (int i = 0; i < login.length; i++, j++)
                msg[j] = login[i];
            msg[j] = (byte) pass.length;
            j++;
            for (int i = 0; i < pass.length; i++, j++)
                msg[j] = pass[i];
            msg[j] = (byte) friend.getBytes().length;
            j++;
            for (int i = 0; i < friend.getBytes().length; i++, j++)
                msg[j] = friend.getBytes()[i];
            msg = giveMeSettings.getEncryptMsg(msg);
            outputStream.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Ожидание ответа от сервера
     *
     * @param inputStream входной поток
     * @param friend      имя подписчика
     */
    private void waitServerMsg(GiveMeSettings giveMeSettings, DataInputStream inputStream, String friend) {
        byte[] msg = new byte[1];
        int len = 0;
        while (len <= 0) {
            try {
                msg = new byte[inputStream.available()];
                len = inputStream.read(msg);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        msg = giveMeSettings.getDecryptMsg(msg); //дешифруем полученное сообщение
        if (msg[0] == (byte)-1) {
            System.out.println("Сообщение дешифровано неверно");
            return;
        } else if (msg[1] == (byte) 103) {
            showDialogInform(1, null);
            return;
        } else if (msg[1] != 3) {
            return;
        } else if (len <= 3) {
            showDialogInform(2, friend);
            return;
        }
        formationListLinks(msg, len, friend);
    }

    /**
     * Формирование результата и вывод на экран. Возможно необходимо для подсистемы отправки эталона.
     *
     * @param msg   байт массив с сервера
     * @param len   длинна массива
     * @param friend имя текущего пользователя
     */
    public void resultSendPhoto(byte[] msg, int len, String friend) {
        formationListLinks(msg, len, friend);
    }

    /**
     * Фомирование результата и вывод на экран.
     *
     * @param msg    байт массив с сервера
     * @param len    длинна массива
     * @param friend имя подписчика (в настоящий момент не используется)
     */
    private void formationListLinks(byte[] msg, int len, String friend) {
        ArrayList<String> arrayList = new ArrayList<>();
        int i = 2;
        while (i < len) {
            int len_link = ByteBuffer.wrap(msg, i, 4).getInt();
            i += 4;
            try {
                String link = new String(msg, i, len_link, "UTF-8");
                link = getIdFromLink(link);
                if (link != null && link.length() != 0)
                    arrayList.add(link);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i += len_link;
        }
        GetSomePrivateData getSomePrivateData = new GetSomePrivateData();
        personInfoArrayListResult = getSomePrivateData.vkGet(arrayList);
    }

    /**
     * Получение id пользователя из ссылки
     *
     * @param link ссылка
     * @return id пользователя
     */
    private String getIdFromLink(String link) {
        String tmp = "";
        if (link.toCharArray()[link.length() - 1] == '/')
            tmp = link.substring(0, link.length() - 2);
        int i = link.length() - 1;
        while (i >= 0 && link.toCharArray()[i] != '/') {
            tmp = link.toCharArray()[i] + tmp;
            i--;
        }
        return tmp;
    }

    /**
     * Отображение диалогового окна с информцией
     *
     * @param what   номер диалового окна
     * @param friend имя подписчика (необязательно, можно оставить пустым)
     */
    private void showDialogInform(final int what, final String friend) {
        switch (what) {
            case 1:
                ShowDialogInfo.showToast("Неправильный логин или пароль",false,Gravity.CENTER);
                break;
            case 2:
                ShowDialogInfo.showToast("Результат" + friend + " пуст", false, Gravity.CENTER);
                break;
            case 3:
                ShowDialogInfo.showToast("Ошибка при подключении к серверу. " +
                        "Проверьте свое подключение к интернету и повторите попытку.",
                        false, Gravity.CENTER);
                break;
        }
    }
}
