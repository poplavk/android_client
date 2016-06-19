package com.example.mkai.pry.aleksey2093;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.Gravity;

import com.example.mkai.pry.suh.GetSomePrivateData;
import com.example.mkai.pry.suh.PersonInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 * Класс постоянно прослушивает сообщения с сервера, что поймать сообщение о входящем результате подписчика.
 */
public class ListenResultFromServer {

    /**
     * Статическая переменная потока в котором работают методы текущего класса
     */
    private static Thread thread;

    /**
     * Указатель на context для обновления информации в интерфейсе
     */
    private static Context context;

    public ListenResultFromServer(Context context)
    {
        ListenResultFromServer.context = context;
    }

    /**
     * Запуск методов класса в отдельном потоке
     */
    public void startListenThread() {
        thread = new Thread(new Runnable() {
            public void run() {
                int err = 0;
                while (true) {
                    System.out.println("Прослушка запущена");
                    listenServer();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        err++;
                        System.out.println("Ошибка прослушки " + err + ": " + ex.getMessage());
                    }
                }
            }
        });
        thread.setName("Прослушка сервера");
        thread.start();
    }

    /**
     * Остановка потока текущего класса
     */
    public void stopListenThread() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread.interrupt();
        thread.stop();
    }

    private static ServerSocket serverSocket;

    /**
     * Ожидание входящих подключений
     */
    private void listenServer() {
        GiveMeSettings giveMeSettings = new GiveMeSettings();
        serverSocket = getServerSocket(giveMeSettings);
        if (serverSocket == null) {
            System.out.println("Ошибка при создании сокета прослушки");
            return;
        } else
            System.out.println("Создан сокет прослушки");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Сокет прослушки получил новое соединение");
                startSocketNewAccept(giveMeSettings, socket);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
    }

    /**
     * Создание сокета типа сервер для ожидания входящих подключений
     * @param giveMeSettings указатель на класс настроек
     * @return сокет
     */
    private ServerSocket getServerSocket(GiveMeSettings giveMeSettings) {
        int err = 0;
        while (true) {
            try {
                if (GiveMeSettings.isOnline())
                    return new ServerSocket(giveMeSettings.getServerPort(3));
                else {
                    if (err < 5)
                        err++;
                    else
                        try {
                            System.out.println("Ошибка подключения к интернету");
                            ShowDialogInfo.showToast("Ошибка подключения к интернету",false, Gravity.CENTER);
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            } catch (Exception ex) {
                System.out.println("Не удалось создать сокет для прослушивания ответов с сервера. Ошибка: " +
                        ex.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                err++;
                if (err > 9) {
                    System.out.println("Количество попыток создания сокета 9. " +
                            "Проверьте настройки приложения и устройства.");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        }
    }

    /**
     * Получение входящего сообщения
     * @param giveMeSettings указатель на класс настроек
     * @param socket указатель на сокет
     */
    private boolean startSocketNewAccept(GiveMeSettings giveMeSettings, Socket socket) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            int len = 0, err = 0;
            byte[] msg = new byte[0];
            System.out.println("Прослушка ожидает входящее сообщение");
            while (len == 0) { //запускаем прослушку
                msg = new byte[inputStream.available()];
                len = inputStream.read(msg);
                if (err > 10) {
                    System.out.println("Превышено время ожидания входящего сообщения");
                    return false;
                } else
                    try {
                        err++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            System.out.println("Прослушка получило новое сообщение от сервера");
            msgPostsProcessing(giveMeSettings,msg, len);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Вызов диалоговых окно
     * @param what номер диалогового окна
     * @param friend имя подписчика
     * @param msg входящее сообщения с сервера
     * @param len длинна сообщения
     */
    private void getResDialogWindow(final int what, final String friend, final byte[] msg, final int len) {
        switch (what) {
            case 1:
                ShowDialogInfo.showNotification("Новый результат в pry", "Уведомление",
                        "Новый результат подписчка " + friend + ". Чтобы посмотреть результат откройте приложение");
                break;
            case 2:
                System.out.println("Прослушкой получен пустой результат");
                break;
            case 3:
                System.out.println("Неправильный логин или пароль");
                break;
        }
    }

    /**
     * Обработка входящего сообщения. Проверка на ошибки и передача на извлечение массива ссылок
     * @param giveMeSettings указатель на класс настроек
     * @param msg входящее сообщние
     * @param len длинна сообщения
     */
    private void msgPostsProcessing(GiveMeSettings giveMeSettings, byte[] msg, int len) {
        msg = giveMeSettings.getDecryptMsg(msg);//дешифруем
        if (msg[0] == (byte) -1) {
            System.out.println("Сообщение дешифровано неверно");
        } else if (msg[1] == (byte) 102) {
            System.out.println("Неправильный логин или пароль. Тип: " + msg[1]);
            getResDialogWindow(3, null, null, -1);
        } else if (msg[1] != 2) {
            System.out.println("Получили левое сообщение. Продолжаем прослушку.");
        } else if (msg[2] < 1) {
            System.out.println("Сообщение неверно дешефровано или отправлено. " +
                    "Длинна логина указана как отрицательная. Продолжаем прослушку.");
        } else {
            try {
                String login = new String(msg, 3, msg[2], "UTF-8");
                /*if (!getResDialogWindow(1,login))
                    return;
                //formationListLinks(msg,len,login);*/
                //getResDialogWindow(1,login,msg,len); //окно уведомления о входящем результата
                //mainFormController.getRadioButton(login);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Формирование списка ссылок
     * @param msg входящее сообщение
     * @param len длинна сообщения
     * @param friend имя подписчика
     */
    private void formationListLinks(byte[] msg, int len, String friend) {
        //Мы получили от пользователя разрешение посмотреть на результат запрос от пользователя
        int jb = 3 + msg[2];
                /*и так мы получили список ссылок в виде (4 байта длинна, ссылка, 4 байта длинна, ссылка....).
                * Начинаем его обрабатывать и потом передать в систему выдачи */
        if (jb >= len) {
            getResDialogWindow(2,null,null,-1);
            return;
        }
        ArrayList<String> links = new ArrayList<String>();
        while (jb < len) {
            int size = ByteBuffer.wrap(msg, jb, 4).getInt();
            jb += 4;
            try {
                String link = null;
                link = new String(msg, jb, size, "UTF-8");
                link = getIdFromLink(link);
                if (link != null && link.length() != 0)
                    links.add(link);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            jb += size;
        }
        GetSomePrivateData getSomePrivateData = new GetSomePrivateData();
        showWindowResult(getSomePrivateData.vkGet(links),friend);
    }

    /**
     * Вызов окна с результатом подписчика
     * @param list Данные из соц. сетей по результату
     * @param friend имя подписчика
     */
    private boolean showWindowResult(final ArrayList<PersonInfo> list, final String friend) {

        /*Platform.runLater(new Runnable() {
            public void run() {
                Stage stage = new Stage();
                Parent root = null;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("resultsForm.fxml"));
                    root = loader.load();
                    ResultsFormController resultsFormController = loader.<ResultsFormController>getController();
                    resultsFormController.setParametr(list);
                    resultsFormController.getScrollPaneResult();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Scene scene = new Scene(root, 600, 790);
                stage.setTitle("Результаты поиска для подписки на " + login);
                stage.setScene(scene);
                stage.show();
            }
        });*/
        return true;
    }

    /**
     * Извлечение id пользователя из ссылки
     * @param link ссылка
     * @return id пользователя
     */
    private String getIdFromLink(String link) {
        String tmp = "";
        if (link.toCharArray()[link.length() - 1] == '/')
            tmp = link.substring(0, link.length() - 2);
        int i = link.length() - 1;
        while (i >= 0 && link.toCharArray()[i] != '/'){
            tmp = link.toCharArray()[i] + tmp;
            i--;
        }
        return tmp;
    }
}
