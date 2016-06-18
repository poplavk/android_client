package com.example.mkai.pry.result;


import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mkai.pry.suh.PersonInfo;

import java.net.URI;

public class PersonDescriptor {
    private String photo;
    private String name;
    private String city;
    private String country;
    private String birthday;
    private String phone;
    private String work;

   /* public PersonDescriptor() {
        name.setText("Вася Пупкин");
        birthday.setText("13 марта");
        city.setText("Москва");
    }*/
    public PersonDescriptor() {
        photo = new String("https://pp.vk.me/c405723/v405723154/9af9/v_JbvY12RnM.jpg");
        name = new String("Вася Пупкин");
        birthday = new String("13 марта");
        city = new String("Москва");
    }

    public PersonDescriptor(PersonInfo personInfo)
    {
        photo = personInfo.image;
        name = personInfo.first_name + " " + personInfo.last_name;
        birthday = personInfo.birthday;
        phone = personInfo.phone;
        work = personInfo.occupation;
        city = personInfo.city;
        country = personInfo.country;
    }

    public String getName() {
        return name;
    }
    public String getBirthday() {
        return birthday;
    }
    public String getCity() {
        return city;
    }
    public String getCountry() {
        return country;
    }
    public String getWork() {
        return work;
    }
    public String getPhoto() {
        return photo;
    }
    public String getPhone() {return phone; }
}
