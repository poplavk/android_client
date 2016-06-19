package com.example.mkai.pry.aleksey2093;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.lang.String;
import java.util.Properties;


public class GiveMeSettings {

    private static Context context;
    private static SharedPreferences preferences;

    public GiveMeSettings() {

    }

    /**
     * Первоначальное создание файла настроек с установлением контекста и параметров
     * @param context  контекст
     * @param preferences  параметры
     */
    public GiveMeSettings(Context context, SharedPreferences preferences) {
        GiveMeSettings.context = context;
        GiveMeSettings.preferences = preferences;
    }

    /**
     * Загружает файл настроек по умолчанию из assets
     * @return Properties с настройками
     */
    private Properties loadSettingFile() {
        Properties property = new Properties();
        try {
            Resources resources = context.getResources();
            AssetManager assetManager = resources.getAssets();
            InputStream inputstream = assetManager.open("settingfile.properties");
            property.load(inputstream);
        } catch (Exception e) {
            System.err.println("ОШИБКА: Файл настроек отсуствует!");
            property = null;
        }
        return property;
    }

    //фильтр и количество.

    /**
     * Возвращает значения фильтров качества и количества
     * @return массив из двух переменных
     */
    public byte[] getFilter() {
        byte[] mass;
        Properties props = loadSettingFile();
        mass = new byte[2];
        mass[0] = (byte) preferences.getInt("filter", Byte.parseByte(props.getProperty("filter")));
        mass[1] = (byte) preferences.getInt("count", Byte.parseByte(props.getProperty("count")));
        return mass;
    }

    /**
     * Возвращает имя сервера
     * @param what 1 - эталон, 2 - подписки, 3 - прослушка
     * @return имя Сервера
     */
    public String getServerName(int what) {
        switch (what) {
            case 1:
                return loadSettingFile().getProperty("server.name_send");//preferences.getString("server.name_send", tmp1);
            case 2:
                return loadSettingFile().getProperty("server.name_friend");//preferences.getString("server.name_friend", tmp2);
            case 3:
                return loadSettingFile().getProperty("server.name_listen");//preferences.getString("server.name_listen", tmp3);
            default:
                return "";
        }
    }

    /**
     * Возвращает номер порта сервера
     * @param what 1 - эталон, 2 - подписки, 3 - прослушка
     * @return порт
     */
    public int getServerPort(int what) {
        switch (what) {
            case 1:
                what = Integer.parseInt(loadSettingFile().getProperty("server.port_send"));
                return what;//preferences.getInt("server.port_send", what);
            case 2:
                what = Integer.parseInt(loadSettingFile().getProperty("server.port_friend"));
                return what;//preferences.getInt("server.port_friend", what);
            case 3:
                what = Integer.parseInt(loadSettingFile().getProperty("server.port_listen"));
                return what;//preferences.getInt("server.port_listen", what);
            default:
                return -1;
        }
    }

    /**
     * Получить логин и пароль, которые приложение заполнило, если их нет, то значения по умолчанию
     * @param what логин (true), пароль (false)
     * @return логин/пароль
     */
    public String getLpkString_Commit(boolean what) {
        if (what) {
            return preferences.getString("sys.login.commit",
                    loadSettingFile().getProperty("sys.login"));
        } else {
            return preferences.getString("sys.pass.commit",
                    loadSettingFile().getProperty("sys.pass"));
        }
    }

    /**
     * Сохранить логин и пароль, которые приложение заполмнило
     * @param login логин
     * @param password пароль
     */
    public void setLpkString_Commit(String login, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sys.login.commit", login);
        editor.putString("sys.pass.commit", password);
        editor.apply();
    }

    /**
     * Возвращает логин пользователя или пароль
     * @param what логин (true), пароль (false)
     * @return логин/пароль
     */
    public String getLpkString(boolean what) {
        if (what) {
            return preferences.getString("sys.login",
                    "");//loadSettingFile().getProperty("sys.login"));
        } else {
            return preferences.getString("sys.pass",
                    "");//loadSettingFile().getProperty("sys.pass"));
        }
    }

    /**
     * Сохранение логина пользователя и пароля
     * @param login - сохраняемый логин
     * @param password - сохраняемый пароль
     */
    public void setLpkString(String login, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("sys.login", login);
        editor.putString("sys.pass", password);
        editor.apply();
    }

    /**
     * Сохранение логина пользователя или пароля
     * @param b - логин(true), пароль(false)
     * @param string - сохраняемое значение
     */
    public void setLpkString(boolean b, String string) {
        if (b)
            preferences.edit().putString("sys.login", string);
        else
            preferences.edit().putString("sys.pass", string);
        preferences.edit().apply();
    }

    /**
     * Возвращает логин пользователя или пароль
     * @param what логин (true), пароль (false)
     * @return логин/пароль
     */
    public byte[] getLpk(boolean what) {
        byte[] mass = new byte[1];
        mass[0] = -1;
        String str;
        if (what) {
            str = preferences.getString("sys.login",
                    loadSettingFile().getProperty("sys.login"));
            mass = str.getBytes();
        } else {
            str = preferences.getString("sys.pass",
                    loadSettingFile().getProperty("sys.pass"));
            mass = str.getBytes();
        }
        return mass;
    }

    /**
     * Возвращает номер типа шифрования
     * @return номер типа шифрования
     */
    public int getEncryption() {
        try {
            return preferences.getInt("encryption",
                    Integer.parseInt(loadSettingFile().getProperty("encryption")));
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * Сохранение настроек шифрования
     * @param encryption выбранный тип шифрования
     */
    public void setEncryption(String encryption) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (encryption)
        {
            case "AES":
                editor.putInt("encryption",1);
                break;
            case "RSA":
                editor.putInt("encryption",2);
                break;
            case "MD5":
            case "ГОСТ":
                editor.putInt("encryption",3);
                break;
        }
        editor.apply();
    }

    /**
     * Сохранение настроек социальных сетей
     * @param mass массив настроек
     */
    public void setInfo(boolean[] mass) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("social.fio", mass[0]);
        editor.putBoolean("social.photo", mass[1]);
        editor.putBoolean("social.datebith", mass[2]);
        editor.putBoolean("social.city", mass[3]);
        editor.putBoolean("social.work", mass[4]);
        editor.putBoolean("social.phone", mass[5]);
        editor.apply();
    }

    /**
     * Возвращает массив настроек социальных сетей
     * @return массив настроек
     */
    public boolean[] getInfo() {
        boolean[] mass = new boolean[6];
        Properties properties = loadSettingFile();
        mass[0] = preferences.getBoolean("social.fio",
                Boolean.parseBoolean(properties.getProperty("social.fio")));
        mass[1] = preferences.getBoolean("social.photo",
                Boolean.parseBoolean(properties.getProperty("social.photo")));
        mass[2] = preferences.getBoolean("social.datebith",
                Boolean.parseBoolean(properties.getProperty("social.datebith")));
        mass[3] = preferences.getBoolean("social.city",
                Boolean.parseBoolean(properties.getProperty("social.city")));
        mass[4] = preferences.getBoolean("social.work",
                Boolean.parseBoolean(properties.getProperty("social.work")));
        mass[5] = preferences.getBoolean("social.phone",
                Boolean.parseBoolean(properties.getProperty("social.phone")));
        return mass;
    }

    /**
     * Сохранение настроек источников
     * @param who сохраняемый параметр
     */
    public void setSourceData(boolean who) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("socialnetwork", who);
        editor.apply();
    }

    /**
     * Возвращает настройки источников
     * @return true - соц. сети, false - база данных Pry
     */
    public boolean getSourceData()
    {
        return preferences.getBoolean("socialnetwork",
                Boolean.parseBoolean(loadSettingFile().getProperty("socialnetwork")));
    }

    /**
     * Шифрование стандратного сообщения
     * @param msg массив байт сообщения
     * @return зашифрованный массив
     */
    public byte[] getEncryptMsg(byte[] msg) {
        /*try {
            byte encrypt_type = msg[0];
            byte[] tmp = new byte[msg.length-1];
            System.arraycopy(msg, 1, tmp, 0, tmp.length);
            switch (encrypt_type) {
                case 1:
                    String key = loadSettingFile().getProperty("encrypt.AES.key");
                    msg = new AES(key).encrypt(tmp);
                    break;
                case 2:
                    msg = new RSA().encrypt(tmp);
                    break;
                case 3:
                    msg = new MD5().encrypt(tmp);
                    break;
                default:
                    return new byte[] { -1 };
            }
            tmp = new byte[msg.length + 1];
            tmp[0] = encrypt_type;
            System.arraycopy(msg, 0, tmp, 1, msg.length);
            return tmp;
        } catch (Exception ex) {
            return new byte[] { -1 };
        }*/
        return  msg;
    }

    /**
     * Дешифрование стандратного сообщения
     * @param msg массив байт сообщения
     * @return расщифрованный массив
     */
    public byte[] getDecryptMsg(byte[] msg)
    {
        /*try {
            byte encrypt_type = msg[0];
            byte[] tmp = new byte[msg.length-1];
            System.arraycopy(msg, 1, tmp, 0, tmp.length);
            switch (encrypt_type)
            {
                case 1:
                    String key = loadSettingFile().getProperty("encrypt.AES.key");
                    msg = new AES(key).decrypt(tmp);
                    break;
                case 2:
                    msg = new RSA().decrypt(tmp);
                    break;
                case 3:
                    msg = new MD5().decrypt(tmp);
                    break;
                default:
                    return new byte[] { -1 };
            }
            tmp = new byte[msg.length+1];
            tmp[0] = bit;
            System.arraycopy(msg, 0, tmp, 1, bit);
            return tmp;
        } catch (Exception ex) {
            return new byte[] { -1 };
        }*/
        return msg;
    }

    /**
     * Проверка на наличие подключения к Интернету
     */
    public static boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
